package io.github.nitianstudio.deacom.fabric;

import io.github.nitianstudio.deacom.Deacom;
import net.fabricmc.api.ModInitializer;

public class DeacomFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        Deacom.init();
    }
}