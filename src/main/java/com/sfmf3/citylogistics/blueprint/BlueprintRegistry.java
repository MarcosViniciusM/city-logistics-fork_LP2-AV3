package com.sfmf3.citylogistics.blueprint;

import com.sfmf3.citylogistics.CityLogistics;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.neoforged.fml.ModList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class BlueprintRegistry {
    private static final Map<String, List<String>> REGISTRY = new HashMap<>();

    public static void scanBlueprints(ResourceManager manager){
        REGISTRY.clear();

        Map<Identifier, Resource> resources = manager.listResources("blueprints",
                location -> location.getPath().endsWith(".json"));

        for(Identifier location : resources.keySet()){
            if(!location.getNamespace().equals("citylogistics")) continue;

            String path = location.getPath();
            String relativePath = path.substring("blueprints/".length());
            int firstSlash = relativePath.indexOf('/');
            if(firstSlash != -1){
                String category = relativePath.substring(0, firstSlash);
                String fileName = relativePath.substring(firstSlash + 1);
                REGISTRY.computeIfAbsent(category, k -> new ArrayList<>()).add(fileName);
                CityLogistics.LOGGER.info("Loaded {}/{}", category, fileName);
            }
        }
    }

    public static List<String> getPaths(String category){
        return REGISTRY.getOrDefault(category, List.of());
    }
}
