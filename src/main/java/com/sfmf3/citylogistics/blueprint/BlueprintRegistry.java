package com.sfmf3.citylogistics.blueprint;

import com.sfmf3.citylogistics.CityLogistics;
import net.neoforged.fml.ModList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class BlueprintRegistry {
    private static final Map<String, List<String>> REGISTRY = new HashMap<>();

    public static void scanBlueprintsFromJar(){
        REGISTRY.clear();

        var jarContents = ModList.get()
                .getModFileById(CityLogistics.MODID)
                .getFile()
                .getContents();

        jarContents.visitContent("default_blueprints", (path, resource) -> {
            if(path.endsWith(".json")){
                String relativePath = path.substring("default_blueprints/".length());

                int firstSlash = relativePath.indexOf('/');
                if(firstSlash != -1){
                    String category = relativePath.substring(0, firstSlash);
                    String fileName = relativePath.substring(firstSlash + 1);
                    REGISTRY.computeIfAbsent(category, k -> new ArrayList<>()).add(fileName);
                    CityLogistics.LOGGER.info("Loaded {}/{}", category, fileName);
                }
            }
        });
    }

    public static List<String> getPaths(String category){
        return REGISTRY.getOrDefault(category, List.of());
    }
}
