package com.britakee.actionmenus.action;

import com.britakee.actionmenus.menu.MenuDefinition;
import com.britakee.actionmenus.menu.MenuSession;
import net.minecraft.server.level.ServerPlayer;

/**
 * Context for action execution.
 */
public class ActionContext {
    
    private final ServerPlayer player;
    private final MenuSession session;
    private final MenuDefinition menu;
    private final int clickedSlot;
    
    public ActionContext(ServerPlayer player, MenuSession session, MenuDefinition menu, int clickedSlot) {
        this.player = player;
        this.session = session;
        this.menu = menu;
        this.clickedSlot = clickedSlot;
    }
    
    public ServerPlayer getPlayer() {
        return player;
    }
    
    public MenuSession getSession() {
        return session;
    }
    
    public MenuDefinition getMenu() {
        return menu;
    }
    
    public int getClickedSlot() {
        return clickedSlot;
    }
    
    public String[] getArguments() {
        return session != null ? session.getArguments() : null;
    }
    
    public String getArgument(int index) {
        return session != null ? session.getArgument(index) : "";
    }
}
