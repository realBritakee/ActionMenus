package com.britakee.actionmenus.config;

import com.britakee.actionmenus.ActionMenus;
import com.britakee.actionmenus.action.ActionRegistry;
import com.britakee.actionmenus.config.model.ActionConfig;
import com.britakee.actionmenus.config.model.ActionConfigDeserializer;
import com.britakee.actionmenus.menu.MenuDefinition;
import com.britakee.actionmenus.menu.MenuRegistry;
import com.britakee.actionmenus.placeholder.PlaceholderManager;
import com.google.gson.*;

import java.io.*;
import java.nio.file.*;
import java.util.*;

/**
 * Manages loading and reloading of all configuration files.
 * Uses JSON format with DeluxeMenus-style structure.
 * 
 * Directory structure:
 * config/actionmenus/
 *   ├── config.json       - Main configuration file listing all menus
 *   └── menus/            - Menu definition JSON files
 *       ├── warps.json
 *       ├── shop.json
 *       └── ...
 */
public class ConfigManager {
    
    private final Path configDir;
    private final Path menusDir;
    private final Path configFile;
    private final MenuRegistry menuRegistry;
    private final ActionRegistry actionRegistry;
    private final PlaceholderManager placeholderManager;
    private final Gson gson;
    private final DeluxeMenuParser menuParser;
    
    // Main config settings
    private boolean debug = false;
    private Map<String, String> guiMenus = new LinkedHashMap<>(); // menu_id -> filename
    
    public ConfigManager(Path configDir, MenuRegistry menuRegistry, 
                         ActionRegistry actionRegistry, PlaceholderManager placeholderManager) {
        this.configDir = configDir;
        this.menusDir = configDir.resolve("menus");
        this.configFile = configDir.resolve("config.json");
        this.menuRegistry = menuRegistry;
        this.actionRegistry = actionRegistry;
        this.placeholderManager = placeholderManager;
        this.gson = new GsonBuilder()
                .setPrettyPrinting()
                .disableHtmlEscaping()
                .registerTypeAdapter(ActionConfig.class, new ActionConfigDeserializer())
                .create();
        this.menuParser = new DeluxeMenuParser(actionRegistry);
    }
    
    /**
     * Load all configurations from disk.
     */
    public void loadAll() {
        ensureDirectoriesExist();
        copyDefaultConfigs();
        loadMainConfig();
        loadMenus();
    }
    
    /**
     * Reload all configurations (hot reload).
     */
    public void reloadAll() {
        ActionMenus.LOGGER.info("Reloading all configurations...");
        
        // Clear existing registries
        menuRegistry.clear();
        guiMenus.clear();
        
        // Reload everything
        loadMainConfig();
        loadMenus();
        
        ActionMenus.LOGGER.info("Reload complete! Loaded {} menus", menuRegistry.getMenuCount());
    }
    
    /**
     * Ensure all required directories exist.
     */
    private void ensureDirectoriesExist() {
        try {
            Files.createDirectories(configDir);
            Files.createDirectories(menusDir);
        } catch (IOException e) {
            ActionMenus.LOGGER.error("Failed to create config directories", e);
        }
    }
    
    /**
     * Copy default configuration files if they don't exist.
     */
    private void copyDefaultConfigs() {
        // Copy main config
        copyDefaultResource("config.json", configFile);
        
        // Copy example menus
        copyDefaultResource("menus/warps.json", menusDir.resolve("warps.json"));
    }
    
    /**
     * Copy a default resource file to the target path.
     */
    private void copyDefaultResource(String resourcePath, Path targetPath) {
        if (Files.exists(targetPath)) {
            return; // Don't overwrite existing files
        }
        
        String fullResourcePath = "/data/actionmenus/default_configs/" + resourcePath;
        
        try (InputStream is = getClass().getResourceAsStream(fullResourcePath)) {
            if (is != null) {
                Files.createDirectories(targetPath.getParent());
                Files.copy(is, targetPath, StandardCopyOption.REPLACE_EXISTING);
                ActionMenus.LOGGER.info("Created default config: {}", resourcePath);
            }
        } catch (IOException e) {
            ActionMenus.LOGGER.warn("Failed to copy default config: {}", resourcePath, e);
        }
    }
    
