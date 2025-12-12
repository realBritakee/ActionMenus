package com.britakee.actionmenus.placeholder.providers;

import com.britakee.actionmenus.menu.MenuSession;
import com.britakee.actionmenus.placeholder.PlaceholderContext;
import com.britakee.actionmenus.placeholder.PlaceholderProvider;

/**
 * Provides session and argument related placeholders.
 * 
 * Supported placeholders:
 * - arg_0, arg_1, etc.: Command arguments
 * - args: All arguments joined
 * - session_page: Current page number
 * - session_menu: Current menu ID
 * - session_data_<key>: Custom session data
 */
public class SessionPlaceholderProvider implements PlaceholderProvider {
    
    @Override
    public String getIdentifier() {
        return "session";
    }
    
    @Override
    public String resolve(String placeholder, PlaceholderContext context) {
        String lowerPlaceholder = placeholder.toLowerCase();
        
        // Argument placeholders
        if (lowerPlaceholder.startsWith("arg_") || lowerPlaceholder.startsWith("args_")) {
            String indexStr = lowerPlaceholder.startsWith("arg_") ? 
                    lowerPlaceholder.substring(4) : lowerPlaceholder.substring(5);
            try {
                int index = Integer.parseInt(indexStr);
                return context.getArgument(index);
            } catch (NumberFormatException e) {
                return "";
            }
        }
        
        if (lowerPlaceholder.equals("args") || lowerPlaceholder.equals("arguments")) {
            return context.getArgumentsString();
        }
        
        if (lowerPlaceholder.equals("args_count") || lowerPlaceholder.equals("argument_count")) {
            String[] args = context.getArguments();
            return String.valueOf(args != null ? args.length : 0);
        }
        
        // Session placeholders
        MenuSession session = context.getSession();
        if (session == null) {
            // Handle some placeholders even without session
            return switch (lowerPlaceholder) {
                case "page", "session_page" -> "0";
                case "menu", "session_menu", "menu_id" -> "";
                default -> null;
            };
        }
        
        // Session data placeholders
        if (lowerPlaceholder.startsWith("data_") || lowerPlaceholder.startsWith("session_data_")) {
            String key = lowerPlaceholder.startsWith("session_data_") ? 
                    lowerPlaceholder.substring(13) : lowerPlaceholder.substring(5);
            Object value = session.getData().get(key);
            return value != null ? String.valueOf(value) : "";
        }
        
        return switch (lowerPlaceholder) {
            case "page", "session_page" -> String.valueOf(session.getCurrentPage());
            case "page_display", "session_page_display" -> String.valueOf(session.getCurrentPage() + 1);
            case "menu", "session_menu", "menu_id" -> session.getMenuId();
            case "menu_title" -> session.getMenu().getTitle();
            case "open_time", "session_open_time" -> formatDuration(session.getOpenDuration());
            case "open_time_seconds", "session_open_seconds" -> 
                    String.valueOf(session.getOpenDuration() / 1000);
            case "open_time_minutes", "session_open_minutes" -> 
                    String.valueOf(session.getOpenDuration() / 1000 / 60);
            default -> null;
        };
    }
    
    private String formatDuration(long ms) {
        long seconds = ms / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        
        seconds %= 60;
        minutes %= 60;
        
        if (hours > 0) {
            return String.format("%dh %dm %ds", hours, minutes, seconds);
        } else if (minutes > 0) {
            return String.format("%dm %ds", minutes, seconds);
        } else {
            return String.format("%ds", seconds);
        }
    }
    
    @Override
    public String[] getPlaceholders() {
        return new String[] {
                "arg_<n>", "args", "args_count",
                "page", "page_display", "menu", "menu_title",
                "open_time", "open_time_seconds", "open_time_minutes",
                "data_<key>"
        };
    }
}
