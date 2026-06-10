package com.hbm.inventory.container;

import com.hbm.api.ntl.StackCache;
import com.hbm.api.ntl.StackCache.CacheSlot;
import com.hbm.inventory.slot.SlotNonRetarded;
import com.hbm.tileentity.network.TileEntityPneumoStorageAccess;
import com.hbm.util.ItemStackUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.items.wrapper.InvWrapper;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ContainerPneumoStorageAccess extends Container {

    protected TileEntityPneumoStorageAccess access;
    protected InventoryPneumoStorageAccess inventory;

    public static final String STACK_SIZE_KEY = "PNEUMO_STACK_SIZE";

    public ContainerPneumoStorageAccess(InventoryPlayer invPlayer, TileEntityPneumoStorageAccess access) {
        this.access = access;
        this.inventory = new InventoryPneumoStorageAccess(access);

        InvWrapper displayWrapper = new InvWrapper(inventory);
        for (int i = 0; i < 6; i++) {
            for (int j = 0; j < 8; j++) {
                this.addSlotToContainer(new SlotNonRetarded(displayWrapper, j + i * 8, 8 + j * 18, 17 + i * 18)); // TODO: add a new slot type that holds a long for the amount
            }
        }

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 9; j++) {
                this.addSlotToContainer(new Slot(invPlayer, j + i * 9 + 9, 8 + j * 18, 169 + i * 18));
            }
        }

        for (int i = 0; i < 9; i++) {
            this.addSlotToContainer(new Slot(invPlayer, i, 8 + i * 18, 227));
        }

        inventory.updateListing();
        this.detectAndSendChanges();
    }

    @Override
    public boolean canInteractWith(EntityPlayer player) {
        return player.getDistanceSq(access.getPos().getX() + 0.5D, access.getPos().getY() + 0.5D, access.getPos().getZ() + 0.5D) <= 15D * 15D;
    }

    @Override
    public ItemStack slotClick(int index, int button, ClickType mode, EntityPlayer player) {

        if (index >= 0 && index < 6 * 8) {
            boolean client = player.world.isRemote;
            Slot slot = this.getSlot(index);
            ItemStack held = player.inventory.getItemStack();

            if (held.isEmpty() && slot.getHasStack() && slot.getStack().hasTagCompound()) {
                ItemStack stack = slot.getStack().copy();

                if (button == 0) {
                    int toGrab = (int) Math.min(stack.getMaxStackSize(), stack.getTagCompound().getLong(STACK_SIZE_KEY));

                    if (client) {
                        stack.setCount(toGrab);
                        player.inventory.setItemStack(stack);
                    } else {
                        if (this.access.cache == null || this.access.cache.hasExpired) return stack;
                        StackCache cache = this.access.cache;
                        stack.setCount((int) cache.consumeItemsAndReturnQuantity(stack, toGrab)); // this can't work because the stack got altered with the description NBT.....
                        player.inventory.setItemStack(stack);
                    }
                }

                return slot.getStack().copy();
            }
        }

        return super.slotClick(index, button, mode, player);
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer player, int index) {
        return ItemStack.EMPTY;
    }

    /** This inventory instance only exists to prepare the contents of a StackCache in such a way that we can use it in a container. */
    public static class InventoryPneumoStorageAccess implements IInventory {

        public StackCache cache;
        public ItemStack[] slots;

        public InventoryPneumoStorageAccess(TileEntityPneumoStorageAccess access) {
            this.slots = new ItemStack[getSizeInventory()];
            for (int i = 0; i < slots.length; i++) slots[i] = ItemStack.EMPTY;
            this.cache = access.cache;
        }

        public void updateListing() { // DEMO
            if (this.cache == null) return;
            List<CacheSlot> cacheSlots = new ArrayList<>(cache.cacheSlots.size());
            cacheSlots.addAll(cache.cacheSlots.values());
            cacheSlots.removeIf(x -> x.stacksize <= 0);
            cacheSlots.sort(SORT_BY_STACK_SIZE);
            int size = cacheSlots.size();

            for (int i = 0; i < slots.length; i++) {
                if (i < size) {
                    CacheSlot cache = cacheSlots.get(i);
                    if (cache.displayStack != null) {
                        slots[i] = cache.displayStack.copy();
                        ItemStackUtil.addTooltipToStack(slots[i], "x" + cache.stacksize, "in " + cache.monitors.size() + " stacks");
                        slots[i].getTagCompound().setLong(STACK_SIZE_KEY, cache.stacksize); // TODO instead of altering the stacks so we can't resolve anything anymore, hijack the progress bar system
                    }
                }
            }
        }

        public static final Comparator<CacheSlot> SORT_BY_STACK_SIZE = (o1, o2) -> {
            if (o1.stacksize > o2.stacksize)        return 1;   if (o1.stacksize < o2.stacksize)        return -1;
            if (o1.itemId < o2.itemId)              return 1;   if (o1.itemId > o2.itemId)              return -1;
            if (o1.meta < o2.meta)                  return 1;   if (o1.meta > o2.meta)                  return -1;
            if (o1.nbt == null && o2.nbt != null)   return 1;   if (o1.nbt != null && o2.nbt == null)   return -1;
            return 0;
        };

        @Override public int getSizeInventory() { return 6 * 9; }
        @Override public ItemStack getStackInSlot(int slot) { return slots[slot]; }
        @Override public int getInventoryStackLimit() { return 64; }

        @Override
        public boolean isEmpty() {
            for (ItemStack stack : slots) if (!stack.isEmpty()) return false;
            return true;
        }

        @Override public ItemStack decrStackSize(int slot, int amount) { return ItemStack.EMPTY; }

        @Override
        public ItemStack removeStackFromSlot(int slot) {
            ItemStack stack = slots[slot];
            slots[slot] = ItemStack.EMPTY;
            return stack;
        }

        @Override
        public void setInventorySlotContents(int slot, ItemStack stack) {
            this.slots[slot] = stack;
        }

        @Override public String getName() { return "null"; }
        @Override public boolean hasCustomName() { return false; }
        @Override public ITextComponent getDisplayName() { return new TextComponentString(getName()); }
        @Override public boolean isItemValidForSlot(int slot, ItemStack stack) { return false; }

        @Override public void markDirty() { }

        @Override public boolean isUsableByPlayer(EntityPlayer player) { return true; }

        @Override public void openInventory(EntityPlayer player) { }
        @Override public void closeInventory(EntityPlayer player) { }

        @Override public int getField(int id) { return 0; }
        @Override public void setField(int id, int value) { }
        @Override public int getFieldCount() { return 0; }

        @Override
        public void clear() {
            for (int i = 0; i < slots.length; i++) slots[i] = ItemStack.EMPTY;
        }
    }
}
