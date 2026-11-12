param([string]$Python="$PSScriptRoot\..\.venv\Scripts\python.exe")
$ErrorActionPreference='Stop'
$projectRoot=Split-Path $PSScriptRoot
& "$projectRoot\minecraft-mod\gradlew.bat" -p "$projectRoot\minecraft-mod" build --no-daemon
if ($LASTEXITCODE -ne 0) { throw 'Minecraft build failed' }
& "$projectRoot\fabric-mod\gradlew.bat" -p "$projectRoot\fabric-mod" build --no-daemon
if ($LASTEXITCODE -ne 0) { throw 'Fabric build failed' }
& $Python -m pytest "$projectRoot\companion\tests" -v
if ($LASTEXITCODE -ne 0) { throw 'Companion tests failed' }
& "$projectRoot\companion\build-windows.ps1" -Python $Python
& $Python "$PSScriptRoot\package.py"
if ($LASTEXITCODE -ne 0) { throw 'Packaging failed' }
