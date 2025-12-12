package com.britakee.actionmenus.action.types;

import com.britakee.actionmenus.ActionMenus;
import com.britakee.actionmenus.action.Action;
import com.britakee.actionmenus.action.ActionContext;
import com.britakee.actionmenus.placeholder.PlaceholderContext;
import net.minecraft.server.level.ServerPlayer;

/**
 * Action that opens another menu.
 */
public class OpenMenuAction extends Action {
    
    private final String menuId;
    private final String[] arguments;
    
    public OpenMenuAction(String value) {
        String[] parts = value.trim().split("\\s+", 2);
        this.menuId = parts[0];
        this.arguments = parts.length > 1 ? parts[1].split("\\s+") : new String[0];
    }
    
    @Override
    public void execute(ActionContext context) {
        ServerPlayer player = context.getPlayer();
        
        // Parse placeholders in menu ID
        PlaceholderContext placeholderCtx = new PlaceholderContext(player, context.getSession(), context.getArguments());
        String parsedMenuId = ActionMenus.getInstance().getPlaceholderManager().parse(menuId, placeholderCtx);
        
        // Parse arguments
        String[] parsedArgs = new String[arguments.length];
        for (int i = 0; i < arguments.length; i++) {
            parsedArgs[i] = ActionMenus.getInstance().getPlaceholderManager().parse(arguments[i], placeholderCtx);
        }
        
        // Open the menu
        ActionMenus.getInstance().getMenuManager().openMenu(player, parsedMenuId, parsedArgs);
    }
    
    @Override
    public String getType() {
        return "open";
    }
    
    @Override
    public String toString() {
        return "open{" + menuId + "}";
    }
}
