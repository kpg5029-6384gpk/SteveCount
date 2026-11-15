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
        button("Dance",64,()->play("Dance"));
        button("Cheer",88,()->play("Cheer"));
        button("Tiny head rider: "+SteveCount.settings.characterHeadRide,112,()->{
            var c=SteveCount.settings;c.characterHeadRide=!c.characterHeadRide;
            if(c.characterHeadRide && !c.showCharacter())c.displayMode="Both";
            c.save();rebuildWidgets();
        });
        button("Stop emote / Return to ground",136,()->{BuddyEmotes.stop();SteveCount.settings.characterHeadRide=false;SteveCount.settings.save();rebuildWidgets();});
        button("Recall buddy",160,FollowerCharacter::recall);
        button("Done",height-26,this::onClose);
    }
    private void play(String name) {BuddyEmotes.play(name);SteveCount.settings.save();minecraft.gui.setScreen(parent);}
    @Override public void extractRenderState(GuiGraphicsExtractor g,int mx,int my,float partial) {
        super.extractRenderState(g,mx,my,partial);
        g.centeredText(font,"Buddy emotes",width/2,14,0xFF7FFFD4);
        g.centeredText(font,"Cosmetic only. Use F5 to see the tiny head rider.",width/2,height-40,0xFFB7CAD0);
    }
    @Override public boolean isPauseScreen() {return false;}
    @Override public void onClose() {SteveCount.settings.save();minecraft.gui.setScreen(parent);}
}
