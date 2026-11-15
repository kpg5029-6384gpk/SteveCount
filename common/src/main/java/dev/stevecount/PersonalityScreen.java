package dev.stevecount;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.network.chat.Component;

public final class PersonalityScreen extends Screen {
    private final Screen parent;
    PersonalityScreen(Screen parent) {super(Component.literal("Buddy personality"));this.parent=parent;}
    private void button(String text,int y,Runnable action) {addRenderableWidget(Button.builder(Component.literal(text),b->action.run()).bounds((width-300)/2,y,300,20).build());}
    private void changed() {BuddyPersonality.rest();SteveCount.settings.save();rebuildWidgets();}
    @Override protected void init() {
        var c=SteveCount.settings;
        button("Random emotes: "+c.randomEmotes,40,()->{c.randomEmotes=!c.randomEmotes;changed();});
        button("Random noises: "+c.buddyNoises,64,()->{c.buddyNoises=!c.buddyNoises;changed();});
        button("Activity: "+c.buddyActivity,88,()->{c.buddyActivity=c.buddyActivity.equals("Calm")?"Playful":"Calm";changed();});
        addRenderableWidget(new AbstractSliderButton((width-300)/2,112,300,20,Component.empty(),c.buddyVolume) {
            {updateMessage();}
            protected void updateMessage() {setMessage(Component.literal("Buddy volume: "+Math.round(value*100)+"%"));}
            protected void applyValue() {c.buddyVolume=(float)value;}
        });
        button("Preview one sound",136,BuddyPersonality::previewNoise);
        button("Turn off all random activity",160,()->{c.randomEmotes=c.buddyNoises=false;changed();});
        button("Save & Done",height-26,this::onClose);
    }
    @Override public void extractRenderState(GuiGraphicsExtractor g,int mx,int my,float partial) {
        super.extractRenderState(g,mx,my,partial);g.centeredText(font,"Buddy personality",width/2,14,0xFF7FFFD4);
        g.centeredText(font,"Cosmetic only. No random damage or gift actions.",width/2,height-40,0xFFB7CAD0);
    }
    @Override public boolean isPauseScreen() {return false;}
    @Override public void onClose() {SteveCount.settings.save();minecraft.gui.setScreen(parent);}
}
