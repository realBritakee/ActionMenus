package com.britakee.actionmenus.action;

import com.britakee.actionmenus.ActionMenus;
import com.britakee.actionmenus.action.types.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Registry for action types.
 * Handles parsing action strings and creating action instances.
 */
public class ActionRegistry {
    
    // Pattern to match [type] value format
    private static final Pattern ACTION_PATTERN = Pattern.compile("^\\[([^\\]]+)\\]\\s*(.*)$");
    
    // Registered action factories
    private final Map<String, Function<String, Action>> actionFactories = new ConcurrentHashMap<>();
    
    /**
     * Register built-in action types.
     */
    public void registerBuiltInActions() {
        // Command actions
        register("command", "cmd", "player", value -> new CommandAction(value, false));
        register("console", "server", value -> new CommandAction(value, true));
        register("op", "sudo", value -> new OpCommandAction(value));
        
        // Message actions
        register("message", "msg", "tell", MessageAction::new);
        register("broadcast", "bc", value -> new BroadcastAction(value));
        register("actionbar", "ab", value -> new ActionBarAction(value));
        register("title", value -> TitleAction.parse(value));
        
        // Menu actions
        register("open", "menu", value -> new OpenMenuAction(value));
        register("close", value -> new CloseAction());
        register("refresh", "update", value -> new RefreshAction());
        register("back", value -> new BackAction());
        
        // Sound action
        register("sound", "playsound", value -> SoundAction.parse(value));
        
        // Item actions
        register("give", "item", value -> GiveItemAction.parse(value));
        register("take", "remove", value -> TakeItemAction.parse(value));
        
        // Teleport action
        register("teleport", "tp", value -> TeleportAction.parse(value));
        
        // Connect (BungeeCord) action - placeholder for future
        register("connect", "server", value -> new ConnectAction(value));
        
        // Data actions
        register("setdata", "data", value -> SetDataAction.parse(value));
        
        // Delay action (special handling)
        register("delay", "wait", value -> {
            try {
                int ticks = Integer.parseInt(value.trim());
                return new DelayAction(ticks);
            } catch (NumberFormatException e) {
                return new DelayAction(20); // Default 1 second
            }
        });
        
        ActionMenus.LOGGER.info("Registered {} built-in action types", actionFactories.size());
    }
    
    /**
     * Register an action factory.
     */
    public void register(String type, Function<String, Action> factory) {
        actionFactories.put(type.toLowerCase(), factory);
    }
    
    /**
     * Register an action factory with aliases.
     */
    public void register(String type, String alias, Function<String, Action> factory) {
        actionFactories.put(type.toLowerCase(), factory);
        actionFactories.put(alias.toLowerCase(), factory);
    }
    
    /**
     * Register an action factory with multiple aliases.
     */
    public void register(String type, String alias1, String alias2, Function<String, Action> factory) {
        actionFactories.put(type.toLowerCase(), factory);
        actionFactories.put(alias1.toLowerCase(), factory);
        actionFactories.put(alias2.toLowerCase(), factory);
    }
    
    /**
     * Parse an action string into an Action.
     * Format: [type] value
     */
    public Action parseAction(String actionString) {
        if (actionString == null || actionString.isBlank()) {
            return null;
        }
        
        Matcher matcher = ACTION_PATTERN.matcher(actionString.trim());
        if (!matcher.matches()) {
            // Try treating as a plain command
            return new CommandAction(actionString, false);
        }
        
        String type = matcher.group(1).toLowerCase();
        String value = matcher.group(2);
        
        Function<String, Action> factory = actionFactories.get(type);
        if (factory == null) {
            ActionMenus.LOGGER.warn("Unknown action type: {}", type);
            return null;
        }
        
        try {
            return factory.apply(value);
        } catch (Exception e) {
            ActionMenus.LOGGER.error("Error creating action '{}': {}", actionString, e.getMessage());
            return null;
        }
    }
    
    /**
     * Create a sequence action.
     */
    public Action createSequenceAction(List<Action> actions) {
        return new SequenceAction(actions);
    }
    
    /**
     * Get all registered action types.
     */
    public Set<String> getActionTypes() {
        return Collections.unmodifiableSet(actionFactories.keySet());
    }
    
    /**
     * Check if an action type is registered.
     */
    public boolean isRegistered(String type) {
        return actionFactories.containsKey(type.toLowerCase());
    }
}
