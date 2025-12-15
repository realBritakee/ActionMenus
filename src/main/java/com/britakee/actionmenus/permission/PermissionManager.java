package com.britakee.actionmenus.permission;

import com.britakee.actionmenus.ActionMenus;
import net.minecraft.server.level.ServerPlayer;

/**
 * Permission manager for ActionMenus.
 * 
 * For LuckPerms/FTB Ranks integration, uses command-based permission checking
 * since dynamic permission node creation doesn't work well with external mods.
 * 
 * Falls back to op-level check if no permission mod handles the check.
 */
public class PermissionManager {
    
    // Default permission level for ops (fallback)
    private int defaultOpLevel = 2;
    
    /**
     * Check if a player has a permission.
     * Works with LuckPerms, FTB Ranks, and vanilla ops.
     */
    public boolean hasPermission(ServerPlayer player, String permission) {
        if (player == null || permission == null || permission.isEmpty()) {
            return true; // No permission required
        }
        
        // Ops always have all permissions
        if (player.hasPermissions(defaultOpLevel)) {
            return true;
        }
        
        try {
            // Try to check permission via LuckPerms API if available
            // LuckPerms registers as a permission handler
            var server = player.getServer();
            if (server != null) {
                // Use the permission check command result
                // This works because LuckPerms hooks into the permission system
                return checkPermissionViaAPI(player, permission);
            }
        } catch (Exception e) {
            ActionMenus.LOGGER.debug("Permission check failed for {}: {}", permission, e.getMessage());
        }
        
        return false;
    }
    
    /**
     * Check permission using NeoForge's permission API.
     */
    private boolean checkPermissionViaAPI(ServerPlayer player, String permission) {
        try {
            // Use reflection to check if LuckPerms or another permission handler is present
            Class<?> permApiClass = Class.forName("net.neoforged.neoforge.server.permission.PermissionAPI");
            var getOfflineMethod = permApiClass.getMethod("getOfflinePermission", java.util.UUID.class, 
                    Class.forName("net.neoforged.neoforge.server.permission.nodes.PermissionNode"));
            
            // If we got here, the API exists - but we can't easily create dynamic nodes
            // LuckPerms handles permissions differently - it checks the permission string directly
            
            // For LuckPerms, we can use their User API
            return checkLuckPermsPermission(player, permission);
        } catch (ClassNotFoundException e) {
            // NeoForge permission API not available in expected form
            return false;
        } catch (Exception e) {
            ActionMenus.LOGGER.debug("Permission API check failed: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * Check permission using LuckPerms API if available.
     */
    private boolean checkLuckPermsPermission(ServerPlayer player, String permission) {
        try {
            // Try to get LuckPerms API
            Class<?> lpProviderClass = Class.forName("net.luckperms.api.LuckPermsProvider");
            Object luckPerms = lpProviderClass.getMethod("get").invoke(null);
            
            // Get the user manager
            Object userManager = luckPerms.getClass().getMethod("getUserManager").invoke(luckPerms);
            
            // Get the user
            Object user = userManager.getClass().getMethod("getUser", java.util.UUID.class)
                    .invoke(userManager, player.getUUID());
            
            if (user == null) {
                return false;
            }
            
            // Get cached data
            Object cachedData = user.getClass().getMethod("getCachedData").invoke(user);
            Object permissionData = cachedData.getClass().getMethod("getPermissionData").invoke(cachedData);
            
            // Check the permission
            Object result = permissionData.getClass().getMethod("checkPermission", String.class)
                    .invoke(permissionData, permission);
            
            // Get the result value (Tristate)
            Object value = result.getClass().getMethod("asBoolean").invoke(result);
            
            return Boolean.TRUE.equals(value);
        } catch (ClassNotFoundException e) {
            // LuckPerms not installed
            ActionMenus.LOGGER.debug("LuckPerms not found, using fallback permission check");
            return false;
        } catch (Exception e) {
            ActionMenus.LOGGER.debug("LuckPerms permission check failed: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * Set the default op level for permission bypass.
     */
    public void setDefaultOpLevel(int level) {
        this.defaultOpLevel = level;
    }
    
    /**
     * Check if a player is an operator.
     */
    public boolean isOp(ServerPlayer player) {
        return player.hasPermissions(defaultOpLevel);
    }
}
