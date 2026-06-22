package com.sfmf3.citylogistics.city;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.sfmf3.citylogistics.building.AbstractBuilding;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;

import java.util.*;

public class City {
    private String name;
    private List<UUID> editors;
    private BlockPos anchor;

    private Map<String, Integer> stockpileMax;
    private Map<String, Integer> stockpileCurrent;
    private Map<BlockPos, AbstractBuilding> buildings;
    private int population;
    private int populationCap;

    // called by creating new city from block
    public City(String name, BlockPos anchor, UUID owner){
        this.name = name; this.anchor = anchor; editors = new ArrayList<>(); editors.add(owner);
        stockpileMax = new HashMap<>();
        stockpileCurrent = new HashMap<>();
        buildings = new HashMap<>();
    }

    // called by codec
    public City(String name, BlockPos anchor, List<UUID> editors, Map<String, Integer> stLim, Map<String, Integer> stCur, Map<BlockPos, AbstractBuilding> buildings, int population, int popCap){
        this.name = name;
        this.anchor = anchor;
        this.editors = new ArrayList<>(editors);
        this.stockpileMax = new HashMap<>(stLim);
        this.stockpileCurrent = new HashMap<>(stCur);
        this.buildings = new HashMap<>(buildings);
        this.population = population;
        this.populationCap = popCap;
    }

    public boolean canEdit(UUID playerUUID){
        return this.editors.contains(playerUUID);
    }

    public static final Codec<City> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("name").forGetter(City::getName),
            BlockPos.CODEC.fieldOf("position").forGetter(City::getAnchor),
            Codec.list(UUIDUtil.STRING_CODEC).fieldOf("editors").forGetter(City::getEditors),
            Codec.unboundedMap(Codec.STRING, Codec.INT).fieldOf("stock limits").forGetter(City::getStockpileMax),
            Codec.unboundedMap(Codec.STRING, Codec.INT).fieldOf("stock current").forGetter(City::getStockpileCurrent),
            Codec.unboundedMap(Codec.STRING, AbstractBuilding.CODEC)
                    .xmap( // blame mojang for not adding blockpos codecs
                           // or me for not noticing sooner
                            map -> {
                                Map<BlockPos, AbstractBuilding> result = new HashMap<>();
                                map.forEach((k, v) -> {
                                    String[] p = k.split(",");
                                    result.put(new BlockPos(
                                            Integer.parseInt(p[0]),
                                            Integer.parseInt(p[1]),
                                            Integer.parseInt(p[2])), v);
                                });
                                return result;
                            },
                            map -> {
                                Map<String, AbstractBuilding> result = new HashMap<>();
                                map.forEach((k, v) -> result.put(k.getX() + "," + k.getY() + "," + k.getZ(), v));
                                return result;
                            }

                    ).fieldOf("buildings").forGetter(City::getBuildings),
            Codec.INT.fieldOf("population").forGetter(City::getPopulation),
            Codec.INT.fieldOf("population cap").forGetter(City::getPopulationCap)
            ).apply(instance, City::new));

    public String getName(){return name;}
    public BlockPos getAnchor(){return anchor;}
    public List<UUID> getEditors() {return editors;}
    public Map<String, Integer> getStockpileMax() {return stockpileMax;}
    public Map<String, Integer> getStockpileCurrent() {return stockpileCurrent;}
    public Map<BlockPos, AbstractBuilding> getBuildings() {return buildings;}
    public int getPopulation() { return population; }
    public void setPopulation(int pop) { this.population = pop; }
    public int getPopulationCap() {return populationCap; }
    public void setPopulationCap(int cap) {this.populationCap = cap;}
}
