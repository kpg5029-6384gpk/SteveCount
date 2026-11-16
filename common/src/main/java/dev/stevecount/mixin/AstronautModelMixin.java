package dev.stevecount.mixin;

import dev.stevecount.AstronautModel;
import dev.stevecount.HeadRide;
import dev.stevecount.MaidCostume;
import dev.stevecount.HeadPassenger;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import dev.stevecount.BuddyRenderState;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(LivingEntityRenderer.class)
abstract class AstronautModelMixin {
    @Inject(method="extractRenderState(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;F)V",at=@At("TAIL"))
    private void stevecount$clearPassenger(net.minecraft.world.entity.LivingEntity entity,LivingEntityRenderState state,float partial,CallbackInfo ci) {
        if(state instanceof HeadPassenger passenger)passenger.stevecount$setPassenger(null);
    }
    @Inject(method="submit(Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/level/CameraRenderState;)V",
        at=@At(value="INVOKE",target="Lnet/minecraft/client/renderer/SubmitNodeCollector;submitModel(Lnet/minecraft/client/model/Model;Ljava/lang/Object;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/rendertype/RenderType;IIILnet/minecraft/client/renderer/texture/UvMapping;I)V"))
    private void stevecount$headPassenger(LivingEntityRenderState state,PoseStack stack,SubmitNodeCollector collector,CameraRenderState camera,CallbackInfo ci) {
        if(state instanceof AvatarRenderState owner)MaidCostume.submit(owner,stack,collector);
        if(state instanceof AvatarRenderState avatar && !(state instanceof BuddyRenderState)
            && ((HeadPassenger)avatar).stevecount$getPassenger()!=null
            && ((LivingEntityRenderer<?,?,?>)(Object)this).getModel() instanceof PlayerModel model)
            HeadRide.submit(avatar,model,stack,collector);
    }

    @ModifyArgs(method="submit(Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/level/CameraRenderState;)V",
        at=@At(value="INVOKE",target="Lnet/minecraft/client/renderer/SubmitNodeCollector;submitModel(Lnet/minecraft/client/model/Model;Ljava/lang/Object;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/rendertype/RenderType;IIILnet/minecraft/client/renderer/texture/UvMapping;I)V"))
    private void stevecount$astronaut(Args args) {
        if(args.get(1) instanceof BuddyRenderState buddy && buddy.astronaut)args.set(0,AstronautModel.instance());
    }
}
