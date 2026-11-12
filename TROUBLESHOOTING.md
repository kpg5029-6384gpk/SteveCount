# Troubleshooting

- **Fabric says incompatible mod:** install SteveCount-26.3-Fabric.jar, Fabric Loader 0.20.1+, and Fabric API 0.166.2+26.3 for Minecraft 26.3. Keep only one SteveCount JAR. The NeoForge JAR cannot load on Fabric.
- **No Mods → Config button under Fabric:** use K or /stevecount; those open the complete settings screen without Mod Menu.

- **Startup FileNotFoundError in aiohttp → ssl.py → SSLContext.__new__:** replace the companion with the updated build. Earlier packages omitted a certificate file required by the build runtime. The corrected build includes it and retains TLS certificate verification. The Minecraft JAR does not need replacing for this companion-only fix.

- **Launcher's window closes or reports an error:** check `%LOCALAPPDATA%\SteveCount\startup-error.log` and `companion.log`. Logs stay local. A second running copy can occupy port 18763. Close it using Exit companion.
- **Windows blocks the executable:** the EXE is unsigned. Respect organizational policy; use an approved signed build. No security bypass is part of installation.
- **Minecraft cannot find the companion:** start the EXE under the same Windows user, then click Reconnect. Do not change the bind address to a LAN address. Check that local port 18763 is free.
- **Offline:** make sure the creator is actually LIVE, and wait up to two minutes for the retry. An offline check can differ from a web page due to restrictions or caching.
- **Error / SignAPIError / rate limits:** TikTok or the required Euler signing service may be unavailable or rate limited. Wait, then reconnect. No paid key is required by SteveCount's configuration; upstream service terms and availability can change.
- **Unavailable viewers while Live:** connected does not mean a viewer event has arrived. SteveCount intentionally does not turn missing data into zero. Wait for `RoomUserSeqEvent.total`.
- **Amber number with ~:** the count is stale or connection interrupted. Last count is retained for context, not presented as fresh. Expanded mode shows status alongside the count.
- **Age-restricted / region-restricted stream:** streams that need account authentication cannot be accessed by this no-login MVP.
- **HUD missing:** join a world, exit F1 hidden-HUD mode, check Appearance scale and Move HUD → Reset. Other overlays may overlap it; drag SteveCount to free space.
- **Minecraft stuck on NeoForge's early loading window:** the development machine exhibited a NeoForge early-window handoff deadlock. The isolated test profile progressed with `earlyWindowControl = false` in `config/fml.toml`. This is an optional NeoForge loading display, independent of SteveCount; no launcher-wide or security settings were changed.
- **K conflicts with another mod:** reassign SteveCount's key in Minecraft Controls, use `/stevecount`, or open its config through Mods.
- **Invalid URL:** use `@username` or `https://www.tiktok.com/@username/live`. Short share links, video links, non-TikTok hosts, and HTTP URLs are rejected.

TikTokLive is unofficial. Platform changes can require an updated companion. Never send passwords, cookies, session files, or the local token to troubleshoot this application.
