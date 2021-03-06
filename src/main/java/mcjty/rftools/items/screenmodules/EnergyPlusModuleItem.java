package mcjty.rftools.items.screenmodules;

import mcjty.rftools.BlockInfo;
import mcjty.rftools.RFTools;
import mcjty.rftools.blocks.screens.ModuleProvider;
import mcjty.rftools.blocks.screens.modules.EnergyPlusBarScreenModule;
import mcjty.rftools.blocks.screens.modules.ScreenModule;
import mcjty.rftools.blocks.screens.modulesclient.ClientScreenModule;
import mcjty.rftools.blocks.screens.modulesclient.EnergyPlusBarClientScreenModule;
import mcjty.varia.EnergyTools;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.World;

import java.util.List;

public class EnergyPlusModuleItem extends Item implements ModuleProvider {

    public EnergyPlusModuleItem() {
        setMaxStackSize(1);
    }

    @Override
    public int getMaxItemUseDuration(ItemStack stack) {
        return 1;
    }

    @Override
    public Class<? extends ScreenModule> getServerScreenModule() {
        return EnergyPlusBarScreenModule.class;
    }

    @Override
    public Class<? extends ClientScreenModule> getClientScreenModule() {
        return EnergyPlusBarClientScreenModule.class;
    }

    @Override
    public String getName() {
        return "RF";
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack itemStack, EntityPlayer player, List list, boolean whatIsThis) {
        super.addInformation(itemStack, player, list, whatIsThis);
        list.add(EnumChatFormatting.GREEN + "Uses " + EnergyPlusBarScreenModule.RFPERTICK + " RF/tick");
        boolean hasTarget = false;
        NBTTagCompound tagCompound = itemStack.getTagCompound();
        if (tagCompound != null) {
            list.add(EnumChatFormatting.YELLOW + "Label: " + tagCompound.getString("text"));
            if (tagCompound.hasKey("monitorx")) {
                int dim = tagCompound.getInteger("dim");
                int monitorx = tagCompound.getInteger("monitorx");
                int monitory = tagCompound.getInteger("monitory");
                int monitorz = tagCompound.getInteger("monitorz");
                String monitorname = tagCompound.getString("monitorname");
                list.add(EnumChatFormatting.YELLOW + "Monitoring: " + monitorname + " (at " + monitorx + "," + monitory + "," + monitorz + ")");
                list.add(EnumChatFormatting.YELLOW + "Dimension: " + dim);
                hasTarget = true;
            }
        }
        if (!hasTarget) {
            list.add(EnumChatFormatting.YELLOW + "Sneak right-click on a machine to set the");
            list.add(EnumChatFormatting.YELLOW + "target for this energy module");
        }
    }

    @Override
    public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float sx, float sy, float sz) {
        TileEntity te = world.getTileEntity(x, y, z);
        NBTTagCompound tagCompound = stack.getTagCompound();
        if (tagCompound == null) {
            tagCompound = new NBTTagCompound();
        }
        if (EnergyTools.isEnergyTE(te)) {
            tagCompound.setInteger("dim", world.provider.dimensionId);
            tagCompound.setInteger("monitorx", x);
            tagCompound.setInteger("monitory", y);
            tagCompound.setInteger("monitorz", z);
            Block block = player.worldObj.getBlock(x, y, z);
            String name = "<invalid>";
            if (block != null && !block.isAir(world, x, y, z)) {
                name = BlockInfo.getReadableName(block, world.getBlockMetadata(x, y, z));
            }
            tagCompound.setString("monitorname", name);
            if (world.isRemote) {
                RFTools.message(player, "Energy module is set to block '" + name + "'");
            }
        } else {
            tagCompound.removeTag("dim");
            tagCompound.removeTag("monitorx");
            tagCompound.removeTag("monitory");
            tagCompound.removeTag("monitorz");
            tagCompound.removeTag("monitorname");
            if (world.isRemote) {
                RFTools.message(player, "Energy module is cleared");
            }
        }
        stack.setTagCompound(tagCompound);
        return true;
    }
}