# Windows installation

Optional follower: join a world, open settings with K or /stevecount, then use **Character → Display → Character/Both**. Existing HUD mode stays the default. See CHARACTER.md.

For Fabric, follow **FABRIC.md** and download **SteveCount-Windows-Fabric.zip**. For NeoForge, download **SteveCount-Windows-NeoForge.zip** (or the legacy SteveCount-Windows.zip) and follow the steps below. Never put both loader-specific SteveCount JARs into the same instance.

1. Extract the Windows ZIP. Keep the EXE, documentation, source archive and license notices together.
2. Create/select a Modrinth App instance using Minecraft Java 26.3 and NeoForge 26.3.0.102 or a compatible 26.3.x release. The NeoForge JAR does not load on Fabric or Forge. A separate Fabric build is available. Java 25 is required; use a matching launcher runtime.
3. Open that instance's folder. Place `SteveCount-26.3-NeoForge.jar` in `mods`. Do not unzip the JAR. Install on your own client only.
4. Launch `SteveCount-Companion.exe` from the extracted folder. No Python installation is needed. Leave its status window open; Exit companion stops the service and releases the port.
5. Start Minecraft, open Mods → SteveCount → Config or press K. In-game, `/stevecount` opens the same settings without server permissions.
6. Enter your TikTok username or full creator LIVE URL; click Connect. Start your stream. Offline retry delays increase from 2 seconds to at most 120 seconds. Stream changes are picked up automatically on later retries.
7. In HUD, choose a preset, head, scale, and opacity. Use Move HUD to drag it, Save position, then Save & Done.
8. Before streaming, use Testing → Demo to preview the overlay offline. Real connections require disabling demo and clicking Connect.

Only launch one companion per Windows user at a time. Restarting it rotates its token; the mod rereads the token on every background request. If the companion starts after a failed Connect, click Reconnect.

Windows organizational Application Control can block unsigned executables. This build is unsigned. The development environment blocked a diagnostic EXE; standalone execution has not been fully verified here. Do not disable security controls. Use an approved build/signing process on managed machines.

To enable optional single-player Hand Heart damage, follow [GIFTS.md](GIFTS.md). It uses normal fist damage and can kill in hardcore. Off by default. The astronaut, menu fix, skins and emotes updates only require replacing the matching SteveCount JAR; the companion protocol is unchanged from the Hand Heart release. The installation ZIP includes the companion too. See [EMOTES.md](EMOTES.md) and [ASTRONAUT.md](ASTRONAUT.md) for the buttons and J shortcut.

To uninstall, close the companion and remove SteveCount's JAR and extracted folder. Its settings are in the instance's `config/stevecount.json`, and local companion files in `%LOCALAPPDATA%\SteveCount`. No entity or custom data is saved in worlds. Normal health/gameplay consequences of previously enabled gift punches remain.

