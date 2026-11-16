package dev.stevecount;

import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.AABB;

/** Explicit dev flag only; runs in FollowerSmokeHarness's fresh disposable world. */
final class WanderSmokeHarness {
    private static double traveled,min=99,max;
    private static int goals;
    private static Vec3 previous,stationary,paused;
    private static void check(boolean ok,String message) {if(!ok)throw new AssertionError("Wander QA: "+message);}
    static void tick(int tick) {
        var mc=Minecraft.getInstance();var c=SteveCount.settings;
        if(tick==550) {
            mc.gui.setScreen(null);c.displayMode="Both";c.streamerMode=false;c.characterHeadRide=false;c.characterWander=true;c.characterGrounded=true;
            c.followDistance=3;c.characterScale=.55f;c.characterSkin="Steve";c.randomEmotes=c.buddyNoises=false;BuddyEmotes.stop();FollowerCharacter.recall();
            mc.options.setCameraType(net.minecraft.client.CameraType.THIRD_PERSON_BACK);mc.player.setXRot(40);goals=FollowerCharacter.wander.destinations;
        }
        if(tick>=570 && tick<1050) {
            Vec3 position=FollowerCharacter.trail.position;
            check(position!=null,"Ground companion exists");
            double radius=position.subtract(mc.player.position()).horizontalDistance();min=Math.min(min,radius);max=Math.max(max,radius);
            check(radius<=3.01,"Idle radius respected");
            check(mc.level.noCollision(new AABB(position.x-.165,position.y+.02,position.z-.165,position.x+.165,position.y+.99,position.z+.165)),"Wandering body does not enter blocks");
            if(previous!=null)traveled+=previous.distanceTo(position);previous=position;
        }
        if(tick==1050) {
            check(traveled>1 && max-min>.2 && FollowerCharacter.wander.destinations-goals>=3,"Idle wander moves, rests and changes distance");
            capture("idle-wandering");BuddyEmotes.play("Dance");
        }
        if(tick==1055)stationary=FollowerCharacter.trail.position;
        if(tick==1070) {check(stationary.distanceTo(FollowerCharacter.trail.position)<.001,"Manual dance stays planted while player is stationary");BuddyEmotes.stop();}
        if(tick>=1080 && tick<1300) {
            if(tick%4==0) {
                var server=mc.getSingleplayerServer();var id=mc.player.getUUID();double z=.5+(tick-1080)*.12;
                server.execute(()->{var player=server.getPlayerList().getPlayer(id);if(player!=null) {
                    double y=server.overworld().getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING,50,(int)z);player.teleportTo(50.5,y,z);
                }});
            }
            if(tick>1090)check(FollowerCharacter.trail.position.subtract(mc.player.position()).horizontalDistance()<3.6,"Moving catch-up stays near player");
        }
        if(tick==1310) {c.characterHeadRide=true;mc.player.setXRot(-65);}
        if(tick==1330) {check(FollowerCharacter.lastSmokeState.headRide && HeadRide.lastSeatError<.0001,"Wandering yields to attached head riding");capture("wander-head-ride");c.characterHeadRide=false;FollowerCharacter.recall();}
        if(tick==1340) {mc.gui.setScreen(new net.minecraft.client.gui.screens.PauseScreen(true));}
        if(tick==1345)paused=FollowerCharacter.trail.position;
        if(tick==1360) {check(paused.distanceTo(FollowerCharacter.trail.position)<.001,"Wandering freezes during actual pause");mc.gui.setScreen(null);}
        if(tick==1370) {c.characterWander=false;FollowerCharacter.recall();}
        if(tick==1390) {check(FollowerCharacter.trail.position!=null,"Classic follow mode remains available");capture("classic-follow");}
        if(tick==1400) {
            c.characterWander=true;c.save();
            com.mojang.logging.LogUtils.getLogger().info("SteveCount wander world QA PASSED: idle travel {} blocks, radial range {} to {}, moving catch-up, collision checks, planted dance, head ride, true pause and classic follow.",traveled,min,max);
            mc.stop();
        }
    }
    private static void capture(String name) {var mc=Minecraft.getInstance();net.minecraft.client.Screenshot.grab(mc.gameDirectory,"stevecount-"+name+".png",mc.gameRenderer.mainRenderTarget(),1,msg->{});}
}
