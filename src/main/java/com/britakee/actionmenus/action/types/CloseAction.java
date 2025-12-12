package com.britakee.actionmenus.action.types;

import com.britakee.actionmenus.ActionMenus;
import com.britakee.actionmenus.action.Action;
import com.britakee.actionmenus.action.ActionContext;
import net.minecraft.server.level.ServerPlayer;

/**
 * Action that closes the current menu.
 */
public class CloseAction extends Action {
    
    @Override
    public void execute(ActionContext context) {
        ServerPlayer player = context.getPlayer();
        ActionMenus.getInstance().getMenuManager().closeMenu(player);
    }
    
    @Override
    public String getType() {
        return "close";
    }
    
    @Override
    public String toString() {
        return "close";
    }
}
