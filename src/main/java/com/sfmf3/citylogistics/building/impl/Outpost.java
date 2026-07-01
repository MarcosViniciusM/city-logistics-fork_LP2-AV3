package com.sfmf3.citylogistics.building.impl;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.sfmf3.citylogistics.building.AbstractBuilding;
import com.sfmf3.citylogistics.building.BuildingState;
import com.sfmf3.citylogistics.building.behavior.IExtraction;
import com.sfmf3.citylogistics.building.behavior.IHousing;
import com.sfmf3.citylogistics.building.behavior.IStorage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Rotation;

import java.util.Map;
import java.util.Set;

public class Outpost extends AbstractBuilding implements IHousing, IExtraction, IStorage {
    private final int housingCapacity;
    private final int workerCapacity;
    private final int storageCapacity;
    private final double extractionRate;
    private final String extractedResource;


    @Override
    public Map<String, Integer> getBuildingCosts() {
        return Map.of();
    }

    public Outpost(ServerLevel level, BlockPos origin, Vec3i dimensions, String path, Rotation rot, boolean mirror) {
        super(level, origin, dimensions, path, rot, mirror);
        this.workerCapacity = calculateCapacity(dimensions, 5, 0.5);
        this.housingCapacity = calculateCapacity(dimensions, 10, 1);
        this.storageCapacity = calculateCapacity(dimensions, 50, 5);
        this.extractionRate = 1;
        this.extractedResource = "food";
    }

    protected Outpost(String path, BlockPos origin, Vec3i dimensions, BuildingState state, Rotation rot, boolean mirror, int workerCapacity, int housingCapacity, int storageCapacity) {
        super(origin, dimensions, path, state, rot, mirror);
        this.workerCapacity = workerCapacity;
        this.housingCapacity = housingCapacity;
        this.storageCapacity = storageCapacity;
        this.extractionRate = 1;
        this.extractedResource = "food";
    }


    public static final MapCodec<Outpost> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.STRING.fieldOf("path").forGetter(Outpost::getPath),
            BlockPos.CODEC.fieldOf("origin").forGetter(Outpost::getOrigin),
            Vec3i.CODEC.fieldOf("dimensions").forGetter(Outpost::getDimensions),
            BuildingState.CODEC.fieldOf("state").forGetter(Outpost::getState),
            Rotation.CODEC.fieldOf("rotation").forGetter(Outpost::getRotation),
            Codec.BOOL.fieldOf("mirror").forGetter(Outpost::getMirrored),
            Codec.INT.fieldOf("workerCapacity").forGetter(Outpost::getExtractionWorkerCapacity),
            Codec.INT.fieldOf("housingCapacity").forGetter(Outpost::getHousingCapacity),
            Codec.INT.fieldOf("storageCapacity").forGetter(Outpost::getMaxStorage)
    ).apply(instance, Outpost::new));

    @Override
    public String getBuildingID() {
        return "outpost";
    }

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

    @Override
    public int getHousingCapacity() {
        return housingCapacity;
    }

    @Override
    public int getMaxStorage() {
        return storageCapacity;
    }

    @Override
    public Set<String> getAllowedResources() {
        return Set.of(
                "food", "wood", "stone"
        );
    }
}
