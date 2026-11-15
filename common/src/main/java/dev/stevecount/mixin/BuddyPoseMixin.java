package dev.stevecount.mixin;

import dev.stevecount.BuddyRenderState;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HumanoidModel.class)
abstract class BuddyPoseMixin {
    @Shadow @Final public ModelPart rightArm;
    @Shadow @Final public ModelPart leftArm;
    @Shadow @Final public ModelPart rightLeg;
    @Shadow @Final public ModelPart leftLeg;
    @Inject(method="setupAnim(Lnet/minecraft/client/renderer/entity/state/HumanoidRenderState;)V",at=@At("TAIL"))
    private void stevecount$pose(HumanoidRenderState state,CallbackInfo ci) {
        if(!(state instanceof BuddyRenderState buddy))return;
        float beat=(float)Math.sin(buddy.emoteTime*9);
        if(buddy.headRide) {
            rightLeg.xRot=leftLeg.xRot=-1.4f;rightLeg.yRot=.3f;leftLeg.yRot=-.3f;
        }
        switch(buddy.emote) {
            case "Wave" -> {rightArm.xRot=-2.5f;rightArm.zRot=-.4f+beat*.4f;}
            case "Cheer" -> {rightArm.xRot=leftArm.xRot=-2.7f+beat*.15f;rightArm.zRot=-.45f;leftArm.zRot=.45f;}
            case "Dance" -> {
                rightArm.xRot=beat*1.1f;leftArm.xRot=-beat*1.1f;
                rightArm.zRot=-.45f;leftArm.zRot=.45f;
                if(!buddy.headRide) {rightLeg.xRot=-beat*.6f;leftLeg.xRot=beat*.6f;}
            }
        }
    }
}
