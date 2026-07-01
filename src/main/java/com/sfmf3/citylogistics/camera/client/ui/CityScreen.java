package com.sfmf3.citylogistics.camera.client.ui;

import com.lowdragmc.lowdraglib2.editor.resource.FilePath;
import com.lowdragmc.lowdraglib2.editor.resource.UIResource;
import com.lowdragmc.lowdraglib2.gui.holder.ModularUIScreen;
import com.lowdragmc.lowdraglib2.gui.ui.ModularUI;
import com.lowdragmc.lowdraglib2.gui.ui.UI;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.data.Vertical;
import com.lowdragmc.lowdraglib2.gui.ui.elements.*;
import com.lowdragmc.lowdraglib2.gui.ui.event.HoverTooltips;
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
import com.sfmf3.citylogistics.network.payload.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
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
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import org.apache.logging.log4j.core.pattern.AbstractStyleNameConverter;
import org.lwjgl.glfw.GLFW;

import static com.sfmf3.citylogistics.camera.CameraController.mc;
import static com.sfmf3.citylogistics.camera.CameraController.orbitPoint;

// sim eu estou implementando uma biblioteca em cima da hora
// god has cursed me for my hubris, and my work is never finished
@EventBusSubscriber(modid = CityLogistics.MODID, value = Dist.CLIENT)
public class CityScreen extends ModularUIScreen {

    public static CityInfoManager.BuildingPlacementContext activeContext = null;
    public static BuildingInformation activeSelection = null;

    private UIElement drawerPlaceholder = null;
    private UIElement buildingInfoPlaceholder = null;
    private UIElement groupContainer = null;
    private ScrollerView resourcePlaceholder = null;
    private UIElement cityFunctions = null;

    private boolean controlHeld = false;

    @Override
    public void init(){
        mc.options.hideGui = true;
        super.init();
    }

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

        drawerPlaceholder = root.select("#building_selection").findFirst().orElse(null);
        buildingInfoPlaceholder = root.select("#building_info").findFirst().orElse(null);
        groupContainer = root.select("#category_group").findFirst().orElse(null);
        resourcePlaceholder = (ScrollerView) root.select("#resources").findFirst().orElse(null);
        cityFunctions = root.select("#city_functions").findFirst().orElse(null);
        cityFunctions.setDisplay(false);

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

        var addCity = cityFunctions.select("#add_city").findFirst().orElse(null);

        Toggle expander = (Toggle) root.select("#city_expander").findFirst().orElse(null);
        expander.setValue(false);
        expander.setOnToggleChanged(state -> {
            cityFunctions.setDisplay(state);
            root.markTaffyStyleDirty();
        });

        if(CityInfoManager.cityAnchor == null) {
            TextField text = (TextField) addCity.select("#text_city").findFirst().orElse(null);

            addCity.select("#button_add_city").findFirst().ifPresent(widget -> {
                if (widget instanceof Button button) {
                    button.setOnClick(_ -> mc.player.connection.send(new AddCityPayload(mc.player.blockPosition(), text.getText())));
                }
            });
            addCity.select("#button_add_pop").findFirst().ifPresent(widget -> {
                if (widget instanceof Button button) {
                    button.setOnClick(_ -> mc.player.connection.send(new AddPopulationPayload(CityInfoManager.cityAnchor, 1)));
                }
            });

            addCity.setDisplay(true);
            root.markTaffyStyleDirty();
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

        root.addEventListener(UIEvents.MOUSE_DOWN, uiEvent -> {});
        buildingInfoPlaceholder.addChild(root);
    }

