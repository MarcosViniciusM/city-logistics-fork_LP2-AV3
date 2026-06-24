package com.sfmf3.citylogistics.camera.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.sfmf3.citylogistics.CityLogistics;
import com.sfmf3.citylogistics.blueprint.Blueprint;
import com.sfmf3.citylogistics.blueprint.BlueprintIO;
import com.sfmf3.citylogistics.city.CityManager;
import com.sfmf3.citylogistics.network.CityOperationException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ShapeRenderer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

import java.util.Map;


@EventBusSubscriber(modid = CityLogistics.MODID, value = Dist.CLIENT)
public class BlueprintPreview {

    private static boolean active = false;
    private static boolean selected = false;
    private static boolean buildings = false;


    public static BlockPos selectedBlock = null;
    public static Blueprint selectedBlueprint = null;
    public static String selectedBuildingId = "";
    public static String selectedPath = "";
    public static Rotation selectedRotation = Rotation.NONE;
    public static boolean isMirrored = false;

    public static void update(BlockPos pos, String id, String path, Rotation rot, boolean mirror) {
        selectedBlock = pos;
        selectedBuildingId = id;
        selectedPath = path;
        selectedRotation = rot;
        isMirrored = mirror;
    }

    public static void updateBlueprint(Blueprint blueprint){
        selectedBlueprint = blueprint;
    }

    public static void clear() {
        selectedBlock = null;
        selectedPath = "";
    }

    // works, but having issues with blueprint building box detection and display.
    // probably related to the 3 different methods of rotation we are using
    // and the getBuildingBox function which is horribly overtuned
    @SubscribeEvent
    public static void onRenderLevel(RenderLevelStageEvent.AfterTranslucentBlocks event) {
        Minecraft mc = Minecraft.getInstance();
        Vec3 camPos = mc.gameRenderer.getMainCamera().position();

        PoseStack poseStack = event.getPoseStack();
        VertexConsumer buffer = mc.renderBuffers().bufferSource().getBuffer(RenderTypes.lines());
        VoxelShape cube = Shapes.block();

        if(buildings){
            for(CityClientInfo.BuildingBox box : CityClientInfo.allBuildings){
                poseStack.pushPose();

                handlePositioning(poseStack, camPos, box.origin(), box.rotation(), box.mirrored());

                ShapeRenderer.renderShape(poseStack, buffer,
                        Shapes.box(
                                0, 0, 0,
                                box.dimensions().getX(),
                                box.dimensions().getY(),
                                box.dimensions().getZ()
                        ), 0, 0, 0,
                        0xFFFFFFFF, 5.0F
                );
                poseStack.popPose();
            }
        }
        if(selected){
            poseStack.pushPose();
            if (selectedBlock == null || selectedPath == null || selectedPath.isEmpty()) return;
            if (mc.level == null || mc.player == null) return;
            if(selectedBlueprint == null) return;

            handlePositioning(poseStack, camPos, selectedBlock, selectedRotation, isMirrored);

            // render building bounding box
            ShapeRenderer.renderShape(poseStack, buffer,
                    Shapes.box(
                            0, 0, 0,
                            selectedBlueprint.getDimensions().getX(),
                            selectedBlueprint.getDimensions().getY(),
                            selectedBlueprint.getDimensions().getZ())
                    , 0, 0, 0, 0xFFFFFFFF, 5.0F);

            for (Map.Entry<BlockPos, BlockState> entry : selectedBlueprint.getBlockData().entrySet()) {
                BlockPos localPos = entry.getKey();
                BlockState state = entry.getValue();
                if (!state.isAir()) {
                    ShapeRenderer.renderShape(poseStack, buffer, cube, localPos.getX(), localPos.getY(), localPos.getZ(), 0xFFFF0000, 1.0f);
                }
            }
            poseStack.popPose();
        }

        mc.renderBuffers().bufferSource().endBatch(RenderTypes.lines());
    }

    private static float getRotationDegrees(Rotation rot) {
        return switch (rot) {
            case CLOCKWISE_90 -> 90.0f;
            case CLOCKWISE_180 -> 180.0f;
            case COUNTERCLOCKWISE_90 -> 270.0f;
            default -> 0.0f;
        };
    }

    private static void setOrigin(PoseStack poseStack, Vec3 cameraPos, BlockPos origin){
        poseStack.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);
        poseStack.translate(
                origin.getX(),
                origin.getY(),
                origin.getZ()
        );
    }
    private static void setRot(PoseStack poseStack, Rotation rotation){
        float rotDegrees = getRotationDegrees(rotation);
        poseStack.mulPose(Axis.YP.rotationDegrees(rotDegrees));
    }
    private static void setMirror(PoseStack poseStack, boolean isMirrored){
        if(isMirrored) poseStack.scale(-1.0f, 1.0f, 1.0f);
    }

    private static void handlePositioning(PoseStack poseStack, Vec3 cameraPos, BlockPos origin, Rotation rotation, boolean mirrored){

        setOrigin(poseStack, cameraPos, origin);
        setRot(poseStack, rotation);
        setMirror(poseStack, mirrored);

    }

    public static void toggleActive(){ active = !active;}
    public static void setActive(boolean state){ active = state;}

    public static void toggleSelected(){selected = !selected; }
    public static void setSelected(boolean state){ selected = state; }

    public static void toggleBuildings(){buildings = !buildings;}
    public static void setBuildings(boolean state) {buildings = state;}
}
