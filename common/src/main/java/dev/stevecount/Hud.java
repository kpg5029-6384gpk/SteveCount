package dev.stevecount;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;

final class Hud {
    static int left,top,width,height;
    static void draw(GuiGraphicsExtractor g, boolean preview) {
        var c=SteveCount.settings;var s=SteveCount.current();var mc=Minecraft.getInstance();var font=mc.font;
        String status=s.status();Long count=s.viewers();
        if(preview && count==null) {count=128L;status="Preview";}
        String text=count!=null && !status.equals("Offline") && !status.equals("Disconnected") && (status.equals("Live") || SteveCount.demo || s.stale() || preview)?SteveCount.format(count):status;
        boolean stale=s.stale() || count!=null && !SteveCount.demo && !preview && System.currentTimeMillis()/1000d-s.updated()>60;
        if(stale) text+=" ~";
        if(SteveCount.demo) text="DEMO "+text;
        boolean expanded=c.preset.equals("Expanded");
        boolean live=c.liveLabel && (c.preset.equals("Streamer") || expanded);
        boolean eye=c.preset.equals("Compact");
        String detail=(SteveCount.demo?"Demo":status)+" | Peak "+SteveCount.format(s.peak())+(s.username().isBlank()?"":" | @"+s.username());
        int head=c.icon.equals("None")?0:21;
        int label=live?35:0;
        int baseW=12+head+label+(eye?15:0)+font.width(text);
        if(expanded) baseW=Math.max(baseW,12+font.width(detail));
        int baseH=expanded?36:26;
        float scale=c.scale;
        width=Math.round(baseW*scale);height=Math.round(baseH*scale);
        left=HudLayout.coordinate(c.x,c.offsetX,g.guiWidth(),width);
        top=HudLayout.coordinate(c.y,c.offsetY,g.guiHeight(),height);
        g.pose().pushMatrix();g.pose().translate(left,top);g.pose().scale(scale,scale);
        if(c.background) {
            int bg=((int)(c.opacity*255)<<24)|(c.backgroundColor&0xFFFFFF);
            g.fill(1,0,baseW-1,baseH,bg);g.fill(0,1,baseW,baseH-1,bg);
        }
        if(c.border) g.outline(0,0,baseW,baseH,0x665B7882);
        int x=6;
        if(head>0) {
            Identifier skin=Identifier.withDefaultNamespace("textures/entity/player/wide/"+(c.icon.equals("Alex")?"alex":"steve")+".png");
            if(c.icon.equals("Player") && mc.player!=null) skin=mc.player.getSkin().body().texturePath();
            g.blit(RenderPipelines.GUI_TEXTURED,skin,x,5,8,8,16,16,8,8,64,64);
            g.blit(RenderPipelines.GUI_TEXTURED,skin,x,5,40,8,16,16,8,8,64,64);x+=head;
        }
        if(live) {g.fill(x,10,x+3,13, status.equals("Live")?0xFFFF5252:0xFF87959E);g.text(font,"LIVE",x+6,9,0xFFFF7676);x+=label;}
        if(eye) {g.fill(x+2,9,x+9,15,0xFFE6EFF2);g.fill(x,11,x+11,13,0xFFE6EFF2);g.fill(x+4,10,x+7,14,0xFF263C4A);x+=15;}
        int color=stale?0xFFFFC267:c.textColor;
        if(c.animations && System.currentTimeMillis()<SteveCount.pulseUntil) color=0xFF7FFFD4;
        g.text(font,text,x,9,color);
        if(expanded) g.text(font,detail,6,24,0xFFB7CAD0);
        g.pose().popMatrix();
        if(c.toast && System.currentTimeMillis()<SteveCount.toastUntil) {
            int w=font.width(SteveCount.milestoneText)+16;
            g.fill(g.guiWidth()-w-8,8,g.guiWidth()-8,30,0xD0102525);
            g.text(font,SteveCount.milestoneText,g.guiWidth()-w,15,0xFF7FFFD4);
        }
    }
}

