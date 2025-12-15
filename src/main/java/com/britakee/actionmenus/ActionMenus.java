package com.britakee.actionmenus;

import com.britakee.actionmenus.action.ActionExecutor;
import com.britakee.actionmenus.action.ActionRegistry;
import com.britakee.actionmenus.command.ActionMenusCommand;
import com.britakee.actionmenus.condition.ConditionEvaluator;
import com.britakee.actionmenus.config.ConfigManager;
import com.britakee.actionmenus.menu.MenuManager;
import com.britakee.actionmenus.menu.MenuRegistry;
import com.britakee.actionmenus.menu.MenuUpdateScheduler;
import com.britakee.actionmenus.permission.PermissionManager;
import com.britakee.actionmenus.placeholder.PlaceholderManager;
import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.minecraft.commands.CommandSourceStack;
import com.mojang.brigadier.CommandDispatcher;
import org.slf4j.Logger;

import java.nio.file.Path;

/**
 * ActionMenus - A server-side inventory GUI menu system for NeoForge 1.21.1
 * 
 * This mod provides functionality similar to DeluxeMenus for Bukkit/Spigot,
 * allowing server administrators to create custom inventory-based menus
 * through JSON configuration files.
 * 
 * Features:
 * - Config-driven menu definitions
 * - Dynamic placeholders with auto-refresh
 * - Conditional item display
 * - Extensible action system
 * - Custom command aliases per menu
 * - No client-side mod required
 */
@Mod(ActionMenus.MOD_ID)
public class ActionMenus {
    
    public static final String MOD_ID = "actionmenus";
    public static final Logger LOGGER = LogUtils.getLogger();
    
    // Singleton instance
    private static ActionMenus instance;
    
    // Core managers
    private final ConfigManager configManager;
    private final PlaceholderManager placeholderManager;
    private final ConditionEvaluator conditionEvaluator;
    private final ActionRegistry actionRegistry;
    private final ActionExecutor actionExecutor;
    private final MenuRegistry menuRegistry;
    private final MenuManager menuManager;
    private final PermissionManager permissionManager;
    private final MenuUpdateScheduler menuUpdateScheduler;
    
    // Config directory
    private final Path configDir;
    
    // Command dispatcher reference for registering menu commands
    private CommandDispatcher<CommandSourceStack> commandDispatcher;
    
    public ActionMenus(IEventBus modEventBus, ModContainer modContainer) {
        instance = this;
        
        // Setup config directory
        this.configDir = FMLPaths.CONFIGDIR.get().resolve(MOD_ID);
        
        // Initialize core managers
        this.permissionManager = new PermissionManager();
        this.placeholderManager = new PlaceholderManager();
        this.conditionEvaluator = new ConditionEvaluator(placeholderManager);
        this.actionRegistry = new ActionRegistry();
        this.actionExecutor = new ActionExecutor(actionRegistry, placeholderManager);
        this.menuRegistry = new MenuRegistry();
        this.menuManager = new MenuManager(menuRegistry, placeholderManager, conditionEvaluator, actionExecutor);
        this.configManager = new ConfigManager(configDir, menuRegistry, actionRegistry, placeholderManager);
        this.menuUpdateScheduler = new MenuUpdateScheduler(menuManager);
        
        // Register mod event listeners
        modEventBus.addListener(this::onCommonSetup);
        
        // Register game event listeners
        NeoForge.EVENT_BUS.register(this);
        NeoForge.EVENT_BUS.register(menuManager);
        NeoForge.EVENT_BUS.register(menuUpdateScheduler);
        
        LOGGER.info("ActionMenus initialized!");
    }
    
    private void onCommonSetup(final FMLCommonSetupEvent event) {
        LOGGER.info("ActionMenus common setup...");
        
        // Register built-in placeholders
        placeholderManager.registerBuiltInProviders();
        
        // Register built-in actions
        actionRegistry.registerBuiltInActions();
        
        LOGGER.info("ActionMenus setup complete!");
    }
    
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        LOGGER.info("ActionMenus loading configuration...");
        
        // Set server reference for placeholders
        placeholderManager.setServer(event.getServer());
        
        // Load all configurations
        configManager.loadAll();
        
        LOGGER.info("ActionMenus loaded {} menus", menuRegistry.getMenuCount());
    }
    
    @SubscribeEvent
    public void onServerStarted(ServerStartedEvent event) {
        // Register menu command aliases after configs are loaded
        if (commandDispatcher != null) {
            ActionMenusCommand.registerMenuAliases(commandDispatcher, this);
            LOGGER.info("Registered menu command aliases");
        }
    }
    
    @SubscribeEvent
    public void onServerStopping(ServerStoppingEvent event) {
        LOGGER.info("ActionMenus shutting down...");
        
        // Close all open menus
        menuManager.closeAllMenus();
        
        // Clear server reference
        placeholderManager.setServer(null);
    }
    
    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        this.commandDispatcher = event.getDispatcher();
        ActionMenusCommand.register(event.getDispatcher(), this);
    }
    
    // Getters for managers
    public static ActionMenus getInstance() {
        return instance;
    }
    
    public ConfigManager getConfigManager() {
        return configManager;
    }
    
    public PlaceholderManager getPlaceholderManager() {
        return placeholderManager;
    }
    
    public ConditionEvaluator getConditionEvaluator() {
        return conditionEvaluator;
    }
    
    public ActionRegistry getActionRegistry() {
        return actionRegistry;
    }
    
    public ActionExecutor getActionExecutor() {
        return actionExecutor;
    }
    
    public MenuRegistry getMenuRegistry() {
        return menuRegistry;
    }
    
    public MenuManager getMenuManager() {
        return menuManager;
    }
    
    public PermissionManager getPermissionManager() {
        return permissionManager;
    }
    
    public Path getConfigDir() {
        return configDir;
    }
    
    public CommandDispatcher<CommandSourceStack> getCommandDispatcher() {
        return commandDispatcher;
    }
}
