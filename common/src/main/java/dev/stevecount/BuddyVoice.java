package dev.stevecount;

import net.minecraft.client.Minecraft;

/** Cosmetic actions only. No path from microphone commands to combat. */
final class BuddyVoice {
    private static final VoiceCursor cursor=new VoiceCursor();
    private static Object world;
    static void tick() {
        var mc=Minecraft.getInstance();
        boolean sameWorld=world==mc.level;world=mc.level;
        String action=cursor.accept(SteveCount.bridge.voice,System.currentTimeMillis()/1000d,
            sameWorld && SteveCount.settings.voiceControl && mc.level!=null && mc.player!=null && mc.player.isAlive() && !mc.isPaused());
        if(action.isEmpty())return;
        var c=SteveCount.settings;
        switch(action) {
            case "head_on" -> {BuddyEmotes.stop();c.characterHeadRide=true;if(!c.showCharacter())c.displayMode="Both";}
            case "head_off" -> {c.characterHeadRide=false;BuddyEmotes.stop();FollowerCharacter.recall();}
            case "Dance","Wave","Feeling" -> {PlayerEmotesBridge.voice(action);BuddyEmotes.limitVoice();}
            case "Cheer" -> {BuddyEmotes.play(action);BuddyEmotes.limitVoice();}
            case "stop" -> {BuddyEmotes.stop();BuddyPersonality.rest();}
            case "recall" -> {c.characterHeadRide=false;FollowerCharacter.recall();}
            default -> {return;}
        }
        c.save();
    }
}
