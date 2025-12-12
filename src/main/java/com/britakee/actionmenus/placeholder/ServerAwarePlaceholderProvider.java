package com.britakee.actionmenus.placeholder;

import net.minecraft.server.MinecraftServer;

/**
 * Interface for placeholder providers that need server access.
 */
public interface ServerAwarePlaceholderProvider extends PlaceholderProvider {
    
    /**
     * Set the server reference.
     */
    void setServer(MinecraftServer server);
    
    /**
     * Get the server reference.
     */
    MinecraftServer getServer();
}
