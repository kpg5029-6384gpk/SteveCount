# SteveCount

Minecraft 26.3 / NeoForge 26.3.0.102 client-side TikTok LIVE viewer HUD.

## Download

Windows builds are published under [Releases](https://github.com/kpg5029-6384gpk/SteveCount/releases). Download **SteveCount-Windows.zip** from the release assets. GitHub's automatically generated Source code ZIP is not the Windows installation package.

The initial release is a release candidate: live TikTok end-to-end integration still needs verification with an accessible active stream. See the included TEST-REPORT.md.

## Install

1. Extract the Windows ZIP on your Windows PC.
2. Put SteveCount-26.3-NeoForge.jar in your Minecraft 26.3 NeoForge instance's mods folder.
3. Launch SteveCount-Companion.exe and keep its window open.
4. Start Minecraft through Modrinth. Press K, use /stevecount, or open Mods > SteveCount > Config.
5. Enter @username or a full TikTok LIVE URL and click Connect.
6. Use Appearance > Move HUD to position it. Testing > Demo works offline.

No TikTok login, server mod, Python installation, or administrator rights are required for end users. TikTokLive is unofficial and depends on TikTok plus its required signing service. The EXE is unsigned.

## Source and build

The authorized source snapshot is stored in source/SteveCount-Source.zip.b64 because this publishing connection accepts UTF-8 text files. Decode it with Python:

```python
import base64, pathlib, zipfile, io
raw = base64.b64decode(pathlib.Path('source/SteveCount-Source.zip.b64').read_text())
zipfile.ZipFile(io.BytesIO(raw)).extractall('.')
```

The archive contains readable Java/Python source, tests, Gradle wrapper, build scripts, documentation, and license notices. It excludes local credentials, caches, logs, and Java/Python runtime installations. The GitHub Actions workflow rebuilds it on Windows and publishes a release only after the checks pass.

SteveCount authored code is MIT licensed; dependency notices and full licenses are included in the Windows/source packages.
