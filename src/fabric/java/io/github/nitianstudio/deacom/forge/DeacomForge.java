package io.github.nitianstudio.deacom.forge;

import dev.architectury.platform.forge.EventBuses;
import io.github.nitianstudio.deacom.Deacom;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(Deacom.MOD_ID)
public class DeacomForge {
    public DeacomForge() {
		// Submit our event bus to let architectury register our content on the right time
        EventBuses.registerModEventBus(Deacom.MOD_ID, FMLJavaModLoadingContext.get().getModEventBus());
        Deacom.init();
    }
}