package com.sfmf3.citylogistics.city;

import com.sfmf3.citylogistics.CityLogistics;
import com.sfmf3.citylogistics.blueprint.Blueprint;
import com.sfmf3.citylogistics.blueprint.BlueprintIO;
import com.sfmf3.citylogistics.building.AbstractBuilding;
import com.sfmf3.citylogistics.building.BuildingBox;
import com.sfmf3.citylogistics.building.BuildingRegistry;
import com.sfmf3.citylogistics.building.BuildingState;
import com.sfmf3.citylogistics.building.behavior.IExtraction;
import com.sfmf3.citylogistics.building.behavior.IHousing;
import com.sfmf3.citylogistics.building.behavior.IProduction;
import com.sfmf3.citylogistics.building.behavior.IStorage;
import com.sfmf3.citylogistics.network.CityOperationException;
import com.sfmf3.citylogistics.network.payload.BuildingRequestPayload;
import com.sfmf3.citylogistics.network.payload.BuildingResponsePayload;
import com.sfmf3.citylogistics.network.payload.CityResponsePayload;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.*;

public class CityManager {

    public static GlobalCitySavedData getCityData(ServerLevel level){
        return level.getServer().overworld().getDataStorage().computeIfAbsent(GlobalCitySavedData.TYPE);
    }

    public static void tickAllCities(ServerLevel level){
        GlobalCitySavedData data = getCityData(level);
        CityLogistics.LOGGER.info("Attempting to tick all cities.");

        for(City city : data.getCities().values()){
            Collection<AbstractBuilding> allBuildings = city.getBuildings().values();

            List<AbstractBuilding> extraction = new ArrayList<>();
            List<AbstractBuilding> production = new ArrayList<>();
            List<AbstractBuilding> unfinished = new ArrayList<>();

            Map<String, Integer> totalStorage = new HashMap<>();
            int totalHousingCapacity = 0;
            int totalExtractionWorkers = 0;
            int totalProductionWorkers = 0;

            // separate buildings into lists
            for(AbstractBuilding building : allBuildings){
                if(building.getState() == BuildingState.OPERATIONAL){
                    if(building instanceof IExtraction extractor){
                        extraction.add(building);
                        totalExtractionWorkers += extractor.getExtractionWorkerCapacity();
                    }

                    if(building instanceof IProduction productor){
                        production.add(building);
                        totalProductionWorkers += productor.getProductionWorkerCapacity();
                    }

                    if(building instanceof IHousing housing){
                        totalHousingCapacity += housing.getHousingCapacity();
                    }

                    if(building instanceof IStorage storage){
                        storage.getAllowedResources().forEach(resource -> totalStorage.merge(resource, storage.getMaxStorage(), Integer::sum));
                    }
                } else if (building.getState() == BuildingState.UNFINISHED) {
                    unfinished.add(building);
                }
            }
            // quick update
            if(city.getPopulationCap() != totalHousingCapacity){city.setPopulationCap(totalHousingCapacity);}
            city.setStockpileMax(totalStorage);

            // lets civilians go if not enough housing or food
            handleCivilianUpkeep(city);

            // handles majority of resource calculations
            int totalSlots = totalExtractionWorkers + totalProductionWorkers;
            int activeWorkers = Math.min(city.getPopulation(), totalSlots);
            if(totalSlots > 0 && activeWorkers > 0){
                for(AbstractBuilding building : extraction){
                    if(building instanceof IExtraction extractor){
                        int capacity = extractor.getExtractionWorkerCapacity();
                        int workers = (int) Math.round((double) activeWorkers * capacity / totalSlots);
                        handleExtraction(city, extractor, workers);
                    }
                }
            }
            if(totalSlots > 0 && activeWorkers > 0){
                for(AbstractBuilding building : production){
                    if(building instanceof IProduction productor){
                        int capacity = productor.getProductionWorkerCapacity();
                        int workers = (int) Math.round((double) activeWorkers * capacity / totalSlots);
                        handleProduction(city, productor, workers);
                    }
                }
            }

            for(AbstractBuilding building : unfinished){
                AABB box = getRealBuildingBox(
                        building.getOrigin(),
                        building.getDimensions(),
                        building.getRotation(),
                        building.getMirrored());
                BlockPos minPos = BlockPos.containing(box.minX, box.minY, box.minZ);
                BlockPos maxPos = BlockPos.containing(box.maxX, box.maxY, box.maxZ);
                if(!level.hasChunksAt(minPos, maxPos)) continue;
                building.tickConstruction(level, city);
            }

        }
        data.setDirty();
    }

