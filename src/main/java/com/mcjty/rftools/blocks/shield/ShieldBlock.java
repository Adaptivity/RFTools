package com.mcjty.rftools.blocks.shield;

import com.mcjty.container.GenericContainerBlock;
import com.mcjty.container.WrenchUsage;
import com.mcjty.rftools.RFTools;
import com.mcjty.rftools.blocks.ModBlocks;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;

/**
 * TODO: Shield configuration:
 *   Base cost: 10RF/tick
 *   - Visual appearances:
 *         - Invisible (0RF/tick extra cost)
 *         - Solid block (2RF/tick)
 *         - Shimmering Shield (4RF/tick)
 *   - Collision:
 *         - Pass through (0RF/tick)
 *         - Solid (2RF/tick)
 *         - Solid selective (3RF/tick)
 *         - Low Damage (10RF/tick)
 *         - Low Damage selective (11RF/tick)
 *         - High Damage (30RF/tick)
 *         - High Damage selective (31RF/tick)
 *   - Damage Burst:
 *         - Low: 500RF
 *         - High: 2000RF
 */

public class ShieldBlock extends GenericContainerBlock {

    private IIcon shieldIcon;

    public static int RENDERID_SHIELD;

    // Current rendering pass for our custom renderer.
    public static int currentPass;


    public ShieldBlock(Material material) {
        super(material, ShieldTileEntity.class);
        setBlockName("shieldBlock");
    }

    @Override
    public int getGuiID() {
        return RFTools.GUI_SHIELD;
    }

    @Override
    public boolean renderAsNormalBlock() {
        return false;
    }

    @Override
    public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase entityLivingBase, ItemStack itemStack) {
        restoreBlockFromNBT(world, x, y, z, itemStack);

        world.setBlock(x, y + 1, z, ModBlocks.invisibleShieldBlock);
        world.setBlock(x, y + 2, z, ModBlocks.invisibleShieldBlock);
        world.setBlock(x, y + 3, z, ModBlocks.invisibleShieldBlock);
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float sidex, float sidey, float sidez) {
        WrenchUsage wrenchUsed = testWrenchUsage(x, y, z, player);
        if (wrenchUsed == WrenchUsage.NORMAL) {
            // Nothing? Surely not rotate.
            return true;
        } else if (wrenchUsed == WrenchUsage.SNEAKING) {
            breakAndRemember(world, x, y, z);
            return true;
        } else {
            return openGui(world, x, y, z, player);
        }
    }

    @Override
    public void breakBlock(World world, int x, int y, int z, Block block, int meta) {
        super.breakBlock(world, x, y, z, block, meta);

        if (ModBlocks.invisibleShieldBlock.equals(world.getBlock(x, y+1, z))) {
            world.setBlockToAir(x, y+1, z);
        }
        if (ModBlocks.invisibleShieldBlock.equals(world.getBlock(x, y+2, z))) {
            world.setBlockToAir(x, y+2, z);
        }
        if (ModBlocks.invisibleShieldBlock.equals(world.getBlock(x, y+3, z))) {
            world.setBlockToAir(x, y+3, z);
        }
    }

    @Override
    public boolean isOpaqueCube() {
        return false;
    }

    @Override
    public boolean canRenderInPass(int pass) {
        // Our renderer needs to work in both transparent as solid pass so we store the current pass here.
        currentPass = pass;
        return pass == 0 || pass == 1;
    }

    @Override
    public int getRenderType() {
        return RENDERID_SHIELD;
    }

    @Override
    public void registerBlockIcons(IIconRegister iconRegister) {
        iconSide = iconRegister.registerIcon(RFTools.MODID + ":" + "machineShieldProjector");
        shieldIcon = iconRegister.registerIcon(RFTools.MODID + ":" + "shieldtexture");
    }

    public IIcon getShieldIcon() {
        return shieldIcon;
    }
}