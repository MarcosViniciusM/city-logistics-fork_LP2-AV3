package com.sfmf3.citylogistics.network.payload;

import com.mojang.serialization.Codec;
import com.sfmf3.citylogistics.CityLogistics;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record AddPopulationPayload(
        BlockPos cityAnchor,
        int count
) implements CustomPacketPayload {

    public static final Type<AddPopulationPayload> TYPE = new Type<>(
            Identifier.fromNamespaceAndPath(CityLogistics.MODID, "add_population"));

    public static final StreamCodec<RegistryFriendlyByteBuf, AddPopulationPayload> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, AddPopulationPayload::cityAnchor,
            ByteBufCodecs.INT, AddPopulationPayload::count,
            AddPopulationPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type(){ return TYPE; }
}
