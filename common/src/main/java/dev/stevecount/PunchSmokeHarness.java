package dev.stevecount;

import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.network.chat.Component;

/** Runs ONLY behind the explicit development flag, in FollowerSmokeHarness's fresh QA world. */
final class PunchSmokeHarness {
    static void check(boolean ok,String description) {if(!ok)throw new AssertionError("Punch QA: "+description);}
    static void tick(int tick) {
        var mc=Minecraft.getInstance();var server=mc.getSingleplayerServer();var id=mc.player.getUUID();
        if(tick==550) {
            mc.options.setCameraType(net.minecraft.client.CameraType.THIRD_PERSON_BACK);
            SteveCount.demo=false;SteveCount.settings.giftPunch=true;SteveCount.settings.displayMode="Both";
            SteveCount.bridge.snapshot=new Bridge.Snapshot("Live",128L,128,System.currentTimeMillis()/1000d,false,"QA synthetic gift","qa","qa");
            SteveCount.bridge.gifts=com.google.gson.JsonParser.parseString("{\"session\":\"qa\",\"sequence\":0,\"events\":[]}").getAsJsonObject();
            server.execute(()->{server.setDifficulty(net.minecraft.world.Difficulty.NORMAL,true);var p=server.getPlayerList().getPlayer(id);p.setGameMode(GameType.SURVIVAL);p.setHealth(20);p.getFoodData().setFoodLevel(10);});
        }
        if(tick==570)mc.gui.setScreen(new net.minecraft.client.gui.screens.inventory.InventoryScreen(mc.player));
        if(tick==590)gift(1);
        if(tick==594) {
            net.minecraft.client.Screenshot.grab(mc.gameDirectory,"stevecount-gift-punch.png",mc.gameRenderer.mainRenderTarget(),1,msg->LogUtils.getLogger().info("Punch QA screenshot: {}",msg.getString()));
        }
        if(tick==610)mc.gui.setScreen(null);
        if(tick==610)server.execute(()->{
            var p=server.getPlayerList().getPlayer(id);
            check(p.getHealth()==19,"one unarmored fist punch must remove exactly 1 HP (half a heart); got "+p.getHealth());
            GiftPunch.attack(p,p.position().add(0,0,1));
            float health=p.getHealth();GiftPunch.attack(p,p.position().add(0,0,1));
            check(p.getHealth()==health,"Vanilla immunity rejects an immediate second hit");
            p.setGameMode(GameType.CREATIVE);p.setHealth(20);GiftPunch.attack(p,p.position().add(0,0,1));
            check(p.getHealth()==20,"Creative immunity");p.setGameMode(GameType.SURVIVAL);
            p.setItemSlot(EquipmentSlot.CHEST,new ItemStack(Items.DIAMOND_CHESTPLATE));
        });
        if(tick==650)server.execute(()->{
            var p=server.getPlayerList().getPlayer(id);p.setHealth(20);
            check(p.getArmorValue()>0,"Armor applied");GiftPunch.attack(p,p.position().add(0,0,1));
            check(p.getHealth()>19 && p.getHealth()<20,"Armor reduces ordinary fist damage; got "+p.getHealth());
            p.setItemSlot(EquipmentSlot.CHEST,ItemStack.EMPTY);
        });
        if(tick==670)server.execute(()->server.getPlayerList().getPlayer(id).setHealth(20));
        if(tick==690)mc.gui.setScreen(new net.minecraft.client.gui.screens.PauseScreen(true));
        if(tick==710) {check(mc.isPaused(),"Real pause menu pauses the game");gift(2);}
        if(tick==730) {check(GiftPunch.pending.waiting(),"Gift retained in pause menu");check(mc.player.getHealth()==20,"No damage forced while truly paused");}
        if(tick==750)mc.gui.setScreen(null);
        if(tick==775)server.execute(()->check(server.getPlayerList().getPlayer(id).getHealth()==19,"One pending gift hits after resume"));
        if(tick==800) {
            SteveCount.settings.characterSkin="Fiishy";SteveCount.settings.giftPunch=false;
            mc.options.setCameraType(net.minecraft.client.CameraType.FIRST_PERSON);
            var d=FollowerCharacter.trail.position.add(0,.8,0).subtract(mc.player.getEyePosition());
            mc.player.setYRot((float)Math.toDegrees(Math.atan2(-d.x,d.z)));
            mc.player.setXRot((float)-Math.toDegrees(Math.atan2(d.y,Math.sqrt(d.x*d.x+d.z*d.z))));
            BuddyEmotes.play("Wave");
        }
        if(tick==820) {check(FollowerCharacter.lastSmokeState.emote.equals("Wave"),"Wave render state");capture("fiishy-wave");}
        if(tick==840)BuddyEmotes.play("Dance");
        if(tick==860) {check(FollowerCharacter.lastSmokeState.emote.equals("Dance"),"Dance render state");capture("dance");}
        if(tick==880)BuddyEmotes.play("Cheer");
        if(tick==900) {check(FollowerCharacter.lastSmokeState.emote.equals("Cheer"),"Cheer render state");capture("cheer");}
        if(tick==920) {
            BuddyEmotes.stop();SteveCount.settings.characterHeadRide=true;
            mc.options.setCameraType(net.minecraft.client.CameraType.THIRD_PERSON_FRONT);mc.player.setXRot(0);
        }
        if(tick==940) {
            var state=FollowerCharacter.lastSmokeState;
            check(state.headRide && state.scale==.18f,"Tiny seated head rider");
            check(Math.abs(state.y-mc.player.getY()-mc.player.getBbHeight()+.12)<.05,"Rider tracks head height");capture("head-ride");
        }
        if(tick==960)SteveCount.settings.animations=false;
        if(tick==980) {check(FollowerCharacter.lastSmokeState.headRide,"Head ride works with animations off");SteveCount.settings.animations=true;SteveCount.settings.characterHeadRide=false;mc.gui.setScreen(new EmoteScreen(null));}
        if(tick==1000)capture("emote-menu");
        if(tick==1020)mc.gui.setScreen(null);
        if(tick==1040) {
            SteveCount.settings.characterSkin="Astronaut";mc.options.setCameraType(net.minecraft.client.CameraType.FIRST_PERSON);
            var d=FollowerCharacter.trail.position.add(0,1.1,0).subtract(mc.player.getEyePosition());
            mc.player.setYRot((float)Math.toDegrees(Math.atan2(-d.x,d.z)));
            mc.player.setXRot((float)-Math.toDegrees(Math.atan2(d.y,Math.sqrt(d.x*d.x+d.z*d.z))));
            BuddyEmotes.play("Wave");
        }
        if(tick==1070) {
            check(FollowerCharacter.lastSmokeState.astronaut && AstronautModel.compiledMeshes>10,"Custom rounded astronaut mesh actually rendered");capture("astronaut-wave");
        }
        if(tick==1075)GiftPunch.preview();
        if(tick==1080)capture("astronaut-punch-preview");
        if(tick==1090)BuddyEmotes.play("Dance");
        if(tick==1110)capture("astronaut-dance");
        if(tick==1130)BuddyEmotes.play("Cheer");
        if(tick==1150)capture("astronaut-cheer");
        if(tick==1170) {BuddyEmotes.stop();SteveCount.settings.characterHeadRide=true;mc.options.setCameraType(net.minecraft.client.CameraType.THIRD_PERSON_FRONT);mc.player.setXRot(0);}
        if(tick==1190) {check(FollowerCharacter.lastSmokeState.astronaut && FollowerCharacter.lastSmokeState.headRide,"Astronaut head ride");capture("astronaut-head-ride");}
        if(tick==1210) {
            for(String tone:new String[]{"chirp","boop","wobble"})check(mc.getSoundManager().getSoundEvent(net.minecraft.resources.Identifier.fromNamespaceAndPath("stevecount","buddy."+tone))!=null,"Sound event loaded: "+tone);
            SteveCount.settings.characterHeadRide=false;SteveCount.settings.randomEmotes=SteveCount.settings.buddyNoises=true;
            SteveCount.settings.buddyVolume=.2f;SteveCount.settings.buddyActivity="Calm";
            BuddyEmotes.stop();BuddyPersonality.schedule.nextEmote=BuddyPersonality.schedule.nextNoise=System.currentTimeMillis()-1;
        }
        if(tick==1220) {
            check(BuddyPersonality.randomEmotesPlayed==1 && BuddyPersonality.noisesPlayed==1,"Automatic emote and one-shot sound dispatched");
            check(!SteveCount.settings.giftPunch,"Random activity never enables gift damage");
            SteveCount.settings.randomEmotes=SteveCount.settings.buddyNoises=false;BuddyPersonality.rest();
        }
        if(tick==1240)mc.gui.setScreen(new PersonalityScreen(null));
        if(tick==1255)capture("personality-menu");
        if(tick==1270) {check(BuddyPersonality.randomEmotesPlayed==1 && BuddyPersonality.noisesPlayed==1,"Disabled random activity stays off");mc.gui.setScreen(null);}
        if(tick==1280) {
            SteveCount.settings.voiceControl=true;voice(0,"head_on");BuddyVoice.tick();
            voice(1,"head_on");BuddyVoice.tick();check(SteveCount.settings.characterHeadRide,"Voice head-on dispatch in game");
            voice(2,"Dance");BuddyVoice.tick();check(BuddyEmotes.current(System.currentTimeMillis()).equals("Dance"),"Voice dance dispatch in game");
            voice(3,"head_off");BuddyVoice.tick();check(!SteveCount.settings.characterHeadRide,"Voice head-off dispatch in game");
            voice(4,"stop");BuddyVoice.tick();check(BuddyEmotes.current(System.currentTimeMillis()).equals("None"),"Voice stop dispatch in game");
            SteveCount.settings.voiceControl=false;voice(5,"head_on");BuddyVoice.tick();
            SteveCount.settings.voiceControl=true;BuddyVoice.tick();check(!SteveCount.settings.characterHeadRide,"Disabled voice not replayed when enabled");
            SteveCount.settings.voiceControl=false;SteveCount.bridge.voice=new com.google.gson.JsonObject();
            LogUtils.getLogger().info("SteveCount voice world QA PASSED: synthetic action feed, head on/off, dance, stop, disabled replay rejection. No microphone capture.");
        }
        if(tick==1310)server.execute(()->{
            var p=server.getPlayerList().getPlayer(id);p.setHealth(.5f);
            GiftPunch.attack(p,p.position().add(0,0,1));check(!p.isAlive(),"No artificial half-heart survival floor");
            LogUtils.getLogger().info("SteveCount astronaut world QA PASSED: rounded mesh rendering, emotes, head ride, random activity/sound dispatch and disabling, inventory/pause gift regression, vanilla combat. Synthetic events only.");
            mc.execute(mc::stop);
        });
        if(tick>1400)throw new AssertionError("Punch QA timed out");
    }
    private static void gift(int sequence) {
        var feed=new com.google.gson.JsonObject();feed.addProperty("session","qa");feed.addProperty("sequence",sequence);
        var events=new com.google.gson.JsonArray();var event=new com.google.gson.JsonObject();event.addProperty("sequence",sequence);event.addProperty("created",System.currentTimeMillis()/1000d);events.add(event);feed.add("events",events);
        SteveCount.bridge.gifts=feed;
    }
    private static void voice(int sequence,String command) {
        var feed=new com.google.gson.JsonObject();feed.addProperty("session","voice-qa");feed.addProperty("sequence",sequence);
        var events=new com.google.gson.JsonArray();var e=new com.google.gson.JsonObject();e.addProperty("sequence",sequence);e.addProperty("created",System.currentTimeMillis()/1000d);e.addProperty("command",command);events.add(e);feed.add("events",events);
        SteveCount.bridge.voice=feed;
    }
    private static void capture(String name) {
        var mc=Minecraft.getInstance();net.minecraft.client.Screenshot.grab(mc.gameDirectory,"stevecount-"+name+".png",mc.gameRenderer.mainRenderTarget(),1,msg->LogUtils.getLogger().info("Emote QA: {}",msg.getString()));
    }
}

