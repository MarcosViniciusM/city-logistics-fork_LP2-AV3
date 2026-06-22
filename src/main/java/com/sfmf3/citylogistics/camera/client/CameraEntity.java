package com.sfmf3.citylogistics.camera.client;

import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.platform.InputConstants;
import com.sfmf3.citylogistics.camera.Motion;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.ClientInput;
import net.minecraft.client.player.KeyboardInput;
import net.minecraft.world.entity.player.Input;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector4f;

import java.util.UUID;

import static com.sfmf3.citylogistics.camera.CameraController.mc;

public class CameraEntity extends AbstractClientPlayer {
    public ClientInput input;
    public float yBob;
    public float xBob;
    public float yBobO;
    public float xBobO;

    public CameraEntity(int id){
        super(mc.level, new GameProfile(UUID.randomUUID(), "Camera_" + mc.player.getName()));

        setId(id);
        this.noPhysics = true;
        getAbilities().flying = true;
        this.input = new KeyboardInput(mc.options) {
            @Override
            public void tick(){
                boolean up = InputConstants.isKeyDown(mc.getWindow(), mc.options.keyUp.getKey().getValue());
                boolean down = InputConstants.isKeyDown(mc.getWindow(), mc.options.keyDown.getKey().getValue());
                boolean left = InputConstants.isKeyDown(mc.getWindow(), mc.options.keyLeft.getKey().getValue());
                boolean right = InputConstants.isKeyDown(mc.getWindow(), mc.options.keyRight.getKey().getValue());

                this.keyPresses = new Input(
                        InputConstants.isKeyDown(mc.getWindow(), mc.options.keyUp.getKey().getValue()),
                        InputConstants.isKeyDown(mc.getWindow(), mc.options.keyDown.getKey().getValue()),
                        InputConstants.isKeyDown(mc.getWindow(), mc.options.keyLeft.getKey().getValue()),
                        InputConstants.isKeyDown(mc.getWindow(), mc.options.keyRight.getKey().getValue()),
                        InputConstants.isKeyDown(mc.getWindow(), mc.options.keyJump.getKey().getValue()),
                        InputConstants.isKeyDown(mc.getWindow(), mc.options.keyShift.getKey().getValue()),
                        InputConstants.isKeyDown(mc.getWindow(), mc.options.keySprint.getKey().getValue())
                );

                float forward = up == down ? 0.0F : (up ? 1.0F : -1.0F);
                float strafe = left == right ? 0.0F : (left ? 1.0F : -1.0F);

                this.moveVector = new net.minecraft.world.phys.Vec2(strafe, forward).normalized();
            }
        };
    }

    @Override
    public void tick(){
        input.tick();
        doMotion();
        super.tick();
    }

    @Override
    protected void applyInput(){
        Vec2 vec2 = this.input.getMoveVector();
        if(vec2.lengthSquared() != 0.0f){
            vec2.scale(0.98f);
        }
        applyInputHelper(vec2, this.input.keyPresses.jump());
    }

    private void applyInputHelper(Vec2 moveVector, boolean jumping){
        this.xxa = moveVector.x;
        this.yya = moveVector.y;
        this.jumping = jumping;
        this.yBobO = this.yBob;
        this.xBobO = this.xBob;
        this.xBob = this.xBob + (this.getXRot() - this.xBob) * 0.5f;
        this.yBob = this.yBob + (this.getYRot() - this.yBob) * 0.5f;
    }

    private void doMotion(){
        getAbilities().setFlyingSpeed(0);
        Motion.doMotion(this, 1.0, 0.75);
        getAbilities().flying = true;
        setOnGround(false);
        this.noPhysics = true;
    }

    private ClientLevel getClientLevel(){
        return (ClientLevel) level();
    }

    public void spawn(){
        getClientLevel().addEntity(this);
    }
    public void despawn(){
        getClientLevel().removeEntity(getId(), RemovalReason.DISCARDED);
    }

    public Vec3 getMouseLookVector(){
        // get [-1,1] mouse coords
        double mouseX = mc.mouseHandler.xpos();
        double mouseY = mc.mouseHandler.ypos();
        float nx = (float)(2.0D * mouseX / (double)mc.getWindow().getScreenWidth() - 1.0D);
        float ny = (float) (1.0D - 2.0D * mouseY / (double)mc.getWindow().getScreenHeight());

        Vector4f ray = new Vector4f(nx, ny, -1.0F, 1.0F);

        // invert the projection matrix (normally goes from world to player, we want the inverse), then apply
        Matrix4f invProj = new Matrix4f(mc.gameRenderer.getGameRenderState().levelRenderState.cameraRenderState.projectionMatrix).invert();
        invProj.transform(ray);

        // remove w from every element (cuz we just multiplied the matrix)
        ray.div(ray.w());

        // rotate by current camera rotation, getting the position of the mouse
        ray.rotate(mc.gameRenderer.getMainCamera().rotation());

        // return vec3 value as direction to be used in pick()
        // normally it would create a ray from the center
        // we override that so it uses our mouse as the point that the raycast will go through
        return new Vec3(ray.x(), ray.y(), ray.z()).normalize();
    }

    @Override
    public float getViewXRot(float partialTick){
        return this.getXRot();
    }

    @Override
    public float getViewYRot(float partialTick){
        return this.getYRot();
    }

    // allows passing through blocks
    @Override
    public boolean isSpectator(){ return true; }

    // allows movement ticking
    @Override
    public boolean isEffectiveAi() { return true; }

    // allows travel()
    @Override
    public boolean canSimulateMovement() { return true; }

    @Override
    public boolean isInvisible() { return true; }
}