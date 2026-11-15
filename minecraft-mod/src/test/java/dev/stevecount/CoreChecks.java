package dev.stevecount;

import java.nio.file.*;

/** Runs without Minecraft, credentials, companion, or a network connection. */
public final class CoreChecks {
    static int checks;
    static void check(boolean ok,String message) {checks++;if(!ok)throw new AssertionError(message);}
    public static void main(String[] args) throws Exception {
        var schedule=new BuddySchedule(new java.util.Random(42));
        check(schedule.tick(1000,true,true,true,true,false,false)==0,"Random activity starts with a delay");
        check(schedule.nextEmote>=36000 && schedule.nextEmote<=76000 && schedule.nextNoise>=51000 && schedule.nextNoise<=101000,"Calm random intervals bounded");
        long emoteDue=schedule.nextEmote;schedule.nextNoise=Long.MAX_VALUE;
        check(schedule.tick(emoteDue,true,true,true,true,false,false)==1,"Random emote becomes due");
        check(schedule.tick(emoteDue+1,true,true,true,true,false,false)==0,"No per-frame emote repetition");
        schedule.nextEmote=emoteDue;schedule.nextNoise=emoteDue;
        check(schedule.tick(emoteDue+2,true,true,true,true,true,false)==0,"Manual emotes and punches are never interrupted");
        check(schedule.tick(emoteDue+3,false,true,true,true,false,false)==0 && schedule.nextEmote==0 && schedule.nextNoise==0,"Pause and hidden buddy reset random timers");
        check(schedule.tick(200000,true,false,false,true,false,false)==0 && schedule.nextEmote==0 && schedule.nextNoise==0,"Disabled activity stays off");
        schedule.tick(200001,true,true,true,false,false,false);
        check(schedule.nextEmote==0 && schedule.nextNoise>200001,"Animations off does not enable random emotes");
        schedule.tick(200002,true,true,true,true,false,true);
        check(schedule.nextEmote>=220002 && schedule.nextEmote<=245002,"Playful mode uses bounded faster interval");
        var rounded=new AstronautModel.RoundMesh(0,0,0,2,3,4,0xFFFFFFFF,16,10);
        var invalidPersonality=new Settings();invalidPersonality.buddyVolume=Float.NaN;invalidPersonality.buddyActivity="invalid";invalidPersonality.validate();
        check(invalidPersonality.buddyVolume==.2f && invalidPersonality.buddyActivity.equals("Calm"),"Invalid personality values recover safely");
        boolean normals=true,curved=false;
        for(int i=0;i<rounded.vertices.length;i+=6) {
            float x=rounded.vertices[i],y=rounded.vertices[i+1],z=rounded.vertices[i+2];
            float nx=rounded.vertices[i+3],ny=rounded.vertices[i+4],nz=rounded.vertices[i+5];
            normals &= Float.isFinite(x+y+z+nx+ny+nz) && Math.abs(nx*nx+ny*ny+nz*nz-1)<.001;
            if(Math.abs(x)>.02 && Math.abs(y)>.02 && Math.abs(z)>.02)curved=true;
        }
        check(rounded.vertices.length==16*10*4*6 && normals && curved,"Rounded mesh has finite smooth normals and curved surfaces");
        for(String tone:new String[]{"chirp","boop","wobble"})try(var sound=CoreChecks.class.getResourceAsStream("/assets/stevecount/sounds/buddy/"+tone+".ogg")) {
            check(sound!=null && java.util.Arrays.equals(sound.readNBytes(4),new byte[]{'O','g','g','S'}),"Bundled Ogg sound: "+tone);
        }
        var pending=new PendingPunch();
        check(pending.update(10000,true,true,false),"Unpaused menus accept punches immediately");
        check(!pending.update(11000,true,true,false),"Actual strikes respect cooldown");
        check(!pending.update(16000,true,true,true) && pending.waiting(),"Paused punch retained");
        check(!pending.update(22000,true,true,true),"Paused gifts coalesce, never stack");
        check(pending.update(23000,false,true,false),"Paused punch executes on resume");
        check(!pending.update(23001,false,true,false),"Only one resumed hit");
        check(!pending.update(29000,true,true,true),"Queue another paused gift");
        check(!pending.update(90000,false,true,false) && !pending.waiting(),"Paused gift expires after a minute");
        pending.update(100000,true,true,true);pending.update(100001,false,false,true);
        check(!pending.waiting(),"Disabling or disconnecting clears paused gift");
        check(BuddyEmotes.duration("Wave")==4000 && BuddyEmotes.duration("Dance")==6000 && BuddyEmotes.duration("Cheer")==4000,"Bounded emote durations");
        check(BuddyEmotes.duration("unknown")==0,"Unknown emote rejected");
        try(var skin=CoreChecks.class.getResourceAsStream("/assets/stevecount/textures/entity/fiishy.png")) {
            check(skin!=null,"Fiishy texture bundled");
            byte[] png=skin.readAllBytes();var header=java.nio.ByteBuffer.wrap(png);
            check(header.getInt(0)==0x89504E47 && header.getInt(16)==64 && header.getInt(20)==64,"Fiishy texture is a 64x64 PNG");
        }
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
            config.displayMode="Both";config.characterSkin="Fiishy";config.characterHeadRide=true;config.characterScale=.4f;config.followDistance=4;config.giftPunch=true;
            config.save();
            var saved=Settings.load();
            var personality=Settings.load();personality.characterSkin="Astronaut";personality.randomEmotes=false;personality.buddyNoises=false;personality.buddyVolume=.17f;personality.buddyActivity="Playful";personality.save();
            var reread=Settings.load();
            check(reread.characterSkin.equals("Astronaut") && !reread.randomEmotes && !reread.buddyNoises && reread.buddyVolume==.17f && reread.buddyActivity.equals("Playful"),"Astronaut and personality preferences persist");
            check(saved.giftPunch,"Gift damage opt-in persists");
            check(!new Settings().giftPunch,"Gift damage defaults off");
            check(saved.username.equals("example") && saved.scale==2 && saved.x==.75f,"Actual Settings.save/load persistence");
            check(saved.displayMode.equals("Both") && saved.characterSkin.equals("Fiishy") && saved.characterHeadRide && saved.characterScale==.4f && saved.followDistance==4,"Follower skin and head ride preferences persist");
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
