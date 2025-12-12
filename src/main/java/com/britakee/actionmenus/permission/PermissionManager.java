package com.britakee.actionmenus.permission;

import net.minecraft.server.level.ServerPlayer;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Simple permission manager for ActionMenus.
 * Provides basic permission checking with op-level fallback.
 * 
 * Can be extended to integrate with permission mods like LuckPerms.
 */
public class PermissionManager {
    
    // Player UUID -> Set of permissions
    private final ConcurrentHashMap<UUID, Set<String>> playerPermissions = new ConcurrentHashMap<>();
    
    // Default permission level for ops
    private int defaultOpLevel = 2;
    
    /**
     * Check if a player has a permission.
     */
    public boolean hasPermission(ServerPlayer player, String permission) {
        if (player == null || permission == null || permission.isEmpty()) {
            return true; // No permission required
        }
        
        // Ops have all permissions
        if (player.hasPermissions(defaultOpLevel)) {
            return true;
        }
        
        // Check explicit permissions
        Set<String> perms = playerPermissions.get(player.getUUID());
        if (perms != null) {
            // Check exact match
            if (perms.contains(permission)) {
                return true;
            }
            
            // Check wildcard permissions
            String[] parts = permission.split("\\.");
            StringBuilder current = new StringBuilder();
            for (int i = 0; i < parts.length; i++) {
                if (i > 0) current.append(".");
                current.append(parts[i]);
                
                if (perms.contains(current + ".*")) {
                    return true;
                }
            }
            
            // Check root wildcard
            if (perms.contains("*") || perms.contains("actionmenus.*")) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Grant a permission to a player.
     */
    public void grantPermission(UUID playerId, String permission) {
        playerPermissions.computeIfAbsent(playerId, k -> new HashSet<>()).add(permission);
    }
    
    /**
     * Revoke a permission from a player.
     */
    public void revokePermission(UUID playerId, String permission) {
        Set<String> perms = playerPermissions.get(playerId);
        if (perms != null) {
            perms.remove(permission);
        }
    }
    
    /**
     * Get all permissions for a player.
     */
    public Set<String> getPermissions(UUID playerId) {
        return playerPermissions.getOrDefault(playerId, new HashSet<>());
    }
    
    /**
     * Clear all permissions for a player.
     */
    public void clearPermissions(UUID playerId) {
        playerPermissions.remove(playerId);
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
