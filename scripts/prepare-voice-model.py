"""Build-time download only; runtime never downloads or uploads speech data."""
from pathlib import Path
import hashlib
import urllib.request
import zipfile

ROOT = Path(__file__).resolve().parents[1]
NAME = 'vosk-model-small-en-us-0.15'
URL = f'https://alphacephei.com/vosk/models/{NAME}.zip'

def main():
    dest = ROOT/'companion/voice-model'
    dest.mkdir(exist_ok=True)
    archive = dest/(NAME+'.zip')
    if not archive.is_file():
        urllib.request.urlretrieve(URL, archive)
    digest = hashlib.sha256(archive.read_bytes()).hexdigest()
    print(f'{NAME}.zip SHA256 {digest}')
    if digest != '30f26242c4eb449f948e42cb302dd7a686cb29a3423a8367f99ff41780942498':
        raise RuntimeError('Unexpected voice model checksum')
    with zipfile.ZipFile(archive) as z:
        for info in z.infolist():
            target = (dest/info.filename).resolve()
            if not target.is_relative_to(dest.resolve()):
                raise RuntimeError('Unsafe model archive path')
        if z.testzip(): raise RuntimeError('Corrupt model archive')
        z.extractall(dest)
    assert (dest/NAME/'am/final.mdl').is_file()

if __name__ == '__main__': main()
