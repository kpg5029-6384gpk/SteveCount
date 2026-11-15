# Player Emotes animations for Little Buddy

## Install

1. Replace SteveCount with the new matching Minecraft 26.3 Fabric or NeoForge JAR.
2. In your Modrinth instance choose **Add content**, search **Player Emotes** by
   **YamiKhal**, and install **2.0.3 for Minecraft 26.3** for your loader.
   Official page: https://modrinth.com/mod/player-emotes
3. Restart Minecraft. Press **J > Player Emotes**.
4. Search or page through the list and click an emote. It plays on Little Buddy.
   **Stop emote** stops loops or held poses. Use F5 to see a head-riding buddy.

Replace the companion EXE as well for the new Feeling voice phrase. No companion,
TikTok connection, server mod or internet is needed for the in-game animation buttons.

Player Emotes is an optional separate mod, not bundled or automatically downloaded.
Without it, SteveCount loads normally and its existing Wave/Dance/Cheer buttons,
head ride, random activity and voice commands continue working. The new library
menu explains the missing installation. Use **J > Player Emotes** for buddy actions;
Player Emotes' own B wheel controls your real player separately.

## Supported behavior

- The tested original Player Emotes 2.0.3 catalog provides **46 compatible solo emotes**.
  Animations are discovered from its loaded registry, including compatible resource-pack
  entries. Search checks names and IDs. Resource reloads are reflected when reopening.
  Variant-based entries use the first available default animation; the adapter does
  not advance Player Emotes' selection cycle or run its variant selectors.
- Native interpolation, Molang sampling, loop and hold timing come from Player Emotes'
  own animation engine. The adapter does not approximate its JSON with new sine-wave poses.
- Steve/Alex/Player/Fiishy use Player Emotes' humanoid pose application, including its
  normal limb bending. The astronaut retargets the same evaluated six-part poses onto
  its rounded rig; its limbs stay rigid, so elbow/knee-heavy motions differ visually.
- Whole-body transforms, such as the Backflip, are cosmetic render transforms only.
  They never move the follower's collision checks, the player, world blocks or hitboxes.
- Head riding keeps the seated legs and disables imported whole-body root motion;
  the upper body can still emote without jumping away from the player's head.
  The seat attaches directly to the rendered head bone, including pitch, yaw,
  crouching and flight transforms. Changing head-riding scale preserves seat contact.
- Playback stops on death, world changes, hiding the character or disabling animations.
  A gift punch temporarily takes visual priority. No animation triggers combat.

## Deliberate limits

Partner emotes, prop/item-dependent emotes, imported sounds/particles and server-side
emote requests are not played by this adapter. Automatic personality actions keep
using built-in emotes. Voice commands now select library emotes: Dance randomly picks Ground Spin, Robot Dance,
Matrix Dodge, Thriller or Side Step Shuffle; Wave uses the library Wave; “how you
feeling little buddy?” chooses a random compatible solo. Without the optional mod,
Dance/Wave fall back to built-ins and Feeling falls back to Cheer. Loops/held poses continue until stopped or replaced.

The optional adapter targets the verified public interfaces in **2.0.3**. If an upstream
update changes them, it disables the integration with a diagnostic instead of crashing
Minecraft. Reinstall the tested matching version if the library becomes unavailable.

## NeoForge preview compatibility

Player Emotes 2.0.3's NeoForge 26.3 build was compiled against an earlier beta API and
passes raw picture-in-picture renderer instances to the GUI. NeoForge 26.3.0.102 expects
renderer registrations with factories, causing a startup ClassCastException without
an adapter. SteveCount's NeoForge-only mixin converts only Player Emotes' legacy preview
entries into registrations. It does not change other mods' registrations. Fabric does
not load this mixin. Newer correctly registered entries pass through unchanged.

## Attribution and testing

Player Emotes and its animations are by **YamiKhal**:
https://github.com/YamiKhal/PlayerEmotes

SteveCount distributes only its own adapter, not upstream JARs, animations or source.
The official Modrinth listing identifies Apache-2.0 while current repository build
metadata identifies MIT; the optional integration avoids redistributing those assets.
Refer to the upstream distribution for its licensing and notices.

Local tests use official Player Emotes 2.0.3 JARs in disposable development instances.
They cover discovery, player and astronaut playback, loops, stopping, head riding,
search and coexistence with the original preview UI. Missing-mod startup and the
explanatory menu are tested separately. No new live TikTok or paid-gift test is claimed.
