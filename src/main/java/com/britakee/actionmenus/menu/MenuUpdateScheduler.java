package com.britakee.actionmenus.menu;

import com.britakee.actionmenus.ActionMenus;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/**
 * Scheduler that handles automatic menu updates.
 * Runs every tick and updates menus that need refreshing.
 */
public class MenuUpdateScheduler {
    
    private final MenuManager menuManager;
    private long currentTick = 0;
    
    public MenuUpdateScheduler(MenuManager menuManager) {
        this.menuManager = menuManager;
    }
    
    @SubscribeEvent
    public void onServerTick(ServerTickEvent.Post event) {
        currentTick++;
        
        // Process all active sessions
        for (MenuSession session : menuManager.getActiveSessions()) {
            // Check if this menu needs updating
            if (!session.getMenu().shouldAutoUpdate()) {
                continue;
            }
            
            // Check if enough time has passed since last update
            if (!session.shouldUpdate(currentTick)) {
                continue;
            }
            
            // Get the player
            ServerPlayer player = event.getServer()
                    .getPlayerList()
                    .getPlayer(session.getPlayerId());
            
            if (player == null) {
                continue;
            }
            
            // Make sure player still has our menu open
            if (!menuManager.hasOpenMenu(player)) {
                continue;
            }
            
            // Update the menu
            try {
                menuManager.updateMenu(player);
            } catch (Exception e) {
                ActionMenus.LOGGER.error("Error updating menu {} for player {}", 
                        session.getMenuId(), session.getPlayerName(), e);
            }
        }
    }
    
    /**
     * Get the current server tick.
     */
    public long getCurrentTick() {
        return currentTick;
    }
}
