package com.britakee.actionmenus.menu;

import com.britakee.actionmenus.ActionMenus;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Registry for all loaded menu definitions.
 * Also manages command aliases for menus.
 */
public class MenuRegistry {
    
    // Menu ID -> MenuDefinition
    private final Map<String, MenuDefinition> menus = new ConcurrentHashMap<>();
    
    // Command alias -> Menu ID
    private final Map<String, String> commandAliases = new ConcurrentHashMap<>();
    
    /**
     * Register a menu definition.
     */
    public void register(MenuDefinition menu) {
        String id = menu.getId().toLowerCase();
        menus.put(id, menu);
        
        // Register command aliases
        for (String command : menu.getCommands()) {
            String cmd = command.toLowerCase().replace("/", "");
            if (commandAliases.containsKey(cmd)) {
                ActionMenus.LOGGER.warn("Command alias '{}' is already registered by menu '{}', overwriting with '{}'",
                        cmd, commandAliases.get(cmd), id);
            }
            commandAliases.put(cmd, id);
        }
        
        ActionMenus.LOGGER.debug("Registered menu: {} with {} command aliases", id, menu.getCommands().size());
    }
    
    /**
     * Unregister a menu by ID.
     */
    public void unregister(String id) {
        MenuDefinition menu = menus.remove(id.toLowerCase());
        if (menu != null) {
            // Remove command aliases
            for (String command : menu.getCommands()) {
                String cmd = command.toLowerCase().replace("/", "");
                commandAliases.remove(cmd);
            }
        }
    }
    
    /**
     * Get a menu by ID.
     */
    public MenuDefinition getMenu(String id) {
        return menus.get(id.toLowerCase());
    }
    
    /**
     * Get a menu by command alias.
     */
    public MenuDefinition getMenuByCommand(String command) {
        String cmd = command.toLowerCase().replace("/", "");
        String menuId = commandAliases.get(cmd);
        if (menuId != null) {
            return menus.get(menuId);
        }
        return null;
    }
    
    /**
     * Check if a command is a menu alias.
     */
    public boolean isMenuCommand(String command) {
        String cmd = command.toLowerCase().replace("/", "");
        return commandAliases.containsKey(cmd);
    }
    
    /**
     * Get all registered menu IDs.
     */
    public Set<String> getMenuIds() {
        return Collections.unmodifiableSet(menus.keySet());
    }
    
    /**
     * Get all registered menus.
     */
    public Collection<MenuDefinition> getAllMenus() {
        return Collections.unmodifiableCollection(menus.values());
    }
    
    /**
     * Get all command aliases.
     */
    public Map<String, String> getCommandAliases() {
        return Collections.unmodifiableMap(commandAliases);
    }
    
    /**
     * Get the number of registered menus.
     */
    public int getMenuCount() {
        return menus.size();
    }
    
    /**
     * Clear all registered menus and aliases.
     */
    public void clear() {
        menus.clear();
        commandAliases.clear();
    }
    
    /**
     * Check if a menu exists.
     */
    public boolean exists(String id) {
        return menus.containsKey(id.toLowerCase());
    }
}
