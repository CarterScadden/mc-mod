package net.sweet.simple.mod;

import net.fabricmc.api.ModInitializer;
import net.sweet.simple.mod.materials.Diamerite;
import net.sweet.simple.mod.materials.Quarry;


public class Main implements ModInitializer {

    // what runs on client and server
    @Override
    public void onInitialize() {
        Diamerite.Register();
        Quarry.Register();
    }
}
