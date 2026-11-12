package dev.stevecount;

import java.nio.file.*;

/** Runs without Minecraft, credentials, companion, or a network connection. */
public final class CoreChecks {
    static int checks;
    static void check(boolean ok,String message) {checks++;if(!ok)throw new AssertionError(message);}
    public static void main(String[] args) throws Exception {
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
            config.save();
            var saved=Settings.load();
            check(saved.username.equals("example") && saved.scale==2 && saved.x==.75f,"Actual Settings.save/load persistence");
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
        System.out.println("SteveCount: "+checks+" core assertions passed");
    }
}
