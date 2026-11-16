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

The bridge uses one daemon background worker and one persistent Java HttpClient, polling the localhost status once per second with bounded timeouts. TikTok updates remain event driven. No external networking or disk reads occur in rendering. Immutable snapshots cross threads. Local token reads and JSON decoding occur on the worker. SteveCount registers its own HUD layer. A client-only HumanoidModel pose hook returns immediately unless the state is our detached BuddyRenderState; normal players and mobs retain vanilla poses.

`Settings` stores only appearance and connection preferences; counts and demo statistics remain in memory. `HudLayout` computes resolution-independent clamped placement. Minecraft's own installed Steve/Alex skins are referenced; those assets are not redistributed. Player head mode uses the local player's already loaded skin, without separate skin HTTP calls.

## Protocol

Fixed endpoint `http://127.0.0.1:18763`. Every request needs `Authorization: Bearer <token>` from `%LOCALAPPDATA%\SteveCount\connection.json`, created only after a successful bind. Host must be exactly `127.0.0.1:18763`; Origin headers are rejected. No CORS headers are supplied.

- GET /status: status, username, viewers (integer or null), peak, updated (Unix seconds), stale, message, room.
- POST /control: JSON `{ "action": "connect", "username": "@example" }` or `{ "action": "disconnect" }`. Input is validated before changing the current connection. Request bodies are capped at 2048 bytes.

The token is a same-user boundary, not protection from malicious software already running as that Windows user. No automatic companion download or execution is implemented.

## Known limits

Live-stream end-to-end verification needs an accessible active TikTok stream. Automated mocks do not establish upstream availability. Native Windows visual automation failed to initialize in the build environment and Application Control blocked the diagnostic EXE. Refer to TEST-REPORT.md; do not infer gameplay or live-stream verification from successful compilation.

Milestone visual feedback highlights the number briefly and shows a small HUD notification rather than using the vanilla toast queue. Arbitrary custom remote head lookup and companion auto-launch are omitted. Color palettes are available in the GUI; arbitrary colors require local JSON editing. No hosted services, other streaming providers, browser overlay, likes or chat are implemented.

## Gift action protocol

`gifts.py` filters completed Hand Heart/Hand Hearts events at diamond_count=100, deduplicates bounded IDs, and rate-limits to one accepted event per five seconds. Status adds `gifts: {session, sequence, events: [{sequence, created}]}`. Timestamps are epoch seconds. Only events at most five seconds old are included. Sender identity stays in bounded companion memory and is never sent to Minecraft or saved.

`GiftCursor` baselines new sessions, consumes while disarmed, rejects stale events and enforces another client-side cooldown. `GiftPunch` requires active single-player gameplay, real LIVE state, enabled character/damage and no demo. It schedules vanilla Player.attack on the integrated server thread with an unregistered empty-handed survival Player, fully charged, grounded, not sprinting, no effects. Never writes target health in production. The explicit opt-in changes gameplay; visual-only claims apply with gift damage disabled.

Use `-PstevecountPunchSmoke` with runClient to create an isolated disposable world, run the existing follower checks and then exercise synthetic-gift damage. QA-only health/equipment/world changes occur solely in that fresh test world.

## Menus, emotes and bundled skin

GiftPunch eligibility no longer requires a null screen. PendingPunch retains one accepted action during true pause, expires it after 60 seconds, clears it on lost eligibility and limits actual hit dispatch to five seconds. GiftCursor still provides strict session/sequence replay protection. Client ticks continue while Minecraft's integrated world is paused.

BuddyEmotes owns bounded cosmetic emote lifetimes; settings store only skin and head-ride preference. BuddyRenderState tags the detached avatar. The isolated client-only BuddyPoseMixin adjusts limbs after HumanoidModel.setupAnim only for that marker. Standard model setup resets poses every frame. Head riding uses interpolated player position and bounding-box height, a fixed .18 scale and a seated pose; first-person hides it. Punch animation takes priority. The bundled PNG is a ResourceTexture with logical ID stevecount:entity/fiishy, resolved to textures/entity/fiishy.png.

## Rounded astronaut and personality

AstronautModel owns immutable ellipsoid vertex arrays with smooth normals and six articulated ModelParts. A narrowly marker-gated ModifyArgs hook substitutes this model only when a submitted BuddyRenderState has astronaut=true; ordinary entity models and previous buddies are untouched. Native transforms, vertex lighting, overlay and viewer name tag submission are preserved. The custom model suppresses vanilla clothing/equipment layers; it is still a detached client render state, not an entity.

BuddySchedule is a pure seeded-testable scheduler. Timers reset on ineligibility, world changes, mute/disable and activity-mode changes. Busy manual emotes/punches defer the next opportunity rather than queueing catch-up actions. BuddyPersonality runs only client-side and dispatches bounded automatic Wave/Dance/Cheer or local one-shot sounds. It never invokes gift controls or damage.

Three original synthesized Oggs and the model's white vertex-color texture are committed. To regenerate them, run scripts/generate-buddy-assets.py with ffmpeg on PATH or imageio-ffmpeg installed in a developer Python environment. This is not needed to build or install the mod. Sounds use the Friendly Creatures category, local attenuation and the separate buddy volume. No new runtime dependency is introduced.

## Optional Player Emotes QA
Install the official matching 2.0.3 JAR only in the development instance run/mods directory. Use `runClient -PstevecountPlayerEmotesSmoke` for disposable-world animation QA. `runClient -PstevecountSmoke` also captures the optional library and, when installed, the native upstream preview library. Run the latter again without the optional JAR to verify fallback. Do not include upstream assets or test dependencies in distribution archives. The adapter reflects verified public 2.0.3 methods so the normal compile/runtime classpath has no required dependency.


Held outfit QA: `-PstevecountCostumeSmoke` creates a fresh disposable world, uses
synthetic cosmetic gift feeds and the approved bundled PNG, and checks 1000+ activation,
reset/999 rejection, self-only rendering, first-person arms, wide/slim variants,
Survival inventory, armor retention, expiry and disabling. Never enable development
smoke flags in an existing player world. Normal installs do not activate them.
