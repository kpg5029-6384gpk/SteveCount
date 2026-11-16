# Little Buddy voice commands

Windows 11, English offline speech recognition, both Minecraft 26.3 loaders.
Install the new matching JAR AND replace the companion EXE for this update.

1. Launch SteveCount-Companion.exe. Voice starts OFF every launch.
2. Select your microphone, then click Start voice. Wait for Listening.
3. In Minecraft press J and enable Voice commands. Enter your world.
4. Say a complete phrase, then pause briefly: “little buddy hop on” or “dance little buddy”.
5. Use F5 to see the tiny head rider; it is hidden in first person.

Supported phrases (put “little buddy” before OR after):

| Command | Action |
| --- | --- |
| hop on / hop on my head / get on my head / head ride | Tiny head rider on |
| hop off / get off / get off my head / come down | Return to ground |
| dance | Random Ground Spin, Robot Dance, Matrix Dodge, Thriller or Side Step Shuffle from Player Emotes |
| wave | Player Emotes Wave |
| how you feeling / how are you feeling / how do you feel | Random compatible Player Emotes solo |
| cheer | Built-in Cheer |
| stop / stop dancing | Stop emote |
| come here / recall | Return to ground and recall buddy |

Install optional **Player Emotes 2.0.3 for Minecraft 26.3**, matching your loader,
for the library animations above. Consecutive random choices avoid the same emote
when another is available. Say “stop little buddy” to end loops. If the library is
missing or unavailable, Dance/Wave use their built-in versions and Feeling uses
built-in Cheer. Buttons for the existing built-in emotes remain unchanged.

This is a limited English command recognizer, not conversation or a speaker identity system.
The name and command must occur in the SAME utterance. Bare “dance” is ignored.
Commands require an active world and living player; discarded while the game is paused,
disabled, or changing worlds. No delayed action queue. Inventory/nonpausing menus work.
There is a short 1.5-second recognition cooldown; last fresh command wins if several arrive
between polls. Typical response includes an utterance-ending pause and up to one second
for localhost polling. Commands never call gift damage.

Both the in-game switch and companion Start are required. To stop microphone capture,
click Stop / mute voice in the companion or exit it. Turning off the Minecraft switch
only stops game actions; the companion's clearly displayed microphone status is authoritative.

Audio is processed locally in memory by Vosk, never recorded, logged or uploaded by SteveCount.
Only recognized action codes cross the authenticated localhost bridge. The English model is
bundled, so no Python installation, voice account, paid API or runtime model download is needed.
The companion uses additional memory for recognition while enabled (the upstream small-model
guidance is approximately 300 MB). TikTok remains independent and requires internet.

Choose the physical microphone, not a desktop/loopback input. Speaker playback or other people
saying the complete phrase may trigger it. Headphones help; this does not authenticate Fiishy's voice.
Windows Settings > Privacy & security > Microphone must allow desktop apps. If unavailable,
refresh microphones and retry; ordinary buttons and the counter continue working.

Vosk 0.3.45: https://github.com/alphacep/vosk-api (Apache-2.0).
Model vosk-model-small-en-us-0.15: https://alphacephei.com/vosk/models (Apache-2.0).
Model archive SHA256: 30f26242c4eb449f948e42cb302dd7a686cb29a3423a8367f99ff41780942498.
sounddevice 0.5.3: https://github.com/spatialaudio/python-sounddevice (MIT); PortAudio notices bundled.

Automated tests cover parsing, cooldown, disabled/expired/malformed feeds and command allowlist.
Synthesized speech is tested separately from a real microphone. Fiishy's actual microphone,
accent and live-stream background noise require real user testing.

## Easier requests and short emotes
You can say "little buddy can you dance please", "could you wave little buddy", or the existing short commands. You can also say "little buddy", pause, then give one command within three seconds, such as "dance". Without a recent wake phrase, bare commands do nothing. The wake window is consumed by the next nonempty recognized utterance and clears when voice is muted or restarted.

Voice-triggered Dance, Wave, Cheer and Feeling emotes now finish within five seconds, including looping Player Emotes animations. Short one-shots finish sooner. Head riding remains active until Hop off; manual menu emotes keep their existing behavior.

If recognition still misses requests, confirm the companion is listening to the intended physical microphone and that its input level is clear. This update adds supported natural phrases and pause handling; it cannot guarantee recognition for every microphone or background-noise condition. Synthesized audio checks pass, but Fiishy's microphone has not been tested here.