    /**
     * Load the main config.json file.
     */
    private void loadMainConfig() {
        if (!Files.exists(configFile)) {
            ActionMenus.LOGGER.warn("Main config file does not exist: {}", configFile);
            return;
        }
        
        try (Reader reader = Files.newBufferedReader(configFile)) {
            JsonObject config = JsonParser.parseReader(reader).getAsJsonObject();
            
            // Load debug setting
            if (config.has("debug")) {
                debug = config.get("debug").getAsBoolean();
            }
            
            // Load gui_menus mapping
            if (config.has("gui_menus") && config.get("gui_menus").isJsonObject()) {
                JsonObject menus = config.getAsJsonObject("gui_menus");
                for (Map.Entry<String, JsonElement> entry : menus.entrySet()) {
                    String menuId = entry.getKey();
                    if (entry.getValue().isJsonObject()) {
                        JsonObject menuEntry = entry.getValue().getAsJsonObject();
                        if (menuEntry.has("file")) {
                            String fileName = menuEntry.get("file").getAsString();
                            guiMenus.put(menuId, fileName);
                        }
                    } else if (entry.getValue().isJsonPrimitive()) {
                        // Simple format: "menuId": "filename.json"
                        guiMenus.put(menuId, entry.getValue().getAsString());
                    }
                }
            }
            
            ActionMenus.LOGGER.info("Loaded main config with {} menu entries", guiMenus.size());
            
        } catch (Exception e) {
            ActionMenus.LOGGER.error("Failed to load main config", e);
        }
    }
    
    /**
     * Load all menu definitions from the menus directory.
     */
    private void loadMenus() {
        if (guiMenus.isEmpty()) {
            ActionMenus.LOGGER.warn("No menus defined in config.json");
            return;
        }
        
        for (Map.Entry<String, String> entry : guiMenus.entrySet()) {
            String menuId = entry.getKey();
            String fileName = entry.getValue();
            
            Path menuFile = menusDir.resolve(fileName);
            if (Files.exists(menuFile)) {
                loadMenuFile(menuId, menuFile);
            } else {
                ActionMenus.LOGGER.warn("Menu file not found: {} (for menu '{}')", fileName, menuId);
            }
        }
    }
    
    /**
     * Load a single menu file.
     */
    private void loadMenuFile(String menuId, Path file) {
        String fileName = file.getFileName().toString();
        
        try (Reader reader = Files.newBufferedReader(file)) {
            JsonObject menuJson = JsonParser.parseReader(reader).getAsJsonObject();
            
            // Parse using DeluxeMenus-style parser
            MenuDefinition menu = menuParser.parse(menuId, menuJson, fileName);
            
            if (menu != null) {
                // Register the menu
                menuRegistry.register(menu);
                
                if (debug) {
                    ActionMenus.LOGGER.info("Loaded menu: {} ({} items) from {}", 
                            menu.getId(), menu.getItems().size(), fileName);
                }
            }
            
        } catch (JsonSyntaxException e) {
            ActionMenus.LOGGER.error("JSON syntax error in menu file {}: {}", fileName, e.getMessage());
        } catch (IOException e) {
            ActionMenus.LOGGER.error("Failed to read menu file: {}", fileName, e);
        } catch (Exception e) {
            ActionMenus.LOGGER.error("Error loading menu file: {}", fileName, e);
        }
    }
    
    /**
     * Get the Gson instance for JSON operations.
     */
    public Gson getGson() {
        return gson;
    }
    
    /**
     * Get the config directory path.
     */
    public Path getConfigDir() {
        return configDir;
    }
    
    /**
     * Get the menus directory path.
     */
    public Path getMenusDir() {
        return menusDir;
    }
    
    /**
     * Check if debug mode is enabled.
     */
    public boolean isDebug() {
        return debug;
    }
}
