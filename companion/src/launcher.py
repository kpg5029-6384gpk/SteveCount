"""Visible companion launcher. No startup registration or automatic downloads."""
import asyncio
import json
import logging
import os
from pathlib import Path
import secrets
import threading
import tkinter as tk
from tkinter import messagebox
from aiohttp import web

def main():
    folder = Path(os.environ.get('LOCALAPPDATA', str(Path.home()))) / 'SteveCount'
    folder.mkdir(parents=True, exist_ok=True)
    logging.basicConfig(filename=folder / 'companion.log', level=logging.WARNING)
    from service import Service
    root = tk.Tk()
    root.title('SteveCount Companion')
    root.geometry('460x230')
    root.resizable(False, False)
    token = secrets.token_urlsafe(32)
    service = Service(token)
    loop = asyncio.new_event_loop()
    stop = None
    ready = threading.Event()
    errors = []
    async def serve():
        nonlocal stop
        stop = asyncio.Event()
        runner = web.AppRunner(service.app())
        try:
            await runner.setup()
            await web.TCPSite(runner, '127.0.0.1', 18763).start()
            (folder / 'connection.json').write_text(json.dumps({'token': token}), encoding='utf-8')
            ready.set()
            await stop.wait()
        except Exception as exc:
            errors.append(str(exc))
            ready.set()
        finally:
            await runner.cleanup()
    thread = threading.Thread(target=lambda: loop.run_until_complete(serve()), daemon=True)
    thread.start()
    tk.Label(root, text='SteveCount', font=('Segoe UI', 22, 'bold')).pack(pady=(16, 5))
    tk.Label(root, text='Keep this window open while playing Minecraft.').pack()
    label = tk.Label(root, text='Starting localhost service…', wraplength=430)
    label.pack(pady=12)
    def close():
        if stop and loop.is_running():
            loop.call_soon_threadsafe(stop.set)
        root.destroy()
    def refresh():
        if errors:
            messagebox.showerror('SteveCount', 'Cannot start companion. Another copy may be running.\n' + errors[0])
            close()
            return
        if ready.is_set():
            state = service.state.snapshot()
            label.config(text=f"{state['status']}  @{state['username']}\nViewers: {state['viewers'] if state['viewers'] is not None else 'Unavailable'}\n{state['message']}")
        root.after(1000, refresh)
    tk.Button(root, text='Exit companion', command=close).pack()
    root.protocol('WM_DELETE_WINDOW', close)
    refresh()
    root.mainloop()
    thread.join(timeout=5)

if __name__ == '__main__':
    try:
        main()
    except Exception:
        import traceback
        folder = Path(os.environ.get('LOCALAPPDATA', str(Path.home()))) / 'SteveCount'
        folder.mkdir(parents=True, exist_ok=True)
        (folder / 'startup-error.log').write_text(traceback.format_exc(), encoding='utf-8')
        messagebox.showerror('SteveCount startup failed', 'See ' + str(folder / 'startup-error.log'))