    private void populateViewer(){
        if(activeSelection == null || buildingInfoPlaceholder == null) return;

        buildingInfoPlaceholder.clearAllChildren();

        var infoUi = loadTemplate("layouts/building_viewer_template.ui.nbt");
        var root = infoUi.select("#root").findFirst().orElse(null);

        ScrollerView info = (ScrollerView) root.select("#building_info").findFirst().orElse(null);

        root.select("#tooltip_name").findFirst().ifPresent(widget -> {
            if(widget instanceof Label label){
                label.setValue(Component.literal(activeSelection.getBuildingId()));
            }
        });

        root.select("#button_view").findFirst().ifPresent(widget -> {
            if(widget instanceof Button button){
                button.setOnClick(_ -> {
                    orbitPoint(activeSelection.box.origin().getCenter());
                });
            }
        });

        root.select("#end_viewer").findFirst().ifPresent(widget -> {
            if(widget instanceof Button button){
                button.setOnClick(_ -> {
                    activeSelection = null;
                    buildingInfoPlaceholder.clearAllChildren();
                });
            }
        });

        root.select("#button_repair").findFirst().ifPresent(widget -> {
            if(widget instanceof Button button){
                button.setOnClick(_ -> {
                    mc.player.connection.send(new ChangeBuildingStatePayload(
                            CityInfoManager.cityAnchor,
                            activeSelection.getBox().origin()
                    ));
                });
                button.addEventListener(UIEvents.HOVER_TOOLTIPS, e -> {
                    e.hoverTooltips = HoverTooltips.empty()
                            .append(Component.literal("Sets building to UNFINISHED."));
                });
            }
        });

        root.select("#button_delete").findFirst().ifPresent(widget -> {
            if(widget instanceof Button button){
                button.setOnClick(_ -> {
                    // mc.player.connection.send(); delete command not implemented
                });

                button.addEventListener(UIEvents.HOVER_TOOLTIPS, e -> {
                    e.hoverTooltips = HoverTooltips.empty()
                            .append(Component.literal("Deletes building."));
                });
            }
        });

        if(activeSelection != null){
            var buildingInfo = loadTemplate("layouts/building_info_template.ui.nbt").select("#root").findFirst().orElse(null);
            var infoGroup = buildingInfo.select("#info").findFirst().orElse(null);
            infoGroup.setDisplay(false);

            buildingInfo.select("#expander").findFirst().ifPresent(widget -> {
                if(widget instanceof Toggle toggle){
                    toggle.setValue(false);
                    toggle.setOnToggleChanged(state -> {
                        infoGroup.setDisplay(state);
                        buildingInfo.markTaffyStyleDirty();
                    });
                }
            });

            buildingInfo.select("#name").findFirst().ifPresent(widget -> {
                if(widget instanceof Label label){
                    label.setValue(Component.literal("Building information"));
                }
            });

            var id = new Label().setValue(Component.literal("ID: " + activeSelection.buildingId));
            id.textStyle(style -> style.fontSize(5)
                    .textAlignVertical(Vertical.CENTER));
            infoGroup.addChild(id);

            var state = new Label().setValue(Component.literal("State: " + activeSelection.state.getSerializedName()));
            state.textStyle(style -> style.fontSize(5)
                    .textAlignVertical(Vertical.CENTER));
            infoGroup.addChild(state);



            info.addScrollViewChild(buildingInfo);
        }

        if(activeSelection.housing != 0){
            var buildingInfo = loadTemplate("layouts/building_info_template.ui.nbt").select("#root").findFirst().orElse(null);
            var infoGroup = buildingInfo.select("#info").findFirst().orElse(null);
            infoGroup.setDisplay(false);

            buildingInfo.select("#expander").findFirst().ifPresent(widget -> {
                if(widget instanceof Toggle toggle){
                    toggle.setValue(false);
                    toggle.setOnToggleChanged(state -> {
                        infoGroup.setDisplay(state);
                        buildingInfo.markTaffyStyleDirty();
                    });
                }
            });

            buildingInfo.select("#name").findFirst().ifPresent(widget -> {
                if(widget instanceof Label label){
                    label.setValue(Component.literal("Housing"));
                }
            });

            var slots = new Label().setValue(Component.literal("Houses " + activeSelection.housing + " people"));
            slots.textStyle(style -> style.fontSize(5)
                    .textAlignVertical(Vertical.CENTER));
            infoGroup.addChild(slots);


            info.addScrollViewChild(buildingInfo);
        }

        if(activeSelection.workers != 0){
            var buildingInfo = loadTemplate("layouts/building_info_template.ui.nbt").select("#root").findFirst().orElse(null);
            var infoGroup = buildingInfo.select("#info").findFirst().orElse(null);
            infoGroup.setDisplay(false);

            buildingInfo.select("#expander").findFirst().ifPresent(widget -> {
                if(widget instanceof Toggle toggle){
                    toggle.setValue(false);
                    toggle.setOnToggleChanged(state -> {
                        infoGroup.setDisplay(state);
                        buildingInfo.markTaffyStyleDirty();
                    });
                }
            });

            buildingInfo.select("#name").findFirst().ifPresent(widget -> {
                if(widget instanceof Label label){
                    label.setValue(Component.literal("Workers"));
                }
            });

            var slots = new Label().setValue(Component.literal(activeSelection.workers + " max slots"));
            slots.textStyle(style -> style.fontSize(5)
                    .textAlignVertical(Vertical.CENTER));
            infoGroup.addChild(slots);


            info.addScrollViewChild(buildingInfo);
        }

        if(!activeSelection.output.isEmpty()){
            var buildingInfo = loadTemplate("layouts/building_info_template.ui.nbt").select("#root").findFirst().orElse(null);
            var infoGroup = buildingInfo.select("#info").findFirst().orElse(null);
            infoGroup.setDisplay(false);

            buildingInfo.select("#expander").findFirst().ifPresent(widget -> {
                if(widget instanceof Toggle toggle){
                    toggle.setValue(false);
                    toggle.setOnToggleChanged(state -> {
                        infoGroup.setDisplay(state);
                        buildingInfo.markTaffyStyleDirty();
                    });
                }
            });

            buildingInfo.select("#name").findFirst().ifPresent(widget -> {
                if(widget instanceof Label label){
                    label.setValue(Component.literal("Output"));
                }
            });

            var max = new Label().setValue(Component.literal("Max output at full workers/2h "));
            max.textStyle(style -> style.fontSize(4.5f)
                    .textAlignVertical(Vertical.CENTER));
            infoGroup.addChild(max);

            activeSelection.output.forEach((resource, amount) -> {
                var output = new Label().setValue(Component.literal(amount.toString() + " " + resource));
                output.textStyle(style -> style.fontSize(4)
                        .textAlignVertical(Vertical.CENTER));
                infoGroup.addChild(output);
            });


            info.addScrollViewChild(buildingInfo);
        }

        if(!activeSelection.input.isEmpty()){
            var buildingInfo = loadTemplate("layouts/building_info_template.ui.nbt").select("#root").findFirst().orElse(null);
            var infoGroup = buildingInfo.select("#info").findFirst().orElse(null);
            infoGroup.setDisplay(false);

            buildingInfo.select("#expander").findFirst().ifPresent(widget -> {
                if(widget instanceof Toggle toggle){
                    toggle.setValue(false);
                    toggle.setOnToggleChanged(state -> {
                        infoGroup.setDisplay(state);
                        buildingInfo.markTaffyStyleDirty();
                    });
                }
            });

            buildingInfo.select("#name").findFirst().ifPresent(widget -> {
                if(widget instanceof Label label){
                    label.setValue(Component.literal("Input"));
                }
            });

            var max = new Label().setValue(Component.literal("Max input at full workers/2h: "));
            max.textStyle(style -> style.fontSize(4.5f)
                    .textAlignVertical(Vertical.CENTER));
            infoGroup.addChild(max);

            activeSelection.input.forEach((resource, amount) -> {
                var input = new Label().setValue(Component.literal(amount.toString() + " " + resource));
                input.textStyle(style -> style.fontSize(4)
                        .textAlignVertical(Vertical.CENTER));
                infoGroup.addChild(input);
            });

            info.addScrollViewChild(buildingInfo);
        }

        if(!activeSelection.storage.isEmpty()){
            var buildingInfo = loadTemplate("layouts/building_info_template.ui.nbt").select("#root").findFirst().orElse(null);
            var infoGroup = buildingInfo.select("#info").findFirst().orElse(null);
            infoGroup.setDisplay(false);

            buildingInfo.select("#expander").findFirst().ifPresent(widget -> {
                if(widget instanceof Toggle toggle){
                    toggle.setValue(false);
                    toggle.setOnToggleChanged(state -> {
                        infoGroup.setDisplay(state);
                        buildingInfo.markTaffyStyleDirty();
                    });
                }
            });

            buildingInfo.select("#name").findFirst().ifPresent(widget -> {
                if(widget instanceof Label label){
                    label.setValue(Component.literal("Storage"));
                }
            });

            var max = new Label().setValue(Component.literal("Max storage: "));
            max.textStyle(style -> style.fontSize(4.5f)
                    .textAlignVertical(Vertical.CENTER));
            infoGroup.addChild(max);

            activeSelection.storage.forEach((resource, amount) -> {
                var storage = new Label().setValue(Component.literal(amount.toString() + " " + resource));
                storage.textStyle(style -> style.fontSize(4)
                        .textAlignVertical(Vertical.CENTER));
                infoGroup.addChild(storage);
            });

            info.addScrollViewChild(buildingInfo);
        }


        root.addEventListener(UIEvents.MOUSE_DOWN, uiEvent -> {});
        buildingInfoPlaceholder.addChild(root);
    }

