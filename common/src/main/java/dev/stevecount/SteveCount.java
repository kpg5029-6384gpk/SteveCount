package dev.stevecount;

import java.text.NumberFormat;
import java.util.*;
import net.minecraft.client.*;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.sounds.SoundEvents;

public final class SteveCount {
    static Settings settings;
    static Bridge bridge;
    static boolean demo, automatic;
    static long demoCount=128, demoPeak=128;
    static String demoStatus="Demo Mode", message="";
    static long pulseUntil, toastUntil;
    static String milestoneText="", session="";
    static final Set<Long> reached = new HashSet<>();
    private static long lastDemo;
    static KeyMapping key;
    static KeyMapping emoteKey;

    static void initialize(java.nio.file.Path configDirectory) {
        Settings.configDirectory=configDirectory;
        settings=Settings.load(); bridge=new Bridge();
        key=new KeyMapping("key.stevecount.settings",75,KeyMapping.Category.MISC);
        emoteKey=new KeyMapping("key.stevecount.emotes",74,KeyMapping.Category.MISC);
        if(settings.autoConnect && !settings.username.isBlank()) bridge.control("connect",settings.username);
    }
    static void open() { var mc=Minecraft.getInstance();mc.gui.setScreen(new SettingsScreen(mc.gui.screen())); }
    static Bridge.Snapshot current() {
        if(demo) return new Bridge.Snapshot(demoStatus,demoCount,demoPeak,System.currentTimeMillis()/1000d,!demoStatus.equals("Demo Mode"),"DEMO — simulated data",settings.username,"demo");
        return bridge.snapshot;
    }
    static void demo(boolean enabled) {
        demo=enabled; automatic=false; reached.clear();session="";
        if(enabled) { bridge.control("disconnect","");demoStatus="Demo Mode";demoPeak=demoCount; }
    }
    static void changeDemo(long n) { demoCount=Math.max(0,n);demoPeak=Math.max(demoPeak,demoCount); }
    static void tick() {
        FollowerCharacter.tick();
        BuddyPersonality.tick();
        GiftPunch.tick();
        BuddyVoice.tick();
        if(Boolean.getBoolean("stevecount.followerSmoke")) FollowerSmokeHarness.tick();
        if(Boolean.getBoolean("stevecount.smoke")) SmokeHarness.tick();
        while(key.consumeClick()) open();
        while(emoteKey.consumeClick())Minecraft.getInstance().gui.setScreen(new EmoteScreen(null));
        long now=System.currentTimeMillis();
        if(demo && automatic && now-lastDemo>2000) {lastDemo=now;changeDemo(demoCount+new Random().nextInt(-8,13));}
        var s=current();
        String id=(demo?"demo:":"live:")+s.username()+":"+s.room();
        if(!id.equals(session)) {reached.clear();session=id;}
        if(s.viewers()==null || s.stale() || !(s.status().equals("Live") || demoStatus.equals(s.status()) && demo)) return;
        for(String part:settings.milestones.split(",")) {
            try {long n=Long.parseLong(part.trim());
                if(n>0 && s.viewers()>=n && reached.add(n)) {
                    milestoneText=format(n)+" viewers reached";
                    if(settings.animations) pulseUntil=now+1600;
                    if(settings.toast) toastUntil=now+3000;
                    if(settings.sound && Minecraft.getInstance().player!=null)
                        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.NOTE_BLOCK_PLING.value(),1f));
                }
            } catch(NumberFormatException ignored) { }
        }
    }
    static String format(long n) {
        if(settings.abbreviated && n>=1000) return String.format(Locale.ROOT,"%.1f%s", n/(n>=1000000?1000000d:1000d),n>=1000000?"M":"K");
        return NumberFormat.getIntegerInstance(Locale.US).format(n);
    }
}


