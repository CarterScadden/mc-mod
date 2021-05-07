package net.sweet.simple.mod;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.screenhandler.v1.ScreenHandlerRegistry;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.Identifier;
import net.sweet.simple.mod.materials.Diamerite;
import net.sweet.simple.mod.blocks.Quarry;


public class ServerMain implements ModInitializer {
    public static final ScreenHandlerType<Quarry.ScreenHandler> SCREEN_HANDLER;
    public static final Identifier BOX = new Identifier(CONSTANTS.MOD_ID, "box_block");

    static {
        SCREEN_HANDLER = ScreenHandlerRegistry.registerSimple(BOX, Quarry.ScreenHandler::new);
    }

    @Override
    public void onInitialize() {
        Diamerite.Register();
        Quarry.Register();
    }
}
