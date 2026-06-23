package com.sfmf3.citylogistics.camera.client;

import com.sfmf3.citylogistics.CityLogistics;
import com.sfmf3.citylogistics.blueprint.BlueprintRegistry;
import com.sfmf3.citylogistics.camera.CameraController;
import com.sfmf3.citylogistics.network.ClientPayloadHandler;
import com.sfmf3.citylogistics.network.payload.AddBuildingPayload;
import com.sfmf3.citylogistics.network.payload.AddCityPayload;
import dev.ftb.mods.ftblibrary.client.gui.input.Key;
import dev.ftb.mods.ftblibrary.client.gui.input.MouseButton;
import dev.ftb.mods.ftblibrary.client.gui.theme.Theme;
import dev.ftb.mods.ftblibrary.client.gui.widget.*;
import dev.ftb.mods.ftblibrary.icon.Icon;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.EventBusSubscriber;
import org.apache.logging.log4j.core.pattern.AbstractStyleNameConverter;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.sfmf3.citylogistics.camera.CameraController.mc;
import static com.sfmf3.citylogistics.camera.client.CityClientInfo.*;

@EventBusSubscriber(Dist.CLIENT)
public class CityBuilderScreen extends BaseScreen {

    protected CityInfoPanel cityInfoPanel;
    protected ResourcePanel resourcePanel;
    protected CategoryPanel categoryPanel;
    protected BuildingDrawerPanel drawerPanel;
    protected SelectedBuildingPanel selectedPanel;

    // required for city operations
    private String activeCategory = "";
    private String selectedBuildingId = "";

    private BlockPos selectedBlock = null;
    private String selectedPath = "";
    private Rotation selectedRotation = Rotation.NONE;
    private boolean isMirrored = false;


    public record BuildingDefinition(String buildingId, String displayName, String category){}
    private static final List<BuildingDefinition> BUILDING_REGISTRY = List.of(
            new BuildingDefinition("mine", "Stone Quarry", "Extractors"),
            new BuildingDefinition("lumbermill", "Lumbermill", "Extractors")
    );

    public CityBuilderScreen(){
        CityClientInfo.getInformation();
    }

    @Override
    public void addWidgets() {
        if(cityAnchor == null){
            this.cityInfoPanel = new CityInfoPanel();
            this.add(cityInfoPanel);
            return;
        }

        BlueprintPreview.setBuildings(true);
        this.categoryPanel = new CategoryPanel();
        this.add(categoryPanel);

        this.resourcePanel = new ResourcePanel();
        this.add(resourcePanel);

        if(!activeCategory.isEmpty()){
            this.drawerPanel = new BuildingDrawerPanel();
            this.add(drawerPanel);
        }

        if(!selectedBuildingId.isEmpty()){
            this.selectedPanel = new SelectedBuildingPanel();
            this.add(selectedPanel);
        }

    }

    @Override
    public boolean onInit(){
        setFullscreen();
        return super.onInit();
    }

    @Override
    public boolean mousePressed(MouseButton button){

        if(button.isLeft() && !this.isMouseOverAnyWidget()){
            HitResult hit = mc.getCameraEntity().pick(64, 1.0F, false);
            if (hit.getType() == HitResult.Type.BLOCK) {
                selectedBlock = ((BlockHitResult) hit).getBlockPos();
                mc.player.sendSystemMessage(Component.literal("Setting point "
                        + selectedBlock.getX() + ", "
                        + selectedBlock.getY() + ", "
                        + selectedBlock.getZ() + "!"
                ));
            } else{ selectedBlock = null; }

        }

        if(CameraController.isAnchorActive() && button.isRight()) {
            GLFW.glfwSetInputMode(mc.getWindow().handle(), GLFW.GLFW_CURSOR, GLFW.GLFW_CURSOR_DISABLED);
            return true;
        }
        return super.mousePressed(button);

    }

    @Override
    public void mouseReleased(MouseButton button){
        if((CameraController.isAnchorActive()) && button.isRight()){
            GLFW.glfwSetInputMode(mc.getWindow().handle(), GLFW.GLFW_CURSOR, GLFW.GLFW_CURSOR_NORMAL);
        }
        super.mouseReleased(button);

    }

    @Override
    public boolean mouseScrolled(double scroll){
        if(super.mouseScrolled(scroll)) { return true; }

        if(this.isMouseOverAnyWidget()){ return false; }

        if(CameraController.isAnchorActive()) {
            CameraController.getAnchor().handleZoom(scroll);
            return true;
        }

        return false;
    }

    @Override
    public boolean mouseDragged(int button, double dragX, double dragY){
        if(button == 1){
            if(mc.getCameraEntity() != null){
                mc.player.turn(dragX, dragY);
            }
            return true;
        }
        return super.mouseDragged(button, dragX, dragY);
    }

    @Override
    public boolean keyPressed(Key event){
        if(keybindPressed(ModKeys.CAMERA_ANCHOR, event)){
            CameraController.anchorToggle();
            return true;
        }


        return super.keyPressed(event);
    }

