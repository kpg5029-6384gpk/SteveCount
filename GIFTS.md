# Hand Heart buddy punch

This optional feature changes gameplay in **single-player**. It is off by default.

1. Update **both** the SteveCount JAR for your loader and the companion EXE.
2. Run the companion and connect to Fiishy's TikTok LIVE in **K → Connect**.
3. Join the single-player world. Open **K → Character**.
4. Choose **Display: Character** or **Both**.
5. Turn **Gift damage: true**, then **Save & Done**.

A completed TikTok gift event named **Hand Heart** or **Hand Hearts**, reporting a unit coin value of **100**, makes the buddy lunge and swing, and performs **one normal, fully charged, non-critical, unarmed Minecraft player attack**. Without armor or other effects this is **1 health point (half a heart)**. No weapon, enchantment, damage multiplier, extra hit, sprint knockback, or critical is added. Vanilla armor, shields, resistance, invulnerability, creative immunity, damage rules and normal knockback apply. Size does not multiply damage.

**This can kill the player in hardcore**, including ordinary consequences of knockback. There is no artificial minimum-health floor. Turn Gift damage off to disable the action.

There is a **global five-second cooldown**. Gifts received during it are discarded. A completed streak triggers at most one punch regardless of its quantity. Duplicate events are ignored. Entering/re-entering a world, enabling damage, restarting/switching the companion, death, demo mode and disconnects do not replay old gifts. The connection/world must be ready for one second before new gifts can trigger damage.

**Inventory and menus:** gifts now work while inventory, crafting, chat, SteveCount settings and other non-pausing screens are open. For a screen that genuinely pauses single-player, such as the Escape pause menu or its options screens, SteveCount retains **one** punch and applies it when play resumes. Multiple gifts while paused do not stack into a burst. The pending punch expires after 60 seconds and is cleared on disconnect, world change, death or disabling gift damage. Minecraft is never forcibly unpaused. The five-second actual-hit cooldown still applies after resuming.

**Preview punch (no damage)** animates the buddy without TikTok and without damaging the player, even when real gift damage is enabled. Demo mode never causes gift damage. Preview does not send a fake gift or count it as a real TikTok statistic.

## Limits and troubleshooting

- Damage operates on the local integrated server in single-player. Multiplayer gift damage is unsupported; no attempt is made to bypass server authority. The visual follower/HUD still work on multiplayer.
- The visual avatar remains render-only. A temporary unarmed player object supplies vanilla's attack logic on the server thread; it is never registered, spawned, saved, equipped, or added to the player list.
- Gift metadata must be present, match one of the two English names above and report 100 coins. Unrecognized, localized or changed gift metadata is ignored. Other 100-coin gifts do not trigger it. The library now fetches room gift metadata when connecting.
- If nothing happens, update both components, check LIVE status, exit demo, enable Character/Both and Gift damage, close settings and wait a second. Do not use creative/spectator to check health loss.
- Events need a usable message ID or sender/group identity for duplicate protection. Unidentifiable events are ignored. Neither sender names nor IDs are included in the localhost feed or saved by this feature.
- Real gifts are subject to the unofficial TikTok library's upstream availability. Automated event and world checks do not establish receipt of a real paid gift; no paid live gift has been verified in development.

Only the opt-in action changes health/gameplay. With it off, SteveCount remains a visual counter and follower.
