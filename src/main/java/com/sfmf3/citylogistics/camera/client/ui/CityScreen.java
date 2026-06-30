package com.sfmf3.citylogistics.camera.client.ui;

import com.lowdragmc.lowdraglib2.editor.resource.FilePath;
import com.lowdragmc.lowdraglib2.editor.resource.UIResource;
import com.lowdragmc.lowdraglib2.gui.holder.ModularUIScreen;
import com.lowdragmc.lowdraglib2.gui.ui.ModularUI;
import com.lowdragmc.lowdraglib2.gui.ui.UI;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.elements.*;
import com.lowdragmc.lowdraglib2.gui.ui.event.UIEvents;
import com.lowdragmc.lowdraglib2.gui.ui.utils.IModularUIProvider;
import com.sfmf3.citylogistics.CityLogistics;
import com.sfmf3.citylogistics.building.BuildingInformation;
import com.sfmf3.citylogistics.building.BuildingRegistry;
import com.sfmf3.citylogistics.camera.CameraController;
import com.sfmf3.citylogistics.camera.client.BlueprintPreview;
import com.sfmf3.citylogistics.camera.client.CityInfoManager;
import com.sfmf3.citylogistics.camera.client.ModKeys;
import com.sfmf3.citylogistics.network.CityOperationException;
import com.sfmf3.citylogistics.network.payload.AddBuildingPayload;
import com.sfmf3.citylogistics.network.payload.BlueprintRequestPayload;
import com.sfmf3.citylogistics.network.payload.BuildingRequestPayload;
import com.sfmf3.citylogistics.network.payload.BuildingResponsePayload;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.InputEvent;
import org.apache.logging.log4j.core.pattern.AbstractStyleNameConverter;
import org.lwjgl.glfw.GLFW;

import static com.sfmf3.citylogistics.camera.CameraController.mc;

// sim eu estou implementando uma biblioteca em cima da hora
// god has cursed me for my hubris, and my work is never finished
@EventBusSubscriber(modid = CityLogistics.MODID, value = Dist.CLIENT)
public class CityScreen extends ModularUIScreen {

    private ModularUI ui;
    public static CityInfoManager.BuildingPlacementContext activeContext = null;
    public static BuildingInformation activeSelection = null;

    private UIElement drawerPlaceholder = null;
    private UIElement buildingInfoPlaceholder = null;
    private UIElement groupContainer = null;

    public CityScreen(Player player){
        var base = new UIElement(){
            {
                layout(layout -> layout.widthPercent(100).heightPercent(100));
            }
        };
        var modularUi = ModularUI.of(UI.of(base), player);
        super(modularUi, Component.literal("CityScreen"));
        this.createModularUI(base);
    }

    public void createModularUI(UIElement base){
        var root = loadTemplate("layouts/base_screen.ui.nbt");

        root.layout(layout -> layout.widthPercent(100).heightPercent(100));

        base.addChild(root);

        // remove later

        drawerPlaceholder = root.select("#building_selection").findFirst().orElse(null);
        buildingInfoPlaceholder = root.select("#building_info").findFirst().orElse(null);

        groupContainer = root.select("#category_group").findFirst().orElse(null);

        // populate building category menu
        for(String cat : BuildingRegistry.CATEGORIES){
            var btnUi = loadTemplate("layouts/toggle_button_template.ui.nbt");
            btnUi.select("#toggle_btn").findFirst().ifPresent(widget -> {
                if(widget instanceof Toggle toggleBtn){

                    toggleBtn.setOnToggleChanged((isActive) -> {
                        if(isActive) populateDrawer(cat);
                        else drawerPlaceholder.clearAllChildren();
                    });
                    var text = toggleBtn.select("#category").findFirst().orElse(null);
                    if(text instanceof TextElement textArea){ textArea.setText(cat); }
                    groupContainer.addChild(toggleBtn);
                }
            });

        }
    }

