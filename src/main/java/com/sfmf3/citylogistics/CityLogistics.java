package com.sfmf3.citylogistics;

import com.mojang.logging.LogUtils;
import com.sfmf3.citylogistics.blueprint.BlueprintRegistry;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import org.slf4j.Logger;

@Mod(CityLogistics.MODID)
public class CityLogistics {
    public static final String MODID = "citylogistics";
    public static final Logger LOGGER = LogUtils.getLogger();

    public CityLogistics(IEventBus modEventBus){
        LOGGER.info("Initializing CityLogistics for NeoForge 26.1");
        BlueprintRegistry.scanBlueprintsFromJar();
    }
}
