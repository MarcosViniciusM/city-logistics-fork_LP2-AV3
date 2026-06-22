package com.sfmf3.citylogistics.blueprint;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import com.mojang.serialization.JsonOps;
import com.sfmf3.citylogistics.CityLogistics;
import net.minecraft.core.Vec3i;
import net.minecraft.resources.RegistryOps;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.LevelResource;
import net.neoforged.fml.ModList;


import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

public class BlueprintIO {
    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .disableHtmlEscaping()
            .create();

    public static void saveToFile(Blueprint blueprint, Level level){
        Path filePath = getDirectory(level).resolve(blueprint.getName() + ".json");
        RegistryOps<JsonElement> ops = level.registryAccess().createSerializationContext(JsonOps.INSTANCE);

        Blueprint.CODEC.encodeStart(ops, blueprint)
                .resultOrPartial(err -> System.err.println("Failed to save blueprint: " + err))
                .ifPresent(json -> {
                    try (var writer = Files.newBufferedWriter(filePath)){
                        GSON.toJson(json, writer);
                    } catch (IOException e){
                        e.printStackTrace();
                    }
                });
    }

    private static Reader getBlueprintReader(String path) {
        try {
            var jarContents = ModList.get().getModFileById(CityLogistics.MODID).getFile().getContents();
            InputStream stream = jarContents.openFile("default_blueprints/" + path);
            if (stream != null) {
                return new InputStreamReader(stream);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Blueprint loadFromFile(String path, Level level) {
        Reader fileReader = getBlueprintReader(path);
        if (fileReader == null) return null;

        try (Reader reader = fileReader) {
            JsonElement json = JsonParser.parseReader(reader);
            RegistryOps<JsonElement> ops = level.registryAccess().createSerializationContext(JsonOps.INSTANCE);

            return Blueprint.CODEC.parse(ops, json)
                    .getOrThrow(err -> new RuntimeException("Failed to parse Blueprint JSON: " + err));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Path getDirectory(Level level){
        Path path = level.getServer().getWorldPath(LevelResource.ROOT).resolve("blueprints");

        try{
            Files.createDirectories(path);
        } catch (IOException e){
            e.printStackTrace();
        }

        return path;
    }

    public static Vec3i getDimensions(String path) {
        Reader fileReader = getBlueprintReader(path);
        if (fileReader == null) return Vec3i.ZERO;

        try (JsonReader reader = new JsonReader(fileReader)) {
            reader.beginObject();
            while (reader.hasNext()) {
                String fieldName = reader.nextName();

                if (fieldName.equals("dimensions")) {
                    reader.beginArray();
                    int x = reader.nextInt();
                    int y = reader.nextInt();
                    int z = reader.nextInt();
                    reader.endArray();

                    return new Vec3i(x, y, z);
                } else {
                    reader.skipValue();
                }
            }
            reader.endObject();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Vec3i.ZERO;
    }
}