    private void populateDrawer(String category){
        drawerPlaceholder.clearAllChildren();

        for(BuildingRegistry.BuildingDefinition def : BuildingRegistry.BUILDING_REGISTRY){
            if(def.category().equals(category)){
                var itemUi = loadTemplate("layouts/building_selection_template.ui.nbt");

                itemUi.select("#building_name").findFirst().ifPresent(widget -> {
                    if(widget instanceof TextElement text){ text.setText(def.displayName()); }
                });

                itemUi.select("#building_icon").findFirst().ifPresent(widget ->{
                   // do icon system later. i dont even have icons in mind yet
                });

                itemUi.select("#button").findFirst().ifPresent(widget -> {
                    if(widget instanceof Button button){
                        button.setOnClick(click ->{
                            populateConstructor(def);
                        });
                    }
                });

                drawerPlaceholder.addChild(itemUi);
            }
        }
        drawerPlaceholder.markTaffyStyleDirty();
    }

    private void populateConstructor(BuildingRegistry.BuildingDefinition def){
        buildingInfoPlaceholder.clearAllChildren();

        activeContext = new CityInfoManager.BuildingPlacementContext(def.buildingId());
        var constructorUi = loadTemplate("layouts/building_constructor_template.ui.nbt");
        var root = constructorUi.select("#root").findFirst().orElse(null);

        root.select("#tooltip_name").findFirst().ifPresent(widget -> {
            if(widget instanceof Label label){
                label.setValue(Component.literal(def.displayName()));
            }
        });

        root.select("#path").findFirst().ifPresent(widget -> {
            if(widget instanceof Selector<?> rawSelector){
                @SuppressWarnings("unchecked")
                var selector = (Selector<String>) rawSelector;
                selector.setCandidates(activeContext.availablePaths);
                selector.setSelected("");

                selector.setOnValueChanged(change -> {
                    activeContext.selectedPath = change;
                    mc.player.connection.send(new BlueprintRequestPayload(def.buildingId()+"/"+change));
                });
            }
        });

        root.select("#render_box").findFirst().ifPresent(widget -> {
            if(widget instanceof Toggle toggle){
                toggle.setOnToggleChanged(BlueprintPreview::setBlueprintBox);
                BlueprintPreview.setBlueprintBox(toggle.getValue());
            }
        });

        root.select("#render_blocks").findFirst().ifPresent(widget -> {
            if(widget instanceof Toggle toggle){
                toggle.setOnToggleChanged(BlueprintPreview::setBlueprintBlocks);
                BlueprintPreview.setBlueprintBlocks(toggle.getValue());
            }
        });

        root.select("#add_building").findFirst().ifPresent(widget -> {
            if(widget instanceof Button button){
                button.setOnClick(_ ->{
                    mc.player.connection.send(new AddBuildingPayload(
                            CityInfoManager.cityAnchor,
                            activeContext.selectedBlock,
                            activeContext.selectedPath,
                            activeContext.selectedRotation,
                            activeContext.isMirrored,
                            activeContext.buildingId
                    ));

                    activeContext = null;
                    buildingInfoPlaceholder.clearAllChildren();
                });
            }
        });

        root.select("#end_constructor").findFirst().ifPresent(widget ->{
            if(widget instanceof Button button){
                button.setOnClick(_ -> {
                    activeContext = null;
                    buildingInfoPlaceholder.clearAllChildren();
                });
            }
        });

        buildingInfoPlaceholder.addChild(root);
    }

    private void populateViewer(){
        if(activeSelection == null || buildingInfoPlaceholder == null) return;

        buildingInfoPlaceholder.clearAllChildren();

        //var infoUi = loadTemplate("layouts/building_info_template.ui.nbt");
        var infoUi = loadTemplate("layouts/building_constructor_template.ui.nbt");

        var root = infoUi.select("#root").findFirst().orElse(null);

        root.select("#tooltip_name").findFirst().ifPresent(widget -> {
            if(widget instanceof Label label){
                label.setValue(Component.literal(activeSelection.getBuildingId()));
            }
        });




        buildingInfoPlaceholder.addChild(root);
    }

