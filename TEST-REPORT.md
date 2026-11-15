# SteveCount verification report — 2026-11-12

## Head attachment and voice library actions — 2026-11-15

- The rider is submitted at the owning avatar's actual rendered head transform, not body height. It follows pitch/yaw and the renderer's crouch/flight transforms. Its seated contact offset scales with the model. Native player states carry only a per-frame attachment, cleared at extraction; no entity or world data is created.
- Both loaders compiled with **236 core assertions** each. Geometry checks cover 8%, 18%, 42% and 60% scales and pitches from -90 to +90 degrees. Voice tests restrict Dance to the five requested IDs, avoid immediate repeats, select the library Wave, select compatible Feeling solos and handle a missing library.
- Real disposable-world tests with official Player Emotes 2.0.3 cover player/astronaut rendering, head riding, looking up/down and enlarged seated contact. Representative screenshots were inspected. The tests also check synthetic voice dispatch and the existing inventory/pause combat regressions. Dedicated flight/crouch animation scenarios, custom player rigs and shader packs were not separately tested.
- **85 Python tests passed locally**, including the new phrase, wake-name requirement and action feed. A locally synthesized “How you feeling little buddy?” sample was recognized as Feeling using the bundled Vosk model. This does not verify Fiishy's microphone, accent or background noise. No new live TikTok or paid-gift test was performed.
- Install Player Emotes 2.0.3 separately to use its animations. Replace the JAR and companion EXE for this update. Without the optional mod, Wave/Dance use built-ins and Feeling uses Cheer.


## Rounded astronaut and random personality — 2026-11-15

- Fabric and NeoForge rendered the rounded custom mesh in real disposable development worlds and passed the extended astronaut/emote/head-ride/automatic activity checks. Native mesh compilation was asserted, and representative wave, dance, cheer, punch-preview, head-ride and personality-menu captures were visually inspected. Inventory and true pause/resume gift tests and normal combat regressions remain passing with synthetic events.
- The core suite now has **140 assertions** per loader: bounded Calm/Playful intervals, no per-frame/catch-up repetition, busy/manual/punch protection, pause/disable reset, animations-off behavior, smooth finite mesh normals, bundled sound format, invalid preferences and personality persistence, plus earlier checks. All **36 companion tests** pass locally; the protocol is unchanged.
- All three original Ogg sounds decoded successfully with ffmpeg, with durations about .30/.44/.62 seconds and peaks below half the available sample range. Sound-event loading and one-shot API dispatch were checked in Minecraft. Physical speaker output was not independently audited. The first preliminary run began before generated sound files were copied into the runtime resources and logged an empty sound; the rebuilt final run includes all Oggs and has no such warning.
- A new persistence test initially mutated the settings object still used by another assertion; the test fixture was corrected. Final builds/core checks pass.
- The model and synthetic sounds are original project assets inspired by the supplied illustration; the reference image is not redistributed. Existing Minecraft-avatar options remain supported. No extra client mod dependency or companion changes are required.
- Real paid TikTok gifts, multiplayer/shader interoperability and FPS profiling are not newly verified. These cosmetic actions never call the gift-damage path. Existing gift acceptance remains restricted to the configured Hand Heart names/value.

## Menu fix, Fiishy skin and emotes — 2026-11-15

- Both Fabric and NeoForge passed the extended in-world smoke test in separate new disposable QA worlds. A synthetic gift received with the actual inventory screen open removed exactly 1 HP without closing the inventory. A gift received during a real Escape pause was retained without damage, then removed exactly 1 HP after resume. Existing armor, immunity, creative and lethal damage checks still passed.
- The core suite now contains **125 assertions** per loader, covering one-slot pause retention, expiry, coalescing, actual-hit cooldown, disconnect/disable clearing, skin/head-ride persistence, emote durations and the bundled PNG signature/dimensions in addition to prior checks. All **36 companion tests** passed locally; no companion protocol changes were needed.
- Wave, Dance, Cheer and the tiny head rider entered the expected render states in both loaders. Head height/scale, animations-off head riding, and the new emote screen were checked. Representative final Fabric skin/emote/head-ride screenshots were inspected. An initial visual run revealed an incorrectly prefixed texture ID; it was corrected and the final skin rendered successfully on rerun.
- The pose hook is client-only and marker-gated to BuddyRenderState. It does not change poses for normal players or mobs. The selected skin is bundled with a separate source/attribution notice. Cosmetic emotes have no gift bindings.
- Tests use synthetic events and fresh QA worlds only. The requester reported the preceding version working during an actual LIVE; this is not a developer-run paid-gift test of this update. Real paid TikTok gifts for the new menu scenarios, multiplayer/shader compatibility and FPS profiling remain unverified.

## Hand Heart punch update — 2026-11-14

- Both Fabric and NeoForge passed the new in-world gift-punch test in fresh disposable QA worlds. A synthetic localhost gift feed passed through the production cursor/action path and removed exactly **1 HP (half a heart)** unarmored. Checks also passed for duplicate-feed suppression, vanilla immediate-hit immunity, reduced damage under diamond armor, creative immunity and lethal damage at low health. No real hardcore save was used.
- The first damage assertion ran in the existing Peaceful QA world, whose automatic regeneration restored health before the assertion. The test now explicitly uses Normal difficulty for damage checks; both reruns passed.
- The companion suite now has **36 passing tests locally**, including the installed-library GiftEvent protobuf shape, completed streaks, name/value filtering, duplicate IDs, global cooldown, discarded cooldown events, bounded memory, stale feed pruning and companion sessions. The runtime-specific SSL regression may skip on standard Python.
- Both loader builds pass **112 Java core assertions**, including feed/session baselining, unknown/malformed payloads, cooldown/replay/expiry behavior and damage opt-in settings persistence/defaults.
- No actual paid TikTok gift has been received or verified. Real TikTok end-to-end integration remains unverified. Multiplayer damage is intentionally unsupported; the action uses the single-player integrated server only.
- Optional gift damage is a deliberate gameplay change, off by default. Earlier render-only/no-gameplay statements describe the HUD and visual follower, not the enabled gift action.

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

## Offline voice update
148 Java core assertions per loader; 78 Python tests. Vosk decoded synthesized English “little buddy hop on” into head_on using the bundled model. Both loaders tested in disposable worlds using synthetic voice feeds: head on/off, dance, stop, disabled replay rejection. No microphone was opened during these tests. Actual microphone capture, Fiishy’s accent and stream background noise remain unverified. The Windows workflow separately tests the packaged recognition libraries/model with synthesized speech and the localhost service.

## Adjustable head rider and Regular mode
156 Java core assertions per loader cover size limits/nonfinite recovery, saved settings, Streamer display restoration and Regular display suppression. Both loaders verified in disposable worlds: custom 42% head-rider render scale, no name tag/HUD in Regular, options screen capture, and existing cosmetic/voice/combat regressions. No new paid gift/live TikTok tests; companion unchanged.

## Player Emotes integration
162 Java core assertions per loader, including no required Player Emotes classes and imported playback duration/stop checks. Official Player Emotes 2.0.3 JARs tested locally on Fabric and NeoForge in disposable worlds: 46 solo entries, native humanoid Wave/Floss, astronaut Wave/Robot Dance/Backflip, stop, loops, seated head-ride emote, search, and original native UI coexistence. Representative screenshots inspected. NeoForge 26.3.0.102 exposed a legacy preview-registration mismatch in the upstream mod; the scoped NeoForge-only adapter corrected it. Separate loaded and missing-mod menu smoke tests are used. No new paid gifts/live TikTok connection tests or FPS profiling. No Player Emotes assets are redistributed.
