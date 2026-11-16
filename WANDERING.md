# Little Buddy wandering

Included in the combined wandering and timed gift outfit release.

## Controls

- **J > Buddy options > Wandering**: On by default. Off restores the previous trailing behavior.
- **K > Character > Roam radius**: Uses your existing follow-distance value, from 1.5 to 6 blocks. This is the horizontal radius for Buddy's center, not a fixed distance he must maintain.
- Settings persist across restarts. Reset character restores wandering and the default 2.5-block radius.

## Behavior

Standing still: Buddy strolls to random near/far spots around you and rests for about
1.5 to 4.5 seconds on arrival. Looking around does not make him orbit your camera.
Walking: every 4 to 8 seconds he picks a position ahead, beside either side, or behind.
He follows your movement direction rather than the direction your camera faces and
moves faster to get into position. Rear positions can use recent footsteps around corners.

Manual emotes hold his position while you remain nearby. If you move out of range,
he catches up. Head riding takes priority over wandering. Actual game pause freezes
movement; world changes, teleporting, recall and hiding Buddy reset his route.

Grounded movement checks nearby ground and body clearance. Blocked destinations are
reselected, and missing ground or large drops are rejected while the player is on foot.
This is lightweight local steering, not a full pathfinding mob: barriers may briefly
leave him outside the radius until he finds a safe nearby position or catches up.
Grounded Off keeps the existing hovering behavior. No world entity, hitbox, server
installation, new external request or companion update is involved.

## Developer checks

Both loaders run core tests, including long deterministic idle and sprint simulations,
near/far coverage, both sides/ahead/behind choices, radius bounds, rest intervals,
teleport/radius changes, obstruction reset and old/new configuration handling.
The explicit `-PstevecountWanderSmoke` developer flag creates a disposable world and
checks actual idle travel, radius/clearance, moving catch-up, planted emotes, head
attachment, true pause and classic-follow mode. The flag is inactive in normal play.
