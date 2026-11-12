package dev.stevecount;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

final class MoveScreen extends Screen {
    final Screen parent;
    boolean dragging;
    double grabX,grabY;
    MoveScreen(Screen parent){super(Component.literal("Move SteveCount HUD"));this.parent=parent;}
    @Override protected void init(){
        addRenderableWidget(Button.builder(Component.literal("Save position"),b->onClose()).bounds(width/2-160,height-30,104,20).build());
        addRenderableWidget(Button.builder(Component.literal("Reset"),b->{var c=SteveCount.settings;c.x=c.y=0;c.offsetX=c.offsetY=8;}).bounds(width/2-52,height-30,104,20).build());
        addRenderableWidget(Button.builder(Component.literal("Preset"),b->SteveCount.settings.preset=SettingsScreen.next(SteveCount.settings.preset,"Minimal","Compact","Streamer","Expanded")).bounds(width/2+56,height-30,104,20).build());
        addRenderableWidget(Button.builder(Component.literal("Size +"),b->SteveCount.settings.scale=Math.min(3,SteveCount.settings.scale+.25f)).bounds(width/2-160,height-54,104,20).build());
        addRenderableWidget(Button.builder(Component.literal("Size -"),b->SteveCount.settings.scale=Math.max(.5f,SteveCount.settings.scale-.25f)).bounds(width/2-52,height-54,104,20).build());
        addRenderableWidget(Button.builder(Component.literal("Opacity"),b->SteveCount.settings.opacity=(SteveCount.settings.opacity+.1f)%1.01f).bounds(width/2+56,height-54,104,20).build());
    }
    @Override public void extractRenderState(GuiGraphicsExtractor g,int mx,int my,float dt){
        super.extractRenderState(g,mx,my,dt);
        g.centeredText(font,"Drag HUD • Preview data is simulated",width/2,height-72,0xFFFFFFFF);
        Hud.draw(g,true);
    }
    @Override public boolean mouseClicked(MouseButtonEvent e,boolean doubleClick){
        if(e.button()==0 && e.x()>=Hud.left && e.x()<=Hud.left+Hud.width && e.y()>=Hud.top && e.y()<=Hud.top+Hud.height){dragging=true;grabX=e.x()-Hud.left;grabY=e.y()-Hud.top;return true;}
        return super.mouseClicked(e,doubleClick);
    }
    @Override public boolean mouseDragged(MouseButtonEvent e,double dx,double dy){
        if(dragging){var c=SteveCount.settings;c.offsetX=c.offsetY=0;c.x=(float)Math.clamp((e.x()-grabX)/Math.max(1,width-Hud.width),0,1);c.y=(float)Math.clamp((e.y()-grabY)/Math.max(1,height-Hud.height),0,1);return true;}
        return super.mouseDragged(e,dx,dy);
    }
    @Override public boolean mouseReleased(MouseButtonEvent e){dragging=false;return super.mouseReleased(e);}
    @Override public void onClose(){SteveCount.settings.save();minecraft.gui.setScreen(parent);}
    @Override public boolean isPauseScreen(){return false;}
}

