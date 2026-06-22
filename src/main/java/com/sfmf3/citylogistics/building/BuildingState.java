package com.sfmf3.citylogistics.building;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringRepresentable;

import java.util.Locale;

public enum BuildingState implements StringRepresentable {
    BLUEPRINT,
    UNFINISHED,
    OPERATIONAL;

    public static final Codec<BuildingState> CODEC = StringRepresentable.fromEnum(BuildingState::values);

    @Override
    public String getSerializedName() {
        return this.name().toLowerCase(Locale.ROOT);
    }
}
