package com.sfmf3.citylogistics.building.impl;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.sfmf3.citylogistics.building.AbstractBuilding;
import com.sfmf3.citylogistics.building.BuildingState;
import com.sfmf3.citylogistics.building.behavior.IExtraction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Rotation;

import java.util.Map;

public class Lumbermill extends AbstractBuilding implements IExtraction {
    private final int workerCapacity;

    public Lumbermill(Level level, BlockPos origin, Vec3i dimensions, String path, Rotation rot, boolean mirrored) {
        super(level, origin, dimensions, path, rot, mirrored);
        workerCapacity = calculateCapacity(dimensions, 30, 0.5);
    }

    protected Lumbermill(BlockPos origin, Vec3i dimensions, String path, BuildingState state, Rotation rot, boolean mirrored, int capacity) {
        super(origin, dimensions, path, state, rot, mirrored);
        workerCapacity = capacity;
    }

    public static final MapCodec<Lumbermill> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            BlockPos.CODEC.fieldOf("origin").forGetter(Lumbermill::getOrigin),
            Vec3i.CODEC.fieldOf("dimensions").forGetter(Lumbermill::getDimensions),
            Codec.STRING.fieldOf("path").forGetter(Lumbermill::getPath),
            BuildingState.CODEC.fieldOf("state").forGetter(Lumbermill::getState),
            Rotation.CODEC.fieldOf("rotation").forGetter(Lumbermill::getRotation),
            Codec.BOOL.fieldOf("mirrored").forGetter(Lumbermill::getMirrored),
            Codec.INT.fieldOf("extractor capacity").forGetter(Lumbermill::getExtractionWorkerCapacity)
    ).apply(instance, Lumbermill::new));

    @Override
    public Map<String, Integer> getBuildingCosts() {
        return Map.of(
                "stone", 5 * workerCapacity,
                "wood", 5 * workerCapacity
        );
    }

    @Override
    public String getBuildingID() {
        return "lumbermill";
    }

    @Override
    public int getExtractionWorkerCapacity() {
        return this.workerCapacity;
    }

    @Override
    public double getExtractionWorkerRate() {
        return 1;
    }

    @Override
    public String getExtractedResource() {
        return "wood";
    }
}