    private void populateResources(){
        resourcePlaceholder.clearAllScrollViewChildren();

        if(CityInfoManager.popcap != 0){
            var root = loadTemplate("layouts/resource_bar_template.ui.nbt").select("#root_resource").findFirst().orElse(null);
            var limit = CityInfoManager.popcap;
            var current = CityInfoManager.pop;

            root.select("#resource_progress").findFirst().ifPresent(widget -> {
                if(widget instanceof ProgressBar bar){
                    bar.setMaxValue(limit);
                    bar.setValue((float) current);
                }
            });

            root.select("#limit").findFirst().ifPresent(widget ->{
                if(widget instanceof Label label){
                    label.setValue(Component.literal(String.valueOf(limit)));
                }
            });

            root.select("#current").findFirst().ifPresent(widget ->{
                if(widget instanceof Label label){
                    label.setValue(Component.literal(String.valueOf(current)));
                }
            });

            root.select("#resource_name").findFirst().ifPresent(widget ->{
                if(widget instanceof Label label){
                    label.setValue(Component.literal("Population"));
                }
            });

            root.addEventListener(UIEvents.MOUSE_DOWN, uiEvent -> {});
            resourcePlaceholder.addScrollViewChild(root);

        };

        CityInfoManager.stockLimits.forEach((resource, limit) -> {
            var root = loadTemplate("layouts/resource_bar_template.ui.nbt").select("#root_resource").findFirst().orElse(null);
            var current = CityInfoManager.stockCurrent.getOrDefault(resource, 0);

            root.select("#resource_progress").findFirst().ifPresent(widget -> {
                if(widget instanceof ProgressBar bar){
                    bar.setMaxValue(limit);
                    bar.setValue((float) current);
                }
            });

            root.select("#limit").findFirst().ifPresent(widget ->{
                if(widget instanceof Label label){
                    label.setValue(Component.literal(limit.toString()));
                }
            });

            root.select("#current").findFirst().ifPresent(widget ->{
                if(widget instanceof Label label){
                    label.setValue(Component.literal(current.toString()));
                }
            });

            root.select("#resource_name").findFirst().ifPresent(widget ->{
                if(widget instanceof Label label){
                    label.setValue(Component.literal(resource));
                }
            });

            root.addEventListener(UIEvents.MOUSE_DOWN, uiEvent -> {});
            resourcePlaceholder.addScrollViewChild(root);

        });

    }

    public static void updateSelection(BuildingInformation information) {
        activeSelection = information;
        if(information == null) return;
        if(mc.screen instanceof CityScreen screen){
            screen.populateViewer();
        }
    }

    public static void updateResources(){
        if(mc.screen instanceof CityScreen screen){
            screen.populateResources();
            screen.cityFunctions.markTaffyStyleDirty();
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

        if(activeContext != null){
            if(controlHeld){
                if(scrollY > 0){
                    activeContext.selectedBlock = activeContext.selectedBlock.above();
                    return true;
                }
                if(scrollY < 0){
                    activeContext.selectedBlock = activeContext.selectedBlock.below();
                    return true;
                }
            }
        }

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
        if(event.key() == GLFW.GLFW_KEY_LEFT_CONTROL){
            controlHeld = true;
            return true;
        }

        return false;
    }

    @Override
    public boolean keyReleased(KeyEvent event){

        if(event.key() == GLFW.GLFW_KEY_LEFT_CONTROL){
            controlHeld = false;
            return true;
        }

        if(super.keyReleased(event)) { return true; }

        return false;
    }

    @Override
    public void removed(){
        mc.options.hideGui = false;
        super.removed();
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
