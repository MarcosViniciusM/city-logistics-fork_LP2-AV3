package com.sfmf3.citylogistics.building.behavior;

import java.util.Set;

public interface IStorage {
    int getMaxStorage();

    Set<String> getAllowedResources();
}
