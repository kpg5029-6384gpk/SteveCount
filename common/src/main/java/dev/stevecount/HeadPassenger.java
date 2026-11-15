package dev.stevecount;

/** Per-frame attachment on the owning avatar state, never a spawned passenger entity. */
public interface HeadPassenger {
    BuddyRenderState stevecount$getPassenger();
    void stevecount$setPassenger(BuddyRenderState passenger);
}