    @Override
    public void onClosed(){
        super.onClosed();
        BlueprintPreview.setBuildings(false);
        BlueprintPreview.clear();
    }

    private boolean keybindPressed(KeyMapping binding, Key event){
        return event.event().input() == binding.getKey().getValue();
    }

    private void updatePreviewState() {
        BlueprintPreview.update(
                this.selectedBlock,
                this.selectedBuildingId,
                this.selectedPath,
                this.selectedRotation,
                this.isMirrored
        );
    }

    // removes ugly background
    @Override
    public void drawBackground(GuiGraphicsExtractor graphics, Theme theme, int x, int y, int w, int h){}

    // removes weird gray background
    @Override
    public boolean drawDefaultBackground(GuiGraphicsExtractor graphics) { return false; }

    // removes background blur
    @Override
    public boolean shouldRenderBlur(){ return false; }

    protected class CategoryPanel extends Panel {
        private final int btnWidth = 75;
        private final int btnHeight = 20;
        private final int spacing = 2;

        protected int panelW;
        protected int panelH;
        protected int panelX;
        protected int panelY;

        public CategoryPanel() {
            super(CityBuilderScreen.this);
            panelW = (btnWidth * 2) + spacing;
            panelH = (btnHeight * 3) + (spacing * 2);
            panelX = 4;
            panelY = getParent().height - panelH - panelX;
            this.setPosAndSize(panelX, panelY, panelW, panelH);
        }

        @Override
        public void addWidgets() {
            String[] categories = {
                    "Primary", "Housing",
                    "Extractors", "Producers",
                    "Warehouses", "Misc."};
            for (int i = 0; i < categories.length; i++) {
                String cat = categories[i];
                int gridX = (i % 2) * (btnWidth + spacing);
                int gridY = (i / 2) * (btnHeight + spacing);

                SimpleTextButton btn = new SimpleTextButton(this, Component.literal(cat), Icon.empty()) {
                    @Override
                    public void onClicked(MouseButton mouseButton) {
                        activeCategory = activeCategory.equals(cat) ? "" : cat;
                        CityBuilderScreen.this.refreshWidgets();
                    }
                };
                btn.setPosAndSize(gridX, gridY, btnWidth, btnHeight);
                add(btn);
            }
        }
        @Override
        public void alignWidgets() {}
    }

    protected class BuildingDrawerPanel extends Panel {
        private final int btnWidth = 50;
        private final int btnHeight = 20;
        private final int spacing = 8;
        private ScrollBar scrollBar;

        protected int drawerX;
        protected int drawerY;
        protected int drawerW;
        protected int drawerH;

        public BuildingDrawerPanel() {
            super(CityBuilderScreen.this);
            drawerX = categoryPanel.posX + (categoryPanel.panelW + spacing);
            drawerW = getParent().width - (drawerX + spacing);
            drawerH = (btnHeight * 2) + spacing;
            drawerY = getParent().height - drawerH;


            this.setPosAndSize(drawerX, drawerY, drawerW, drawerH);
        }

        @Override
        public void drawBackground(GuiGraphicsExtractor graphics, Theme theme, int x, int y, int w, int h){
            theme.drawPanelBackground(graphics, x, y, w, h);
        }

        @Override
        public void addWidgets() {
            scrollBar = new ScrollBar(this, ScrollBar.Plane.VERTICAL, 12);
            scrollBar.setPosAndSize(width - 3, 0, 10, height);
            add(scrollBar);

            int index = 0;
            for (BuildingDefinition def : BUILDING_REGISTRY) {
                if (def.category().equals(activeCategory)) {
                    int gridX = (index % 2) * (btnWidth + spacing);
                    int gridY = (index / 2) * (btnHeight + spacing);

                    SimpleTextButton btn = new SimpleTextButton(this, Component.literal(def.displayName()), Icon.empty()) {
                        @Override
                        public void onClicked(MouseButton mouseButton) {
                            selectedBuildingId = def.buildingId();
                            CityBuilderScreen.this.refreshWidgets();
                        }
                    };
                    btn.setPosAndSize(gridX, gridY, btnWidth, btnHeight);
                    add(btn);
                    index++;
                }
            }

            int totalRows = (int) Math.ceil(index / 2.0);
            int totalContentHeight = totalRows * (btnHeight + spacing);
            scrollBar.setMaxValue(Math.max(0, totalContentHeight - height));
        }

        @Override
        public double getScrollY() { return scrollBar != null ? scrollBar.getValue() : 0; }
        @Override
        public void setScrollY(double scroll) { if (scrollBar != null) scrollBar.setValue(scroll); }
        @Override
        public void alignWidgets() {}
    }

    protected class SelectedBuildingPanel extends Panel {
        private final int spacing = 10;
        private final int btnX = 60;
        private final int btnY = 15;

        protected int panelW;
        protected int panelH;
        protected int panelX;
        protected int panelY;

        private final List<String> availablePaths;

