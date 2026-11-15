# SteveCount

**Rounded astronaut and random personality:** choose **K → Character → Buddy: Astronaut** for the new non-blocky model. **J → Buddy options → Personality / sounds** controls random Wave/Dance/Cheer, quiet robot noises, Calm/Playful timing and volume. These are cosmetic and work offline. See [ASTRONAUT.md](ASTRONAUT.md).

**Menu fix, Fiishy skin and emotes:** gift punches now work in inventory/non-pausing menus; true pause retains one expiring punch for resume. Select **K → Character → Buddy: Fiishy** for the linked skin. Press **J**, or choose **Buddy options > Emotes / Voice commands**, for Wave, Dance, Cheer and the tiny head rider. See [EMOTES.md](EMOTES.md).

**Optional 100-coin Hand Heart punch:** in single-player, enable **Character → Gift damage** to have the buddy perform one ordinary fist attack per accepted gift. Unarmored damage is half a heart; vanilla armor and immunity apply. Five-second cooldown, no queued hits, off by default. Can kill in hardcore. Update both the JAR and EXE. See [GIFTS.md](GIFTS.md).

Download the Windows bundles from the [GitHub Releases page](https://github.com/kpg5029-6384gpk/SteveCount/releases). Choose **SteveCount-Windows-Fabric.zip** or **SteveCount-Windows-NeoForge.zip**, matching the loader in your Modrinth instance.

A client-side Minecraft HUD for TikTok LIVE concurrent viewers. SteveCount includes a Steve/Alex/player head, four presets, drag positioning, viewer milestones, and a demo that works without Internet or the companion.

**New optional follower character:** open settings → **Character**, then choose **Character** or **Both**. A miniature Steve, Alex, or your player skin follows you in the world with the current count above it. It is render-only and visible only to your client/game capture. See [CHARACTER.md](CHARACTER.md).

**Build targets:** Minecraft Java 26.3 with either NeoForge 26.3.0.102 or Fabric Loader 0.20.1 + Fabric API 0.166.2+26.3. Java 25, Windows 11 x64. This is an initial MVP build, not a published Modrinth project. Read TEST-REPORT.md for the precise verification status before using it on stream.

## Fabric support

Download **SteveCount-Windows-Fabric.zip** for a Fabric instance, or **SteveCount-Windows-NeoForge.zip** for NeoForge. The legacy SteveCount-Windows.zip filename is the NeoForge bundle. Install only the JAR for your loader. Both builds use the same companion EXE, settings format, HUD, presets, demo mode, and milestones. Fabric additionally requires the Minecraft 26.3 version of Fabric API, installed separately through Modrinth's Add content feature. See FABRIC.md for exact steps.

## Install and connect

1. Extract SteveCount-Windows.zip to a normal folder you can write to.
2. In Modrinth App, use a Minecraft **26.3 NeoForge** instance. Add `SteveCount-26.3-NeoForge.jar` through the instance's local-content option, or open the instance folder and copy it into `mods`.
3. Double-click `SteveCount-Companion.exe`. Keep its small status window open. Python, pip, administrator rights, and server installation are not required.
4. Start Minecraft. Press **K** (reassignable in Controls) or enter `/stevecount` in a world. NeoForge also offers **Mods → SteveCount → Config**. The Fabric build does not require Mod Menu.
5. Under Connection, enter `@yourname` or `https://www.tiktok.com/@yourname/live`. Click Connect.
6. Begin your TikTok LIVE. SteveCount retries while you are offline. The count appears only after a genuine viewer event arrives.

The HUD renders inside Minecraft's normal client HUD and is included in captures of the game. F1 hides it along with the standard HUD. The HUD/follower are visual; the optional single-player gift punch explicitly changes gameplay when enabled. No matching server mod is needed for single-player.

## Customize

Appearance offers Minimal (head + count), Compact (head + eye + count), Streamer (head + LIVE + count), and Expanded (status, count, peak, username). Choose Steve, Alex, your own loaded player skin, or no head. Scale ranges from 0.5 to 3, with configurable background opacity and borders. Advanced offers white/aqua text palettes, dark/slate backgrounds, offsets, and milestone controls. Arbitrary ARGB/RGB colors are also supported in the local settings file.

Select Move HUD to drag the preview. Size, opacity, preset and reset controls update immediately. Save position, then Save & Done. Coordinates use relative screen anchors plus GUI-pixel offsets and are clamped to the screen, accounting for GUI scaling. Settings are saved in the instance's `config/stevecount.json`.

## Test without streaming

Open Demo and enable Demo. Set a whole-number count, use +10 / -10, turn on automatic simulation, or simulate Offline / Reconnecting. Move HUD previews 128 when no real count exists. Demo data is labeled DEMO, never written as stream statistics, and resets when Minecraft restarts. Turning demo on disconnects the companion's stream. Click Connect to return to real data.

## States and count accuracy

| State | Meaning |
|---|---|
| Not Configured | No active connection configured |
| Connecting | Opening the requested creator's LIVE |
| Live | Connected; the number may still be unavailable until the first viewer event |
| Offline | Creator offline or an end event arrived; automatic retries continue |
| Reconnecting | A connection was interrupted or companion is unavailable |
| Disconnected | You disconnected intentionally |
| Error | Access, upstream, input, or signing failure; details appear in settings |
| Demo Mode | Local simulated data, never a real audience |

`~` and amber text indicate stale data: the last valid count is over 60 seconds old, or the connection is interrupted. This is a cautious UI threshold, not a claim that TikTok promises events every minute. A true zero renders as 0; unavailable data never becomes zero. Old counts are retained during short interruptions. Offline status is displayed explicitly. Session peak resets when the companion observes a different room ID or you switch/connect an account; it is kept only in memory.

Milestones default to 10,25,50,100,250,500,1000. Customize the comma-separated list in Advanced. Each triggers at most once per observed session. Animation, a small HUD notification, and sound can be disabled independently. Sound defaults off.

## Privacy and upstream dependency

No passwords, login, browser sessions, analytics, telemetry, log uploads, or tracking services are implemented. The companion binds **127.0.0.1:18763** only, requires a per-launch random bearer token, rejects Origin headers and unexpected Host headers, and limits request bodies. Token and logs live under `%LOCALAPPDATA%\SteveCount`. Do not share `connection.json` while the companion is running.

TikTokLive 7.0.1 is unofficial. Its required connection signing uses **api.eulerstream.com**; TikTok endpoints and that signing service are necessary external dependencies. No API key or paid plan is configured by SteveCount. Availability, anonymous quotas, regional restrictions, and TikTok changes may prevent access. Restricted streams requiring login are unsupported; SteveCount will not request credentials. It uses actual `RoomUserSeqEvent.total` and never cumulative `total_user`, join totals, followers, likes, or estimated viewers.

The companion must be started explicitly. Automatic launch was considered but omitted to avoid brittle installation paths or executing an unexpected file. Minecraft auto-connect is optional and only contacts the installed localhost companion; nothing is downloaded or executed by the mod.

See INSTALL.md, TROUBLESHOOTING.md, DEVELOPMENT.md and TEST-REPORT.md.


## Optional offline voice commands
Replace both the JAR and companion for this update. In the companion select your microphone and Start voice. In Minecraft press J and enable Voice commands. Say “little buddy hop on”, “hop off little buddy”, or “dance little buddy”. Voice starts off each companion launch. Stop / mute voice stops microphone capture. See [VOICE.md](VOICE.md) for commands, privacy and troubleshooting.

## Regular mode and adjustable head rider
Press **J > Buddy options** (or K > Character > Buddy options). The head-riding size slider changes the seated buddy from 8% to 60%, separately from ground size. **Regular** keeps the buddy without viewer HUD/status labels, milestones or TikTok gift damage; **Streamer** restores stream displays. Both settings persist. See [BUDDY-MODES.md](BUDDY-MODES.md). This update needs only the matching new JAR; the offline-voice companion remains compatible.

## Optional Player Emotes library
Install **Player Emotes 2.0.3 for Minecraft 26.3** for your loader from https://modrinth.com/mod/player-emotes, alongside the matching updated SteveCount JAR. Restart and press **J > Player Emotes** to search 46 compatible solo animations for Little Buddy. Player-style skins use native limb poses; the astronaut retargets them onto its rigid rounded limbs. Partner/prop emotes and imported sound/particle effects are excluded. SteveCount still works without this optional mod, and the companion is unchanged. See [PLAYER-EMOTES.md](PLAYER-EMOTES.md).
