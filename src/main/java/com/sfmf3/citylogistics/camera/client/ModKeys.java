package com.sfmf3.citylogistics.camera.client;

import com.mojang.blaze3d.platform.InputConstants;
import com.sfmf3.citylogistics.CityLogistics;
import net.minecraft.client.KeyMapping;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import org.lwjgl.glfw.GLFW;

@EventBusSubscriber(modid = CityLogistics.MODID, value = Dist.CLIENT)
public class ModKeys {

    @SubscribeEvent
    public static void registerKeys(RegisterKeyMappingsEvent event){
        event.register(CAMERA_TOGGLE);
        event.register(CAMERA_ANCHOR);
        event.register(CAMERA_COMMIT);
        event.register(CAMERA_INTERACT);
        event.register(CAMERA_REGISTER);
        event.register(BUILDING_ROTATE);
        event.register(BUILDING_MIRROR);
    }

    public static final KeyMapping CAMERA_TOGGLE = new KeyMapping(
            "key.citylogistics.toggle",
            KeyConflictContext.UNIVERSAL    ,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_Z,
            KeyMapping.Category.MISC
    );

    public static final KeyMapping CAMERA_ANCHOR = new KeyMapping(
            "key.citylogistics.anchor",
            KeyConflictContext.UNIVERSAL,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_X,
            KeyMapping.Category.MISC
    );

    public static final KeyMapping CAMERA_INTERACT = new KeyMapping(
            "key.citylogistics.interact",
            KeyConflictContext.UNIVERSAL,
            InputConstants.Type.MOUSE,
            GLFW.GLFW_MOUSE_BUTTON_LEFT,
            KeyMapping.Category.MISC
    );

    public static final KeyMapping CAMERA_COMMIT = new KeyMapping(
            "key.citylogistics.commit",
            KeyConflictContext.UNIVERSAL,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_C,
            KeyMapping.Category.MISC
    );

    public static final KeyMapping CAMERA_REGISTER = new KeyMapping(
            "key.citylogistics.register",
            KeyConflictContext.UNIVERSAL,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_V,
            KeyMapping.Category.MISC
    );

    public static final KeyMapping BUILDING_ROTATE = new KeyMapping(
            "key.citylogistics.rotate",
            KeyConflictContext.UNIVERSAL,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_R,
            KeyMapping.Category.MISC
    );
    public static final KeyMapping BUILDING_MIRROR = new KeyMapping(
            "key.citylogistics.mirror",
            KeyConflictContext.UNIVERSAL,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_T,
            KeyMapping.Category.MISC
    );
}
