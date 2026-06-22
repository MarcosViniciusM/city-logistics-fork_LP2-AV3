package com.sfmf3.citylogistics.network;

import com.sfmf3.citylogistics.CityLogistics;
import com.sfmf3.citylogistics.network.payload.*;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@EventBusSubscriber(modid = CityLogistics.MODID)
public class ModNetwork {

    @SubscribeEvent
    public static void register(final RegisterPayloadHandlersEvent event){
        final PayloadRegistrar registrar = event.registrar("1");

        registrar.playToServer(
                AddBuildingPayload.TYPE,
                AddBuildingPayload.STREAM_CODEC,
                ServerPayloadHandler::handleAddBuilding
        );

        registrar.playToServer(
                RemoveBuildingPayload.TYPE,
                RemoveBuildingPayload.STREAM_CODEC,
                ServerPayloadHandler::handleRemoveBuilding
        );

        registrar.playToServer(
                AddCityPayload.TYPE,
                AddCityPayload.STREAM_CODEC,
                ServerPayloadHandler::handleAddCity
        );

        registrar.playToServer(
                RemoveCityPayload.TYPE,
                RemoveCityPayload.STREAM_CODEC,
                ServerPayloadHandler::handleRemoveCity
        );

        registrar.playToServer(
                AddPopulationPayload.TYPE,
                AddPopulationPayload.STREAM_CODEC,
                ServerPayloadHandler::handleAddPopulation
        );

        registrar.playToServer(
                CityRequestPayload.TYPE,
                CityRequestPayload.STREAM_CODEC,
                ServerPayloadHandler::handleCityRequest
        );
        registrar.playToClient(
                CityResponsePayload.TYPE,
                CityResponsePayload.STREAM_CODEC,
                ClientPayloadHandler::handleCityResponse
        );
    }
}
