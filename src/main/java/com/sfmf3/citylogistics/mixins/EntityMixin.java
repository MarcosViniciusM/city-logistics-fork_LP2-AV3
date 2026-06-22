package com.sfmf3.citylogistics.mixins;

import com.sfmf3.citylogistics.camera.CameraController;
import com.sfmf3.citylogistics.camera.client.CameraEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static com.sfmf3.citylogistics.camera.CameraController.mc;

@Mixin(Entity.class)
@SuppressWarnings("EqualsBetweenInconvertibleTypes")
public class EntityMixin {

    @Inject(method = "turn", at = @At("HEAD"), cancellable = true)
    private void onChangeLookDirection(double xo, double yo, CallbackInfo ci){
        // change later
        if(CameraController.isActive() && this.equals(mc.player)){
            CameraController.getCamera().turn(xo, yo);
            if(CameraController.isAnchorActive()){
                CameraController.updateAnchor();
            }
            ci.cancel();
        }
    }

    // overrides logic from pick(), allows picking block based on mouse location
    @Inject(method = "getViewVector", at = @At("HEAD"), cancellable = true)
    private void onGetViewVector(float a, CallbackInfoReturnable<Vec3> ci){
        if((Object)this instanceof CameraEntity){
            if(CameraController.isAnchorActive()){
                ci.setReturnValue(CameraController.getCamera().getMouseLookVector());
            }
        }
    }
}
