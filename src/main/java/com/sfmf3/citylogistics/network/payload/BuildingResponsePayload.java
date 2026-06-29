package com.sfmf3.citylogistics.network.payload;

import com.sfmf3.citylogistics.CityLogistics;
import com.sfmf3.citylogistics.building.BuildingInformation;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record BuildingResponsePayload(
    BuildingInformation information
) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<BuildingResponsePayload> TYPE = new CustomPacketPayload.Type<>(
            Identifier.fromNamespaceAndPath(CityLogistics.MODID, "building_response"));

    public static final StreamCodec<RegistryFriendlyByteBuf, BuildingResponsePayload> STREAM_CODEC = StreamCodec.composite(
            BuildingInformation.STREAM_CODEC, BuildingResponsePayload::information,
            BuildingResponsePayload::new
    );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type(){ return TYPE; }
}
