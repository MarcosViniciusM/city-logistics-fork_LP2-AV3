package com.sfmf3.citylogistics.building;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import org.lwjgl.system.ffm.mapping.Mapping;

import java.util.HashMap;
import java.util.Map;

public class BuildingInformation {

    public BuildingBox box;
    public String buildingId;
    public BuildingState state;

    public Map<String, Integer> output = new HashMap<>();
    public Map<String, Integer> input = new HashMap<>();
    public Map<String, Integer> storage = new HashMap<>();
    public int housing = 0;
    public int workers = 0;

    public BuildingInformation(){}

    public BuildingInformation(BuildingBox box, String buildingId, BuildingState state,
                               Map<String, Integer> output, Map<String, Integer> input,
                               Map<String, Integer> storage, int housing, int workers) {
        this.box = box;
        this.buildingId = buildingId;
        this.state = state;
        this.output = output;
        this.input = input;
        this.storage = storage;
        this.housing = housing;
        this.workers = workers;
    }

    public static final StreamCodec<RegistryFriendlyByteBuf, BuildingInformation> STREAM_CODEC = StreamCodec.composite(
            BuildingBox.STREAM_CODEC, BuildingInformation::getBox,
            ByteBufCodecs.STRING_UTF8, BuildingInformation::getBuildingId,
            ByteBufCodecs.fromCodec(BuildingState.CODEC), BuildingInformation::getState,
            ByteBufCodecs.map(HashMap::new, ByteBufCodecs.STRING_UTF8, ByteBufCodecs.INT), BuildingInformation::getOutput,
            ByteBufCodecs.map(HashMap::new, ByteBufCodecs.STRING_UTF8, ByteBufCodecs.INT), BuildingInformation::getInput,
            ByteBufCodecs.map(HashMap::new, ByteBufCodecs.STRING_UTF8, ByteBufCodecs.INT), BuildingInformation::getStorage,
            ByteBufCodecs.INT, BuildingInformation::getHousing,
            ByteBufCodecs.INT, BuildingInformation::getWorkers,
            BuildingInformation::new
    );

    public BuildingBox getBox() {
        return box;
    }

    public String getBuildingId() {
        return buildingId;
    }

    public BuildingState getState() {
        return state;
    }

    public Map<String, Integer> getOutput() {
        return output;
    }

    public Map<String, Integer> getInput() {
        return input;
    }

    public Map<String, Integer> getStorage() {
        return storage;
    }

    public int getHousing() {
        return housing;
    }

    public int getWorkers() {
        return workers;
    }
}
