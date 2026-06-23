package com.sfmf3.citylogistics.network.payload;

import com.sfmf3.citylogistics.CityLogistics;
import com.sfmf3.citylogistics.blueprint.Blueprint;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record BlueprintResponsePayload(Blueprint blueprint) implements CustomPacketPayload {
    public static final Type<BlueprintResponsePayload> TYPE = new Type<>(
            Identifier.fromNamespaceAndPath(CityLogistics.MODID, "blueprint_response"));

    public static final StreamCodec<RegistryFriendlyByteBuf, BlueprintResponsePayload> STREAM_CODEC = StreamCodec.composite(
            Blueprint.STREAM_CODEC, BlueprintResponsePayload::blueprint,
            BlueprintResponsePayload::new
    );
    @Override
    public Type<? extends CustomPacketPayload> type() {return TYPE;}
}
