package dev.stevecount;

import java.util.Random;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;

final class BuddyPersonality {
    private static final Random random=new Random();
    static final BuddySchedule schedule=new BuddySchedule(random);
    static int randomEmotesPlayed,noisesPlayed;
    static void tick() {
        var mc=Minecraft.getInstance();var c=SteveCount.settings;long now=System.currentTimeMillis();
        boolean eligible=mc.player!=null && mc.level!=null && mc.player.isAlive() && c.showCharacter()
            && !mc.isPaused() && !mc.gui.hud.isHidden();
        if(!eligible || !c.randomEmotes)BuddyEmotes.stopAutomatic();
        boolean busy=!BuddyEmotes.current(now).equals("None") || now-GiftPunch.animationStart<900;
        int actions=schedule.tick(now,eligible,c.randomEmotes,c.buddyNoises && c.buddyVolume>0,c.animations,busy,c.buddyActivity.equals("Playful"));
        if((actions&1)!=0) {BuddyEmotes.playAutomatically(new String[]{"Wave","Dance","Cheer"}[random.nextInt(3)],now);randomEmotesPlayed++;}
        if((actions&2)!=0)previewNoise();
    }
    static void rest() {schedule.reset();BuddyEmotes.stopAutomatic();}
    static void previewNoise() {
        var mc=Minecraft.getInstance();var c=SteveCount.settings;
        if(mc.player==null || mc.level==null || c.buddyVolume<=0 || mc.isPaused())return;
        var position=c.characterHeadRide?mc.player.getEyePosition():FollowerCharacter.trail.position;
        if(position==null)position=mc.player.position();
        String name=new String[]{"chirp","boop","wobble"}[random.nextInt(3)];
        mc.getSoundManager().play(new SimpleSoundInstance(Identifier.fromNamespaceAndPath("stevecount","buddy."+name),
            SoundSource.NEUTRAL,c.buddyVolume,.9f+random.nextFloat()*.2f,RandomSource.create(),false,0,
            SoundInstance.Attenuation.LINEAR,position.x,position.y,position.z,false));
        noisesPlayed++;
    }
}
