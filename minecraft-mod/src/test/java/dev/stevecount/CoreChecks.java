package dev.stevecount;

import java.nio.file.*;

/** Runs without Minecraft, credentials, companion, or a network connection. */
public final class CoreChecks {
    static int checks;
    static void check(boolean ok,String message) {checks++;if(!ok)throw new AssertionError(message);}
    public static void main(String[] args) throws Exception {
        var cursor=new GiftCursor();
        var feed=com.google.gson.JsonParser.parseString("{\"session\":\"a\",\"sequence\":1,\"events\":[{\"sequence\":1,\"created\":100}]}").getAsJsonObject();
        check(!cursor.accept(feed,100,true),"First feed cannot replay history");
        feed.addProperty("sequence",2);feed.getAsJsonArray("events").get(0).getAsJsonObject().addProperty("sequence",2);
        check(cursor.accept(feed,101,true),"Fresh gift triggers");
        check(!cursor.accept(feed,107,true),"Duplicate gift rejected");
        feed.addProperty("sequence",3);feed.getAsJsonArray("events").get(0).getAsJsonObject().addProperty("sequence",3);
        check(!cursor.accept(feed,102,true),"Cooldown skips gift");
        check(!cursor.accept(feed,108,true),"Cooldown gift never queued");
        feed.addProperty("session","b");check(!cursor.accept(feed,109,true),"Companion restart baseline");
        feed.addProperty("sequence",4);check(!cursor.accept(feed,110,false),"Disabled discards gifts");
        check(!cursor.accept(feed,111,true),"Re-enabling cannot replay");
        feed.addProperty("sequence",5);feed.getAsJsonArray("events").get(0).getAsJsonObject().addProperty("sequence",5);
        check(!cursor.accept(feed,120,true),"Expired gift discarded");
        check(!cursor.accept(new com.google.gson.JsonObject(),120,true),"Malformed gift feed harmless");
        for(int screen:new int[]{320,427,640,854,1920,3840}) {
            for(int extent:new int[]{26,100,300,900}) {
                for(float anchor:new float[]{0,.5f,1}) {
                    int pos=HudLayout.coordinate(anchor,8,screen,extent);
                    check(pos>=0 && pos<=Math.max(0,screen-extent),"HUD bounds");
                }
            }
        }
        Settings config=new Settings();config.username="example";config.scale=2;config.x=.75f;config.preset="Expanded";
        Path file=Files.createTempFile("stevecount-config-test",".json");
        try {
            Files.writeString(file,Settings.JSON.toJson(config));
            var loaded=Settings.JSON.fromJson(Files.readString(file),Settings.class);loaded.validate();
            check(loaded.username.equals("example") && loaded.scale==2 && loaded.x==.75f && loaded.preset.equals("Expanded"),"Config persistence");
            check(!Files.readString(file).contains("demoCount"),"Demo statistics must not persist");
        } finally {Files.deleteIfExists(file);}
        Path directory=Files.createTempDirectory("stevecount-settings-test");
        var originalDirectory=Settings.configDirectory;
        try {
            Settings.configDirectory=directory;
            config.displayMode="Both";config.characterSkin="Alex";config.characterScale=.4f;config.followDistance=4;config.giftPunch=true;
            config.save();
            var saved=Settings.load();
            check(saved.giftPunch,"Gift damage opt-in persists");
            check(!new Settings().giftPunch,"Gift damage defaults off");
            check(saved.username.equals("example") && saved.scale==2 && saved.x==.75f,"Actual Settings.save/load persistence");
            check(saved.displayMode.equals("Both") && saved.characterSkin.equals("Alex") && saved.characterScale==.4f && saved.followDistance==4,"Follower preferences persist");
            Files.writeString(directory.resolve("stevecount.json"),"malformed JSON");
            check(Settings.load().preset.equals("Compact"),"Corrupt settings recover to defaults");
        } finally {
            Settings.configDirectory=originalDirectory;
            Files.deleteIfExists(directory.resolve("stevecount.json"));
            Files.deleteIfExists(directory);
        }
        config.scale=Float.NaN;config.opacity=99;config.x=-1;config.preset="invalid";config.icon="invalid";config.validate();
        check(config.scale==1 && config.opacity==1 && config.x==0 && config.preset.equals("Compact") && config.icon.equals("Steve"),"Config validation");
        var unknown=Bridge.Snapshot.empty("Disconnected","");
        check(unknown.viewers()==null,"Unavailable is not zero");
        var zero=new Bridge.Snapshot("Live",0L,0,100,false,"","x","1");
        check(zero.viewers()==0 && !zero.stale(),"Real zero preserved");
        SteveCount.settings=new Settings();
        check(SteveCount.format(1250).equals("1,250"),"Full numeric format");
        SteveCount.settings.abbreviated=true;
        check(SteveCount.format(12500).equals("12.5K") && SteveCount.format(1200000).equals("1.2M"),"Abbreviated numeric format");
        SteveCount.demo=true;SteveCount.changeDemo(0);
        check(SteveCount.current().status().equals("Demo Mode") && SteveCount.current().viewers()==0,"Offline demo source");
        SteveCount.demoStatus="Reconnecting";
        check(SteveCount.current().stale(),"Demo connection transition");
        Settings modes=new Settings();
        check(modes.showHud() && !modes.showCharacter(),"Old settings keep HUD default");
        modes.displayMode="Character";check(!modes.showHud() && modes.showCharacter(),"Character-only mode");
        modes.displayMode="Both";check(modes.showHud() && modes.showCharacter(),"Both display modes");
        modes.resetHudAppearance();check(modes.showCharacter(),"Reset HUD preserves character display mode");
        modes.characterScale=Float.NaN;modes.followDistance=999;modes.characterSkin="invalid";modes.validate();
        check(modes.characterScale==.55f && modes.followDistance==6 && modes.characterSkin.equals("Steve"),"Character input validation");
        var route=new FollowTrail();
        var origin=new net.minecraft.world.phys.Vec3(0,64,0);
        var first=route.target(origin,0,2.5);
        check(first.distanceTo(new net.minecraft.world.phys.Vec3(0,64,-2.5))<.001,"Initial follow offset");
        check(route.target(origin,180,2.5).distanceTo(first)<.001,"Turning camera does not orbit idle companion");
        route.target(new net.minecraft.world.phys.Vec3(0,64,2),0,2.5);
        var corner=route.target(new net.minecraft.world.phys.Vec3(2,64,2),-90,2.5);
        check(corner.distanceTo(new net.minecraft.world.phys.Vec3(0,64,1.5))<.001,"Follower tracks route around corner");
        route.propose(origin,first);route.accept(first);
        var step=route.propose(origin,origin);
        check(step.distanceTo(first)<=.241,"Bounded movement speed");
        route.target(new net.minecraft.world.phys.Vec3(100,80,100),0,2.5);
        check(route.points.size()==1 && route.position==null,"Teleport clears previous route");
        for(int i=0;i<600;i++)route.target(new net.minecraft.world.phys.Vec3(100+i,80,100),0,2.5);
        check(route.points.size()<=256,"Route memory is bounded");
        check(FollowTrail.wrap(-358)==2,"Rotation uses shortest direction");
        SteveCount.demo=false;
        check(FollowerCharacter.label(unknown).contains("unavailable") && !FollowerCharacter.label(unknown).contains("0 viewers"),"Character unknown count stays unavailable");
        var actualZero=new Bridge.Snapshot("Live",0L,0,System.currentTimeMillis()/1000d,false,"","x","1");
        check(FollowerCharacter.label(actualZero).contains("0 viewers"),"Character genuine zero");
        var stale=new Bridge.Snapshot("Reconnecting",128L,128,1,true,"","x","1");
        check(FollowerCharacter.label(stale).contains("128 viewers ~"),"Character stale label");
        SteveCount.demo=true;
        check(FollowerCharacter.label(stale).startsWith("DEMO Reconnecting"),"Character demo is explicit");
        System.out.println("SteveCount: "+checks+" core assertions passed");
    }
}
