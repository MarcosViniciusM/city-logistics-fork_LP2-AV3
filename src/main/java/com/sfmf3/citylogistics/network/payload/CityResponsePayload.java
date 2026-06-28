package com.sfmf3.citylogistics.network.payload;

import com.sfmf3.citylogistics.CityLogistics;
import com.sfmf3.citylogistics.camera.client.CityInfoManager;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


public record CityResponsePayload(
        BlockPos cityAnchor,
        Map<String, Integer> stockCurrent,
        Map<String, Integer> stockLimits,
        int pop, int popcap,
        List<CityInfoManager.BuildingBox> buildings
) implements CustomPacketPayload {
    public static final Type<CityResponsePayload> TYPE = new Type<>(Identifier.fromNamespaceAndPath(CityLogistics.MODID, "city_response"));

    public static final StreamCodec<RegistryFriendlyByteBuf, CityResponsePayload> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, CityResponsePayload::cityAnchor,
            ByteBufCodecs.map(HashMap::new, ByteBufCodecs.STRING_UTF8, ByteBufCodecs.INT), CityResponsePayload::stockCurrent,
            ByteBufCodecs.map(HashMap::new, ByteBufCodecs.STRING_UTF8, ByteBufCodecs.INT), CityResponsePayload::stockLimits,
            ByteBufCodecs.INT, CityResponsePayload::pop,
            ByteBufCodecs.INT, CityResponsePayload::popcap,
            CityInfoManager.BuildingBox.STREAM_CODEC.apply(ByteBufCodecs.list()), CityResponsePayload::buildings,
            CityResponsePayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
