package com.sfmf3.citylogistics.network;

import com.sfmf3.citylogistics.CityLogistics;
import com.sfmf3.citylogistics.city.CityManager;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

@EventBusSubscriber(modid = CityLogistics.MODID)
public class TickEventHandler {

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event){
        long gameTime = event.getServer().overworld().getGameTime();

        if(gameTime%2000 == 0){
            CityManager.tickAllCities(event.getServer().overworld().getLevel());
        }
    }
}
