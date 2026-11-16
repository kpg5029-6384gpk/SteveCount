import asyncio
import sys
from pathlib import Path
sys.path.insert(0, str(Path(__file__).parents[1] / 'src'))
import pytest
from aiohttp.test_utils import TestClient, TestServer
from TikTokLive.events import RoomUserSeqEvent
from core import State
from service import Service

@pytest.mark.asyncio
async def test_http_security_and_control():
    s=Service('secret')
    async with TestClient(TestServer(s.app())) as client:
        headers={'Host':'127.0.0.1:18763','Authorization':'Bearer secret'}
        response=await client.get('/status',headers=headers)
        assert response.status==200
        status=await response.json()
        assert 'costumes' in status and 'gifts' in status
        assert status['costumes']['session']!=status['gifts']['session']
        assert (await client.get('/status',headers={'Host':'127.0.0.1:18763'})).status==401
        assert (await client.get('/status',headers={**headers,'Origin':'https://evil.example'})).status==403
        assert (await client.get('/status',headers={**headers,'Host':'evil.example'})).status==403
        assert (await client.post('/control',headers=headers,json={'action':'connect','username':'https://evil.com/@x'})).status==400
        assert (await client.post('/control',headers=headers,data='{')).status==400
        response=await client.post('/control',headers=headers,json={'action':'disconnect'})
        assert response.status==200 and (await response.json())['status']=='Disconnected'

def test_installed_library_event():
    s=State()
    s.event(RoomUserSeqEvent(total=0,total_user=999999))
    assert s.viewers==0
    s.event(RoomUserSeqEvent(total=128,total_user=999999))
    assert s.viewers==128

@pytest.mark.asyncio
async def test_offline_retry(monkeypatch):
    import service
    attempts=[]
    class FakeClient:
        def __init__(self,**kwargs): pass
        def on(self,event): return lambda handler: handler
        async def start(self,**kwargs):
            attempts.append(1)
            raise service.UserOfflineError('offline')
        async def disconnect(self,**kwargs): pass
    sleeps=[]
    async def sleep(delay):
        sleeps.append(delay)
        if len(sleeps)==3: raise asyncio.CancelledError
    monkeypatch.setattr(service,'TikTokLiveClient',FakeClient)
    monkeypatch.setattr(service.asyncio,'sleep',sleep)
    s=Service('secret')
    with pytest.raises(asyncio.CancelledError): await s.watch()
    assert s.state.status=='Offline'
    assert sleeps==[2,4,8] and len(attempts)==3

@pytest.mark.asyncio
async def test_control_validation_and_valid_connect():
    s = Service('secret')
    async def watch():
        await asyncio.Event().wait()
    s.watch = watch
    headers = {'Host': '127.0.0.1:18763', 'Authorization': 'Bearer secret'}
    async with TestClient(TestServer(s.app())) as client:
        for data in [None, [], {'action': 'connect'}, {'action': 'connect', 'username': 12}, {'action': 'connect', 'username': ''}]:
            response = await client.post('/control', headers=headers, json=data)
            assert response.status == 400
            assert (await response.json())['error']
        response = await client.post('/control', headers=headers, json={'action': 'connect', 'username': 'www.tiktok.com/@.fishyplays/live'})
        assert response.status == 200
        assert (await response.json())['username'] == '.fishyplays'
        response = await client.post('/control', headers=headers, json={'action': 'connect', 'username': 'Display Name'})
        assert response.status == 400
        assert 'display name' in (await response.json())['error']
        assert s.state.username == '.fishyplays' and s.task is not None
