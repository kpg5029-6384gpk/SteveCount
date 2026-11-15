package dev.stevecount;

import java.util.*;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;

/** Searchable, paged selection; plays only our buddy, never the player's real avatar. */
public final class PlayerEmotesScreen extends Screen {
    private final Screen parent;
    private List<PlayerEmotesBridge.Choice> choices=List.of(),filtered=List.of();
    private final List<Button> rows=new ArrayList<>();
    private int page;
    private String query="";
    private Button previous,next;
    PlayerEmotesScreen(Screen parent) {super(Component.literal("Player Emotes for Little Buddy"));this.parent=parent;}
    @Override protected void init() {
        choices=PlayerEmotesBridge.choices();rows.clear();
        int left=(width-300)/2;
        EditBox search=addRenderableWidget(new EditBox(font,left,32,300,20,Component.literal("Search emotes")));
        search.setMaxLength(80);search.setHint(Component.literal("Search Player Emotes…"));search.setValue(query);
        for(int i=0;i<5;i++) {
            final int row=i;
            rows.add(addRenderableWidget(Button.builder(Component.empty(),b->{
                int index=page*5+row;
                if(index<filtered.size()){BuddyEmotes.play(filtered.get(index));SteveCount.settings.save();minecraft.gui.setScreen(parent);}
            }).bounds(left,60+i*24,300,20).build()));
        }
        previous=addRenderableWidget(Button.builder(Component.literal("< Previous"),b->{page--;updateRows();}).bounds(left,180,92,20).build());
        addRenderableWidget(Button.builder(Component.literal("Stop emote"),b->{BuddyEmotes.stop();BuddyPersonality.rest();}).bounds(left+100,180,100,20).build());
        next=addRenderableWidget(Button.builder(Component.literal("Next >"),b->{page++;updateRows();}).bounds(left+208,180,92,20).build());
        addRenderableWidget(Button.builder(Component.literal("Done"),b->onClose()).bounds(left,height-26,300,20).build());
        search.setResponder(text->{query=text;page=0;filter();});filter();
    }
    private void filter() {
        String q=query.toLowerCase(Locale.ROOT);
        filtered=choices.stream().filter(c->c.name().toLowerCase(Locale.ROOT).contains(q)||c.id().toLowerCase(Locale.ROOT).contains(q)).toList();
        updateRows();
    }
    private void updateRows() {
        page=Math.clamp(page,0,Math.max(0,(filtered.size()-1)/5));
        for(int i=0;i<rows.size();i++) {
            int index=page*5+i;Button b=rows.get(i);b.visible=index<filtered.size();
            if(b.visible)b.setMessage(Component.literal(font.plainSubstrByWidth(filtered.get(index).name(),282)));
        }
        previous.active=page>0;next.active=(page+1)*5<filtered.size();
    }
    @Override public void extractRenderState(GuiGraphicsExtractor g,int mx,int my,float dt) {
        super.extractRenderState(g,mx,my,dt);
        g.centeredText(font,"Player Emotes for Little Buddy",width/2,14,0xFF7FFFD4);
        if(choices.isEmpty()) {
            g.centeredText(font,PlayerEmotesBridge.available()?"No compatible solo emotes loaded.":"Player Emotes 2.0.3 is needed for this menu.",width/2,84,0xFFFFFFFF);
            g.centeredText(font,"Install its matching Minecraft 26.3 mod, then restart.",width/2,104,0xFFB7CAD0);
            g.centeredText(font,"SteveCount's built-in emotes still work.",width/2,124,0xFFB7CAD0);
        }else if(filtered.isEmpty())g.centeredText(font,"No matching emotes",width/2,90,0xFFB7CAD0);
        if(height>=260)g.centeredText(font,font.plainSubstrByWidth(PlayerEmotesBridge.status(),width-20),width/2,height-40,0xFFB7CAD0);
    }
    @Override public boolean isPauseScreen() {return false;}
    @Override public void onClose() {minecraft.gui.setScreen(parent);}
}
