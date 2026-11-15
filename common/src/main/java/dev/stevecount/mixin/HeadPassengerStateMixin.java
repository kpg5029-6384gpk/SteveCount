package dev.stevecount.mixin;

import dev.stevecount.BuddyRenderState;
import dev.stevecount.HeadPassenger;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(AvatarRenderState.class)
abstract class HeadPassengerStateMixin implements HeadPassenger {
    @Unique private BuddyRenderState stevecount$passenger;
    public BuddyRenderState stevecount$getPassenger() {return stevecount$passenger;}
    public void stevecount$setPassenger(BuddyRenderState passenger) {stevecount$passenger=passenger;}
}
