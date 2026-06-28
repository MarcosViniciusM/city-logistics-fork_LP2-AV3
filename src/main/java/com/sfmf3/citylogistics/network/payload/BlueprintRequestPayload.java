package com.sfmf3.citylogistics.network.payload;

import com.sfmf3.citylogistics.CityLogistics;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record BlueprintRequestPayload(String path) implements CustomPacketPayload {

    public static final Type<BlueprintRequestPayload> TYPE = new Type<>(
            Identifier.fromNamespaceAndPath(CityLogistics.MODID, "blueprint_request"));

    public static final StreamCodec<RegistryFriendlyByteBuf, BlueprintRequestPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, BlueprintRequestPayload::path,
            BlueprintRequestPayload::new
    );
    @Override
    public Type<? extends CustomPacketPayload> type(){ return TYPE; }
}
