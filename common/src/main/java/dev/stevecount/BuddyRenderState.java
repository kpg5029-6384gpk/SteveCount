package dev.stevecount;

import net.minecraft.client.renderer.entity.state.AvatarRenderState;

/** Marker confines the pose hook to our detached avatar, never normal players. */
public final class BuddyRenderState extends AvatarRenderState {
    public String emote="None";
    public float emoteTime;
    public boolean headRide;
    public boolean astronaut;
    public Object playerEmoteFrame;
}
