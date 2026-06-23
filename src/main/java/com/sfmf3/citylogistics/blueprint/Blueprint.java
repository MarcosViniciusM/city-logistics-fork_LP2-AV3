package com.sfmf3.citylogistics.blueprint;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class Blueprint {
    private final String name;
    private final Vec3i dimensions;
    private final Map<BlockPos, BlockState> blockData;

    public Blueprint(String name, Vec3i dimensions){
        this.name = name;
        this.dimensions = dimensions;
        this.blockData = new HashMap<>();
    }

    public Blueprint(String name, Vec3i dimensions, Map<BlockPos, BlockState> blockData){
        this.name = name;
        this.dimensions = dimensions;
        this.blockData = blockData;
    }

    public void addBlock(BlockPos relativePos, BlockState state){
        this.blockData.put(relativePos, state);
    }

    public static final Codec<Blueprint> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("name").forGetter(Blueprint::getName),
            Vec3i.CODEC.fieldOf("dimensions").forGetter(Blueprint::getDimensions),
            Codec.unboundedMap(Codec.STRING, BlockState.CODEC)
                    .xmap(
                            map -> {
                                Map<BlockPos, BlockState> result = new HashMap<>();
                                map.forEach((k, v) -> {
                                    String[] p = k.split(",");
                                    result.put(new BlockPos(Integer.parseInt(p[0]), Integer.parseInt(p[1]), Integer.parseInt(p[2])), v);
                                });
                                return result;
                            },
                            map -> {
                                Map<String, BlockState> result = new HashMap<>();
                                map.forEach((k, v) -> result.put(k.getX() + "," + k.getY() + "," + k.getZ(), v));
                                return result;
                            }
                    ).fieldOf("blocks").forGetter(Blueprint::getBlockData)
            ).apply(instance, Blueprint::new)
    );

    public String getName() { return name; }
    public Vec3i getDimensions() { return dimensions; }
    public Map<BlockPos, BlockState> getBlockData(){
        return Collections.unmodifiableMap(blockData);
    }
}
