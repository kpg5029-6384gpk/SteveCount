package dev.stevecount;

import net.minecraft.client.Minecraft;
import net.minecraft.commands.Commands;
import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.client.event.*;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

@Mod(value="stevecount",dist=Dist.CLIENT)
public final class NeoForgeEntrypoint {
    public NeoForgeEntrypoint(IEventBus bus,ModContainer container) {
        SteveCount.initialize(FMLPaths.CONFIGDIR.get());
        NeoForge.EVENT_BUS.addListener((ExtractLevelRenderStateEvent e)->FollowerCharacter.extract(e.getRenderState()));
        container.registerExtensionPoint(IConfigScreenFactory.class,(mc,parent)->new SettingsScreen(parent));
        bus.addListener((RegisterKeyMappingsEvent e)->{e.register(SteveCount.key);e.register(SteveCount.emoteKey);});
        bus.addListener((RegisterGuiLayersEvent e)->e.registerAboveAll(Identifier.fromNamespaceAndPath("stevecount","hud"),(g,d)->{
            var mc=Minecraft.getInstance();
            if(SteveCount.settings.showHud() && mc.player!=null && !mc.gui.hud.isHidden() && !(mc.gui.screen() instanceof MoveScreen)) Hud.draw(g,false);
        }));
        NeoForge.EVENT_BUS.addListener((RegisterClientCommandsEvent e)->e.getDispatcher().register(Commands.literal("stevecount").executes(c->{Minecraft.getInstance().schedule(SteveCount::open);return 1;})));
        NeoForge.EVENT_BUS.addListener((ClientTickEvent.Post e)->SteveCount.tick());
    }
}
