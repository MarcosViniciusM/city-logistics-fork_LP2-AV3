package com.sfmf3.citylogistics.building;

import com.mojang.serialization.Codec;
import com.sfmf3.citylogistics.blueprint.Blueprint;
import com.sfmf3.citylogistics.blueprint.BlueprintIO;
import com.sfmf3.citylogistics.building.behavior.IExtraction;
import com.sfmf3.citylogistics.building.behavior.IHousing;
import com.sfmf3.citylogistics.building.behavior.IProduction;
import com.sfmf3.citylogistics.building.behavior.IStorage;
import com.sfmf3.citylogistics.city.City;
import com.sfmf3.citylogistics.city.CityManager;
import com.sfmf3.citylogistics.network.CityOperationException;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Map;

public abstract class AbstractBuilding {
    private final String BLUEPRINT_PATH;
    private final BlockPos ORIGIN;
    private final Vec3i dimensions;
    private BuildingState state;
    private final Rotation rotation;
    private final boolean mirrored;

    // uninitialized variables for building construction
    private Integer currentBuildingY = null;
    private Blueprint currentTarget = null;

    public abstract Map<String, Integer> getBuildingCosts();
    public abstract String getBuildingID();


    // building created from scratch
    public AbstractBuilding(Level level, BlockPos origin, Vec3i dimensions, String path, Rotation rot, boolean mirrored) {
        BLUEPRINT_PATH = path;
        this.ORIGIN = origin;
        this.dimensions = dimensions;
        state = BuildingState.BLUEPRINT;
        this.rotation = rot;
        this.mirrored = mirrored;
    }

    // building created from codec
    protected AbstractBuilding(BlockPos origin, Vec3i dimensions, String path, BuildingState state, Rotation rot, boolean mirrored){
        BLUEPRINT_PATH = path;
        this.ORIGIN = origin;
        this.dimensions = dimensions;
        this.state = state;
        this.rotation = rot;
        this.mirrored = mirrored;
    }

    protected static int calculateCapacity(Vec3i dimensions, int maxValue, double multiplier){
        double footprintValue = dimensions.getX() * dimensions.getZ() * multiplier;

        double heightValue = dimensions.getY() * multiplier * 0.5;

        double total = footprintValue * heightValue;

        return Mth.clamp(Mth.floor(total), 0, maxValue);
    }

    public void tickConstruction(ServerLevel level, City city){
        if(currentTarget == null) { currentTarget = BlueprintIO.loadFromFile(getPath(), level); }
        if(currentBuildingY == null) { findLowestUnfinishedLayer(level); }
        if(currentBuildingY >= this.currentTarget.getDimensions().getY()) { findLowestUnfinishedLayer(level); }
        if(currentBuildingY < 0) {
            setState(BuildingState.OPERATIONAL);
            cleanupConstructionVariables();
            return;
        }

        if(!layerUnfinished(level, currentBuildingY)){ currentBuildingY++; return; }
        if(CityManager.tryConsumeResource(city, getBuildingCosts())){
            fixLayer(level, currentBuildingY++);
        }
    }

    private void findLowestUnfinishedLayer(Level level){
        if(this.currentTarget == null){
            this.currentBuildingY = -1;
            return;
        }

        Vec3i dims = this.currentTarget.getDimensions();
        for(int y = 0;y<dims.getY();y++){
            for(int x = 0;x<dims.getX();x++){
                for(int z = 0;z<dims.getZ();z++){
                    BlockPos relPos = new BlockPos(x, y, z);
                    if(checkBlockState(level, mirrored, rotation, relPos)){
                        this.currentBuildingY = y;
                        return;
                    }
                }
            }
        }
        this.currentBuildingY = -1;
    }

    private boolean layerUnfinished(Level level, int y){
        if(this.currentTarget == null) return false;

        Vec3i dims = this.currentTarget.getDimensions();

        if(y < 0 || y >= dims.getY()) return false;

        for(int x = 0;x<dims.getX();x++){
            for(int z = 0;z< dims.getZ();z++){
                BlockPos relPos = new BlockPos(x, y, z);
                if(checkBlockState(level, mirrored, rotation, relPos)) return true;
            }
        }
        return false;
    }

