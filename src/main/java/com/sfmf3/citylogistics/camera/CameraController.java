package com.sfmf3.citylogistics.camera;

import com.sfmf3.citylogistics.camera.client.CameraEntity;
import com.sfmf3.citylogistics.camera.client.CityBuilderScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.ClientInput;
import net.minecraft.client.player.KeyboardInput;
import net.minecraft.world.entity.player.Input;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.ApiStatus;


public class CameraController {
    public static final Minecraft mc = Minecraft.getInstance();

    private static boolean active;
    private static boolean anchorActive;
    private static CameraEntity cameraInstance;
    private static AnchorHandler anchor;

    @ApiStatus.Internal
    public static void preTick(Minecraft mc){
        if(active){
            if(mc.player != null){
                ClientInput input = new ClientInput();
                Input keyPresses = mc.player.input.keyPresses;
                input.keyPresses = new Input(
                        false,
                        false,
                        false,
                        false,
                        false,
                        keyPresses.shift(),
                        false
                );

                mc.player.input = input;
            }
        }
    }

    public static void toggle(){
        if(active){
            if(anchorActive){
                anchorToggle();
            }

            disableCamera();
        } else{
            enableCamera();
        }
        active = !active;
    }

    private static void enableCamera(){
        cameraInstance = new CameraEntity(-27);
        // move camera to entity player
        if(mc.player != null) cameraInstance.copyPosition(mc.player);
        cameraInstance.spawn();
        mc.setCameraEntity(cameraInstance);
    }

    private static void disableCamera(){
        mc.setCameraEntity(mc.player);
        cameraInstance.despawn();
        // removes input from camera
        cameraInstance.input = new ClientInput();
        cameraInstance = null;

        // gives input back to player
        if(mc.player != null){
            mc.player.input = new KeyboardInput(mc.options);
        }
    }

    public static void anchorToggle(){
        if(!active) return;
        if(anchorActive) {
            disableAnchor();
        } else{
            enableAnchor();
        }
        anchorActive = !anchorActive;

        if(!anchorActive){
            mc.mouseHandler.grabMouse();
        }
    }

    private static void enableAnchor(){
        mc.mouseHandler.releaseMouse();
        anchor = new AnchorHandler(32.0D);

        new CityBuilderScreen().openGui();
    }

    private static void disableAnchor(){
        mc.mouseHandler.grabMouse();
        anchor = null;
        if(mc.screen != null){
            mc.setScreen(null);
        }
    }

    public static void orbitPoint(Vec3 point){
        if(!active){
            toggle();
        }
        if(!anchorActive){
            mc.mouseHandler.releaseMouse();
            anchor = new AnchorHandler(point, 32.0D);
            anchorActive = true;
        } else{
            anchor.orbitThis(point);
        }
    }

    public static void updateAnchor(){ anchor.updateOrbit(); }

    public static CameraEntity getCamera(){ return cameraInstance; }
    public static AnchorHandler getAnchor() { return anchor; }
    public static boolean isActive() { return active; }
    public static boolean isAnchorActive() { return  anchorActive; }
}
