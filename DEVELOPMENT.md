# Development

## Follower character

`FollowTrail` keeps a bounded route and computes smooth follower movement. `FollowerCharacter` samples client terrain and appends a new AvatarRenderState to the level's render state. It never creates, registers, spawns, saves, or sends an entity. Fabric uses LevelExtractionEvents.END_EXTRACTION; NeoForge uses ExtractLevelRenderStateEvent. The renderer receives only snapshot fields, not the trail or world reference. Built-in Steve/Alex skins and the player's already-loaded skin require no extra HTTP requests.

Run either module's `runClient -PstevecountFollowerSmoke` for the opt-in in-world test. This explicit development flag creates a **new named QA world** in that module's isolated run directory, captures the follower, checks rendered frames, walking, teleport catch-up and disabled-mode suppression, then exits. It does not open an existing user world. Production installations never activate it unless that JVM property is deliberately supplied.

## Multi-loader layout

`common/src/main/java` contains the shared HUD, settings, bridge, demo, milestones and screens. `minecraft-mod` supplies only the NeoForge entrypoint; `fabric-mod` supplies the Fabric client entrypoint and metadata. Each build compiles shared sources directly, so there is no extra runtime shared-library JAR. Both are client-only.

Fabric is pinned to Minecraft 26.3, Fabric Loader 0.20.1, Fabric API 0.166.2+26.3, Loom 1.19.4, Gradle 9.8.2, and Java 25, based on Fabric's official 26.3 example and API sources. Minecraft 26.3 uses unobfuscated official class names, so no Yarn/official mappings layer is added. `fabric-mod/gradlew.bat -p fabric-mod build` runs the same core assertions as NeoForge. `runClient -PstevecountSmoke` exercises the same eight screen/preset captures under Fabric. Use a separate development run directory for each loader.

Packaging creates separate Fabric and NeoForge Windows ZIPs, each containing exactly one loader JAR and the common companion. The legacy SteveCount-Windows.zip remains a NeoForge bundle. Fabric API is a user-installed dependency, not bundled.

## Toolchain

The mod is based on the official https://github.com/NeoForgeMDKs/MDK-26.3-ModDevGradle template: Minecraft 26.3, NeoForge 26.3.0.102, ModDevGradle 2.0.148, Gradle wrapper 9.2.1 and Java 25. The initial MDK checkout was downloaded rather than substituted with an older Minecraft target. See https://docs.neoforged.net/.

Set JAVA_HOME to a Java 25 JDK. Run `minecraft-mod\gradlew.bat -p minecraft-mod build`. `check` runs the dependency-free executable CoreChecks assertion suite; the standard test task is not a JUnit suite. `runClient` starts an isolated development client.

For opt-in rendered screen captures, run `gradlew.bat runClient -PstevecountSmoke` from minecraft-mod. SmokeHarness cycles all settings tabs and four preview presets, writes engine screenshots, and exits. It is inactive without the system property and does not create or open a world.

For companion development only, install Python 3.12, create `.venv`, then install `companion/requirements-lock.txt`. End users use the EXE and never need Python. Run `.venv\Scripts\python.exe -m pytest companion\tests -v`. Run `companion/build-windows.ps1` to package the application. PyInstaller must include betterproto2 metadata: its version check uses importlib.metadata at import time.

`scripts/build-all.ps1` rebuilds/tests both components and packages the Windows ZIP. Required dependencies must already be installed; it never silently installs software.

## Architecture and provider evaluation

The maintained Java candidate https://github.com/jwdeveloper/TikTokLiveJava was investigated along with the preferred Python https://github.com/isaackogan/TikTokLive library. The Java project also relies on reverse-engineered WebCast behavior and external signing; a reliable, independent pure-Java path meeting all constraints was not established. Python TikTokLive 7.0.1 was selected for its documented current event schema and isolation from Minecraft. This is an engineering choice, not a claim that Java integration is impossible.

`core.py` owns normalized input, validated counts, timestamps, session state and backoff. `service.py` adapts TikTokLive to that state. A future provider can populate the same snapshot without changing the renderer. `launcher.py` gives a visible start/status/exit window. Asyncio handles TikTok and the local aiohttp API; Tk runs on the main thread.

The bridge uses one daemon background worker and one persistent Java HttpClient, polling the localhost status once per second with bounded timeouts. TikTok updates remain event driven. No external networking or disk reads occur in rendering. Immutable snapshots cross threads. Local token reads and JSON decoding occur on the worker. No mixins or modifications to vanilla HUD layers are used; SteveCount registers its own layer.

`Settings` stores only appearance and connection preferences; counts and demo statistics remain in memory. `HudLayout` computes resolution-independent clamped placement. Minecraft's own installed Steve/Alex skins are referenced; those assets are not redistributed. Player head mode uses the local player's already loaded skin, without separate skin HTTP calls.

## Protocol

Fixed endpoint `http://127.0.0.1:18763`. Every request needs `Authorization: Bearer <token>` from `%LOCALAPPDATA%\SteveCount\connection.json`, created only after a successful bind. Host must be exactly `127.0.0.1:18763`; Origin headers are rejected. No CORS headers are supplied.

- GET /status: status, username, viewers (integer or null), peak, updated (Unix seconds), stale, message, room.
- POST /control: JSON `{ "action": "connect", "username": "@example" }` or `{ "action": "disconnect" }`. Input is validated before changing the current connection. Request bodies are capped at 2048 bytes.

The token is a same-user boundary, not protection from malicious software already running as that Windows user. No automatic companion download or execution is implemented.

## Known limits

Live-stream end-to-end verification needs an accessible active TikTok stream. Automated mocks do not establish upstream availability. Native Windows visual automation failed to initialize in the build environment and Application Control blocked the diagnostic EXE. Refer to TEST-REPORT.md; do not infer gameplay or live-stream verification from successful compilation.

Milestone visual feedback highlights the number briefly and shows a small HUD notification rather than using the vanilla toast queue. Arbitrary custom remote head lookup and companion auto-launch are omitted. Color palettes are available in the GUI; arbitrary colors require local JSON editing. No hosted services, other streaming providers, browser overlay, likes, chat or gifts are implemented.