    private void fixLayer(Level level, int y){
        if(this.currentTarget == null) return;
        Mirror mirrorEnum = mirrored ? Mirror.LEFT_RIGHT : Mirror.NONE;

        Vec3i dims = this.currentTarget.getDimensions();
        for(int x = 0; x < dims.getX();x++){
            for(int z = 0; z < dims.getZ();z++){
                BlockPos relPos = new BlockPos(x, y, z);
                BlockPos worldPos = this.getOrigin().offset(getTransformedOffset(relPos, rotation, mirrored));
                BlockState expected = this.currentTarget.getBlockData()
                        .getOrDefault(relPos, Blocks.AIR.defaultBlockState())
                        .mirror(mirrorEnum)
                        .rotate(level, worldPos, rotation);

                level.setBlock(worldPos, expected, 3);
            }
        }
    }

    private BlockPos getTransformedOffset(BlockPos relPos, Rotation rotation, boolean mirrored) {
        BlockPos finalPos = mirrored ? new BlockPos(relPos.getX(), relPos.getY(), -relPos.getZ()) : relPos;
        return finalPos.rotate(rotation);
    }

    private boolean checkBlockState(Level level, boolean mirrored, Rotation rotation, BlockPos pos){
        BlockPos worldPos = this.getOrigin().offset(getTransformedOffset(pos, rotation, mirrored));
        BlockState actual = level.getBlockState(worldPos);
        Mirror mirrorEnum = mirrored ? Mirror.LEFT_RIGHT : Mirror.NONE;
        BlockState expected = this.currentTarget.getBlockData()
                .getOrDefault(pos, Blocks.AIR.defaultBlockState())
                .mirror(mirrorEnum)
                .rotate(level, pos, rotation);

        return actual.equals(expected);
    }

    private void cleanupConstructionVariables(){
        currentTarget = null;
        currentBuildingY = null;
    }

    public BuildingBox getBox(){
        return new BuildingBox(
                getOrigin(),
                getDimensions(),
                getRotation(),
                getMirrored(),
                getBuildingID()
        );
    }

    // there will be no setters for BuildingState.
    // instead, we will always set it to UNFINISHED.
    // if building was blueprint, it will run the construction steps as its supposed to
    // if building was operational, it will check for any irregularities and run the repairs
    // smart implementation i think
    public void setConstruction(){
        if(this.state == BuildingState.UNFINISHED) throw new CityOperationException("Building already set to unfinished!");
        this.state = BuildingState.UNFINISHED;
    }

    // this is a horrible implementation but whatever
    public BuildingInformation getInformation(){
        var info = new BuildingInformation();
        info.box = getBox();
        info.buildingId = this.getBuildingID();
        info.state = this.state;

        if(this instanceof IExtraction){
            info.input.put(
                    ((IExtraction) this).getExtractedResource(),
                    (int) (((IExtraction) this).getExtractionWorkerRate() * ((IExtraction) this).getExtractionWorkerCapacity())
            );
            info.workers += ((IExtraction) this).getExtractionWorkerCapacity();
        }
        if(this instanceof IProduction){
            info.input.putAll(((IProduction) this).getProducedMaterials());
            info.workers += ((IProduction) this).getProductionWorkerCapacity();
        }
        if(this instanceof IStorage storage){
            for(String resource : storage.getAllowedResources()){
                info.storage.put(resource, storage.getMaxStorage());
            }
        }
        if(this instanceof IHousing){
            info.housing += ((IHousing) this).getHousingCapacity();
        }

        return info;
    }


    public static final Codec<AbstractBuilding> CODEC = Codec.STRING.dispatch(
            AbstractBuilding::getBuildingID,
            BuildingRegistry::getCodec
    );

    public String getPath(){ return BLUEPRINT_PATH; }
    public BlockPos getOrigin() { return ORIGIN; }
    public Vec3i getDimensions(){ return dimensions; }
    protected void setState(BuildingState state) { this.state = state; }
    public BuildingState getState(){ return state; }

    public Rotation getRotation() { return rotation; }
    public boolean getMirrored() { return mirrored; }
}
