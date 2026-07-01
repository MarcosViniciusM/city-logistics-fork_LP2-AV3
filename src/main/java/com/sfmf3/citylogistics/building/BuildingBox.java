package com.sfmf3.citylogistics.building;

import com.sfmf3.citylogistics.camera.client.CityInfoManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.level.block.Rotation;

import java.util.List;

public record BuildingBox(
        BlockPos origin,
        Vec3i dimensions,
        Rotation rotation,
        boolean mirrored,
        String buildingId,
        BuildingState state) {
    public static final StreamCodec<RegistryFriendlyByteBuf, BuildingBox> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, BuildingBox::origin,
            Vec3i.STREAM_CODEC, BuildingBox::dimensions,
            Rotation.STREAM_CODEC, BuildingBox::rotation,
            ByteBufCodecs.BOOL, BuildingBox::mirrored,
            ByteBufCodecs.STRING_UTF8, BuildingBox::buildingId,
            ByteBufCodecs.fromCodec(BuildingState.CODEC), BuildingBox::state,
            BuildingBox::new
    );
    public static final StreamCodec<RegistryFriendlyByteBuf, List<BuildingBox>> LIST_CODEC =
            BuildingBox.STREAM_CODEC.apply(ByteBufCodecs.list());
}
