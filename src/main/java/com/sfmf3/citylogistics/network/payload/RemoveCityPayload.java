package com.sfmf3.citylogistics.network.payload;

import com.sfmf3.citylogistics.CityLogistics;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record RemoveCityPayload(
        BlockPos cityAnchor
) implements CustomPacketPayload {
    public static final Type<RemoveCityPayload> TYPE = new Type<>(
            Identifier.fromNamespaceAndPath(CityLogistics.MODID, "remove_city"));

    public static final StreamCodec<RegistryFriendlyByteBuf, RemoveCityPayload> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, RemoveCityPayload::cityAnchor,
            RemoveCityPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type(){ return TYPE; }
}

