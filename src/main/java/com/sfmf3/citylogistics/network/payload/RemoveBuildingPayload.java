package com.sfmf3.citylogistics.network.payload;

import com.sfmf3.citylogistics.CityLogistics;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record RemoveBuildingPayload(
        BlockPos cityAnchor,
        BlockPos buildingPos
) implements CustomPacketPayload {

    public static final Type<RemoveBuildingPayload> TYPE = new Type<>(
            Identifier.fromNamespaceAndPath(CityLogistics.MODID, "remove_building"));

    public static final StreamCodec<RegistryFriendlyByteBuf, RemoveBuildingPayload> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, RemoveBuildingPayload::cityAnchor,
            BlockPos.STREAM_CODEC, RemoveBuildingPayload::buildingPos,
            RemoveBuildingPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }


}
