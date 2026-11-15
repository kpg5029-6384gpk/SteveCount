"""Package only authored source and build inputs; never credentials or dev caches."""
from pathlib import Path
import hashlib
import importlib.metadata as md
import shutil
import ssl
import zipfile

ROOT=Path(__file__).resolve().parents[1]
DIST=ROOT/'dist'

def main():
    DIST.mkdir(exist_ok=True)
    jar=ROOT/'minecraft-mod/build/libs/stevecount-1.0.0.jar'
    if not jar.is_file(): raise SystemExit('Build mod first')
    shutil.copy2(jar,DIST/'SteveCount-26.3-NeoForge.jar')
    fabric_jar=ROOT/'fabric-mod/build/libs/stevecount-fabric-1.0.0.jar'
    if not fabric_jar.is_file(): raise SystemExit('Build Fabric mod first')
    shutil.copy2(fabric_jar,DIST/'SteveCount-26.3-Fabric.jar')
    notices=DIST/'licenses'
    notices.mkdir(exist_ok=True)
    lines=['# Third-party notices','', 'SteveCount authored code: MIT; see LICENSE.',
           'User-selected Fiishy skin: see SKIN-NOTICE.md for source and artwork attribution.',
           'Minecraft Steve/Alex textures are referenced from the installed game and are not redistributed.',
           'NeoForge MDK template: see minecraft-mod/TEMPLATE_LICENSE.txt in the source archive.',
           'TikTokLive and TikTokLiveProto include modified AGPL-3.0 terms with integration exceptions; full texts are included below.', '']
    for d in sorted(md.distributions(),key=lambda d:d.metadata['Name'].lower()):
        name=d.metadata['Name']
        # Build/test packages are documented too for reproducibility.
        lines.append(f"- {name} {d.version}: {d.metadata.get('License-Expression') or d.metadata.get('License') or 'See bundled license files / upstream metadata'}")
        for f in d.files or []:
            if 'dist-info' in str(f) and any(s in f.name.lower() for s in ('license','copying','notice')):
                dest=notices/name/f.name
                dest.parent.mkdir(parents=True,exist_ok=True)
                shutil.copy2(d.locate_file(f),dest)
    (ROOT/'THIRD-PARTY-NOTICES.md').write_text('\n'.join(lines)+'\n',encoding='utf-8')
    include=[]
    for p in ROOT.iterdir():
        if p.is_file() and (p.suffix=='.md' or p.name in ('LICENSE','.gitignore')): include.append(p)
    for directory in ('.github','common/src','companion/src','companion/tests','scripts','minecraft-mod/src','minecraft-mod/gradle','fabric-mod/src','fabric-mod/gradle'):
        include.extend(p for p in (ROOT/directory).rglob('*') if p.is_file() and not {'__pycache__','.pytest_cache'}.intersection(p.parts))
    for pattern in ('minecraft-mod/*.gradle','minecraft-mod/*.properties','minecraft-mod/gradlew*','minecraft-mod/TEMPLATE_LICENSE.txt','fabric-mod/*.gradle','fabric-mod/*.properties','fabric-mod/gradlew*','companion/*.txt','companion/*.ps1','companion/pytest.ini'):
        include.extend(p for p in ROOT.glob(pattern) if p.is_file())
    with zipfile.ZipFile(DIST/'SteveCount-Source.zip','w',zipfile.ZIP_DEFLATED) as z:
        for p in sorted(set(include)): z.write(p,'SteveCount/'+p.relative_to(ROOT).as_posix())
    for p in ROOT.glob('*.md'): shutil.copy2(p,DIST/p.name)
    shutil.copy2(ROOT/'LICENSE',DIST/'LICENSE')
    exe=DIST/'SteveCount-Companion.exe'
    if not exe.is_file(): raise SystemExit('Build companion first')
    from PyInstaller.archive.readers import CArchiveReader
    archive=CArchiveReader(str(exe))
    if not any('betterproto2-' in name and name.endswith('METADATA') for name in archive.toc):
        raise SystemExit('Frozen companion is missing betterproto2 metadata')
    ssl_asset=Path(ssl.__file__).with_name('codex-rejected-cas.pem')
    if ssl_asset.is_file() and ssl_asset.name not in archive.toc:
        raise SystemExit('Frozen companion is missing its required SSL certificate asset')
    # Separate installer bundles prevent installing both loader JARs by accident.
    shared=[p for p in DIST.rglob('*') if p.is_file() and p.suffix not in ('.zip','.jar') and p.name!='SHA256SUMS.txt']
    shared.append(DIST/'SteveCount-Source.zip')
    variants={'SteveCount-Windows.zip':'NeoForge','SteveCount-Windows-NeoForge.zip':'NeoForge','SteveCount-Windows-Fabric.zip':'Fabric'}
    for filename,loader in variants.items():
        with zipfile.ZipFile(DIST/filename,'w',zipfile.ZIP_DEFLATED) as z:
            for p in shared+[DIST/f'SteveCount-26.3-{loader}.jar']:
                z.write(p,p.relative_to(DIST).as_posix())
    sums=[]
    for name in ('SteveCount-26.3-NeoForge.jar','SteveCount-26.3-Fabric.jar','SteveCount-Companion.exe','SteveCount-Source.zip',*variants):
        p=DIST/name
        sums.append(hashlib.sha256(p.read_bytes()).hexdigest()+'  '+name)
        print(f'{name}: {p.stat().st_size:,} bytes')
    (DIST/'SHA256SUMS.txt').write_text('\n'.join(sums)+'\n')

if __name__=='__main__': main()
