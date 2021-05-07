package net.sweet.simple.mod;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.screenhandler.v1.ScreenRegistry;
import net.sweet.simple.mod.blocks.Quarry;

@Environment(EnvType.CLIENT)
public class Main implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        ScreenRegistry.register(ServerMain.SCREEN_HANDLER, Quarry.Screen::new);
    }
}
