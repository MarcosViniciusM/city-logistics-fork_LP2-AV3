package com.sfmf3.citylogistics.mixins;

import com.sfmf3.citylogistics.camera.CameraController;
import net.minecraft.client.MouseHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.sfmf3.citylogistics.camera.CameraController.mc;

@Mixin(MouseHandler.class)
public class MouseHandlerMixin {

    @Inject(method = "grabMouse", at = @At("HEAD"), cancellable = true)
    private void onGrabMouse(CallbackInfo ci){
        // prevents game from automatically grabbing the mouse back
        if(CameraController.isAnchorActive()){
            if(!mc.mouseHandler.isRightPressed()){
                ci.cancel();
            }
        }
    }
}
