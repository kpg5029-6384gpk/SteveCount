"""Rebuild original synthetic buddy sounds and the model's white vertex-color texture.
Requires ffmpeg or imageio-ffmpeg only when regenerating; release assets are committed.
"""
from pathlib import Path
import math, struct, wave, zlib, subprocess, shutil, io
ROOT=Path(__file__).resolve().parents[1]
ASSETS=ROOT/'minecraft-mod/src/main/resources/assets/stevecount'
def png_white():
    def chunk(kind,data): return struct.pack('>I',len(data))+kind+data+struct.pack('>I',zlib.crc32(kind+data)&0xffffffff)
    data=b'\x89PNG\r\n\x1a\n'+chunk(b'IHDR',struct.pack('>IIBBBBB',1,1,8,6,0,0,0))+chunk(b'IDAT',zlib.compress(b'\x00\xff\xff\xff\xff'))+chunk(b'IEND',b'')
    (ASSETS/'textures/entity/astronaut.png').write_bytes(data)
def main():
    png_white()
    ffmpeg=shutil.which('ffmpeg')
    if not ffmpeg:
        from imageio_ffmpeg import get_ffmpeg_exe
        ffmpeg=get_ffmpeg_exe()
    sample_rate=22050
    for name,duration in [('chirp',.42),('boop',.28),('wobble',.6)]:
        samples=[];phase=0
        for i in range(int(duration*sample_rate)):
            t=i/sample_rate;u=t/duration
            freq={'chirp':650+1200*u,'boop':520-220*u,'wobble':750+180*math.sin(t*40)}[name]
            phase+=2*math.pi*freq/sample_rate
            envelope=math.sin(math.pi*u)**2
            tone=math.sin(phase)*.85+math.sin(phase*2)*.12
            samples.append(struct.pack('<h',round(32767*.35*envelope*tone)))
        wav=io.BytesIO()
        with wave.open(wav,'wb') as f:
            f.setnchannels(1);f.setsampwidth(2);f.setframerate(sample_rate);f.writeframes(b''.join(samples))
        dest=ASSETS/f'sounds/buddy/{name}.ogg';dest.parent.mkdir(parents=True,exist_ok=True)
        subprocess.run([ffmpeg,'-hide_banner','-loglevel','error','-y','-i','pipe:0','-c:a','libvorbis','-q:a','3',str(dest)],input=wav.getvalue(),check=True)
        print(name,dest.stat().st_size)
if __name__=='__main__':main()
