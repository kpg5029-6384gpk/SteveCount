"""Optional, local-only speech commands. No audio/transcript files or uploads."""
import json
import queue
import re
import sys
import threading
import time
import uuid
from collections import deque
from pathlib import Path

ALIASES = {
    'hop on': 'head_on', 'hop on my head': 'head_on', 'get on my head': 'head_on',
    'head ride': 'head_on', 'hop off': 'head_off', 'get off': 'head_off',
    'get off my head': 'head_off', 'come down': 'head_off',
    'dance': 'Dance', 'wave': 'Wave', 'cheer': 'Cheer',
    'stop': 'stop', 'stop dancing': 'stop', 'come here': 'recall', 'recall': 'recall',
}
PHRASES = {phrase: command for words, command in ALIASES.items()
           for phrase in (f'little buddy {words}', f'{words} little buddy')}

def parse_command(text):
    if not isinstance(text, str) or len(text) > 256:
        return None
    words = re.sub(r'[^a-z ]', ' ', text.lower())
    return PHRASES.get(' '.join(words.split()))

def model_path():
    root = Path(getattr(sys, '_MEIPASS', Path(__file__).resolve().parents[1]))
    return root / 'voice-model' / 'vosk-model-small-en-us-0.15'

class Voice:
    def __init__(self, clock=time.time):
        self.clock = clock
        self.lock = threading.Lock()
        self.session = uuid.uuid4().hex
        self.sequence = 0
        self.events = deque(maxlen=16)
        self.status = 'Off'
        self.last_command = ''
        self.last = float('-inf')
        self.stop_event = threading.Event()
        self.thread = None

    def snapshot(self):
        with self.lock:
            return dict(session=self.session, sequence=self.sequence, events=list(self.events),
                        status=self.status, lastCommand=self.last_command)

    def recognized(self, text):
        command = parse_command(text)
        with self.lock:
            now = self.clock()
            if not command or self.stop_event.is_set() or now-self.last < 1.5:
                return False
            self.last = now
            self.sequence += 1
            self.events.append(dict(sequence=self.sequence, created=now, command=command))
            self.last_command = command
            return True

    def start(self, device=None):
        if self.thread and self.thread.is_alive():
            return
        self.stop_event.clear()
        with self.lock:
            self.session = uuid.uuid4().hex
            self.sequence = 0
            self.events.clear()
            self.last = float('-inf')
            self.status = 'Starting microphone…'
        self.thread = threading.Thread(target=self._listen, args=(device,), daemon=True, name='SteveCount voice')
        self.thread.start()

    def stop(self):
        self.stop_event.set()
        with self.lock:
            self.events.clear()
            self.status = 'Off'

    def _listen(self, device):
        try:
            import sounddevice as sd
            from vosk import Model, KaldiRecognizer, SetLogLevel
            SetLogLevel(-1)
            path = model_path()
            if not (path/'am/final.mdl').is_file():
                raise RuntimeError('Bundled English voice model missing; reinstall the full companion')
            model = Model(str(path))  # Explicit path: never auto-download a model.
            if self.stop_event.is_set():
                return
            rate = int(sd.query_devices(device, 'input')['default_samplerate'])
            recognizer = KaldiRecognizer(model, rate, json.dumps([*PHRASES, '[unk]']))
            chunks = queue.Queue(maxsize=16)
            def callback(data, frames, timestamp, status):
                if status:
                    while not chunks.empty():
                        try: chunks.get_nowait()
                        except queue.Empty: break
                try: chunks.put_nowait(bytes(data))
                except queue.Full: pass
            if self.stop_event.is_set():
                return
            with sd.RawInputStream(samplerate=rate, blocksize=max(800,rate//5), device=device,
                                   channels=1, dtype='int16', callback=callback):
                with self.lock:
                    if not self.stop_event.is_set(): self.status = 'Listening for “little buddy”'
                while not self.stop_event.is_set():
                    try: data = chunks.get(timeout=.25)
                    except queue.Empty: continue
                    if recognizer.AcceptWaveform(data):
                        self.recognized(json.loads(recognizer.Result()).get('text', ''))
        except Exception as exc:
            with self.lock:
                if not self.stop_event.is_set():
                    self.status = f'Voice unavailable: {type(exc).__name__}: {str(exc)[:180]}'
        finally:
            if self.stop_event.is_set():
                with self.lock: self.status = 'Off'
