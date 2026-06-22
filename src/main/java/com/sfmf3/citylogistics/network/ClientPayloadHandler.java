package com.sfmf3.citylogistics.network;

import com.sfmf3.citylogistics.camera.client.CityClientInfo;
import com.sfmf3.citylogistics.network.payload.CityResponsePayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class ClientPayloadHandler {
    public static void handleCityResponse(final CityResponsePayload payload, final IPayloadContext context){
        context.enqueueWork(() -> {
            CityClientInfo.cityAnchor = payload.cityAnchor();
            CityClientInfo.pop = payload.pop();
            CityClientInfo.popcap = payload.popcap();
            CityClientInfo.stockCurrent = payload.stockCurrent();
            CityClientInfo.stockLimits = payload.stockLimits();
            CityClientInfo.allBuildings = payload.buildings();
        });
    }
}
