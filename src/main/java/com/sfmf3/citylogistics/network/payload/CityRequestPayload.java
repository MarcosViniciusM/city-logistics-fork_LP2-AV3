package com.sfmf3.citylogistics.network.payload;

import com.sfmf3.citylogistics.CityLogistics;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;


public record CityRequestPayload(BlockPos playerPos) implements CustomPacketPayload {
    public static final Type<CityRequestPayload> TYPE = new Type<>(Identifier.fromNamespaceAndPath(CityLogistics.MODID, "city_request"));

    public static final StreamCodec<FriendlyByteBuf, CityRequestPayload> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, CityRequestPayload::playerPos,
            CityRequestPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {return TYPE; }
}


