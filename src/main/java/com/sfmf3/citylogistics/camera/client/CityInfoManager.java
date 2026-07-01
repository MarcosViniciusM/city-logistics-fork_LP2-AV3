package com.sfmf3.citylogistics.camera.client;

import com.sfmf3.citylogistics.CityLogistics;
import com.sfmf3.citylogistics.blueprint.Blueprint;
import com.sfmf3.citylogistics.blueprint.BlueprintRegistry;
import com.sfmf3.citylogistics.building.BuildingBox;
import com.sfmf3.citylogistics.building.BuildingInformation;
import com.sfmf3.citylogistics.building.BuildingState;
import com.sfmf3.citylogistics.camera.client.ui.CityScreen;
import com.sfmf3.citylogistics.city.City;
import com.sfmf3.citylogistics.network.payload.BlueprintResponsePayload;
import com.sfmf3.citylogistics.network.payload.CityRequestPayload;
import com.sfmf3.citylogistics.network.payload.CityResponsePayload;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Rotation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.EventBusSubscriber;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.sfmf3.citylogistics.camera.CameraController.mc;

@EventBusSubscriber(modid = CityLogistics.MODID, value = Dist.CLIENT)
public class CityInfoManager {
    // needed for most city operations.
    public static BlockPos cityAnchor = null;

    // needed for resource information.
    public static Map<String, Integer> stockCurrent = null;
    public static Map<String, Integer> stockLimits = null;
    public static int pop;
    public static int popcap;


    public static List<BuildingBox> allBuildings = new ArrayList<>();



    // runs when CityBuilderScreen is initialized
    public static void getInformation() {
        mc.player.connection.send(new CityRequestPayload(mc.player.blockPosition()));
    }

    // populates main values
    // maybe separate buildings into its own later
    public static void setCityInfo(CityResponsePayload payload){
        cityAnchor = payload.cityAnchor();
        pop = payload.pop();
        popcap = payload.popcap();
        stockCurrent = payload.stockCurrent();
        stockLimits = payload.stockLimits();
        allBuildings = payload.buildings();

        CityLogistics.LOGGER.info(stockCurrent.toString());
        CityLogistics.LOGGER.info(stockLimits.toString());
        CityScreen.updateResources();
        BlueprintPreview.setBuildings(true);
    }

    public static class BuildingPlacementContext {
        public final String buildingId;
        public final List<String> availablePaths;

        public String selectedPath;
        public BlockPos selectedBlock = BlockPos.ZERO;
        public Rotation selectedRotation = Rotation.NONE;
        public boolean isMirrored = false;
        public Blueprint blueprint = null;

        public BuildingPlacementContext(String buildingId){
            this.buildingId = buildingId;
            this.availablePaths = BlueprintRegistry.getPaths(buildingId);
            this.selectedPath = this.availablePaths.isEmpty() ? "" : this.availablePaths.getFirst();
        }

        public void updateBlueprint(BlueprintResponsePayload payload){
            this.blueprint = payload.blueprint();
        }
    }

}

