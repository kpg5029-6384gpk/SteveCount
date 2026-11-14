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
        if(tick==590) {
            var feed=new com.google.gson.JsonObject();feed.addProperty("session","qa");feed.addProperty("sequence",1);
            var events=new com.google.gson.JsonArray();var event=new com.google.gson.JsonObject();event.addProperty("sequence",1);event.addProperty("created",System.currentTimeMillis()/1000d);events.add(event);feed.add("events",events);
            SteveCount.bridge.gifts=feed;
        }
        if(tick==594) {
            net.minecraft.client.Screenshot.grab(mc.gameDirectory,"stevecount-gift-punch.png",mc.gameRenderer.mainRenderTarget(),1,msg->LogUtils.getLogger().info("Punch QA screenshot: {}",msg.getString()));
        }
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
        if(tick==690)server.execute(()->{
            var p=server.getPlayerList().getPlayer(id);p.setHealth(.5f);
            GiftPunch.attack(p,p.position().add(0,0,1));check(!p.isAlive(),"No artificial half-heart survival floor");
            LogUtils.getLogger().info("SteveCount gift punch world QA PASSED: feed -> cursor -> animation -> vanilla fist damage (1 HP), duplicate prevention, i-frames, armor, creative immunity, lethal damage. Synthetic events only.");
            mc.execute(mc::stop);
        });
        if(tick>750)throw new AssertionError("Punch QA timed out");
    }
}

