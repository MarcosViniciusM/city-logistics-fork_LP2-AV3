package com.sfmf3.citylogistics.camera;

import com.sfmf3.citylogistics.camera.client.CameraEntity;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import static com.sfmf3.citylogistics.camera.CameraController.mc;

public class AnchorHandler {
    private Vec3 zoomOffset;
    private double zoom;
    private final CameraEntity camera;

    // sets up variables based on entering anchor mode
    public AnchorHandler(double z){
        this.camera = CameraController.getCamera();
        this.zoom = getDistanceToTarget(z);
        zoomOffset = Vec3.directionFromRotation(camera.getXRot(), camera.getYRot()).scale(zoom);
    }

    public AnchorHandler(Vec3 point, double z){
        this.camera = CameraController.getCamera();
        camera.setXRot(30.0f); camera.setYRot(0.0f);
        this.zoom = z;
        orbitThis(point);
    }

    // hooked to player turning
    // will set camera pos according to xo & yo
    public void updateOrbit(){
        Vec3 anchorPoint = camera.position().add(zoomOffset);
        zoomOffset = Vec3.directionFromRotation(camera.getXRot(), camera.getYRot()).scale(zoom);
        Vec3 newPos = anchorPoint.subtract(zoomOffset);
        camera.setPos(newPos);
        camera.xo = newPos.x; camera.yo = newPos.y; camera.zo = newPos.z;
    }

    public void orbitThis(Vec3 orbitPoint) {
        // basically the same as updateOrbit, just sets an anchorPoint.
        zoomOffset = Vec3.directionFromRotation(camera.getXRot(), camera.getYRot()).scale(zoom);
        Vec3 newPos = orbitPoint.subtract(zoomOffset);
        camera.setPos(newPos);
        camera.xo = newPos.x; camera.yo = newPos.y; camera.zo = newPos.z;
    }

    public void handleZoom(double amount){
        this.zoom -= amount * 1.5D;
        this.zoom = Mth.clamp(this.zoom, 2.0D, 64.0D);
        updateOrbit();
    }

    public static double getDistanceToTarget(double maxDistance){
        HitResult hit = mc.getCameraEntity().pick(maxDistance, 1.0F, false);

        if(hit.getType() == HitResult.Type.MISS){
            return maxDistance/2;
        }

        return mc.getCameraEntity().getEyePosition(1.0F).distanceTo(hit.getLocation());
    }

    public Vec3 getAnchorPoint(){
        return CameraController.getCamera().position().add(zoomOffset);
    }
}
