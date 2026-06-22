package com.sfmf3.citylogistics.camera;

import com.sfmf3.citylogistics.camera.client.CameraEntity;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public class Motion {
    public static final double DIAGONAL_MULTIPLIER = Mth.sin((float) Math.toRadians(45));

    public static void doMotion(CameraEntity cameraEntity, double hSpeed, double vSpeed){
        float yaw = cameraEntity.getYRot();
        double velocityX = 0.0;
        double velocityY = 0.0;
        double velocityZ = 0.0;

        Vec3 forward = Vec3.directionFromRotation(0, yaw);
        Vec3 side = Vec3.directionFromRotation(0, yaw + 90);

        hSpeed = hSpeed * (cameraEntity.isSprinting() ? 1.5 : 1.0);

        boolean straight = false;
        if(cameraEntity.input.keyPresses.forward()){
            velocityX += forward.x * hSpeed;
            velocityZ += forward.z * hSpeed;
            straight = true;
        }
        if(cameraEntity.input.keyPresses.backward()){
            velocityX -= forward.x * hSpeed;
            velocityZ -= forward.z * hSpeed;
            straight = true;
        }

        boolean strafing = false;
        if(cameraEntity.input.keyPresses.right()){
            velocityX += side.x * hSpeed;
            velocityZ += side.z * hSpeed;
            strafing = true;
        }
        if(cameraEntity.input.keyPresses.left()){
            velocityX -= side.x * hSpeed;
            velocityZ -= side.z * hSpeed;
            strafing = true;
        }

        if(strafing && straight){
            velocityX *= DIAGONAL_MULTIPLIER;
            velocityZ *= DIAGONAL_MULTIPLIER;
        }

        if(cameraEntity.input.keyPresses.jump()){
            velocityY += vSpeed;
        }
        if(cameraEntity.input.keyPresses.shift()){
            velocityY -= vSpeed;
        }

        cameraEntity.setDeltaMovement(velocityX, velocityY, velocityZ);
    }
}
