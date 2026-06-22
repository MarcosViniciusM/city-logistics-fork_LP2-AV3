package com.sfmf3.citylogistics.network.payload;

import com.sfmf3.citylogistics.CityLogistics;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Rotation;

public record AddBuildingPayload(
        BlockPos cityAnchor,
        BlockPos origin,
        String path,
        Rotation rot,
        boolean mirrored,
        String buildingId
) implements CustomPacketPayload {

    public static final Type<AddBuildingPayload> TYPE = new Type<>(
            Identifier.fromNamespaceAndPath(CityLogistics.MODID, "add_building"));

    public static final StreamCodec<RegistryFriendlyByteBuf, AddBuildingPayload> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, AddBuildingPayload::cityAnchor,
            BlockPos.STREAM_CODEC, AddBuildingPayload::origin,
            ByteBufCodecs.STRING_UTF8, AddBuildingPayload::path,
            Rotation.STREAM_CODEC, AddBuildingPayload::rot,
            ByteBufCodecs.BOOL, AddBuildingPayload::mirrored,
            ByteBufCodecs.STRING_UTF8, AddBuildingPayload::buildingId,
            AddBuildingPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type(){ return TYPE; }

}
