package mcjty.rftools.blocks.dimlets;

import mcjty.container.GenericGuiContainer;
import mcjty.gui.Window;
import mcjty.gui.layout.PositionalLayout;
import mcjty.gui.widgets.EnergyBar;
import mcjty.gui.widgets.ImageLabel;
import mcjty.gui.widgets.Panel;
import mcjty.gui.widgets.Widget;
import mcjty.rftools.RFTools;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.ForgeDirection;

import java.awt.*;

public class GuiDimletResearcher extends GenericGuiContainer<DimletResearcherTileEntity> {
    public static final int RESEARCHER_WIDTH = 180;
    public static final int RESEARCHER_HEIGHT = 152;

    private EnergyBar energyBar;
    private ImageLabel arrow;

    private static final ResourceLocation iconLocation = new ResourceLocation(RFTools.MODID, "textures/gui/dimletresearcher.png");
    private static final ResourceLocation iconGuiElements = new ResourceLocation(RFTools.MODID, "textures/gui/guielements.png");

    public GuiDimletResearcher(DimletResearcherTileEntity dimletResearcherTileEntity, DimletResearcherContainer container) {
        super(dimletResearcherTileEntity, container);
        dimletResearcherTileEntity.setCurrentRF(dimletResearcherTileEntity.getEnergyStored(ForgeDirection.DOWN));

        xSize = RESEARCHER_WIDTH;
        ySize = RESEARCHER_HEIGHT;
    }

    @Override
    public void initGui() {
        super.initGui();

        int maxEnergyStored = tileEntity.getMaxEnergyStored(ForgeDirection.DOWN);
        energyBar = new EnergyBar(mc, this).setVertical().setMaxValue(maxEnergyStored).setLayoutHint(new PositionalLayout.PositionalHint(10, 7, 8, 54)).setShowText(false);
        energyBar.setValue(tileEntity.getCurrentRF());

        arrow = new ImageLabel(mc, this).setImage(iconGuiElements, 192, 0);
        arrow.setLayoutHint(new PositionalLayout.PositionalHint(90, 26, 16, 16));

        Widget toplevel = new Panel(mc, this).setBackground(iconLocation).setLayout(new PositionalLayout()).addChild(energyBar).addChild(arrow);
        toplevel.setBounds(new Rectangle(guiLeft, guiTop, xSize, ySize));

        window = new Window(this, toplevel);
        tileEntity.requestRfFromServer();
        tileEntity.requestResearchingFromServer();
    }


    @Override
    protected void drawGuiContainerBackgroundLayer(float v, int i, int i2) {
        int researching = tileEntity.getResearching();
        if (researching > 0) {
            arrow.setImage(iconGuiElements, 144, 0);
        } else {
            arrow.setImage(iconGuiElements, 192, 0);
        }

        window.draw();

        energyBar.setValue(tileEntity.getCurrentRF());

        tileEntity.requestRfFromServer();
        tileEntity.requestResearchingFromServer();
    }
}
