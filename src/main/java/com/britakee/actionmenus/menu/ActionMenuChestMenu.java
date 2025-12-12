package com.britakee.actionmenus.menu;

import com.britakee.actionmenus.ActionMenus;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.server.level.ServerPlayer;

/**
 * Custom chest menu that intercepts clicks and forwards them to ActionMenus.
 * Prevents item theft and handles click actions.
 */
public class ActionMenuChestMenu extends ChestMenu {
    
    private final String menuId;
    
    public ActionMenuChestMenu(MenuType<ChestMenu> type, int containerId, Inventory playerInventory, 
                                Container container, int rows, String menuId) {
        super(type, containerId, playerInventory, container, rows);
        this.menuId = menuId;
    }
    
    @Override
    public void clicked(int slotId, int button, ClickType clickType, Player player) {
        // Check if this is a menu slot (not player inventory)
        if (slotId >= 0 && slotId < this.getContainer().getContainerSize()) {
            // This is a menu slot - handle the click
            if (player instanceof ServerPlayer serverPlayer) {
                handleMenuClick(serverPlayer, slotId, button, clickType);
            }
            // Don't call super - prevents item movement
            return;
        }
        
        // For player inventory slots, prevent shift-clicking into menu
        if (clickType == ClickType.QUICK_MOVE) {
            // Prevent shift-click from moving items
            return;
        }
        
        // Allow normal inventory operations in player's own inventory
        // But still prevent any item from entering the menu container
        if (slotId >= this.getContainer().getContainerSize()) {
            // This is a player inventory slot - allow normal behavior
            // but monitor for any attempts to move items to menu
            super.clicked(slotId, button, clickType, player);
        }
    }
    
    /**
     * Handle a click on a menu slot.
     */
    private void handleMenuClick(ServerPlayer player, int slot, int button, ClickType clickType) {
        // Convert to our ClickType
        com.britakee.actionmenus.menu.ClickType actionClickType = 
                com.britakee.actionmenus.menu.ClickType.fromMinecraft(clickType, button, player.isShiftKeyDown());
        
        // Forward to MenuManager
        ActionMenus.getInstance().getMenuManager().handleClick(player, slot, actionClickType);
    }
    
    @Override
    public ItemStack quickMoveStack(Player player, int slot) {
        // Prevent shift-click item movement
        return ItemStack.EMPTY;
    }
    
    @Override
    public boolean stillValid(Player player) {
        return true;
    }
    
    /**
     * Get the menu ID.
     */
    public String getMenuId() {
        return menuId;
    }
}
