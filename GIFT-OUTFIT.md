# Fiishy's timed gift outfit 

The approved maid skin overlay is applied only to the local player's appearance.
Little Buddy, other players and the Minecraft account skin stay unchanged.

- An individual TikTok gift priced **1,000 coins or more** starts **30 minutes**.
- A later qualifying gift **restarts** the timer at 30 minutes. Time does not stack.
- A streak/multiple cheaper gifts does not satisfy the individual-gift threshold.
- Completed gift events are deduplicated; stale events and previous sessions do not replay.
- This is separate from the 100-coin Hand Heart punch trigger and never causes damage.

The deadline and player UUID persist in the existing local settings. Real elapsed time
continues while paused, offline or closed. Expiry automatically restores the ordinary
player rendering. No cached skin replaces the actual current skin, so the original
appearance returns naturally. Another player's UUID cannot inherit the timer.

## In-game controls

**J > Buddy options > Gift outfit** offers enable/disable, a clearly labeled manual
30-minute preview, Restore original outfit now, and Reload approved costume PNG.
A small bottom-right countdown appears while wearing it. F1 hides it with the HUD.
Disabling restores immediately. New real-gift triggers require Streamer mode, Live
status and demo off. Once started, the timer continues if the stream ends.

The outfit works in third person, inventory previews and first-person arms. Armor is
hidden in render-state copies while the costume is active so it remains visible;
real equipment, protection, durability and held items are not changed. This is a
client-side effect visible in Fiishy's game capture, not a skin broadcast to servers.

The approved PNG is bundled. An optional 64 x 64 slim overlay at
`config/stevecount/maid-outfit.png` can override it after Reload. Missing/invalid files
fail gracefully. See COSTUME-NOTICE.md for 8onfire's original asset attribution.

Both the updated mod JAR and companion EXE are needed for the new cosmetic event
feed. Previous companions continue working for the other features. Both features are included in the combined release.
