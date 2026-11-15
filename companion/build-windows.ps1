param([string]$Python = "$PSScriptRoot\..\.venv\Scripts\python.exe")
$ErrorActionPreference = 'Stop'
& $Python "$PSScriptRoot\..\scripts\prepare-voice-model.py"
if ($LASTEXITCODE -ne 0) { throw 'Voice model preparation failed' }
$sslAsset = & $Python -c "import ssl,pathlib; p=pathlib.Path(ssl.__file__).with_name('codex-rejected-cas.pem'); print(str(p) if p.is_file() else '')"
if ($LASTEXITCODE -ne 0) { throw 'Could not inspect SSL runtime' }
$extraData = @()
if ($sslAsset) { $extraData = @('--add-data', "$sslAsset;.") }
& $Python -m PyInstaller --noconfirm --onefile --windowed --name SteveCount-Companion --collect-all vosk --collect-all sounddevice --collect-all _sounddevice_data --add-data "$PSScriptRoot\voice-model\vosk-model-small-en-us-0.15;voice-model/vosk-model-small-en-us-0.15" --collect-all TikTokLive --collect-all TikTokLiveProto --collect-all EulerApiSdk --copy-metadata betterproto2 @extraData --distpath "$PSScriptRoot\..\dist" --workpath "$PSScriptRoot\build" --specpath $PSScriptRoot "$PSScriptRoot\src\launcher.py"
if ($LASTEXITCODE -ne 0) { throw 'Companion build failed' }
