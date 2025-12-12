package com.britakee.actionmenus.action.types;

import com.britakee.actionmenus.ActionMenus;
import com.britakee.actionmenus.action.Action;
import com.britakee.actionmenus.action.ActionContext;
import com.britakee.actionmenus.menu.MenuSession;
import com.britakee.actionmenus.placeholder.PlaceholderContext;

/**
 * Action that sets session data.
 * Format: key=value or key value
 */
public class SetDataAction extends Action {
    
    private final String key;
    private final String value;
    
    public SetDataAction(String key, String value) {
        this.key = key;
        this.value = value;
    }
    
    public static SetDataAction parse(String input) {
        String key;
        String value;
        
        if (input.contains("=")) {
            String[] parts = input.split("=", 2);
            key = parts[0].trim();
            value = parts.length > 1 ? parts[1].trim() : "";
        } else {
            String[] parts = input.trim().split("\\s+", 2);
            key = parts[0];
            value = parts.length > 1 ? parts[1] : "";
        }
        
        return new SetDataAction(key, value);
    }
    
    @Override
    public void execute(ActionContext context) {
        MenuSession session = context.getSession();
        if (session == null) return;
        
        // Parse placeholders in value
        PlaceholderContext placeholderCtx = new PlaceholderContext(
                context.getPlayer(), session, context.getArguments());
        String parsedValue = ActionMenus.getInstance().getPlaceholderManager().parse(value, placeholderCtx);
        
        session.setData(key, parsedValue);
    }
    
    @Override
    public String getType() {
        return "setdata";
    }
    
    @Override
    public String toString() {
        return "setdata{" + key + "=" + value + "}";
    }
}
