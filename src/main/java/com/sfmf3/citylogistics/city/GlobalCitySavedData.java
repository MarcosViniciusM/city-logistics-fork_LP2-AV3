package com.sfmf3.citylogistics.city;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.sfmf3.citylogistics.CityLogistics;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

import java.util.HashMap;
import java.util.Map;

public class GlobalCitySavedData extends SavedData {
    private final Map<BlockPos, City> ACTIVE_CITIES;

    public GlobalCitySavedData(){ this.ACTIVE_CITIES = new HashMap<>(); }
    public GlobalCitySavedData(Map<BlockPos, City> activeCities) {
        this.ACTIVE_CITIES = new HashMap<>(activeCities);
    }

    private static final Identifier ID = Identifier.fromNamespaceAndPath(CityLogistics.MODID, "cities");

    // honestly next time i'm saving the coordinates as its own position record
    // of which i can apply custom distance logic to disregard Y levels
    // but you forge the chains you wear in life etc etc
    public static final Codec<GlobalCitySavedData> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.unboundedMap(Codec.STRING, City.CODEC)
                            .xmap(
                                    map -> {
                                        Map<BlockPos, City> result = new HashMap<>();
                                        map.forEach((k, v) -> {
                                            String[] p = k.split(",");
                                            result.put(new BlockPos(Integer.parseInt(p[0]), Integer.parseInt(p[1]), Integer.parseInt(p[2])), v);
                                        });
                                        return result;
                                    },
                                    map -> {
                                        Map<String, City> result = new HashMap<>();
                                        map.forEach((k, v) -> result.put(k.getX() + "," + k.getY() + "," + k.getZ(), v));
                                        return result;
                                    }
                            ).fieldOf("cities").forGetter(GlobalCitySavedData::getCities)
            ).apply(instance, GlobalCitySavedData::new)
    );

    public static final SavedDataType<GlobalCitySavedData> TYPE = new SavedDataType<>(
            ID,
            provider -> new GlobalCitySavedData(),
            provider -> CODEC,
            DataFixTypes.LEVEL
    );

    public Map<BlockPos, City> getCities() {
        return ACTIVE_CITIES;
    }
}
