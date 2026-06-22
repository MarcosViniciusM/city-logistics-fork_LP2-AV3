package com.sfmf3.citylogistics.network.payload;

import com.sfmf3.citylogistics.CityLogistics;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record AddCityPayload(
        BlockPos cityAnchor,
        String name
) implements CustomPacketPayload {
    public static final Type<AddCityPayload> TYPE = new Type<>(
            Identifier.fromNamespaceAndPath(CityLogistics.MODID, "add_city"));

    public static final StreamCodec<RegistryFriendlyByteBuf, AddCityPayload> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, AddCityPayload::cityAnchor,
            ByteBufCodecs.STRING_UTF8, AddCityPayload::name,
            AddCityPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
