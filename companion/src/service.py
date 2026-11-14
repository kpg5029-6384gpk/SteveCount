import asyncio
import contextlib
import logging
from aiohttp import web
from TikTokLive import TikTokLiveClient
from TikTokLive.events import ConnectEvent, RoomUserSeqEvent, LiveEndEvent, GiftEvent
from gifts import Gifts
from TikTokLive.client.errors import UserOfflineError
from core import State, normalize, backoff

LOG = logging.getLogger('SteveCount')

class Service:
    def __init__(self, token):
        self.token = token
        self.state = State()
        self.task = None
        self.client = None
        self.gifts = Gifts()

    async def stop(self):
        if self.client:
            with contextlib.suppress(Exception):
                await self.client.disconnect(close_client=True)
        if self.task:
            self.task.cancel()
            with contextlib.suppress(asyncio.CancelledError):
                await self.task
        self.task = self.client = None
        self.state.status = 'Disconnected'

    async def watch(self):
        attempt = 0
        while True:
            self.state.status = 'Connecting' if attempt == 0 else 'Reconnecting'
            client = self.client = TikTokLiveClient(unique_id=self.state.username)
            @client.on(ConnectEvent)
            async def connected(event):
                nonlocal attempt
                attempt = 0
                room = str(event.room_id)
                if room != self.state.room:
                    self.state.room = room
                    self.state.peak = 0
                    self.state.viewers = None
                    self.state.updated = 0
                self.state.status = 'Live'
                self.state.message = 'Waiting for viewer event'
            @client.on(RoomUserSeqEvent)
            async def viewers(event):
                try:
                    self.state.event(event)
                except (ValueError, AttributeError, TypeError):
                    self.state.message = 'Unsupported viewer event; waiting for valid data'
                    LOG.warning('Rejected malformed viewer event')
            @client.on(LiveEndEvent)
            async def ended(event):
                self.state.status = 'Offline'
                await client.disconnect()
            @client.on(GiftEvent)
            async def gift(event):
                if self.state.status != 'Live':
                    return
                try:
                    self.gifts.event(event)
                except (ValueError, AttributeError, TypeError):
                    LOG.warning('Rejected malformed gift event')
            try:
                # start returns the websocket task; avoid connect()'s nested-loop cancellation cleanup.
                task = await client.start(fetch_gift_info=True, fetch_room_info=False)
                await task
                if self.state.status != 'Offline':
                    self.state.status = 'Reconnecting'
            except UserOfflineError:
                self.state.status = 'Offline'
                self.state.message = 'Creator is offline; retrying automatically'
            except asyncio.CancelledError:
                raise
            except Exception as exc:
                self.state.status = 'Error'
                self.state.message = f'{type(exc).__name__}: TikTok unavailable; retrying automatically'
                LOG.warning('TikTok connection failed: %s', type(exc).__name__)
            finally:
                with contextlib.suppress(Exception):
                    await client.disconnect(close_client=True)
            attempt += 1
            await asyncio.sleep(backoff(attempt))

    def app(self):
        @web.middleware
        async def security(request, handler):
            if request.headers.get('Origin') or request.headers.get('Host') != '127.0.0.1:18763':
                raise web.HTTPForbidden()
            if request.headers.get('Authorization') != 'Bearer ' + self.token:
                raise web.HTTPUnauthorized()
            return await handler(request)
        app = web.Application(middlewares=[security], client_max_size=2048)
        async def status(request):
            return web.json_response({**self.state.snapshot(), 'gifts': self.gifts.snapshot()})
        async def control(request):
            try:
                data = await request.json()
                action = data['action']
                if action not in ('connect', 'disconnect'):
                    raise ValueError('Unknown action')
                username = normalize(data['username']) if action == 'connect' else ''
            except (ValueError, KeyError, TypeError):
                raise web.HTTPBadRequest(text='Invalid action or username')
            await self.stop()
            self.gifts = Gifts()
            if action == 'connect':
                self.state = State(username=username, status='Connecting')
                self.task = asyncio.create_task(self.watch())
            return web.json_response(self.state.snapshot())
        app.router.add_get('/status', status)
        app.router.add_post('/control', control)
        async def cleanup(app):
            await self.stop()
        app.on_cleanup.append(cleanup)
        return app
