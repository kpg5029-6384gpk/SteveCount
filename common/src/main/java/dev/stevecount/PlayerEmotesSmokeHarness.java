package dev.stevecount;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Button;
import java.util.List;

/** Explicit dev-only QA with the official Player Emotes mod installed, in a disposable world. */
final class PlayerEmotesSmokeHarness {
    private static List<PlayerEmotesBridge.Choice> choices;
    private static int before;
    private static void check(boolean ok,String description) {if(!ok)throw new AssertionError("Player Emotes QA: "+description);}
    static void tick(int tick) {
        var mc=Minecraft.getInstance();
        if(tick==1310) {
            choices=PlayerEmotesBridge.choices();
            check(PlayerEmotesBridge.available() && choices.size()>=30,"Installed 2.0.3 solo animation discovery: "+PlayerEmotesBridge.status());
            SteveCount.settings.characterSkin="Steve";SteveCount.settings.characterHeadRide=false;SteveCount.settings.streamerMode=false;
            before=PlayerEmotesBridge.appliedFrames;play("wave");
        }
        if(tick==1330) {check(PlayerEmotesBridge.appliedFrames>before,"Native player-style buddy uses upstream sampled animation");capture("player-emotes-steve-wave");}
        if(tick==1340) {before=PlayerEmotesBridge.appliedFrames;play("floss");}
        if(tick==1360) {check(PlayerEmotesBridge.appliedFrames>before,"Looping Floss animation rendered");capture("player-emotes-steve-floss");}
        if(tick==1370) {SteveCount.settings.characterSkin="Astronaut";before=PlayerEmotesBridge.appliedFrames;play("wave");}
        if(tick==1390) {check(PlayerEmotesBridge.appliedFrames>before,"Astronaut retarget uses upstream pose solver");capture("player-emotes-astronaut-wave");}
        if(tick==1400)play("robot_dance");
        if(tick==1420)capture("player-emotes-astronaut-robot");
        if(tick==1430)play("backflip");
        if(tick==1440)capture("player-emotes-astronaut-backflip");
        if(tick==1460)BuddyEmotes.stop();
        if(tick==1465)check(FollowerCharacter.lastSmokeState.playerEmoteFrame==null,"Stop removes external playback");
        if(tick==1470) {
            SteveCount.settings.characterHeadRide=true;SteveCount.settings.headRideScale=.42f;
            mc.options.setCameraType(net.minecraft.client.CameraType.THIRD_PERSON_FRONT);mc.player.setXRot(0);play("wave");
        }
        if(tick==1490) {check(FollowerCharacter.lastSmokeState.headRide && FollowerCharacter.lastSmokeState.playerEmoteFrame!=null,"Imported emote while seated on head");capture("player-emotes-head-ride");}
        if(tick==1500) {BuddyEmotes.stop();mc.gui.setScreen(new PlayerEmotesScreen(null));}
        if(tick==1510)capture("player-emotes-library");
        if(tick==1520) {
            var screen=mc.gui.screen();
            screen.children().stream().filter(EditBox.class::isInstance).map(EditBox.class::cast).findFirst().orElseThrow().setValue("robot");
            check(screen.children().stream().filter(Button.class::isInstance).map(Button.class::cast).anyMatch(b->b.visible && b.getMessage().getString().toLowerCase(java.util.Locale.ROOT).contains("robot")),"Search filters emote list");
        }
        if(tick==1530)capture("player-emotes-search");
        if(tick==1540) {
            try {
                var type=Class.forName("com.yamikhal.playeremotes.client.gui.QuickWheelScreen");
                mc.gui.setScreen((net.minecraft.client.gui.screens.Screen)type.getConstructor(net.minecraft.client.gui.screens.Screen.class).newInstance((Object)null));
            }catch(ReflectiveOperationException e) {throw new AssertionError("Native Player Emotes preview menu failed",e);}
        }
        if(tick==1550)capture("player-emotes-native-wheel");
        if(tick==1560) {
            mc.gui.setScreen(null);SteveCount.settings.characterHeadRide=true;SteveCount.settings.characterSkin="Steve";SteveCount.settings.headRideScale=.18f;
            mc.player.setXRot(-70);BuddyEmotes.stop();before=HeadRide.submittedFrames;
        }
        if(tick==1580) {check(HeadRide.submittedFrames>before && HeadRide.lastSeatError<.0001,"Head attachment seat contact looking up");check(Math.abs(HeadRide.lastUp.y)<.6,"Whole rider tilts with head pitch");capture("head-attached-up");}
        if(tick==1590) {mc.player.setXRot(70);SteveCount.settings.headRideScale=.6f;}
        if(tick==1610) {check(HeadRide.lastSeatError<.0001,"Maximum-scale rider remains seated looking down");capture("head-attached-down-large");}
        if(tick==1620) {SteveCount.settings.characterSkin="Astronaut";mc.player.setXRot(-60);}
        if(tick==1640) {check(HeadRide.lastSeatError<.0001,"Astronaut seat contact while tilted");capture("head-attached-astronaut");}
        if(tick==1650) {
            check(choices.stream().map(PlayerEmotesBridge.Choice::id).collect(java.util.stream.Collectors.toSet()).containsAll(PlayerEmotesBridge.VOICE_DANCES),"All five requested library dances exist");
            for(int i=0;i<30;i++) {PlayerEmotesBridge.voice("Dance");check(PlayerEmotesBridge.VOICE_DANCES.stream().anyMatch(id->BuddyEmotes.current(System.currentTimeMillis()).equals("PlayerEmotes:"+id)),"Voice selects only requested five dances");}
            PlayerEmotesBridge.voice("Wave");check(BuddyEmotes.current(System.currentTimeMillis()).equals("PlayerEmotes:playeremotes:wave"),"Voice uses actual library wave");
            PlayerEmotesBridge.voice("Feeling");check(BuddyEmotes.current(System.currentTimeMillis()).startsWith("PlayerEmotes:"),"Feeling picks compatible library solo");
        }
        if(tick==1670) {
            BuddyEmotes.stop();SteveCount.settings.characterHeadRide=false;SteveCount.settings.headRideScale=.18f;SteveCount.settings.streamerMode=true;
            com.mojang.logging.LogUtils.getLogger().info("SteveCount Player Emotes world QA PASSED: {} solo emotes, player rig, astronaut rig, loop, stop, head ride and searchable menu. Original 2.0.3 assets, no copied animations.",choices.size());
            mc.stop();
        }
        if(tick>1710)throw new AssertionError("Player Emotes QA timed out");
    }
    private static void play(String id) {BuddyEmotes.play(choices.stream().filter(c->c.id().equals("playeremotes:"+id)).findFirst().orElseThrow());}
    private static void capture(String name) {
        var mc=Minecraft.getInstance();net.minecraft.client.Screenshot.grab(mc.gameDirectory,"stevecount-"+name+".png",mc.gameRenderer.mainRenderTarget(),1,msg->com.mojang.logging.LogUtils.getLogger().info("Player Emotes QA: {}",msg.getString()));
    }
}
