package com.sfmf3.citylogistics.network;

import com.sfmf3.citylogistics.CityLogistics;
import com.sfmf3.citylogistics.building.AbstractBuilding;
import com.sfmf3.citylogistics.camera.client.CityClientInfo;
import com.sfmf3.citylogistics.city.City;
import com.sfmf3.citylogistics.city.CityManager;
import com.sfmf3.citylogistics.network.payload.*;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.ArrayList;
import java.util.List;

public class ServerPayloadHandler {
    public static void handleAddBuilding(final AddBuildingPayload payload, final IPayloadContext context){
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer serverPlayer){
                ServerLevel level = serverPlayer.level();

                try{
                    City city = CityManager.getCityData(level).getCities().get(payload.cityAnchor());
                    if(city == null) throw new CityOperationException("Unable to fetch city!");
                    CityManager.addBuilding(
                            level, city,
                            payload.origin(),
                            payload.path(),
                            payload.rot(),
                            payload.mirrored(),
                            payload.buildingId()
                    );
                    serverPlayer.sendSystemMessage(Component.literal("Building created successfully!"));
                } catch (Exception e) {
                    serverPlayer.sendSystemMessage(Component.literal("Error: " + e.getMessage()).withStyle(ChatFormatting.DARK_RED));
                    CityLogistics.LOGGER.error("Failed to add building: " + e);
                }
            }
        });
    }

    public static void handleRemoveBuilding(final RemoveBuildingPayload payload, final IPayloadContext context){
        context.enqueueWork(() -> {
            if(context.player() instanceof ServerPlayer serverPlayer){
                ServerLevel level = serverPlayer.level();

                City city = CityManager.getCityData(level).getCities().get(payload.cityAnchor());
                if(city != null && city.canEdit(serverPlayer.getUUID())){
                    CityManager.removeBuilding(city, payload.buildingPos());
                }
            }
        });
    }

    public static void handleAddCity(final AddCityPayload payload, final IPayloadContext context){
        context.enqueueWork(() -> {
            if(context.player() instanceof ServerPlayer serverPlayer) {
                ServerLevel level = serverPlayer.level();

                try {
                    CityManager.addCity(level, payload.name(), payload.cityAnchor(), serverPlayer.getUUID());
                    serverPlayer.sendSystemMessage(Component.literal("City created successfully!"));
                } catch (Exception e){
                    serverPlayer.sendSystemMessage(
                            Component.literal("Error: " + e.getMessage()).withStyle(ChatFormatting.DARK_RED)
                    );
                    CityLogistics.LOGGER.error("Failed to add city: ", e);
                }
            }
        });
    }

    public static void handleRemoveCity(final RemoveCityPayload payload, final IPayloadContext context){
        context.enqueueWork(() -> {
            if(context.player() instanceof ServerPlayer serverPlayer){
                ServerLevel level = serverPlayer.level();

                CityManager.removeCity(level, payload.cityAnchor(), serverPlayer.getUUID());
            }
        });
    }

    public static void handleAddPopulation(final AddPopulationPayload payload, final IPayloadContext context){
        context.enqueueWork(() -> {
           if(context.player() instanceof ServerPlayer serverPlayer){
               ServerLevel level = serverPlayer.level();

               CityManager.addPopulation(
                       CityManager.getCityData(level).getCities().get(payload.cityAnchor()),
                       payload.count()
               );
           }
        });
    }

    public static void handleCityRequest(final CityRequestPayload payload, final IPayloadContext context){
        context.enqueueWork(() -> {
            if(context.player() instanceof ServerPlayer serverPlayer) {
                try{
                    ServerLevel level = serverPlayer.level();
                    BlockPos cityAnchor = CityManager.getClosestCity(payload.playerPos(), level);
                    if (cityAnchor == null) throw new CityOperationException("Close enough city not found!");
                    City city = CityManager.getCityData(level).getCities().get(cityAnchor);

                    if (city == null)
                        throw new CityOperationException("Honestly i have no idea how you got this error. Congratulations");
                    if (!city.canEdit(serverPlayer.getUUID()))
                        throw new CityOperationException("Player has no editing privileges in this city!");
                    List<CityClientInfo.BuildingBox> boxes = new ArrayList<>();

                    for (AbstractBuilding b : city.getBuildings().values()) {
                        boxes.add(new CityClientInfo.BuildingBox(
                                b.getOrigin(),
                                b.getDimensions(),
                                b.getRotation(),
                                b.getMirrored(),
                                b.getBuildingID()
                        ));
                    }

                    context.reply(new CityResponsePayload(
                            cityAnchor,
                            city.getStockpileCurrent(),
                            city.getStockpileMax(),
                            city.getPopulation(),
                            city.getPopulationCap(),
                            boxes
                    ));
                } catch (Exception e) {
                    serverPlayer.sendSystemMessage(Component.literal("Error: " + e.getMessage()).withStyle(ChatFormatting.DARK_RED));
                    CityLogistics.LOGGER.error("Failed to handle city request for " + serverPlayer.getName());
                }
            }
        });
    }
}