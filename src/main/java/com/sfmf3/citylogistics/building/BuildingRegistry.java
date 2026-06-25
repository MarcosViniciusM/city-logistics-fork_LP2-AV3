package com.sfmf3.citylogistics.building;

import com.mojang.serialization.MapCodec;
import com.sfmf3.citylogistics.building.impl.Lumbermill;
import com.sfmf3.citylogistics.building.impl.Mine;
import com.sfmf3.citylogistics.building.impl.Outpost;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Rotation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BuildingRegistry {
    public static final Map<String, BuildingType<?>> BUILDINGS = new HashMap<>();

    static {
        BUILDINGS.put("mine", new BuildingType<>(Mine::new, Mine.CODEC));
        BUILDINGS.put("lumbermill", new BuildingType<>(Lumbermill::new, Lumbermill.CODEC));
        BUILDINGS.put("outpost", new BuildingType<>(Outpost::new, Outpost.CODEC));
    }

    public static MapCodec<? extends AbstractBuilding> getCodec(String id){
        BuildingType<?> type = BUILDINGS.get(id);
        if(type == null){
            throw new IllegalArgumentException("Unknown building type identifier!" + id);
        }
        return type.codec();
    }

    public record BuildingType<T extends AbstractBuilding>(
            TriFunction<ServerLevel, BlockPos, Vec3i, String, Rotation, Boolean, T> factory,
            MapCodec<T> codec
    ){
        @FunctionalInterface
        public interface TriFunction<A, B, C, D, E, F, R> { R apply (A a, B b, C c, D d, E e, F f); }
    }

    public record BuildingDefinition(String buildingId, String displayName, String category){}
    public static final List<BuildingDefinition> BUILDING_REGISTRY = List.of(
            new BuildingDefinition("mine", "Stone Quarry", "Extractors"),
            new BuildingDefinition("lumbermill", "Lumbermill", "Extractors"),
            new BuildingDefinition("outpost", "Outpost", "Primary")
    );
}

