package dev.stevecount;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.network.chat.Component;

/** Shared buddy controls, with live previews in the running world. */
public final class BuddyOptionsScreen extends Screen {
    private final Screen parent;
    BuddyOptionsScreen(Screen parent) {super(Component.literal("Little Buddy options"));this.parent=parent;}
    private void button(String label,int y,Runnable action) {
        addRenderableWidget(Button.builder(Component.literal(label),b->action.run()).bounds((width-300)/2,y,300,20).build());
    }
    private void changed() {SteveCount.settings.save();rebuildWidgets();}
    @Override protected void init() {
        var c=SteveCount.settings;
        button("Mode: "+(c.streamerMode?"Streamer":"Regular"),40,()->{
            c.streamerMode=!c.streamerMode;
            if(!c.streamerMode && !c.showCharacter())c.displayMode="Both";
            if(!c.streamerMode) {SteveCount.demo(false);SteveCount.bridge.control("disconnect","");}
            else if(c.autoConnect && !c.username.isBlank())SteveCount.bridge.control("connect",c.username);
            SteveCount.toastUntil=SteveCount.pulseUntil=0;
            changed();
        });
        addRenderableWidget(new AbstractSliderButton((width-300)/2,64,300,20,Component.empty(),(c.headRideScale-.08)/.52) {
            {updateMessage();}
            protected void updateMessage() {setMessage(Component.literal("Head-riding size: "+Math.round((.08+value*.52)*100)+"%"));}
            protected void applyValue() {c.headRideScale=(float)(.08+value*.52);}
        });
        button("Head riding: "+(c.characterHeadRide?"On":"Off"),88,()->{
            c.characterHeadRide=!c.characterHeadRide;
            if(c.characterHeadRide && !c.showCharacter())c.displayMode="Both";
            changed();
        });
        button("Personality / sounds",112,()->minecraft.gui.setScreen(new PersonalityScreen(this)));
        button("Emotes / Voice commands",136,()->minecraft.gui.setScreen(new EmoteScreen(this)));
        button("Wandering: "+(c.characterWander?"On":"Off"),160,()->{c.characterWander=!c.characterWander;FollowerCharacter.recall();changed();});
        addRenderableWidget(Button.builder(Component.literal("Reset rider size"),b->{c.headRideScale=.18f;changed();}).bounds((width-300)/2,184,146,20).build());
        addRenderableWidget(Button.builder(Component.literal("Gift outfit"),b->minecraft.gui.setScreen(new CostumeScreen(this))).bounds((width-300)/2+154,184,146,20).build());
        button("Save & Done",height-26,this::onClose);
    }
    @Override public void extractRenderState(GuiGraphicsExtractor g,int mx,int my,float partial) {
        super.extractRenderState(g,mx,my,partial);
        g.centeredText(font,"Little Buddy options",width/2,14,0xFF7FFFD4);
        if(height>=280)g.centeredText(font,"Regular: buddy only. Streamer: viewer HUD and label.",width/2,height-40,0xFFB7CAD0);
    }
    @Override public boolean isPauseScreen() {return false;}
    @Override public void onClose() {SteveCount.settings.save();minecraft.gui.setScreen(parent);}
}
