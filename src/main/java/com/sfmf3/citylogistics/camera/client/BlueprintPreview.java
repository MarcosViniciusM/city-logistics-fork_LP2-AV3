package com.sfmf3.citylogistics.camera.client;

import com.lowdragmc.lowdraglib2.gui.holder.ModularUIScreen;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.sfmf3.citylogistics.CityLogistics;
import com.sfmf3.citylogistics.building.BuildingBox;
import com.sfmf3.citylogistics.camera.client.ui.CityScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ShapeRenderer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.core.BlockPos;
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


    private static boolean buildings = false;

    private static boolean blueprintBox = false;
    private static boolean blueprintBlocks = false;

    // works!
    // real headache. im not doing this again without libraries being ported to 26.x.x
    @SubscribeEvent
    public static void onRenderLevel(RenderLevelStageEvent.AfterTranslucentBlocks event) {
        Minecraft mc = Minecraft.getInstance();
        Vec3 camPos = mc.gameRenderer.getMainCamera().position();


        if(mc.screen instanceof ModularUIScreen){
            if(CityScreen.activeContext == null && CityInfoManager.allBuildings == null){
                return;
            }
        }
        else return;

        PoseStack poseStack = event.getPoseStack();
        VertexConsumer buffer = mc.renderBuffers().bufferSource().getBuffer(RenderTypes.lines());
        VoxelShape cube = Shapes.block();
        var activeContext = CityScreen.activeContext;

        // render current city buildings
        if(buildings){
            for(BuildingBox box : CityInfoManager.allBuildings){
                poseStack.pushPose();

                handlePositioning(poseStack, camPos, box.origin(), box.rotation(), box.mirrored());

                // render block origin
                ShapeRenderer.renderShape(poseStack, buffer, cube, 0, 0, 0, 0xFFFFFFFF, 1.0f);
                // render building box
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

        // renders current blueprint placement context
        if(activeContext != null){

            if (activeContext.selectedBlock == null) return;
            poseStack.pushPose();

            handlePositioning(poseStack, camPos, activeContext.selectedBlock, activeContext.selectedRotation, activeContext.isMirrored);

            // render selected block
            ShapeRenderer.renderShape(poseStack, buffer, cube, 0, 0, 0, 0xFFFFFFFF, 1.0f);

            if(activeContext.blueprint == null) return;
            // render building bounding box
            if(blueprintBox) {
                ShapeRenderer.renderShape(poseStack, buffer,
                        Shapes.box(
                                0, 0, 0,
                                activeContext.blueprint.getDimensions().getX(),
                                activeContext.blueprint.getDimensions().getY(),
                                activeContext.blueprint.getDimensions().getZ())
                        , 0, 0, 0, 0xFFFFFFFF, 5.0F);
            }

            // render individual block placement
            if(blueprintBlocks) {
                for (Map.Entry<BlockPos, BlockState> entry : activeContext.blueprint.getBlockData().entrySet()) {
                    BlockPos localPos = entry.getKey();
                    BlockState state = entry.getValue();
                    if (!state.isAir()) {
                        ShapeRenderer.renderShape(poseStack, buffer, cube, localPos.getX(), localPos.getY(), localPos.getZ(), 0xFFFF0000, 1.0f);
                    }
                }
            }
            poseStack.popPose();
        }

        mc.renderBuffers().bufferSource().endBatch(RenderTypes.lines());
    }

    private static float getRotationDegrees(Rotation rot) {
        return switch (rot) {
            case CLOCKWISE_90 -> -90.0f;
            case CLOCKWISE_180 -> -180.0f;
            case COUNTERCLOCKWISE_90 -> -270.0f;
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
        if(isMirrored) poseStack.scale(1.0f, 1.0f, -1.0f);
    }

    private static void handlePositioning(PoseStack poseStack, Vec3 cameraPos, BlockPos origin, Rotation rotation, boolean mirrored){
        setOrigin(poseStack, cameraPos, origin);
        // translate to middle of block
        poseStack.translate(0.5, 0, 0.5);
        // dont ask me why you have to rotate first, then mirror later. it just works.
        setRot(poseStack, rotation);
        setMirror(poseStack, mirrored);
        // sets box to block edge, regardless of orientation
        poseStack.translate(-0.5, 0, -0.5);

    }

    public static void setBuildings(boolean state) {buildings = state;}
    public static void setBlueprintBox(boolean state) {
        blueprintBox = state;
    }
    public static void setBlueprintBlocks(boolean state) {
        blueprintBlocks = state;
    }
}
