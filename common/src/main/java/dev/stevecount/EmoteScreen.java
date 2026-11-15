package dev.stevecount;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

public final class EmoteScreen extends Screen {
    private final Screen parent;
    EmoteScreen(Screen parent) {super(Component.literal("Buddy emotes"));this.parent=parent;}
    private void button(String label,int y,Runnable action) {
        addRenderableWidget(Button.builder(Component.literal(label),b->action.run()).bounds((width-300)/2,y,300,20).build());
    }
    @Override protected void init() {
        button("Wave",40,()->play("Wave"));
        addRenderableWidget(Button.builder(Component.literal("Dance"),b->play("Dance")).bounds((width-300)/2,64,146,20).build());
        addRenderableWidget(Button.builder(Component.literal("Player Emotes"),b->minecraft.gui.setScreen(new PlayerEmotesScreen(this))).bounds((width-300)/2+154,64,146,20).build());
        button("Cheer",88,()->play("Cheer"));
        button("Tiny head rider: "+SteveCount.settings.characterHeadRide,112,()->{
            var c=SteveCount.settings;c.characterHeadRide=!c.characterHeadRide;
            if(c.characterHeadRide && !c.showCharacter())c.displayMode="Both";
            c.save();rebuildWidgets();
        });
        button("Stop emote / Return to ground",136,()->{BuddyEmotes.stop();BuddyPersonality.rest();SteveCount.settings.characterHeadRide=false;SteveCount.settings.save();rebuildWidgets();});
        addRenderableWidget(Button.builder(Component.literal("Buddy options"),b->minecraft.gui.setScreen(new BuddyOptionsScreen(this))).bounds((width-300)/2,160,194,20).build());
        addRenderableWidget(Button.builder(Component.literal("Recall"),b->FollowerCharacter.recall()).bounds((width-300)/2+202,160,98,20).build());
        button("Voice commands: "+SteveCount.settings.voiceControl,184,()->{SteveCount.settings.voiceControl=!SteveCount.settings.voiceControl;SteveCount.settings.save();rebuildWidgets();});
        button("Done",height-26,this::onClose);
    }
    private void play(String name) {BuddyEmotes.play(name);SteveCount.settings.save();minecraft.gui.setScreen(parent);}
    @Override public void extractRenderState(GuiGraphicsExtractor g,int mx,int my,float partial) {
        super.extractRenderState(g,mx,my,partial);
        g.centeredText(font,"Buddy emotes",width/2,14,0xFF7FFFD4);
        if(height>=280)g.centeredText(font,"Voice: enable here, then Start voice in the companion. F5: head rider.",width/2,height-40,0xFFB7CAD0);
    }
    @Override public boolean isPauseScreen() {return false;}
    @Override public void onClose() {SteveCount.settings.save();minecraft.gui.setScreen(parent);}
}
