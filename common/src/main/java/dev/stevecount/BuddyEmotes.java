package dev.stevecount;

/** Short cosmetic emotes. Head riding is a separate, persistent appearance mode. */
public final class BuddyEmotes {
    private static String active="None";
    private static long started;
    private static boolean automatic;
    static String current(long now) {
        if(!SteveCount.settings.animations || now-started>=duration(active))return "None";
        return active;
    }
    static int duration(String name) {return switch(name) {case "Wave","Cheer"->4000;case "Dance"->6000;default->0;};}
    static void play(String name) {
        if(duration(name)==0) {stop();return;}
        SteveCount.settings.animations=true;
        if(!SteveCount.settings.showCharacter())SteveCount.settings.displayMode="Both";
        active=name;started=System.currentTimeMillis();
        automatic=false;
    }
    static void playAutomatically(String name,long now) {if(duration(name)>0){active=name;started=now;automatic=true;}}
    static void stopAutomatic() {if(automatic)stop();}
    static float elapsed(long now) {return (now-started)/1000f;}
    static void stop() {active="None";automatic=false;}
}