        public SelectedBuildingPanel() {
            super(CityBuilderScreen.this);
            panelW = 150;
            panelH = (getParent().height / 2);
            panelX = getParent().width - (panelW + spacing);
            panelY = getParent().height - (panelH + drawerPanel.drawerH + spacing);

            this.setPosAndSize(panelX, panelY, panelW, panelH);

            this.availablePaths = BlueprintRegistry.getPaths(selectedBuildingId);
            if (selectedPath.isEmpty() || !availablePaths.contains(selectedPath)) {
                selectedPath = availablePaths.isEmpty() ? "" : availablePaths.getFirst();
            }
        }

        @Override
        public void drawBackground(GuiGraphicsExtractor graphics, Theme theme, int x, int y, int w, int h){
            theme.drawPanelBackground(graphics, x, y, w, h);
        }

        @Override
        public void addWidgets() {
            SimpleTextButton dropdown = new SimpleTextButton(this, Component.literal(selectedPath), Icon.empty()) {
                @Override
                public void onClicked(MouseButton mouseButton) {
                    List<ContextMenuItem> items = new ArrayList<>();
                    for (String path : availablePaths) {
                        items.add(new ContextMenuItem(Component.literal(path), Icon.empty(), (i) -> {
                            selectedPath = path;
                            this.setTitle(Component.literal(path));
                        }));
                    }
                    getGui().openContextMenu(items);
                }
            };
            dropdown.setPosAndSize((width - btnX)/2, 5, btnX, btnY);
            add(dropdown);

            SimpleTextButton preview = new SimpleTextButton(this, Component.literal("Preview"), Icon.empty()) {
                @Override
                public void onClicked(MouseButton mouseButton) {
                    updatePreviewState();
                }
            };
            preview.setPosAndSize((width - (btnX*2)), height - btnY - 3, btnX, btnY);
            add(preview);

            SimpleTextButton build = new SimpleTextButton(this, Component.literal("Build"), Icon.empty()) {
                @Override
                public void onClicked(MouseButton mouseButton) {
                    if(!selectedPath.isEmpty() && selectedBlock != null){
                        mc.player.connection.send(new AddBuildingPayload(
                                cityAnchor,
                                selectedBlock,
                                selectedPath,
                                selectedRotation,
                                isMirrored,
                                selectedBuildingId
                        ));
                    }
                }
            };
            build.setPosAndSize((width - btnX), height - btnY - 3, btnX, btnY);
            add(build);
        }

        @Override
        public void alignWidgets() {}
    }

    protected class ResourcePanel extends Panel {
        protected int btnW = 75;
        protected int btnH = 20;
        protected int spacing = 2;

        protected int panelX;
        protected int panelY;
        protected int panelW;
        protected int panelH;

        public ResourcePanel(){
            super(CityBuilderScreen.this);
            panelX = spacing;
            panelY = spacing;
            panelW = btnW + (spacing * 2);
            panelH = (btnH * 4) + (spacing * 3);

            this.setPosAndSize(panelX, panelY, panelW, panelH);
        }

        @Override
        public void addWidgets(){
            if(stockCurrent == null) {return;}
            int i = 0;

            for(Map.Entry<String, Integer> entry : stockCurrent.entrySet()){
                String itemKey = entry.getKey();
                int limit = stockLimits.get(itemKey);
                int current = entry.getValue();
                int gridY = i * (btnH + spacing);

                String textDisplay = itemKey + ": " + current + "/" + limit;
                TextBox text = new TextBox(this){

                };
                text.setText(textDisplay);
                text.setY(gridY);
                this.add(text);
                i++;
            }
        }

        @Override
        public void alignWidgets() {}
    }

    protected class CityInfoPanel extends Panel{

        protected int panelX;
        protected int panelY;
        protected int panelW;
        protected int panelH;

        public CityInfoPanel() {
            super(CityBuilderScreen.this);
            panelW = 300;
            panelH = 100;
            panelX = (getParent().width - panelW) / 2;
            panelY = 0;

            this.setPosAndSize(panelX, panelY, panelW, panelH);
        }

        @Override
        public void addWidgets() {
            // if no city being edited, create simple addCity helper
            if(stockCurrent == null){
                int textW = 100;
                int textH = 20;
                TextBox textBox = new TextBox(this){};
                textBox.setPosAndSize((panelW - textW)/2, 0, textW, textH);
                add(textBox);

                SimpleTextButton createButton = new SimpleTextButton(this, Component.literal("New city..."), Icon.empty()) {
                    @Override
                    public void onClicked(MouseButton mouseButton) {
                        if(!textBox.getText().isEmpty()){
                            mc.player.connection.send(new AddCityPayload(mc.player.blockPosition(), textBox.getText()));
                        }
                    }
                };
                createButton.setPosAndSize(textBox.posX, textBox.posY + textH + 5, textW, textH);
                add(createButton);
            }
            else{

            }

        }

        @Override
        public void alignWidgets() {

        }
    }
}
