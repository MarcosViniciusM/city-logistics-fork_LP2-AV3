package com.sfmf3.citylogistics.blueprint;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import com.mojang.serialization.JsonOps;
import com.sfmf3.citylogistics.CityLogistics;
import com.sfmf3.citylogistics.network.CityOperationException;
import net.minecraft.core.Vec3i;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.RegistryOps;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.entity.player.Input;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.LevelResource;
import net.neoforged.fml.ModList;


import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class BlueprintIO {
    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .disableHtmlEscaping()
            .create();

    // i miss creating your own blueprints from scratch
    public static Blueprint loadFromFile(String path, Level level) {
        Identifier location = Identifier.fromNamespaceAndPath(CityLogistics.MODID, "blueprints/"+path);

        try{
            ResourceManager manager = level.getServer().getResourceManager();
            Resource resource = manager.getResource(location)
                    .orElseThrow(() -> new RuntimeException("Blueprint file missing at: " + location));

            try (Reader reader = resource.openAsReader()){
                JsonElement json = JsonParser.parseReader(reader);
                RegistryOps<JsonElement> ops = level.registryAccess().createSerializationContext(JsonOps.INSTANCE);

                return Blueprint.CODEC.parse(ops, json)
                        .getOrThrow(err -> new RuntimeException("Failed to parse Blueprint JSON: " + err));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
