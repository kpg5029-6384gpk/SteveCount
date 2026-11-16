package dev.stevecount;

import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.entity.EquipmentSlot;

/** Synthetic gifts with the user-approved 8onfire costume PNG. */
final class CostumeSmokeHarness {
    private static int frames,handFrames;
    private static long before;
    private static void check(boolean ok,String text) {if(!ok)throw new AssertionError("Costume QA: "+text);}
    static void tick(int tick) {
        var mc=Minecraft.getInstance();var c=SteveCount.settings;
        if(tick==550) {
            mc.gui.setScreen(null);SteveCount.demo=false;c.streamerMode=true;c.maidGift=true;c.characterHeadRide=false;c.displayMode="Both";
            mc.options.setCameraType(net.minecraft.client.CameraType.THIRD_PERSON_FRONT);mc.player.setXRot(0);MaidCostume.restore();MaidCostume.reload();
            SteveCount.bridge.snapshot=new Bridge.Snapshot("Live",128L,128,System.currentTimeMillis()/1000d,false,"SYNTHETIC QA","QA", "test");
            feed(0,1000);frames=MaidCostume.renderedFrames;
            var server=mc.getSingleplayerServer();var id=mc.player.getUUID();
            server.execute(()->{var player=server.getPlayerList().getPlayer(id);player.gameMode.changeGameModeForPlayer(net.minecraft.world.level.GameType.SURVIVAL);player.setItemSlot(EquipmentSlot.CHEST,new ItemStack(Items.DIAMOND_CHESTPLATE));});
        }
        if(tick==555)feed(1,1000);
        if(tick==565) {
            check(MaidCostume.active(),"Synthetic 1000-coin gift activates self costume");check(MaidCostume.renderedFrames>frames,"Third-person overlay actually submitted");
            check(mc.player.getItemBySlot(EquipmentSlot.CHEST).is(Items.DIAMOND_CHESTPLATE),"Render hiding never removes real equipment");
            var other=new net.minecraft.client.renderer.entity.state.AvatarRenderState();other.id=-100;check(!MaidCostume.wears(other),"Other players excluded");
            check(!MaidCostume.wears(FollowerCharacter.lastSmokeState),"Little Buddy excluded");
            check(c.maidUntil-System.currentTimeMillis()>1_790_000,"Full thirty-minute deadline");
            before=c.maidUntil;capture("maid-costume");
        }
        if(tick==570) {MaidCostume.smokeModel=net.minecraft.world.entity.player.PlayerModelType.WIDE;}
        if(tick==573)capture("maid-wide");
        if(tick==575) {MaidCostume.smokeModel=net.minecraft.world.entity.player.PlayerModelType.SLIM;feed(2,5000);}
        if(tick==578)capture("maid-slim");
        if(tick==600) {mc.gui.setScreen(new net.minecraft.client.gui.screens.inventory.InventoryScreen(mc.player));}
        if(tick==615)capture("maid-inventory");
        if(tick==580) {check(c.maidUntil>before,"New qualifying gift resets thirty minutes");before=c.maidUntil;feed(3,999);}
        if(tick==585) {check(c.maidUntil==before,"Cheap gift cannot reset timer");mc.gui.setScreen(new CostumeScreen(null));}
        if(tick==590)capture("maid-controls");
        if(tick==620) {mc.gui.setScreen(null);mc.options.setCameraType(net.minecraft.client.CameraType.FIRST_PERSON);handFrames=MaidCostume.handFrames;}
        if(tick==635) {check(MaidCostume.handFrames>handFrames,"First-person hand overlay submitted");capture("maid-first-person");}
        if(tick==640) {c.maidUntil=System.currentTimeMillis()-1;}
        if(tick==645) {check(c.maidUntil==0 && !MaidCostume.active(),"Expiry restores original rendering");check(mc.player.getItemBySlot(EquipmentSlot.CHEST).is(Items.DIAMOND_CHESTPLATE),"Armor remains intact after expiry");}
        if(tick==650) {MaidCostume.start();c.maidGift=false;}
        if(tick==655)check(c.maidUntil==0 && !MaidCostume.active(),"Disable immediately restores outfit");
        if(tick==660) {
            c.maidGift=true;MaidCostume.smokeModel=null;MaidCostume.restore();SteveCount.bridge.snapshot=Bridge.Snapshot.empty("Disconnected","");SteveCount.bridge.costumes=new com.google.gson.JsonObject();
            com.mojang.logging.LogUtils.getLogger().info("SteveCount costume world QA PASSED: approved 8onfire PNG, synthetic 1000+/reset/999 events, self-only rendering, hands, armor protection preserved, expiry and disable. No paid gifts tested.");
            mc.stop();
        }
    }
    private static void feed(long seq,int coins) {
        var j=new com.google.gson.JsonObject();j.addProperty("session","costume-world-qa");j.addProperty("sequence",seq);var a=new com.google.gson.JsonArray();
        if(seq>0) {var e=new com.google.gson.JsonObject();e.addProperty("sequence",seq);e.addProperty("created",System.currentTimeMillis()/1000d);e.addProperty("unitCoins",coins);a.add(e);}
        j.add("events",a);SteveCount.bridge.costumes=j;
    }
    private static void capture(String name) {var mc=Minecraft.getInstance();net.minecraft.client.Screenshot.grab(mc.gameDirectory,"stevecount-"+name+".png",mc.gameRenderer.mainRenderTarget(),1,msg->{});}
}
