package com.sfmf3.citylogistics.network;

import com.sfmf3.citylogistics.camera.client.CityBuilderScreen;
import com.sfmf3.citylogistics.camera.client.CityClientInfo;
import com.sfmf3.citylogistics.network.payload.BlueprintResponsePayload;
import com.sfmf3.citylogistics.network.payload.CityResponsePayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class ClientPayloadHandler {
    public static void handleCityResponse(final CityResponsePayload payload, final IPayloadContext context){
        context.enqueueWork(() -> {
            CityClientInfo.setCityInfo(payload);
        });
    }

    public static void handleBlueprintResponse(final BlueprintResponsePayload payload, final IPayloadContext context){
        context.enqueueWork(() -> {
            CityClientInfo.setBlueprintInfo(payload);
        });
    }
}
