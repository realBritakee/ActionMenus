package com.britakee.actionmenus.action.types;

import com.britakee.actionmenus.ActionMenus;
import com.britakee.actionmenus.action.Action;
import com.britakee.actionmenus.action.ActionContext;
import com.britakee.actionmenus.menu.MenuSession;
import net.minecraft.server.level.ServerPlayer;

/**
 * Action that goes back to the previous menu (if stored in session).
 */
public class BackAction extends Action {
    
    @Override
    public void execute(ActionContext context) {
        ServerPlayer player = context.getPlayer();
        MenuSession session = context.getSession();
        
        if (session != null && session.hasData("previousMenu")) {
            String previousMenu = session.getData("previousMenu", "");
            if (!previousMenu.isEmpty()) {
                ActionMenus.getInstance().getMenuManager().openMenu(player, previousMenu);
                return;
            }
        }
        
        // No previous menu, just close
        ActionMenus.getInstance().getMenuManager().closeMenu(player);
    }
    
    @Override
    public String getType() {
        return "back";
    }
    
    @Override
    public String toString() {
        return "back";
    }
}
