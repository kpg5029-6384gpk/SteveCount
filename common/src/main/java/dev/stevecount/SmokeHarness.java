package dev.stevecount;

import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import com.mojang.logging.LogUtils;

/** Opt-in development render smoke test. Inactive in normal installations. */
final class SmokeHarness {
    static int ticks,phase;
    static final String[] names={"connection","appearance","testing","character","advanced","minimal","compact","streamer","expanded","player-emotes","player-emotes-previews"};
    static void tick() {
        var mc=Minecraft.getInstance();ticks++;
        if(ticks<100) return;
        int elapsed=ticks-100;
        if(elapsed%60==0 && phase<names.length) {
            SteveCount.demo=true;
            if(phase<5) {var screen=new SettingsScreen(null);screen.tab=phase;mc.gui.setScreen(screen);}
            else if(phase<9) {SteveCount.settings.preset=new String[]{"Minimal","Compact","Streamer","Expanded"}[phase-5];mc.gui.setScreen(new MoveScreen(null));}
            else if(phase==9 || !PlayerEmotesBridge.available())mc.gui.setScreen(new PlayerEmotesScreen(null));
            else {
                try {
                    var type=Class.forName("com.yamikhal.playeremotes.client.gui.EmoteListScreen");
                    mc.gui.setScreen((net.minecraft.client.gui.screens.Screen)type.getConstructor(net.minecraft.client.gui.screens.Screen.class).newInstance((Object)null));
                }catch(ReflectiveOperationException e) {throw new AssertionError("Native emote preview library failed",e);}
            }
        }
        if(elapsed%60==40 && phase<names.length) {
            Screenshot.grab(mc.gameDirectory,"stevecount-qa-"+names[phase]+".png",mc.gameRenderer.mainRenderTarget(),1,msg->LogUtils.getLogger().info("SteveCount smoke capture: {}",msg.getString()));
            phase++;
            if(phase==10)LogUtils.getLogger().info("SteveCount optional emote library: {}",PlayerEmotesBridge.status());
        }
        if(phase==names.length && elapsed%60==59) {LogUtils.getLogger().info("SteveCount render smoke completed");mc.stop();}
    }
}
