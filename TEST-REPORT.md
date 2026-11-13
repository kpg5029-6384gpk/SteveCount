# SteveCount verification report — 2026-11-12

## Follower character update — 2026-11-13

- Both loader builds compile; the core suite now contains **100 assertions**, including display modes, follower settings persistence, bounded route memory, corner-following, speed limits, idle camera turns, teleport reset, shortest-angle rotation, and truthful unknown/zero/stale/demo labels.
- Fabric and NeoForge each passed an **in-world smoke test in a fresh isolated QA world**, producing Steve/Alex, small zero-count demo, large stale-demo, character-only and teleport captures. The test checks that the avatar enters the render-state list, moves, catches up after teleport, and stops rendering in HUD-only mode.
- In-world screenshots were inspected. A final Fabric nine-screen GUI/preset smoke test also passed and its Character settings capture was inspected. The native nameplate anchor was adjusted after visual inspection.
- The first simultaneous NeoForge test was paused by lost focus and failed the test's render-frame assertion. The explicit test harness now disables pause-on-lost-focus in its isolated profile; the rerun passed. This test-only setting is not changed by the production feature.
- Rendering uses detached AvatarRenderState objects, not a spawned client/server entity. The production code does not create world entities or send movement/interaction packets. Only the explicit opt-in QA harness creates and teleports a player in its new test world.
- These new checks supersede the earlier no-in-world-testing limitation for the follower feature. They do **not** establish live TikTok integration, multiplayer/third-party shader compatibility, or FPS performance. The in-world counts used clearly labeled demo data.

The companion implementation is unchanged; the GitHub release workflow repeats its tests and standalone EXE startup check before publishing.

## Fabric addition and NeoForge regression verification

- Added Fabric 26.3 with Fabric Loader 0.20.1, Fabric API 0.166.2+26.3, Loom 1.19.4, Gradle 9.8.2, and Java 25.
- Both Fabric and NeoForge builds compile and pass **83 core assertions each**. The extra assertions exercise the actual Settings.save/load implementation and recovery from malformed on-disk settings.
- Both isolated development clients loaded successfully and completed all **eight settings/preset render captures each**, then exited successfully. Fabric's Expanded HUD and Appearance screenshots were visually inspected.
- Both loaders compile the same HUD, settings, screens, demo, milestones and bridge sources, with separate client-only registration adapters. The companion protocol and EXE are unchanged.
- Packaging validates separate Windows ZIPs containing exactly one appropriate loader JAR. Fabric API is required separately; Mod Menu is not required.
- No active TikTok stream, multiplayer server, interactive command/key/drag session, or gameplay capture was newly tested. Existing live-integration limitations below still apply.

The companion EXE in the preceding GitHub release passed startup and authenticated localhost checks on a GitHub Windows runner. The updated multi-loader workflow repeats those checks before publishing the new release.

## Passed

- Official Minecraft **26.3**, NeoForge **26.3.0.102**, Java **25.0.5**, Gradle **9.2.1**, ModDevGradle **2.0.148** dependency resolution and compilation. No Minecraft downgrade.
- Mod loaded in a real isolated NeoForge 26.3 development client. Client-only entrypoint was discovered and constructed. The complete render smoke client ran and exited successfully.
- Eight Minecraft-engine screenshots: Connection, Appearance, Testing, Advanced, Minimal, Compact, Streamer and Expanded. Screenshots were generated without a world, Internet viewer data, or companion. Representative settings and HUD images were visually inspected. The inspected Expanded image exposed a LIVE-label spacing issue, corrected in the final source.
- **81 Java core assertions**: config JSON file round-trip, demo-stat exclusion from config, invalid config clamps, placement bounds across GUI widths 320/427/640/854/1920/3840 and multiple HUD widths, full/abbreviated formatting, unknown versus genuine zero, demo data source, and stale demo transition.
- **31 Python tests**: username and URL normalization/rejection; supported numeric formats; malformed counts; zero preservation; peak and stale semantics; actual installed TikTokLive RoomUserSeqEvent construction; authenticated localhost HTTP status/control; invalid JSON; hostile Origin and Host rejection; unauthorized requests; mocked offline detection and exponential retry; relocated SSL certificate-asset regression.
- Windows x64 PyInstaller one-file, windowed companion build. TikTokLive 7.0.1 and betterproto2 metadata are bundled; source dependency versions are frozen in requirements-lock.txt.

## Not verified / blockers

- **No active TikTok LIVE stream was supplied or tested.** Real TikTok connectivity and propagation of real concurrent viewers to Minecraft remain unverified. Event-shape verification and mocked events are not live integration tests.
- **Standalone EXE execution is not fully verified.** The initial package displayed a startup exception. A user traceback later identified SSLContext construction failing because the build runtime's ssl.py requires codex-rejected-cas.pem. The build now bundles that asset at the frozen runtime root. A subprocess regression reproduced the identical FileNotFoundError without the asset and created a verified TLS context successfully with it. Missing betterproto2 distribution metadata was also corrected. Windows Application Control blocked the diagnostic EXE with “An Application Control policy has blocked this file.” The corrected final EXE was built but not executed to bypass that policy. No code-signing certificate was supplied.
- Native Windows automation could not initialize: its kernel twice failed with “failed to prepare MXC sandbox: permission path cannot be represented losslessly.” Visual QA therefore used opt-in Minecraft-engine screenshots, not automated Windows clicks.
- A default NeoForge early-loading-window handoff stalled on this machine. A JVM thread dump located the wait inside DisplayWindow.crashElegantly / handOverToMinecraft. Disabling the optional early window **only in the isolated development profile** allowed the client to load and the full render smoke to pass.
- Regular single-player gameplay, a remote multiplayer server, Modrinth import, capture in OBS/TikTok Studio, FPS profiling, mouse dragging and key/command input were not exercised interactively. The implemented rendering uses the registered client HUD layer; the successful screenshot smoke exercises its shared renderer in a positioning screen, not a gameplay world.
- Persistence was tested with a real temporary JSON file round-trip, not by interactively saving settings and restarting Minecraft.

## Warnings and release status

TikTokLive imports deprecated legacy websocket APIs. The tests pass with the locked dependencies; this upstream warning is not hidden. Gradle reports deprecated features in upstream tooling. The machine's Windows performance counters also generate OSHI diagnostics during Minecraft startup, without preventing the successful smoke run.

This is a compiled, tested **initial MVP / release candidate**, not a fully production-validated release. The remaining acceptance checks are an approved execution of the packaged companion, an accessible active stream end-to-end, and an in-world gameplay/capture check. Do not describe these as completed.

Optional omissions: automatic companion launching, arbitrary remote custom head lookup, a full GUI color picker, and vanilla toast-queue integration. Palette controls, loaded local-player head support, and a small HUD milestone notification are implemented.
