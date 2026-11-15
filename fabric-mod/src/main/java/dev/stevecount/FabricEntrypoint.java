package dev.stevecount;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelExtractionEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;

public final class FabricEntrypoint implements ClientModInitializer {
    @Override public void onInitializeClient() {
        SteveCount.initialize(FabricLoader.getInstance().getConfigDir());
        KeyMappingHelper.registerKeyMapping(SteveCount.key);
        KeyMappingHelper.registerKeyMapping(SteveCount.emoteKey);
        LevelExtractionEvents.END_EXTRACTION.register(context->FollowerCharacter.extract(context.levelState()));
        ClientTickEvents.END_CLIENT_TICK.register(mc->SteveCount.tick());
        ClientCommandRegistrationCallback.EVENT.register((dispatcher,registry)->dispatcher.register(ClientCommands.literal("stevecount").executes(c->{Minecraft.getInstance().schedule(SteveCount::open);return 1;})));
        HudElementRegistry.addLast(Identifier.fromNamespaceAndPath("stevecount","hud"),(g,d)->{
            var mc=Minecraft.getInstance();
            if(SteveCount.settings.showHud() && mc.player!=null && !mc.gui.hud.isHidden() && !(mc.gui.screen() instanceof MoveScreen)) Hud.draw(g,false);
        });
    }
}
