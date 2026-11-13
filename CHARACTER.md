# In-world follower character

SteveCount can show its count as a miniature character that follows your player, inspired by the small follower in the reference video. This is optional; existing installations keep the HUD by default.

## Enable it

1. Install the SteveCount JAR for your loader and join a world or server.
2. Press **K** or enter **/stevecount**.
3. Open **Character**.
4. Click **Display** to cycle **HUD → Character → Both**.
5. Choose **Steve**, **Alex**, or **Player** (your already-loaded player skin).
6. Adjust **Character size** and **Follow distance**. Turn the count label or grounded movement on/off as desired.
7. Click **Save & Done**. These options persist in `config/stevecount.json`.

Press **F5** for a third-person view. In first person, a character following behind you will normally be outside your view; turn around while standing still to see it. **Recall companion** brings it back to your current vicinity. **Reset character** restores defaults and returns to HUD mode.

The character follows your recent movement route, animates its arms/legs, and looks toward you. Grounded mode samples nearby blocks to keep its feet near the ground; turn it off for a hovering follower. It catches up after a large teleport and resets when you disconnect or change dimensions. F1 hides it with the HUD.

## Counts and testing

The nameplate uses the same source as the HUD: a real received viewer count, an explicit unavailable status, or a count marked `~` when stale. A real zero remains zero. Offline and disconnected states are labeled. Use **Demo** to try simulated counts and connection states without TikTok or the Python companion; demo nameplates are clearly marked DEMO.

The global **Animations** setting under **More** disables its limb/idle animation along with the existing milestone animation. Movement still follows your player. Size ranges from 0.25 to 1.0 of the base avatar and follow distance from 1.5 to 6 blocks.

## Client-only behavior

This is a render-only avatar, not an entity inserted into the world. It has no collision, health, AI, inventory, hitbox, or interaction. It cannot push mobs, trigger plates, take damage, or change blocks. No server installation is needed and it is not saved in your world. Other players do not see it, but streaming software capturing your Minecraft view can capture it.

The movement is a lightweight route follower, not full mob pathfinding. It can catch up by repositioning when stuck or far away; it does not break blocks or navigate every obstacle like a server-controlled pet. Steve/Alex textures come from the installed game; the reference video's custom model and texture are not copied.
