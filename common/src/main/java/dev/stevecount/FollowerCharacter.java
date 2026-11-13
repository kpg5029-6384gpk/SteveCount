package dev.stevecount;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.ClientAsset;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.entity.player.PlayerModelType;
import net.minecraft.world.entity.player.PlayerSkin;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/** Pure client render state: no entity spawn, world mutation, interaction or network packets. */
final class FollowerCharacter {
    static final FollowTrail trail=new FollowTrail();
    private static ClientLevel level;
    static int extractedFrames;
    static int extractionCallbacks;
    private static final PlayerSkin STEVE=skin("wide/steve",PlayerModelType.WIDE);
    private static final PlayerSkin ALEX=skin("slim/alex",PlayerModelType.SLIM);
    private static PlayerSkin skin(String path,PlayerModelType type) {
        return new PlayerSkin(new ClientAsset.ResourceTexture(Identifier.withDefaultNamespace("entity/player/"+path)),null,null,type,true);
    }
    static void recall() {trail.reset();}
    static void tick() {
        var mc=Minecraft.getInstance();var c=SteveCount.settings;
        if(mc.level!=level) {trail.reset();level=mc.level;}
        if(mc.player==null || level==null || !c.showCharacter() || !mc.player.isAlive()) {trail.reset();return;}
        if(mc.isPaused())return;
        Vec3 player=mc.player.position();
        Vec3 target=trail.target(player,mc.player.getYRot(),c.followDistance);
        boolean relocated=trail.position==null || trail.position.distanceToSqr(player)>256 || Math.abs(trail.position.y-player.y)>8 || trail.blocked>30;
        Vec3 next=trail.propose(player,target);
        if(c.characterGrounded && mc.player.onGround()) {
            next=ground(next,c.characterScale,player.y);
            if(!fits(next,c.characterScale)) {
                trail.blocked++;
                if(relocated) {
                    Vec3 safe=null;
                    for(int i=0;i<8;i++) {
                        double angle=i*Math.PI/4;
                        Vec3 probe=ground(player.add(Math.sin(angle)*c.followDistance,0,Math.cos(angle)*c.followDistance),c.characterScale,player.y);
                        if(fits(probe,c.characterScale)) {safe=probe;break;}
                    }
                    if(safe==null) {trail.reset();return;}
                    next=safe;trail.previous=safe;
                } else next=trail.position;
            }
            else trail.blocked=0;
        }
        trail.accept(next);
    }
    private static boolean fits(Vec3 pos,float scale) {
        double width=.6*scale,height=1.8*scale;
        return level.noCollision(new AABB(pos.x-width/2,pos.y+.02,pos.z-width/2,pos.x+width/2,pos.y+height,pos.z+width/2));
    }
    private static Vec3 ground(Vec3 pos,float scale,double fallbackY) {
        BlockPos at=BlockPos.containing(pos);
        if(!level.hasChunkAt(at))return new Vec3(pos.x,fallbackY,pos.z);
        // Read a small vertical column; never alter blocks or run server AI.
        for(int y=at.getY()+1;y>=at.getY()-3;y--) {
            BlockPos block=new BlockPos(at.getX(),y,at.getZ());
            var shape=level.getBlockState(block).getCollisionShape(level,block);
            if(!shape.isEmpty()) {
                double top=y+shape.max(Direction.Axis.Y);
                if(top<=pos.y+1.05) return new Vec3(pos.x,top,pos.z);
            }
        }
        return pos;
    }
    static String label(Bridge.Snapshot state) {
        String prefix=SteveCount.demo?(state.status().equals("Demo Mode")?"DEMO":"DEMO "+state.status()):state.status();
        if(state.viewers()==null)return prefix+" | Viewers unavailable";
        if(state.status().equals("Offline") || state.status().equals("Disconnected"))return prefix;
        boolean stale=state.stale() || !SteveCount.demo && System.currentTimeMillis()/1000d-state.updated()>60;
        return prefix+" | "+SteveCount.format(state.viewers())+" viewers"+(stale?" ~":"");
    }
    static void extract(LevelRenderState world) {
        if(Boolean.getBoolean("stevecount.followerSmoke"))extractionCallbacks++;
        var mc=Minecraft.getInstance();var c=SteveCount.settings;
        if(!c.showCharacter() || trail.position==null || level!=mc.level || mc.player==null || !mc.player.isAlive() || mc.gui.hud.isHidden())return;
        float partial=world.worldPartialTicks;
        Vec3 pos=trail.interpolated(partial);
        AvatarRenderState state=new AvatarRenderState();
        state.entityType=EntityTypes.PLAYER;
        state.x=pos.x;state.y=pos.y;state.z=pos.z;
        state.scale=c.characterScale;
        state.boundingBoxWidth=.6f*c.characterScale;
        state.boundingBoxHeight=1.8f*c.characterScale;
        state.eyeHeight=1.62f*c.characterScale;
        state.ageInTicks=c.animations?mc.player.tickCount+partial:0;
        state.bodyRot=trail.previousYaw+FollowTrail.wrap(trail.yaw-trail.previousYaw)*partial;
        Vec3 toward=mc.player.getEyePosition().subtract(pos);
        float look=(float)Math.toDegrees(Math.atan2(-toward.x,toward.z));
        state.yRot=Math.clamp(FollowTrail.wrap(look-state.bodyRot),-55,55);
        state.walkAnimationPos=trail.walk;
        state.walkAnimationSpeed=c.animations?trail.speed:0;
        state.skin=switch(c.characterSkin) {case "Alex"->ALEX;case "Player"->mc.player.getSkin();default->STEVE;};
        state.showCape=false;
        state.distanceToCameraSq=world.cameraRenderState.pos.distanceToSqr(pos);
        var lightPos=BlockPos.containing(pos.add(0,.7,0));
        state.lightCoords=LightCoordsUtil.pack(level.getBrightness(LightLayer.BLOCK,lightPos),level.getBrightness(LightLayer.SKY,lightPos));
        if(c.characterLabel) {
            var snapshot=SteveCount.current();
            boolean stale=snapshot.stale() || !SteveCount.demo && System.currentTimeMillis()/1000d-snapshot.updated()>60;
            int color=SteveCount.demo?0x7FFFD4:stale?0xFFC267:0xFFFFFF;
            state.nameTag=Component.literal(label(snapshot)).withColor(color);
            // Vanilla's name tag submitter adds another 0.5 blocks.
            state.nameTagAttachment=new Vec3(0,state.boundingBoxHeight-.30,0);
        }
        world.entityRenderStates.add(state);
        if(Boolean.getBoolean("stevecount.followerSmoke"))extractedFrames++;
    }
}
