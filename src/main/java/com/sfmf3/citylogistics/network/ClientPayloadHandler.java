package com.sfmf3.citylogistics.network;

import com.sfmf3.citylogistics.CityLogistics;
import com.sfmf3.citylogistics.camera.client.CityInfoManager;
import com.sfmf3.citylogistics.camera.client.ui.CityScreen;
import com.sfmf3.citylogistics.network.payload.BlueprintResponsePayload;
import com.sfmf3.citylogistics.network.payload.CityResponsePayload;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class ClientPayloadHandler {
    public static void handleCityResponse(final CityResponsePayload payload, final IPayloadContext context){
        context.enqueueWork(() -> {
            CityInfoManager.setCityInfo(payload);
        });
    }

    public static void handleBlueprintResponse(final BlueprintResponsePayload payload, final IPayloadContext context){
        context.enqueueWork(() -> {
            try{
                CityScreen.activeContext.updateBlueprint(payload);
            } catch (Exception e){
                context.player().sendSystemMessage(Component.literal(("Error: " + e.getMessage())).withStyle(ChatFormatting.DARK_RED));
                CityLogistics.LOGGER.error("Failed to handle blueprint response for " +context.player().getName());
            }
        });
    }
}
