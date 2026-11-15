package dev.stevecount;

import com.mojang.logging.LogUtils;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.WorldDataConfiguration;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.levelgen.presets.WorldPresets;

/** Explicit development-only flag creates a fresh QA world in the isolated run profile. */
final class FollowerSmokeHarness {
    static int ticks,worldTicks,disabledFrames;
    static boolean started;
    static void tick() {
        var mc=Minecraft.getInstance();ticks++;mc.options.pauseOnLostFocus=false;
        if(!started && ticks>=100) {
            started=true;
            var settings=new LevelSettings("SteveCount follower QA",GameType.CREATIVE,new LevelSettings.DifficultySettings(Difficulty.PEACEFUL,false,false),true,WorldDataConfiguration.DEFAULT);
            mc.createWorldOpenFlows().createFreshLevel("stevecount-follower-qa-"+System.currentTimeMillis(),settings,new WorldOptions(42L,false,false),WorldPresets::createTestWorldDimensions,mc.gui.screen());
            return;
        }
        if(mc.player==null || mc.level==null)return;
        worldTicks++;
        if(worldTicks==20)teleport(0.5,0.5);
        if(worldTicks==60) {
            SteveCount.settings.randomEmotes=SteveCount.settings.buddyNoises=false;BuddyPersonality.rest();
            mc.gui.setScreen(null);SteveCount.demo=true;SteveCount.demoStatus="Demo Mode";SteveCount.demoCount=128;
            SteveCount.settings.displayMode="Both";SteveCount.settings.characterSkin="Steve";
            mc.options.setCameraType(CameraType.FIRST_PERSON);mc.player.setYRot(0);mc.player.setXRot(20);FollowerCharacter.recall();
        }
        if(worldTicks>=100 && worldTicks<360) {mc.player.setYRot(180);mc.player.setXRot(20);}
        if(worldTicks==140) {
            if(FollowerCharacter.extractedFrames<10)throw new AssertionError("Follower never entered world render state; callbacks="+FollowerCharacter.extractionCallbacks+", position="+FollowerCharacter.trail.position+", paused="+mc.isPaused()+", hidden="+mc.gui.hud.isHidden());
            capture("steve");
        }
        if(worldTicks==160)SteveCount.settings.characterSkin="Alex";
        if(worldTicks==180)capture("alex");
        if(worldTicks==200) {SteveCount.settings.characterScale=.3f;SteveCount.demoCount=0;}
        if(worldTicks==220)capture("small-zero");
        if(worldTicks==240) {SteveCount.settings.characterScale=1;SteveCount.demoStatus="Reconnecting";}
        if(worldTicks==260)capture("large-stale");
        if(worldTicks==280) {SteveCount.settings.characterScale=.55f;SteveCount.demoStatus="Demo Mode";SteveCount.settings.displayMode="Character";}
        if(worldTicks==300)capture("character-only");
        if(worldTicks>=320 && worldTicks<380 && worldTicks%10==0)teleport((worldTicks-310)*.12,.5);
        if(worldTicks==400) {
            if(FollowerCharacter.trail.walk<=0)throw new AssertionError("Follower did not walk");
            teleport(50.5,.5);
        }
        if(worldTicks==480) {
            if(FollowerCharacter.trail.position==null || FollowerCharacter.trail.position.distanceTo(mc.player.position())>10)throw new AssertionError("Follower did not catch up after teleport");
            mc.player.setYRot(180);mc.player.setXRot(20);capture("teleport");
        }
        if(worldTicks==500) {SteveCount.settings.displayMode="HUD";disabledFrames=FollowerCharacter.extractedFrames;}
        if(worldTicks==540) {
            if(FollowerCharacter.extractedFrames!=disabledFrames)throw new AssertionError("Disabled follower still renders");
            LogUtils.getLogger().info("SteveCount follower world smoke passed: skins, sizes, labels, movement, teleport and disable; {} frames",FollowerCharacter.extractedFrames);
            if(!Boolean.getBoolean("stevecount.punchSmoke"))mc.stop();
        }
        if(Boolean.getBoolean("stevecount.playerEmotesSmoke") && worldTicks>=1310)PlayerEmotesSmokeHarness.tick(worldTicks);
        else if(Boolean.getBoolean("stevecount.punchSmoke") && worldTicks>=550)PunchSmokeHarness.tick(worldTicks);
    }
    private static void teleport(double x,double z) {
        var mc=Minecraft.getInstance();var server=mc.getSingleplayerServer();var id=mc.player.getUUID();
        server.execute(()->{var player=server.getPlayerList().getPlayer(id);if(player!=null){double y=server.overworld().getHeight(Heightmap.Types.MOTION_BLOCKING,(int)x,(int)z);player.teleportTo(x,y,z);}});
    }
    private static void capture(String name) {
        var mc=Minecraft.getInstance();
        Screenshot.grab(mc.gameDirectory,"stevecount-follower-"+name+".png",mc.gameRenderer.mainRenderTarget(),1,msg->LogUtils.getLogger().info("Follower QA: {}",msg.getString()));
    }
}
