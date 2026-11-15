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
    static BuddyRenderState lastSmokeState;
    private static final PlayerSkin STEVE=skin("wide/steve",PlayerModelType.WIDE);
    private static final PlayerSkin ALEX=skin("slim/alex",PlayerModelType.SLIM);
    private static final PlayerSkin FIISHY=new PlayerSkin(new ClientAsset.ResourceTexture(Identifier.fromNamespaceAndPath("stevecount","entity/fiishy")),null,null,PlayerModelType.WIDE,true);
    private static final PlayerSkin ASTRONAUT=new PlayerSkin(new ClientAsset.ResourceTexture(Identifier.fromNamespaceAndPath("stevecount","entity/astronaut")),null,null,PlayerModelType.WIDE,true);
    private static PlayerSkin skin(String path,PlayerModelType type) {
        return new PlayerSkin(new ClientAsset.ResourceTexture(Identifier.withDefaultNamespace("entity/player/"+path)),null,null,type,true);
    }
    static void recall() {trail.reset();}
    static void tick() {
        var mc=Minecraft.getInstance();var c=SteveCount.settings;
        if(mc.level!=level) {trail.reset();level=mc.level;BuddyEmotes.stop();BuddyPersonality.rest();}
        if(mc.player==null || level==null || !c.showCharacter() || !mc.player.isAlive()) {trail.reset();BuddyEmotes.stop();return;}
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
        boolean astronaut=SteveCount.settings.characterSkin.equals("Astronaut");
        double width=(astronaut?1.05:.6)*scale,height=(astronaut?2.3:1.8)*scale;
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
        long now=System.currentTimeMillis();
        Vec3 pos=trail.interpolated(partial);
        float punch=(now-GiftPunch.animationStart)/600f;
        boolean punching=c.animations && punch>=0 && punch<1;
        boolean riding=c.characterHeadRide && !punching;
        if(riding && mc.options.getCameraType().isFirstPerson())return;
        float size=riding?c.headRideScale:c.characterScale;
        String emote=punching?"None":BuddyEmotes.current(now);
        if(riding)pos=mc.player.getPosition(partial);
        if(emote.equals("Cheer") && !riding)pos=pos.add(0,Math.abs(Math.sin(BuddyEmotes.elapsed(now)*7))*.2,0);
        if(punching)pos=pos.lerp(GiftPunch.strikePosition(mc.player.position(),mc.player.getYRot()),Math.sin(punch*Math.PI));
        BuddyRenderState state=new BuddyRenderState();
        state.emote=emote;state.emoteTime=BuddyEmotes.elapsed(now);state.headRide=riding;
        state.playerEmoteFrame=PlayerEmotesBridge.frame(emote,state.emoteTime);
        state.astronaut=c.characterSkin.equals("Astronaut");
        state.isSpectator=state.astronaut; // Suppress vanilla clothing/equipment layers for the custom model.
        state.entityType=EntityTypes.PLAYER;
        state.x=pos.x;state.y=pos.y;state.z=pos.z;
        state.scale=size;
        state.boundingBoxWidth=(state.astronaut?1.05f:.6f)*size;
        state.boundingBoxHeight=(state.astronaut?2.3f:1.8f)*size;
        state.eyeHeight=1.62f*size;
        state.ageInTicks=c.animations?mc.player.tickCount+partial:0;
        state.bodyRot=trail.previousYaw+FollowTrail.wrap(trail.yaw-trail.previousYaw)*partial;
        if(riding)state.bodyRot=mc.player.getYRot();
        if(emote.equals("Dance"))state.bodyRot+=(float)Math.sin(state.emoteTime*5)*30;
        Vec3 toward=mc.player.getEyePosition().subtract(pos);
        float look=(float)Math.toDegrees(Math.atan2(-toward.x,toward.z));
        state.yRot=riding?0:Math.clamp(FollowTrail.wrap(look-state.bodyRot),-55,55);
        state.walkAnimationPos=trail.walk;
        state.walkAnimationSpeed=c.animations && !riding?trail.speed:0;
        if(punching) {
            state.swingAnimation=punch;
            state.currentSwing=new net.minecraft.world.entity.LivingEntity.SwingDescription(net.minecraft.world.InteractionHand.MAIN_HAND,net.minecraft.world.item.component.SwingAnimation.DEFAULT,6);
        }
        state.skin=switch(c.characterSkin) {case "Alex"->ALEX;case "Player"->mc.player.getSkin();case "Fiishy"->FIISHY;case "Astronaut"->ASTRONAUT;default->STEVE;};
        state.showCape=false;
        state.distanceToCameraSq=world.cameraRenderState.pos.distanceToSqr(pos);
        var lightPos=BlockPos.containing(pos.add(0,.7,0));
        state.lightCoords=LightCoordsUtil.pack(level.getBrightness(LightLayer.BLOCK,lightPos),level.getBrightness(LightLayer.SKY,lightPos));
        if(c.showCountLabel() && !riding) {
            var snapshot=SteveCount.current();
            boolean stale=snapshot.stale() || !SteveCount.demo && System.currentTimeMillis()/1000d-snapshot.updated()>60;
            int color=SteveCount.demo?0x7FFFD4:stale?0xFFC267:0xFFFFFF;
            state.nameTag=Component.literal(label(snapshot)).withColor(color);
            // Vanilla's name tag submitter adds another 0.5 blocks.
            state.nameTagAttachment=new Vec3(0,state.boundingBoxHeight-.30,0);
        }
        if(riding) {
            for(var owner:world.entityRenderStates)
                if(owner instanceof AvatarRenderState avatar && !(owner instanceof BuddyRenderState) && avatar.id==mc.player.getId()) {
                    ((HeadPassenger)avatar).stevecount$setPassenger(state);break;
                }
        } else world.entityRenderStates.add(state);
        if(Boolean.getBoolean("stevecount.followerSmoke")) {extractedFrames++;lastSmokeState=state;}
    }
}
