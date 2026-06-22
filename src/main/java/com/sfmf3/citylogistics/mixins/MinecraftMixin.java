package com.sfmf3.citylogistics.mixins;

import com.sfmf3.citylogistics.camera.CameraController;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Minecraft.class)
public class MinecraftMixin {

    // destructor of sorts
    @Inject(method = "disconnect*", at = @At(value = "HEAD"))
    private void onDisconnect(CallbackInfo ci){
        if(CameraController.isAnchorActive()){
            CameraController.anchorToggle();
        }
        if(CameraController.isActive()){
            CameraController.toggle();
        }
    }

    // stops attacks
    @Inject(method = "startAttack", at = @At("HEAD"), cancellable = true)
    private void onAttack(CallbackInfoReturnable<Boolean> ci){
        if(CameraController.isActive()){
            ci.cancel();
        }
    }

    // stops block breaking
    @Inject(method = "continueAttack", at = @At("HEAD"), cancellable = true)
    private void onBlockBreaking(CallbackInfo ci){
        if(CameraController.isActive()){
            ci.cancel();
        }
    }
}
