package dev.stevecount;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import org.joml.Vector3f;

/** Uses the same model/head transform as the owner, including crouching and flight. */
public final class HeadRide {
    public static int submittedFrames;
    public static float lastSeatError;
    public static Vector3f lastUp=new Vector3f();
    public static void submit(AvatarRenderState owner,PlayerModel model,PoseStack parent,SubmitNodeCollector collector) {
        BuddyRenderState buddy=((HeadPassenger)owner).stevecount$getPassenger();
        if(buddy==null)return;
        // Set up the owner's actual pose before reading its head bone. No world coordinates
        // or camera angles are guessed, and the parent's stack is never modified.
        model.setupAnim(owner);
        PoseStack mounted=new PoseStack();mounted.mulPose(parent.last().pose());
        model.root().translateAndRotate(mounted);
        model.head.translateAndRotate(mounted);
        mounted.translate(0,-.5,0); // Top surface of the eight-pixel player head.
        Vector3f contact=mounted.last().pose().transformPosition(new Vector3f());
        lastUp=mounted.last().pose().transformDirection(new Vector3f(0,-1,0)).normalize();
        mountSeat(mounted,buddy.scale);
        lastSeatError=mounted.last().pose().transformPosition(new Vector3f(0,.75f,0)).distance(contact);
        net.minecraft.client.model.Model<? super BuddyRenderState> buddyModel=buddy.astronaut?AstronautModel.instance():Minecraft.getInstance().getEntityRenderDispatcher().getRenderer(buddy).getModel();
        collector.submitModel(buddyModel,buddy,mounted,RenderTypes.entityCutout(buddy.skin.body().texturePath()),buddy.lightCoords,OverlayTexture.NO_OVERLAY,-1,null,0);
        submittedFrames++;
    }
    static void mountSeat(PoseStack stack,float scale) {
        stack.scale(scale,scale,scale);
        stack.translate(0,-.75,0); // Seated pelvis is at model pixel 12, independent of scale.
    }
}
