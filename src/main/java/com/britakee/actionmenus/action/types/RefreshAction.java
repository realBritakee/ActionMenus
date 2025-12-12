package com.britakee.actionmenus.action.types;

import com.britakee.actionmenus.ActionMenus;
import com.britakee.actionmenus.action.Action;
import com.britakee.actionmenus.action.ActionContext;
import net.minecraft.server.level.ServerPlayer;

/**
 * Action that refreshes/updates the current menu.
 */
public class RefreshAction extends Action {
    
    @Override
    public void execute(ActionContext context) {
        ServerPlayer player = context.getPlayer();
        ActionMenus.getInstance().getMenuManager().updateMenu(player);
    }
    
    @Override
    public String getType() {
        return "refresh";
    }
    
    @Override
    public String toString() {
        return "refresh";
    }
}
