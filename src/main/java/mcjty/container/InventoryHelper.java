package mcjty.container;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;

import java.util.List;

public class InventoryHelper {
    private final TileEntity tileEntity;
    private final ContainerFactory containerFactory;
    private final ItemStack stacks[];

    public InventoryHelper(TileEntity tileEntity, ContainerFactory containerFactory, int count) {
        this.tileEntity = tileEntity;
        this.containerFactory = containerFactory;
        stacks = new ItemStack[count];
    }

    public static class SlotModifier {
        private final int slot;
        private final ItemStack old;

        public SlotModifier(int slot, ItemStack old) {
            this.slot = slot;
            this.old = old;
        }

        public int getSlot() {
            return slot;
        }

        public ItemStack getOld() {
            return old;
        }
    }

    /**
     * Merges provided ItemStack with the first available one in this inventory. It will return the amount
     * of items that could not be merged. Also fills the undo buffer in case you want to undo the operation.
     */
    public static int mergeItemStack(IInventory inventory, ItemStack result, int start, int stop, List<SlotModifier> undo) {
        int k = start;

        ItemStack itemstack1;
        int itemsToPlace = result.stackSize;

        if (result.isStackable()) {
            while (itemsToPlace > 0 && (k < stop)) {
                itemstack1 = inventory.getStackInSlot(k);

                if (itemstack1 != null && itemstack1.getItem() == result.getItem() && (!result.getHasSubtypes() || result.getItemDamage() == itemstack1.getItemDamage()) && ItemStack.areItemStackTagsEqual(result, itemstack1)) {
                    int l = itemstack1.stackSize + itemsToPlace;

                    if (l <= result.getMaxStackSize()) {
                        if (undo != null) {
                            undo.add(new SlotModifier(k, itemstack1.copy()));
                        }
                        itemsToPlace = 0;
                        itemstack1.stackSize = l;
                        inventory.markDirty();
                    } else if (itemstack1.stackSize < result.getMaxStackSize()) {
                        if (undo != null) {
                            undo.add(new SlotModifier(k, itemstack1.copy()));
                        }
                        itemsToPlace -= result.getMaxStackSize() - itemstack1.stackSize;
                        itemstack1.stackSize = result.getMaxStackSize();
                        inventory.markDirty();
                    }
                }

                ++k;
            }
        }

        if (itemsToPlace > 0) {
            k = start;

            while (k < stop) {
                itemstack1 = inventory.getStackInSlot(k);

                if (itemstack1 == null) {
                    if (undo != null) {
                        undo.add(new SlotModifier(k, null));
                    }
                    inventory.setInventorySlotContents(k, result.copy());
                    inventory.markDirty();
                    itemsToPlace = 0;
                    break;
                }

                ++k;
            }
        }

        return itemsToPlace;
    }

    public ItemStack[] getStacks() {
        return stacks;
    }

    public ItemStack decrStackSize(int index, int amount) {
        if (containerFactory.isGhostSlot(index) || containerFactory.isGhostOutputSlot(index)) {
            ItemStack old = stacks[index];
            stacks[index] = null;
            if (old == null) {
                return null;
            }
            old.stackSize = 0;
            return old;
        } else {
            if (stacks[index] != null) {
                if (stacks[index].stackSize <= amount) {
                    ItemStack old = stacks[index];
                    stacks[index] = null;
                    tileEntity.markDirty();
                    return old;
                }
                ItemStack its = stacks[index].splitStack(amount);
                if (stacks[index].stackSize == 0) {
                    stacks[index] = null;
                }
                tileEntity.markDirty();
                return its;
            }
            return null;
        }
    }

    public void setInventorySlotContents(int stackLimit, int index, ItemStack stack) {
        if (containerFactory.isGhostSlot(index)) {
            if (stack != null) {
                stacks[index] = stack.copy();
                if (index < 9) {
                    stacks[index].stackSize = 1;
                }
            } else {
                stacks[index] = null;
            }
        } else if (containerFactory.isGhostOutputSlot(index)) {
            if (stack != null) {
                stacks[index] = stack.copy();
            } else {
                stacks[index] = null;
            }
        } else {
            stacks[index] = stack;
            if (stack != null && stack.stackSize > stackLimit) {
                stack.stackSize = stackLimit;
            }
            tileEntity.markDirty();
        }
    }
}