    public static void updateSelection(BuildingInformation information) {
        activeSelection = information;
        if(information == null) return;
        if(mc.screen instanceof CityScreen screen){
            screen.populateViewer();
        }
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent button, boolean doubleClick){

        // checks if mouse bubbles down correctly
        if(super.mouseClicked(button, doubleClick)){ return true; }
        // if not, run code that is based on clicking out of the ui

        if(button.button() == 0){
            if(activeContext != null){
                if(!doubleClick){
                    HitResult hit = mc.getCameraEntity().pick(64, 1.0F, false);
                    if (hit.getType() == HitResult.Type.BLOCK) {
                        activeContext.selectedBlock = ((BlockHitResult) hit).getBlockPos();
                        mc.player.sendSystemMessage(Component.literal("Setting point "
                                + activeContext.selectedBlock.getX() + ", "
                                + activeContext.selectedBlock.getY() + ", "
                                + activeContext.selectedBlock.getZ() + "!"
                        ));
                    } else {
                        activeContext.selectedBlock = null;
                    }
                }
                else { activeContext.selectedBlock = null; }
                return true;
            }
            else{
                if(!doubleClick){
                    HitResult hit = mc.getCameraEntity().pick(64, 1.0F, false);
                    if (hit.getType() == HitResult.Type.BLOCK) {
                        mc.player.connection.send(new BuildingRequestPayload(CityInfoManager.cityAnchor,((BlockHitResult) hit).getBlockPos()));
                    }
                }
            }

        }

        if(button.button() == 1){
            GLFW.glfwSetInputMode(mc.getWindow().handle(), GLFW.GLFW_CURSOR, GLFW.GLFW_CURSOR_DISABLED);
            return true;
        }

        return false;
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent button){
        if(button.button() == 1) {
            if (CameraController.isAnchorActive()){
                GLFW.glfwSetInputMode(mc.getWindow().handle(), GLFW.GLFW_CURSOR, GLFW.GLFW_CURSOR_NORMAL);
                return  true;
            }
        }

        return super.mouseReleased(button);
    }

    @Override
    public boolean mouseScrolled(double x, double y, double scrollX, double scrollY){
        if (super.mouseScrolled(x, y, scrollX, scrollY)) { return true; }

        if(CameraController.isAnchorActive()){
            CameraController.getAnchor().handleZoom(scrollY);
            return true;
        }

        return false;
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent button, double dx, double dy){

        if(button.button() == 1){
            if(CameraController.isAnchorActive()){
                mc.player.turn(dx, dy);
                return true;
            }
        }

        return super.mouseDragged(button, dx, dy);
    }

    @Override
    public boolean keyPressed(KeyEvent event){
        if(super.keyPressed(event)) { return true; }

        if(activeContext != null){
            if(event.key() == GLFW.GLFW_KEY_R){
                activeContext.selectedRotation = activeContext.selectedRotation.getRotated(Rotation.CLOCKWISE_90);
                return true;
            }
            if(event.key() == GLFW.GLFW_KEY_T){
                activeContext.isMirrored = !activeContext.isMirrored;
                return true;
            }
        }

        if(event.key() == GLFW.GLFW_KEY_X){
            CameraController.anchorToggle();
            return true;
        }
        if(event.key() == GLFW.GLFW_KEY_Z){
            CameraController.toggle();
            return true;
        }

        return false;
    }

    @Override
    public void resize(int width, int height){
        super.resize(width, height);
    }



    private UIElement loadTemplate(String path){
        var resource = UIResource.INSTANCE.getResourceInstance()
                .getResource(new FilePath(Identifier.parse(CityLogistics.MODID+":"+path)));

        if(resource == null){
            throw new CityOperationException("Couldn't find template: "+path);
        }

        return resource.createUI().getRootElement();
    }

}
