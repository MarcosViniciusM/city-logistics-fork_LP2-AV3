package com.sfmf3.citylogistics.camera.client;

import com.sfmf3.citylogistics.CityLogistics;
import com.sfmf3.citylogistics.camera.CameraController;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import org.lwjgl.glfw.GLFW;

import static com.sfmf3.citylogistics.camera.CameraController.isActive;
import static com.sfmf3.citylogistics.camera.CameraController.mc;

@EventBusSubscriber(modid = CityLogistics.MODID, value = Dist.CLIENT)
public class CameraClientEvents {
    private static BlockPos selectedPoint = null;

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void preTick(ClientTickEvent.Pre event){
        CameraController.preTick(Minecraft.getInstance());
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void postTick(ClientTickEvent.Post event){
        if(ModKeys.CAMERA_TOGGLE.consumeClick()){
            CameraController.toggle();
        }
        if(ModKeys.CAMERA_ANCHOR.consumeClick()){
            CameraController.anchorToggle();
        }
        if(ModKeys.CAMERA_INTERACT.consumeClick() && isActive()) {
            HitResult hit = mc.getCameraEntity().pick(64, 1.0F, false);
            if (hit.getType() == HitResult.Type.BLOCK) {
                selectedPoint = ((BlockHitResult) hit).getBlockPos();
                mc.player.sendSystemMessage(Component.literal("Setting point "
                        + selectedPoint.getX() + ", "
                        + selectedPoint.getY() + ", "
                        + selectedPoint.getZ() + "!"
                ));
            }
        }
        if(ModKeys.CAMERA_COMMIT.consumeClick() && isActive()){
            selectedPoint = null;
            mc.player.sendSystemMessage(Component.literal("Clearing selection."));
        }
        if(ModKeys.CAMERA_REGISTER.consumeClick() && isActive()){
            // pass for now
        }
    }

    @SubscribeEvent
    public static void onMouseInput(InputEvent.MouseButton.Post event){
        if(CameraController.isAnchorActive()){
            if(event.getButton() == GLFW.GLFW_MOUSE_BUTTON_2){
                if(event.getAction() == GLFW.GLFW_PRESS){
                    mc.mouseHandler.grabMouse();
                }
                else if(event.getAction() == GLFW.GLFW_RELEASE){
                    mc.mouseHandler.releaseMouse();
                }
            }
        }
    }

    public static BlockPos getSelectedPoint() { return selectedPoint; }
}