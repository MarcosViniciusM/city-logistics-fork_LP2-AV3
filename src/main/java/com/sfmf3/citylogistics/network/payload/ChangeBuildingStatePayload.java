package com.sfmf3.citylogistics.network.payload;

import com.sfmf3.citylogistics.CityLogistics;
import com.sfmf3.citylogistics.building.BuildingState;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record ChangeBuildingStatePayload(
        BlockPos cityAnchor,
        BlockPos buildingAnchor,
        BuildingState state
) implements CustomPacketPayload {

    public static Type<ChangeBuildingStatePayload> TYPE = new Type<>(
            Identifier.fromNamespaceAndPath(CityLogistics.MODID, "change_building_state"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ChangeBuildingStatePayload> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, ChangeBuildingStatePayload::cityAnchor,
            BlockPos.STREAM_CODEC, ChangeBuildingStatePayload::buildingAnchor,
            ByteBufCodecs.fromCodec(BuildingState.CODEC), ChangeBuildingStatePayload::state,
            ChangeBuildingStatePayload::new
    );

    public Type<? extends CustomPacketPayload> type() { return TYPE; }
}