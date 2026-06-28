package com.sfmf3.citylogistics;

import com.mojang.logging.LogUtils;
import com.sfmf3.citylogistics.blueprint.BlueprintRegistry;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AddServerReloadListenersEvent;
import org.slf4j.Logger;

@Mod(CityLogistics.MODID)
public class CityLogistics {
    public static final String MODID = "citylogistics";
    public static final Logger LOGGER = LogUtils.getLogger();

    public CityLogistics(IEventBus modEventBus){
        LOGGER.info("Initializing CityLogistics for NeoForge 26.1");
        NeoForge.EVENT_BUS.addListener(this::onAddServerReloadListeners);
    }

    private void onAddServerReloadListeners(AddServerReloadListenersEvent event){
        Identifier listenerId = Identifier.fromNamespaceAndPath(MODID, "blueprint_loader");

        event.addListener(listenerId, (ResourceManagerReloadListener) BlueprintRegistry::scanBlueprints);
    }
}
