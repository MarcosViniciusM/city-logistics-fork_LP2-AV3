package com.sfmf3.citylogistics.building.behavior;

import java.util.Map;

public interface IProduction {

    int getProductionWorkerCapacity();

    Map<String, Integer> getRawMaterials();
    Map<String, Integer> getProducedMaterials();
}
