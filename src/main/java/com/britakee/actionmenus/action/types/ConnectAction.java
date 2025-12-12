package com.britakee.actionmenus.action.types;

import com.britakee.actionmenus.action.Action;
import com.britakee.actionmenus.action.ActionContext;

/**
 * Action that would connect player to another BungeeCord server.
 * This is a placeholder for future BungeeCord integration.
 */
public class ConnectAction extends Action {
    
    private final String serverName;
    
    public ConnectAction(String serverName) {
        this.serverName = serverName;
    }
    
    @Override
    public void execute(ActionContext context) {
        // BungeeCord plugin message channel would be used here
        // For now, this is a placeholder
        context.getPlayer().sendSystemMessage(
                net.minecraft.network.chat.Component.literal("§cServer switching requires BungeeCord/Velocity support.")
        );
    }
    
    @Override
    public String getType() {
        return "connect";
    }
    
    @Override
    public String toString() {
        return "connect{" + serverName + "}";
    }
}
