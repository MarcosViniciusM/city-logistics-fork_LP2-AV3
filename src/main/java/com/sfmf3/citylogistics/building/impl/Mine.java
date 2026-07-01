package com.sfmf3.citylogistics.building.impl;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.sfmf3.citylogistics.building.AbstractBuilding;
import com.sfmf3.citylogistics.building.BuildingState;
import com.sfmf3.citylogistics.building.behavior.IExtraction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Rotation;

import java.util.Map;

public class Mine extends AbstractBuilding implements IExtraction {
    private final int workerCapacity;
    private final double extractionRate;
    private final String extractedResource;

    public Mine(ServerLevel level, BlockPos origin, Vec3i dimensions, String path, Rotation rot, boolean mirror) {
        super(level, origin, dimensions, path, rot, mirror);
        this.workerCapacity = calculateCapacity(dimensions, 30, 0.5);
        this.extractionRate = 1;
        this.extractedResource = "stone";
    }

    protected Mine(String path, BlockPos origin, Vec3i dimensions, BuildingState state, Rotation rot, boolean mirror, int workerCapacity) {
        super(origin, dimensions, path, state, rot, mirror);
        this.workerCapacity = workerCapacity;
        this.extractionRate = 1;
        this.extractedResource = "stone";
    }

    @Override
    public Map<String, Integer> getBuildingCosts() {
        return Map.of(
                "stone", 5 * workerCapacity,
                "wood", 15 * workerCapacity
        );
    }

    @Override
    public String getBuildingID() {
        return "mine";
    }

    public static final MapCodec<Mine> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.STRING.fieldOf("path").forGetter(Mine::getPath),
            BlockPos.CODEC.fieldOf("origin").forGetter(Mine::getOrigin),
            Vec3i.CODEC.fieldOf("dimensions").forGetter(Mine::getDimensions),
            BuildingState.CODEC.fieldOf("state").forGetter(Mine::getState),
            Rotation.CODEC.fieldOf("rotation").forGetter(Mine::getRotation),
            Codec.BOOL.fieldOf("mirror").forGetter(Mine::getMirrored),
            Codec.INT.fieldOf("workerCapacity").forGetter(Mine::getExtractionWorkerCapacity)
    ).apply(instance, Mine::new));


    @Override
    public int getExtractionWorkerCapacity() {
        return workerCapacity;
    }

    @Override
    public double getExtractionWorkerRate() {
        return extractionRate;
    }

    @Override
    public String getExtractedResource() {
        return extractedResource;
    }
}
