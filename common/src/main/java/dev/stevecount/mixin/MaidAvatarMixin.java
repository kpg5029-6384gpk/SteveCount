package dev.stevecount.mixin;

import dev.stevecount.MaidCostume;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.world.entity.Avatar;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AvatarRenderer.class)
abstract class MaidAvatarMixin {
    @Inject(method="renderHand",at=@At("TAIL"))
    private void stevecount$hand(com.mojang.blaze3d.vertex.PoseStack stack,net.minecraft.client.renderer.SubmitNodeCollector collector,int light,net.minecraft.resources.Identifier skin,net.minecraft.client.model.geom.ModelPart arm,boolean sleeve,CallbackInfo ci) {
        var renderer=(AvatarRenderer<?>)(Object)this;
        MaidCostume.hand(arm,arm==renderer.getModel().rightArm,stack,collector,light);
    }
    @Inject(method="extractRenderState(Lnet/minecraft/world/entity/Avatar;Lnet/minecraft/client/renderer/entity/state/AvatarRenderState;F)V",at=@At("TAIL"))
    private void stevecount$costume(Avatar player,AvatarRenderState state,float partial,CallbackInfo ci) {MaidCostume.extract(state);}
}
