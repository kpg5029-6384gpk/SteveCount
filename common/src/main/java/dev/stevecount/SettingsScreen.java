package dev.stevecount;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.*;
import net.minecraft.network.chat.Component;
import java.time.*;
import java.time.format.DateTimeFormatter;

public final class SettingsScreen extends Screen {
    final Screen parent;
    int tab, left, y;
    EditBox input;
    SettingsScreen(Screen parent) { super(Component.literal("SteveCount")); this.parent=parent; }
    void button(String name, int x, int yy, int w, Runnable action) {
        addRenderableWidget(Button.builder(Component.literal(name), b->action.run()).bounds(x,yy,w,20).build());
    }
    void row(String a, Runnable one, String b, Runnable two) {
        button(a,left,y,156,one);button(b,left+164,y,156,two);y+=24;
    }
    void refresh() { rebuildWidgets(); }
    @Override protected void init() {
        left=(width-320)/2;y=58;var c=SteveCount.settings;
        String[] tabs={"Connect","HUD","Demo","Character","More"};
        for(int i=0;i<5;i++) {final int index=i;button(tabs[i],left+i*65,29,60,()->{tab=index;refresh();});}
        if(tab==0) {
            input=addRenderableWidget(new EditBox(font,left,y,320,20,Component.literal("TikTok username or LIVE URL")));
            input.setMaxLength(512);input.setHint(Component.literal("@username or TikTok LIVE URL"));input.setValue(c.username);input.setResponder(v->c.username=v);y+=26;
            row("Connect",()->{SteveCount.demo(false);c.save();SteveCount.bridge.control("connect",c.username);},"Disconnect",()->{SteveCount.demo(false);SteveCount.bridge.control("disconnect","");});
            row("Reconnect",()->{SteveCount.demo(false);SteveCount.bridge.control("connect",c.username);},"Auto-connect: "+c.autoConnect,()->{c.autoConnect=!c.autoConnect;refresh();});
            y+=52;
            button("Move HUD / preview",left,y,320,()->minecraft.gui.setScreen(new MoveScreen(this)));
        } else if(tab==1) {
            row("Preset: "+c.preset,()->{c.preset=next(c.preset,"Minimal","Compact","Streamer","Expanded");refresh();},"Head: "+c.icon,()->{c.icon=next(c.icon,"Steve","Alex","Player","None");refresh();});
            slider("Scale",.5,3,c.scale,v->c.scale=(float)v);
            slider("Opacity",0,1,c.opacity,v->c.opacity=(float)v);
            row("Background: "+c.background,()->{c.background=!c.background;refresh();},"Border: "+c.border,()->{c.border=!c.border;refresh();});
            row("LIVE label: "+c.liveLabel,()->{c.liveLabel=!c.liveLabel;refresh();},"Abbreviate: "+c.abbreviated,()->{c.abbreviated=!c.abbreviated;refresh();});
            row("Move HUD",()->minecraft.gui.setScreen(new MoveScreen(this)),"Reset HUD",()->{c.resetHudAppearance();refresh();});
        } else if(tab==2) {
            row("Demo: "+SteveCount.demo,()->{SteveCount.demo(!SteveCount.demo);refresh();},"Auto simulate: "+SteveCount.automatic,()->{if(!SteveCount.demo) SteveCount.demo(true);SteveCount.automatic=!SteveCount.automatic;refresh();});
            input=addRenderableWidget(new EditBox(font,left,y,156,20,Component.literal("Demo viewer count")));
            input.setMaxLength(12);input.setValue(Long.toString(SteveCount.demoCount));
            button("Set demo count",left+164,y,156,()->{try{long n=Long.parseLong(input.getValue());if(!SteveCount.demo)SteveCount.demo(true);SteveCount.changeDemo(Math.clamp(n,0,999999999999L));}catch(NumberFormatException ex){SteveCount.message="Enter a positive whole number";}});y+=24;
            row("+ 10 viewers",()->{if(!SteveCount.demo)SteveCount.demo(true);SteveCount.changeDemo(SteveCount.demoCount+10);input.setValue(""+SteveCount.demoCount);},"- 10 viewers",()->{if(!SteveCount.demo)SteveCount.demo(true);SteveCount.changeDemo(SteveCount.demoCount-10);input.setValue(""+SteveCount.demoCount);});
            row("Simulate lost connection",()->{if(!SteveCount.demo)SteveCount.demo(true);SteveCount.demoStatus="Reconnecting";},"Simulate offline",()->{if(!SteveCount.demo)SteveCount.demo(true);SteveCount.demoStatus="Offline";});
            row("Restore demo live",()->{if(!SteveCount.demo)SteveCount.demo(true);SteveCount.demoStatus="Demo Mode";},"Reset demo milestones",()->{SteveCount.reached.clear();SteveCount.demoPeak=SteveCount.demoCount;});
            button("Preview / move HUD",left,y,320,()->minecraft.gui.setScreen(new MoveScreen(this)));
        } else if(tab==3) {
            row("Display: "+c.displayMode,()->{c.displayMode=next(c.displayMode,"HUD","Character","Both");FollowerCharacter.recall();refresh();},"Buddy: "+c.characterSkin,()->{c.characterSkin=next(c.characterSkin,"Steve","Alex","Player","Fiishy","Astronaut");refresh();});
            slider("Character size",.25,1,c.characterScale,v->c.characterScale=(float)v);
            slider("Follow distance",1.5,6,c.followDistance,v->c.followDistance=(float)v);
            row("Count label: "+c.characterLabel,()->{c.characterLabel=!c.characterLabel;refresh();},"Grounded: "+c.characterGrounded,()->{c.characterGrounded=!c.characterGrounded;refresh();});
            row("Emotes / Head ride",()->minecraft.gui.setScreen(new EmoteScreen(this)),"Reset character",()->{c.displayMode="HUD";c.characterSkin="Steve";c.characterScale=.55f;c.followDistance=2.5f;c.characterLabel=c.characterGrounded=true;c.characterHeadRide=false;c.giftPunch=false;BuddyEmotes.stop();FollowerCharacter.recall();refresh();});
            row("Gift damage: "+c.giftPunch,()->{c.giftPunch=!c.giftPunch;c.save();refresh();},"Preview punch (no damage)",GiftPunch::preview);
        } else {
            row("Animations: "+c.animations,()->{c.animations=!c.animations;refresh();},"Milestone sound: "+c.sound,()->{c.sound=!c.sound;refresh();});
            row("Milestone toast: "+c.toast,()->{c.toast=!c.toast;refresh();},"Reset position",()->{c.x=c.y=0;c.offsetX=c.offsetY=8;refresh();});
            input=addRenderableWidget(new EditBox(font,left,y,320,20,Component.literal("Milestones, comma separated")));
            input.setMaxLength(256);input.setValue(c.milestones);input.setResponder(v->c.milestones=v);y+=24;
            row("Text: white / aqua",()->{c.textColor=c.textColor==0xFFFFFFFF?0xFF7FFFD4:0xFFFFFFFF;},"Background: dark / slate",()->{c.backgroundColor=c.backgroundColor==0x101820?0x26364A:0x101820;});
            row("Offset X -",()->c.offsetX--,"Offset X +",()->c.offsetX++);
            row("Offset Y -",()->c.offsetY--,"Offset Y +",()->c.offsetY++);
        }
        button("Save & Done",left,height-26,320,this::onClose);
    }
    static String next(String value,String... values) {for(int i=0;i<values.length;i++)if(values[i].equals(value))return values[(i+1)%values.length];return values[0];}
    void slider(String name,double min,double max,double current,java.util.function.DoubleConsumer update) {
        addRenderableWidget(new AbstractSliderButton(left,y,320,20,Component.empty(),(current-min)/(max-min)) {
            {updateMessage();}
            protected void updateMessage(){setMessage(Component.literal(name+": "+String.format(java.util.Locale.ROOT,"%.2f",min+value*(max-min))));}
            protected void applyValue(){update.accept(min+value*(max-min));}
        });y+=24;
    }
    @Override public void extractRenderState(GuiGraphicsExtractor g,int mx,int my,float dt) {
        super.extractRenderState(g,mx,my,dt);
        g.centeredText(font,"SteveCount",width/2,10,0xFF7FFFD4);
        if(tab==0) {
            var s=SteveCount.current();
            g.text(font,s.status()+" | Viewers: "+(s.viewers()==null?"Unavailable":SteveCount.format(s.viewers()))+(s.stale()?" (stale)":""),left,137,0xFFFFFFFF);
            String time=s.updated()==0?"No viewer events yet":DateTimeFormatter.ofPattern("HH:mm:ss").format(Instant.ofEpochSecond((long)s.updated()).atZone(ZoneId.systemDefault()));
            g.text(font,"Last count: "+time,left,151,0xFFB7CAD0);
            g.text(font,font.plainSubstrByWidth(s.message(),320),left,165,0xFFFFC267);
        }
        if(tab==3) {
            g.text(font,"Real fist damage. Single-player. Can kill.",left,height-39,0xFFFFC267);
        }
        if(!SteveCount.message.isEmpty())g.text(font,font.plainSubstrByWidth(SteveCount.message,320),left,height-39,0xFFFFC267);
    }
    @Override public void onClose() {SteveCount.settings.save();minecraft.gui.setScreen(parent);}
    @Override public boolean isPauseScreen(){return false;}
}

