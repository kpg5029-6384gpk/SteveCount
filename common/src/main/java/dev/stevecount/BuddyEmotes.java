package dev.stevecount;

/** Short cosmetic emotes. Head riding is a separate, persistent appearance mode. */
public final class BuddyEmotes {
    private static String active="None";
    private static long started;
    private static boolean automatic, voiceTriggered;
    static String current(long now) {
        if(!SteveCount.settings.animations || now-started>=playbackDuration(active,voiceTriggered))return "None";
        return active;
    }
    static int playbackDuration(String name,boolean voice) {return voice?Math.min(5000,duration(name)):duration(name);}
    static void limitVoice() {voiceTriggered=true;}
    static int duration(String name) {return switch(name) {case "Wave","Cheer"->4000;case "Dance"->6000;default->PlayerEmotesBridge.duration(name);};}
    static void play(PlayerEmotesBridge.Choice choice) {
        PlayerEmotesBridge.select(choice);play("PlayerEmotes:"+choice.id());
    }
    static void play(String name) {
        if(duration(name)==0) {stop();return;}
        SteveCount.settings.animations=true;
        if(!SteveCount.settings.showCharacter())SteveCount.settings.displayMode="Both";
        active=name;started=System.currentTimeMillis();
        automatic=false;voiceTriggered=false;
    }
    static void playAutomatically(String name,long now) {if(duration(name)>0){active=name;started=now;automatic=true;voiceTriggered=false;}}
    static void stopAutomatic() {if(automatic)stop();}
    static float elapsed(long now) {return (now-started)/1000f;}
    static void stop() {active="None";automatic=false;voiceTriggered=false;PlayerEmotesBridge.stop();}
}
