# Fabric installation — Minecraft 26.3

1. Download **SteveCount-Windows-Fabric.zip** from the GitHub release Assets section and extract it on Windows.
2. Select/create a Modrinth instance for **Minecraft 26.3**, **Fabric Loader 0.20.1 or newer**, and **Java 25**.
3. In that instance, use Add content to install **Fabric API for Minecraft 26.3**, version **0.166.2+26.3 or newer compatible 26.3 release**. Do not install the NeoForge version of Fabric API or an API build for a different Minecraft version.
4. Open the instance folder and copy **SteveCount-26.3-Fabric.jar** into `mods`. Remove the NeoForge SteveCount JAR from this Fabric instance if you previously copied it there. Keep only one SteveCount JAR.
5. Launch **SteveCount-Companion.exe** from the extracted ZIP and keep its status window open. If you already have the corrected companion from the NeoForge package, the same EXE works with Fabric; do not launch a second copy.
6. Start the instance through Modrinth and join a world/server.
7. Press **K** or enter **/stevecount**. No operator permissions or server installation are required. K can be reassigned in Minecraft Controls.
8. Connection: enter **@username** or a full TikTok LIVE URL, click **Connect**, and start the stream. Offline streams are retried automatically; unknown viewer counts stay unavailable until a genuine count event arrives.
9. Appearance: choose a preset/head, size, and opacity. Move HUD opens the drag editor. Save position, then Save & Done.
10. Testing: enable Demo to preview without Internet, TikTok, or the companion. Demo counts are visibly labeled and do not become stream statistics. Click Connect to return to real data.

For the optional in-world follower, open **Character → Display** and choose **Character** or **Both**. See CHARACTER.md. Current menu tabs are **Connect**, **HUD**, **Demo**, **Character**, and **More**.

Fabric does not need Mod Menu; use K or /stevecount. NeoForge's Mods → Config button is loader-specific.

The Fabric and NeoForge builds share all HUD/settings/bridge code. Settings use the same `config/stevecount.json` schema in each instance. A separate instance has its own settings file; switching instances does not automatically copy it. No world files are modified.

Both builds use the existing unofficial TikTokLive integration and the same localhost companion protocol. Real-stream availability remains subject to TikTok and the signing service; see TEST-REPORT.md for verification limits.

