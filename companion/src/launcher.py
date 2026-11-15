"""Visible companion launcher. No startup registration or automatic downloads."""
import asyncio
import json
import logging
import os
from pathlib import Path
import secrets
import threading
import sys
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
    root.geometry('520x410')
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
        service.voice.stop()
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
    from tkinter import ttk
    tk.Label(root, text='Offline voice control — microphone stays off until Start').pack(pady=(8, 3))
    devices = []
    microphone = ttk.Combobox(root, state='readonly', width=62)
    microphone.pack()
    def refresh_devices():
        nonlocal devices
        try:
            import sounddevice as sd
            devices = [(None, 'Windows default microphone')] + [(i, d['name']) for i, d in enumerate(sd.query_devices()) if d['max_input_channels'] > 0]
            microphone['values'] = [name for _, name in devices]
            microphone.current(0)
        except Exception as exc:
            voice_label.config(text='Microphone unavailable: ' + type(exc).__name__)
    voice_label = tk.Label(root, text='Off', wraplength=490)
    voice_label.pack(pady=5)
    row = tk.Frame(root)
    row.pack()
    tk.Button(row, text='Start voice', command=lambda: service.voice.start(devices[max(0,microphone.current())][0]) if devices else refresh_devices()).pack(side='left', padx=4)
    tk.Button(row, text='Stop / mute voice', command=service.voice.stop).pack(side='left', padx=4)
    tk.Button(row, text='Refresh microphones', command=refresh_devices).pack(side='left', padx=4)
    tk.Label(root, text='Say “little buddy hop on”, “hop off little buddy”, “dance little buddy”, or “how you feeling little buddy?”.\nAudio stays on this PC. Nothing is recorded or uploaded.', wraplength=490).pack(pady=8)
    def refresh_voice():
        state = service.voice.snapshot()
        voice_label.config(text=state['status'] + (' | Last: ' + state['lastCommand'] if state['lastCommand'] else ''))
        root.after(500, refresh_voice)
    refresh_devices()
    refresh_voice()
    tk.Button(root, text='Exit companion', command=close).pack()
    root.protocol('WM_DELETE_WINDOW', close)
    refresh()
    root.mainloop()
    thread.join(timeout=5)

if __name__ == '__main__':
    if len(sys.argv) in (4,5) and sys.argv[1]=='--voice-self-test':
        # Explicit diagnostic mode: prerecorded file only, never opens a microphone.
        try:
            import wave
            import sounddevice
            from vosk import Model, KaldiRecognizer, SetLogLevel
            from voice import model_path, PHRASES, parse_command
            SetLogLevel(-1)
            with wave.open(sys.argv[2]) as audio:
                rec=KaldiRecognizer(Model(str(model_path())),audio.getframerate(),json.dumps([*PHRASES,'[unk]']))
                rec.AcceptWaveform(audio.readframes(audio.getnframes()))
                text=json.loads(rec.FinalResult()).get('text','')
            command=parse_command(text)
            expected=sys.argv[4] if len(sys.argv)==5 else 'head_on'
            assert command==expected, 'Speech fixture was not recognized'
            Path(sys.argv[3]).write_text(json.dumps({'passed':True,'command':command}),encoding='utf-8')
        except Exception as exc:
            Path(sys.argv[3]).write_text(json.dumps({'passed':False,'error':str(exc)}),encoding='utf-8')
            sys.exit(1)
        sys.exit(0)
    try:
        main()
    except Exception:
        import traceback
        folder = Path(os.environ.get('LOCALAPPDATA', str(Path.home()))) / 'SteveCount'
        folder.mkdir(parents=True, exist_ok=True)
        (folder / 'startup-error.log').write_text(traceback.format_exc(), encoding='utf-8')
        messagebox.showerror('SteveCount startup failed', 'See ' + str(folder / 'startup-error.log'))
