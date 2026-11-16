package dev.stevecount;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;

/** Self-only cosmetic event controls. Preview is explicitly separate from a real gift. */
final class CostumeScreen extends Screen {
    private final Screen parent;
    CostumeScreen(Screen parent) {super(Component.literal("Gift outfit"));this.parent=parent;}
    private void button(String text,int y,Runnable action) {addRenderableWidget(Button.builder(Component.literal(text),b->{action.run();rebuildWidgets();}).bounds((width-300)/2,y,300,20).build());}
    @Override protected void init() {
        button("1,000+ coin costume: "+(SteveCount.settings.maidGift?"On":"Off"),40,()->{var c=SteveCount.settings;c.maidGift=!c.maidGift;if(!c.maidGift)MaidCostume.restore();c.save();});
        button("Preview outfit (30-minute timer)",66,()->{MaidCostume.start();if(MaidCostume.active())minecraft.gui.setScreen(parent);});
        button("Restore original outfit now",92,MaidCostume::restore);
        button("Reload approved costume PNG",118,MaidCostume::reload);
        button("Done",height-26,this::onClose);
    }
    @Override public void extractRenderState(GuiGraphicsExtractor g,int x,int y,float partial) {
        super.extractRenderState(g,x,y,partial);g.centeredText(font,"Fiishy's gift outfit",width/2,14,0xFFFFD2ED);
        g.centeredText(font,MaidCostume.timeLabel(),width/2,148,0xFFFFFFFF);
        g.centeredText(font,MaidCostume.assetStatus,width/2,164,0xFFB7CAD0);
        if(height>=260)g.centeredText(font,"Each 1,000+ coin gift restarts 30 minutes. F5: outfit preview.",width/2,184,0xFFB7CAD0);
    }
    @Override public boolean isPauseScreen() {return false;}
    @Override public void onClose() {SteveCount.settings.save();minecraft.gui.setScreen(parent);}
}
