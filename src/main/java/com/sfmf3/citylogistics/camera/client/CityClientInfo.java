package com.sfmf3.citylogistics.camera.client;

import com.sfmf3.citylogistics.CityLogistics;
import com.sfmf3.citylogistics.blueprint.Blueprint;
import com.sfmf3.citylogistics.building.BuildingState;
import com.sfmf3.citylogistics.network.payload.BlueprintResponsePayload;
import com.sfmf3.citylogistics.network.payload.CityRequestPayload;
import com.sfmf3.citylogistics.network.payload.CityResponsePayload;
import dev.ftb.mods.ftblibrary.client.gui.widget.ScreenWrapper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.level.block.Rotation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.EventBusSubscriber;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.sfmf3.citylogistics.camera.CameraController.mc;

@EventBusSubscriber(modid = CityLogistics.MODID, value = Dist.CLIENT)
public class CityClientInfo {
    // needed for most city operations.
    public static BlockPos cityAnchor = null;

    // needed for resource information.
    public static Map<String, Integer> stockCurrent = null;
    public static Map<String, Integer> stockLimits = null;
    public static int pop;
    public static int popcap;
    public static List<BuildingBox> allBuildings = new ArrayList<>();


    // blueprint. gotten only when going to place buildings
    public static Blueprint selectedBlueprint = null;

    // gotten only when selecting a specific building
    public static SelectedBuildingDetails selectedBuildingDetails = null;

    public static void setBlueprintInfo(BlueprintResponsePayload payload) {
        selectedBlueprint = payload.blueprint();
        if(mc.screen instanceof ScreenWrapper wrapper){
            if(wrapper.getGui() instanceof CityBuilderScreen screen){
                screen.updateBlueprint();
                screen.updatePreviewState();
            }
        }
    }

    public record BuildingBox(
            BlockPos origin,
            Vec3i dimensions,
            Rotation rotation,
            boolean mirrored,
            String buildingId)
    {
        public static final StreamCodec<RegistryFriendlyByteBuf, BuildingBox> STREAM_CODEC = StreamCodec.composite(
                BlockPos.STREAM_CODEC, BuildingBox::origin,
                Vec3i.STREAM_CODEC, BuildingBox::dimensions,
                Rotation.STREAM_CODEC, BuildingBox::rotation,
                ByteBufCodecs.BOOL, BuildingBox::mirrored,
                ByteBufCodecs.STRING_UTF8, BuildingBox::buildingId,
                BuildingBox::new
        );
    }

    public record SelectedBuildingDetails(String buildingId, String blueprintPath, BuildingState state, Map<String, String> details){}

    // runs when CityBuilderScreen is initialized
    public static void getInformation(){
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

        if(mc.screen instanceof ScreenWrapper wrapper){
            if(wrapper.getGui() instanceof CityBuilderScreen screen){
                screen.refreshWidgets();
            }
        }
    }
}