    public static void addCity(ServerLevel level, String name, BlockPos pos, UUID playerUUID){
        GlobalCitySavedData data = getCityData(level);


        for(BlockPos existingPos : data.getCities().keySet()){
            if(pos.closerThan(existingPos, 150)){
                throw new CityOperationException("City placement failed. Too close to existing cities!");
            }
        }
        data.getCities().put(pos, new City(name, pos, playerUUID));
        data.setDirty();
    }

    public static void removeCity(ServerLevel level, BlockPos pos, UUID playerUUID){
        GlobalCitySavedData data = getCityData(level);
        City city = data.getCities().get(pos);

        if(city == null) { return; }

        if(!city.getEditors().contains(playerUUID)){ return; }

        data.getCities().remove(pos);
        data.setDirty();
    }

    public static void addBuilding(ServerLevel level, City city, BlockPos origin, String path, Rotation rot, boolean mirrored, String buildingId){
        BuildingRegistry.BuildingType<?> type = BuildingRegistry.BUILDINGS.get(buildingId);
        if(type == null) { throw new CityOperationException("Unknown building type? " + buildingId); }

        Blueprint blueprint = BlueprintIO.loadFromFile(buildingId+"/"+path, level);
        if(blueprint == null) throw new CityOperationException("Blueprint null!");
        Vec3i dimensions = blueprint.getDimensions();
        AABB boundingBox = getRealBuildingBox(origin, dimensions, rot, mirrored);

        for(BlockPos pos : city.getBuildings().keySet()){
            if(pos.closerThan(origin, 50)){

                AbstractBuilding existing = city.getBuildings().get(pos);
                AABB existingBox = getRealBuildingBox(existing.getOrigin(), existing.getDimensions(), existing.getRotation(), existing.getMirrored());
                if(boundingBox.intersects(existingBox)) throw new CityOperationException
                        (
                                "Building "+origin+" -- "+ boundingBox.getMinPosition()+ ", " + boundingBox.getMaxPosition() +
                                        "intersects with building "+existing.getOrigin()+" -- " + existingBox.getMinPosition() + ", "+ existingBox.getMaxPosition()
                        );
            }
        }
        // if no building intersects, run as normal
        AbstractBuilding building = type.factory().apply(level, origin, dimensions, path, rot, mirrored);
        city.getBuildings().put(origin, building);

    }

    public static void removeBuilding(City city, BlockPos pos){
        city.getBuildings().remove(pos);
    }

    public static void removeBuilding(City city, AbstractBuilding building){
        if(building != null){
            city.getBuildings().remove(building.getOrigin());
        }
    }

    public static boolean tryConsumeResource(City city, String resource, int amount){
        Map<String, Integer> cityStockpile = city.getStockpileCurrent();
        int currentAmount = cityStockpile.getOrDefault(resource, 0);
        if (currentAmount >= amount){
            cityStockpile.put(resource, currentAmount - amount);
            return true;
        }
        return false;
    }

    public static boolean tryConsumeResource(City city, Map<String, Integer> requirements){
        Map<String, Integer> cityStockpile = city.getStockpileCurrent();
        for(Map.Entry<String, Integer> entry : requirements.entrySet()) {
            if(cityStockpile.getOrDefault(entry.getKey(), 0) < entry.getValue()){
                return false;
            }
        }

        for(Map.Entry<String, Integer> entry : requirements.entrySet()) {
            cityStockpile.merge(entry.getKey(), -entry.getValue(), Integer::sum);
        }

        return true;
    }

    public static int tryAddResource(City city, String resource, int amount){
        if (amount <= 0) return 0;

        int current = city.getStockpileCurrent().getOrDefault(resource, 0);
        int max = city.getStockpileMax().getOrDefault(resource, 0);
        int availableSpace = max - current;

        if(availableSpace <= 0) { return amount; }

        if(amount <= availableSpace) { city.getStockpileCurrent().put(resource, current + amount); return 0; }

        city.getStockpileCurrent().put(resource, max);
        return amount - availableSpace;
    }

    public static void addPopulation(City city, int count){
        int pop = city.getPopulation();
        //int requiredFood = (int) ((pop * pop) / ((pop * 4) + 1));
        int requiredFood = 0; // debug for now

        for(int x = 0; x < count; x++){
            if(tryConsumeResource(city, "food", requiredFood)){city.setPopulation(city.getPopulation() + 1);}
            else{ return; }
        }
    }

    public static Map<String, Integer> tryAddResource(City city, Map<String, Integer> amount){
        Map<String, Integer> leftovers = new HashMap<>();

        for(Map.Entry<String, Integer> entry : amount.entrySet()) {
            int leftover = tryAddResource(city, entry.getKey(), entry.getValue());
            if (leftover > 0){
                leftovers.put(entry.getKey(), entry.getValue());
            }
        }

        return leftovers;
    }

