package com.britakee.actionmenus.menu;

import net.minecraft.core.NonNullList;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * Server-side container for ActionMenus.
 * This is a simple container that doesn't persist and prevents item extraction.
 */
public class ActionMenuContainer implements Container {
    
    private final NonNullList<ItemStack> items;
    private final int size;
    
    public ActionMenuContainer(int size) {
        this.size = size;
        this.items = NonNullList.withSize(size, ItemStack.EMPTY);
    }
    
    @Override
    public int getContainerSize() {
        return size;
    }
    
    @Override
    public boolean isEmpty() {
        for (ItemStack stack : items) {
            if (!stack.isEmpty()) {
                return false;
            }
        }
        return true;
    }
    
    @Override
    public ItemStack getItem(int slot) {
        if (slot >= 0 && slot < items.size()) {
            return items.get(slot);
        }
        return ItemStack.EMPTY;
    }
    
    @Override
    public ItemStack removeItem(int slot, int amount) {
        // Prevent item removal - this is a display-only container
        return ItemStack.EMPTY;
    }
    
    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        // Prevent item removal
        return ItemStack.EMPTY;
    }
    
    @Override
    public void setItem(int slot, ItemStack stack) {
        if (slot >= 0 && slot < items.size()) {
            items.set(slot, stack);
        }
    }
    
    @Override
    public void setChanged() {
        // No-op for virtual container
    }
    
    @Override
    public boolean stillValid(Player player) {
        return true;
    }
    
    @Override
    public void clearContent() {
        items.clear();
    }
    
    /**
     * Check if a slot is within bounds.
     */
    public boolean isValidSlot(int slot) {
        return slot >= 0 && slot < size;
    }
    
    /**
     * Get all items as a list.
     */
    public NonNullList<ItemStack> getItems() {
        return items;
    }
}
