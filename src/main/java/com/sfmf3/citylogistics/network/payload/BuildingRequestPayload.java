package com.sfmf3.citylogistics.network.payload;

import com.sfmf3.citylogistics.CityLogistics;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record BuildingRequestPayload(
        BlockPos anchor,
        BlockPos selection
) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<BuildingRequestPayload> TYPE = new CustomPacketPayload.Type<>(
            Identifier.fromNamespaceAndPath(CityLogistics.MODID, "building_request"));

    public static final StreamCodec<RegistryFriendlyByteBuf, BuildingRequestPayload> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, BuildingRequestPayload::anchor,
            BlockPos.STREAM_CODEC, BuildingRequestPayload::selection,
            BuildingRequestPayload::new
    );
    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type(){ return TYPE; }
}