    public static void handleCivilianUpkeep(City city){
        int safeCapacity = (int) (city.getPopulationCap() * 1.1);
        int popCount = city.getPopulation();

        if(popCount <= 0) return;
        // if not enough housing
        if(popCount > safeCapacity){
            int unhoused = popCount - safeCapacity;
            int leaving = Math.max(1, unhoused/10);
            city.setPopulation(popCount - leaving);
        }
        // if not enough food
        if(!tryConsumeResource(city, "food", (int)(popCount * 0.25))){
            city.setPopulation(popCount - 2);
        }
    }

    public static void handleExtraction(City city, IExtraction extractor, int workers){
        if(workers <= 0) return;

        int amountProduced = (int) (workers * extractor.getExtractionWorkerRate());
        tryAddResource(city, extractor.getExtractedResource(), amountProduced);
    }

    public static void handleProduction(City city, IProduction productor, int workers){
        if (workers <= 0) return;

        Map<String, Integer> requiredMaterials = new HashMap<>();
        for(Map.Entry<String, Integer> entry : productor.getRawMaterials().entrySet()){
            requiredMaterials.put(entry.getKey(), entry.getValue() * workers);
        }

        if(tryConsumeResource(city, requiredMaterials)){
            Map<String, Integer> producedMaterials = new HashMap<>();
            for(Map.Entry<String, Integer> entry : productor.getProducedMaterials().entrySet()){
                producedMaterials.put(entry.getKey(), entry.getValue() * workers);
            }

            tryAddResource(city, producedMaterials);
        }
    }

    public static BlockPos getRotatedBlock(BlockPos relativePos, Rotation rot, boolean mirrored){
        BlockPos mirrorPos = mirrored ? new BlockPos(relativePos.getX(), relativePos.getY(), -relativePos.getZ()) : relativePos;

        return mirrorPos.rotate(rot);
    }

    public static AABB getRealBuildingBox(BlockPos origin, Vec3i dimensions, Rotation rot, boolean mirrored){
        BlockPos edge = getRotatedBlock(new BlockPos(dimensions).offset(-1, -1, -1), rot, mirrored);

        return AABB.encapsulatingFullBlocks(origin, origin.offset(edge));
    }

    public static BlockPos getClosestCity(BlockPos currentPos, ServerLevel level){
        GlobalCitySavedData data = getCityData(level);


        for(BlockPos existingPos : data.getCities().keySet()){
            if(currentPos.closerThan(existingPos, 80)){ return existingPos;}
        }

        return null;
    }

    // this is just bad form ngl. has some weird interactions when building are too close
    // but it'll work for now
    public static BlockPos getClosestBuilding(City city, BlockPos pos, ServerLevel level){
        return city.getBuildings().keySet().stream()
                .filter(existingPos -> pos.closerThan(existingPos, 15))
                .min(Comparator.comparingDouble(pos::distSqr))
                .orElse(null);
    }

    public static CityResponsePayload returnInfo(BlockPos anchor, UUID playerUUID, ServerLevel level){

        City city = getCityData(level).getCities().get(anchor);
        if(!city.canEdit(playerUUID)) throw new CityOperationException("Player has unauthorized access!");

        List<BuildingBox> boxes = new ArrayList<>();
        for (AbstractBuilding b : city.getBuildings().values()) {
            boxes.add(new BuildingBox(
                    b.getOrigin(),
                    b.getDimensions(),
                    b.getRotation(),
                    b.getMirrored(),
                    b.getBuildingID(),
                    b.getState()
            ));
        }
        return new CityResponsePayload(
                anchor,
                city.getStockpileCurrent(),
                city.getStockpileMax(),
                city.getPopulation(),
                city.getPopulationCap(),
                boxes
        );
    }
    public static BuildingResponsePayload returnInfo(BuildingRequestPayload payload, IPayloadContext context, ServerLevel level){
        City city = getCityData(level).getCities().get(payload.anchor());
        if(!city.canEdit(context.player().getUUID())) throw new CityOperationException("Player has unauthorized access!");

        AbstractBuilding building = city.getBuildings().get(CityManager.getClosestBuilding(city, payload.selection(), level));

        var box = getRealBuildingBox(
                building.getOrigin(),
                building.getDimensions(),
                building.getRotation(),
                building.getMirrored()).inflate(3);

        if(!box.contains(payload.selection().getCenter())) throw new CityOperationException("Couldn't find building!");

        return new BuildingResponsePayload(building.getInformation());

    }
}
