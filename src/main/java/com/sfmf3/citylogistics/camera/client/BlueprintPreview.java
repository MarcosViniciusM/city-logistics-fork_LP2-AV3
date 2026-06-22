package com.sfmf3.citylogistics.camera.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.sfmf3.citylogistics.CityLogistics;
import com.sfmf3.citylogistics.blueprint.Blueprint;
import com.sfmf3.citylogistics.blueprint.BlueprintIO;
import com.sfmf3.citylogistics.city.CityManager;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.ShapeRenderer;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.model.data.ModelData;

import java.util.Map;

import static com.sfmf3.citylogistics.camera.CameraController.mc;

@EventBusSubscriber(modid = CityLogistics.MODID, value = Dist.CLIENT)
public class BlueprintPreview {

    public static BlockPos selectedBlock = null;
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

    public static void clear() {
        selectedBlock = null;
        selectedPath = "";
    }

    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent.AfterTranslucentBlocks event) {
        BlockPos targetPos = selectedBlock;
        String blueprintPath = selectedPath;
        if (targetPos == null || blueprintPath == null || blueprintPath.isEmpty()) return;

        if (mc.level == null || mc.player == null) return;

        Blueprint blueprint = BlueprintIO.loadFromFile((selectedBuildingId + "/" + selectedPath), mc.level);
        if (blueprint == null) return;

        BlockPos centerOffset = CityManager.getCenterBlock(targetPos, blueprint.getDimensions(), selectedRotation, isMirrored);

        PoseStack poseStack = event.getPoseStack();
        VertexConsumer buffer = Minecraft.getInstance().renderBuffers().bufferSource().getBuffer(RenderTypes.lines());
        VoxelShape cube = Shapes.block();
        Vec3 camPos = mc.gameRenderer.getMainCamera().position();

        poseStack.pushPose();

        poseStack.translate(
                targetPos.getX() - camPos.x,
                targetPos.getY() - camPos.y,
                targetPos.getZ() - camPos.z
        );
        poseStack.translate(0.5, 0.5, 0.5);

        float rotDegrees = getRotationDegrees(selectedRotation);
        poseStack.mulPose(Axis.YP.rotationDegrees(rotDegrees));

        if (isMirrored) {
            poseStack.scale(-1.0f, 1.0f, 1.0f);
        }

        poseStack.translate(
                -0.5 - centerOffset.getX(),
                -0.5 - centerOffset.getY(),
                -0.5 - centerOffset.getZ()
        );

        // render building bounding box
        ShapeRenderer.renderShape(poseStack, buffer,
                Shapes.box(
                        0, 0, 0,
                        blueprint.getDimensions().getX(),
                        blueprint.getDimensions().getY(),
                        blueprint.getDimensions().getZ())
        , 0, 0, 0, 0xFFFFFFFF, 5.0F);

      //  if(!renderBlocks) { poseStack.popPose(); return; }


        for (Map.Entry<BlockPos, BlockState> entry : blueprint.getBlockData().entrySet()) {
            BlockPos localPos = entry.getKey();
            BlockState state = entry.getValue();

            // if solid block, renders wireframe
            if(state.isSolidRender()){
                ShapeRenderer.renderShape(poseStack, buffer, cube, localPos.getX(), localPos.getY(), localPos.getZ(), 0xFFFF0000, 1.0f);
            }
        }

        poseStack.popPose();
    }

    private static float getRotationDegrees(Rotation rot) {
        return switch (rot) {
            case CLOCKWISE_90 -> 90.0f;
            case CLOCKWISE_180 -> 180.0f;
            case COUNTERCLOCKWISE_90 -> 270.0f;
            default -> 0.0f;
        };
    }
}
