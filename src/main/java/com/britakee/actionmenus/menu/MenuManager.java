package com.britakee.actionmenus.menu;

import com.britakee.actionmenus.ActionMenus;
import com.britakee.actionmenus.action.Action;
import com.britakee.actionmenus.action.ActionContext;
import com.britakee.actionmenus.action.ActionExecutor;
import com.britakee.actionmenus.condition.ConditionContext;
import com.britakee.actionmenus.condition.ConditionEvaluator;
import com.britakee.actionmenus.placeholder.PlaceholderContext;
import com.britakee.actionmenus.placeholder.PlaceholderManager;
import com.britakee.actionmenus.util.ItemBuilder;
import com.britakee.actionmenus.util.TextUtil;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerContainerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Core manager for opening, closing, and updating menus.
 * Handles player sessions and inventory events.
 */
public class MenuManager {
    
    private final MenuRegistry menuRegistry;
    private final PlaceholderManager placeholderManager;
    private final ConditionEvaluator conditionEvaluator;
    private final ActionExecutor actionExecutor;
    
    // Player UUID -> Menu Session
    private final Map<UUID, MenuSession> activeSessions = new ConcurrentHashMap<>();
    
    // Container ID -> Player UUID (for tracking which container belongs to which session)
    private final Map<Integer, UUID> containerToPlayer = new ConcurrentHashMap<>();
    
    public MenuManager(MenuRegistry menuRegistry, PlaceholderManager placeholderManager,
                       ConditionEvaluator conditionEvaluator, ActionExecutor actionExecutor) {
        this.menuRegistry = menuRegistry;
        this.placeholderManager = placeholderManager;
        this.conditionEvaluator = conditionEvaluator;
        this.actionExecutor = actionExecutor;
    }
    
    /**
     * Open a menu for a player.
     */
    public boolean openMenu(ServerPlayer player, String menuId) {
        return openMenu(player, menuId, null);
    }
    
    /**
     * Open a menu for a player with arguments.
     */
    public boolean openMenu(ServerPlayer player, String menuId, String[] arguments) {
        MenuDefinition menu = menuRegistry.getMenu(menuId);
        if (menu == null) {
            ActionMenus.LOGGER.warn("Attempted to open non-existent menu: {}", menuId);
            player.sendSystemMessage(TextUtil.colorize("&cMenu not found: " + menuId));
            return false;
        }
        
        return openMenu(player, menu, arguments);
    }
    
    /**
     * Open a menu definition for a player.
     */
    public boolean openMenu(ServerPlayer player, MenuDefinition menu, String[] arguments) {
        try {
            // Check open requirements
            if (menu.hasOpenRequirement()) {
                PlaceholderContext placeholderCtx = new PlaceholderContext(player, null, arguments);
                ConditionContext condCtx = new ConditionContext(player, placeholderCtx, placeholderManager, conditionEvaluator);
                
                if (!conditionEvaluator.evaluate(menu.getOpenRequirement(), condCtx)) {
                    ActionMenus.LOGGER.debug("Player {} failed open requirements for menu {}", 
                            player.getName().getString(), menu.getId());
                    // Could send deny message here
                    return false;
                }
            }
            
            // Check permission
            if (menu.hasPermission()) {
                if (!ActionMenus.getInstance().getPermissionManager().hasPermission(player, menu.getPermission())) {
                    player.sendSystemMessage(TextUtil.colorize("&cYou don't have permission to open this menu."));
                    return false;
                }
            }
        
        // Close any existing menu
        closeMenu(player);
        
        // Create session
        MenuSession session = new MenuSession(player, menu, arguments);
        activeSessions.put(player.getUUID(), session);
        
        // Build the inventory
        PlaceholderContext ctx = new PlaceholderContext(player, session, arguments);
        Component title = TextUtil.colorize(placeholderManager.parse(menu.getTitle(), ctx));
        
        // Determine menu type based on rows
        MenuType<ChestMenu> menuType = getMenuType(menu.getRows());
        
        // Open the container
        player.openMenu(new SimpleMenuProvider(
                (containerId, playerInv, p) -> {
                    ActionMenuContainer container = new ActionMenuContainer(menu.getSize());
                    ActionMenuChestMenu chestMenu = new ActionMenuChestMenu(
                            menuType, containerId, playerInv, container, menu.getRows(), menu.getId());
                    
                    // Track this container
                    containerToPlayer.put(containerId, player.getUUID());
                    
                    // Populate items
                    populateMenu(chestMenu, player, session);
                    
                    return chestMenu;
                },
                title
        ));
        
        // Play open sound if configured
        if (menu.hasOpenSound()) {
            playSound(player, menu.getOpenSound(), menu.getOpenSoundVolume(), menu.getOpenSoundPitch());
        }
        
        // Execute open actions
        if (!menu.getOpenActions().isEmpty()) {
            ActionContext actionCtx = new ActionContext(player, session, menu, -1);
            for (Action action : menu.getOpenActions()) {
                actionExecutor.execute(action, actionCtx);
            }
        }
        
        session.setLastUpdateTick(player.server.getTickCount());
        
        ActionMenus.LOGGER.debug("Opened menu {} for player {}", menu.getId(), player.getName().getString());
        return true;
        
        } catch (Exception e) {
            ActionMenus.LOGGER.error("Error opening menu '{}' for player '{}': ", 
                    menu.getId(), player.getName().getString(), e);
            player.sendSystemMessage(TextUtil.colorize("&cError opening menu. Check server console."));
            return false;
        }
    }
    
    /**
     * Play a sound for a player.
     * Supports any namespace:sound format (minecraft:, modid:, etc.)
     */
    private void playSound(ServerPlayer player, String soundId, float volume, float pitch) {
        try {
            ResourceLocation soundLocation = ResourceLocation.tryParse(soundId);
            if (soundLocation == null) {
                ActionMenus.LOGGER.warn("Invalid sound ID: {}", soundId);
                return;
            }
            
            // Get the sound event from registry
            SoundEvent soundEvent = BuiltInRegistries.SOUND_EVENT.get(soundLocation);
            if (soundEvent != null) {
                player.playNotifySound(soundEvent, SoundSource.MASTER, volume, pitch);
            } else {
                // For modded sounds or sounds not in registry, create a direct holder
                player.playNotifySound(SoundEvent.createVariableRangeEvent(soundLocation), SoundSource.MASTER, volume, pitch);
            }
        } catch (Exception e) {
            ActionMenus.LOGGER.warn("Failed to play sound {}: {}", soundId, e.getMessage());
        }
    }
    
    /**
     * Populate a menu container with items.
     */
    private void populateMenu(ActionMenuChestMenu container, ServerPlayer player, MenuSession session) {
        MenuDefinition menu = session.getMenu();
        PlaceholderContext ctx = new PlaceholderContext(player, session, session.getArguments());
        ConditionContext condCtx = new ConditionContext(player, ctx, placeholderManager, conditionEvaluator);
        
        for (int slot = 0; slot < menu.getSize(); slot++) {
            MenuItem item = menu.getItem(slot);
            
            if (item == null) {
                container.getContainer().setItem(slot, ItemStack.EMPTY);
                continue;
            }
            
            // Check view requirement
            if (item.hasViewRequirement()) {
                if (!conditionEvaluator.evaluate(item.getViewRequirement(), condCtx)) {
                    container.getContainer().setItem(slot, ItemStack.EMPTY);
                    session.setCachedItem(slot, ItemStack.EMPTY);
                    continue;
                }
            }
            
            // Build the item
            ItemStack stack = buildItem(item, ctx);
            container.getContainer().setItem(slot, stack);
            session.setCachedItem(slot, stack);
        }
    }
    
    /**
     * Build an ItemStack from a MenuItem with placeholder resolution.
     */
    private ItemStack buildItem(MenuItem item, PlaceholderContext ctx) {
        ItemBuilder builder = new ItemBuilder(item.getMaterial())
                .amount(item.getAmount());
        
        // Display name with placeholders
        if (item.getDisplayName() != null) {
            String name = placeholderManager.parse(item.getDisplayName(), ctx);
            builder.name(name);
        }
        
        // Lore with placeholders
        if (item.getLore() != null && !item.getLore().isEmpty()) {
            List<String> parsedLore = new ArrayList<>();
            for (String line : item.getLore()) {
                parsedLore.add(placeholderManager.parse(line, ctx));
            }
            builder.lore(parsedLore);
        }
        
        // Modifiers
        if (item.getCustomModelData() > 0) {
            builder.customModelData(item.getCustomModelData());
        }
        if (item.isEnchanted()) {
            builder.enchantGlint();
        }
        if (item.getSkullOwner() != null) {
            String owner = placeholderManager.parse(item.getSkullOwner(), ctx);
            // If the owner name matches the player, use their full GameProfile with skin data
            if (owner.equalsIgnoreCase(ctx.getPlayer().getName().getString())) {
                builder.skullProfile(ctx.getPlayer().getGameProfile());
            } else {
                builder.skullOwner(owner);
            }
        }
        if (item.getSkullTexture() != null) {
            builder.skullTexture(item.getSkullTexture());
        }
        if (item.getItemFlags() != null) {
            builder.hideFlags(item.getItemFlags());
        }
        
        return builder.build();
    }
    
    /**
     * Update a player's open menu.
     */
    public void updateMenu(ServerPlayer player) {
        MenuSession session = activeSessions.get(player.getUUID());
        if (session == null) {
            return;
        }
        
        if (player.containerMenu instanceof ChestMenu chestMenu) {
            MenuDefinition menu = session.getMenu();
            PlaceholderContext ctx = new PlaceholderContext(player, session, session.getArguments());
            ConditionContext condCtx = new ConditionContext(player, ctx, placeholderManager, conditionEvaluator);
            
            boolean changed = false;
            
            for (int slot = 0; slot < menu.getSize(); slot++) {
                MenuItem item = menu.getItem(slot);
                
                ItemStack newStack;
                if (item == null) {
                    newStack = ItemStack.EMPTY;
                } else if (item.hasViewRequirement() && !conditionEvaluator.evaluate(item.getViewRequirement(), condCtx)) {
                    newStack = ItemStack.EMPTY;
                } else {
                    newStack = buildItem(item, ctx);
                }
                
                // Compare with cached (handle null cached items)
                ItemStack cached = session.getCachedItem(slot);
                if (cached == null) {
                    cached = ItemStack.EMPTY;
                }
                if (!ItemStack.isSameItemSameComponents(newStack, cached)) {
                    chestMenu.getContainer().setItem(slot, newStack);
                    session.setCachedItem(slot, newStack);
                    changed = true;
                }
            }
            
            // Update title if it has placeholders
            if (menu.getTitle().contains("%")) {
                Component newTitle = TextUtil.colorize(placeholderManager.parse(menu.getTitle(), ctx));
                // Note: Title updates require packet manipulation in 1.21.1
                // This is a limitation - title only updates on reopen
            }
            
            if (changed) {
                // Send slot updates to client
                player.containerMenu.broadcastChanges();
            }
            
            session.setLastUpdateTick(player.server.getTickCount());
        }
    }
    
    /**
     * Handle a click on a menu.
     */
    public void handleClick(ServerPlayer player, int slot, ClickType clickType) {
        MenuSession session = activeSessions.get(player.getUUID());
        if (session == null) {
            return;
        }
        
        MenuDefinition menu = session.getMenu();
        if (slot < 0 || slot >= menu.getSize()) {
            return;
        }
        
        MenuItem item = menu.getItem(slot);
        if (item == null) {
            return;
        }
        
        // Check view requirement (item might have become hidden)
        PlaceholderContext ctx = new PlaceholderContext(player, session, session.getArguments());
        ConditionContext condCtx = new ConditionContext(player, ctx, placeholderManager, conditionEvaluator);
        
        if (item.hasViewRequirement() && !conditionEvaluator.evaluate(item.getViewRequirement(), condCtx)) {
            return;
        }
        
        // Check item permission (works with LuckPerms, FTB Ranks, etc.)
        if (item.hasPermission()) {
            if (!ActionMenus.getInstance().getPermissionManager().hasPermission(player, item.getPermission())) {
                // Send custom permission message or default
                String message = item.getPermissionMessage();
                if (message == null || message.isEmpty()) {
                    message = "&cYou don't have permission to use this!";
                }
                player.sendSystemMessage(TextUtil.colorize(message));
                ActionMenus.LOGGER.debug("Player {} lacks permission {} for slot {} in menu {}",
                        player.getName().getString(), item.getPermission(), slot, menu.getId());
                return;
            }
        }
        
        // Check click requirement
        if (item.hasClickRequirement()) {
            if (!conditionEvaluator.evaluate(item.getClickRequirement(), condCtx)) {
                ActionMenus.LOGGER.debug("Player {} failed click requirement for slot {} in menu {}",
                        player.getName().getString(), slot, menu.getId());
                return;
            }
        }
        
        // Get actions for this click type
        List<Action> actions = item.getActionsForClick(clickType);
        
        if (!actions.isEmpty()) {
            ActionContext actionCtx = new ActionContext(player, session, menu, slot);
            for (Action action : actions) {
                actionExecutor.execute(action, actionCtx);
            }
        }
        
        // Update menu if configured
        if (menu.isUpdateOnClick()) {
            updateMenu(player);
        }
    }
    
    /**
     * Close a player's menu.
     */
    public void closeMenu(ServerPlayer player) {
        MenuSession session = activeSessions.remove(player.getUUID());
        if (session == null) {
            return;
        }
        
        // Execute close actions
        MenuDefinition menu = session.getMenu();
        if (!menu.getCloseActions().isEmpty()) {
            ActionContext actionCtx = new ActionContext(player, session, menu, -1);
            for (Action action : menu.getCloseActions()) {
                actionExecutor.execute(action, actionCtx);
            }
        }
        
        // Close the actual container
        if (player.containerMenu != player.inventoryMenu) {
            player.closeContainer();
        }
        
        ActionMenus.LOGGER.debug("Closed menu {} for player {}", menu.getId(), player.getName().getString());
    }
    
    /**
     * Close all open menus (for shutdown/reload).
     */
    public void closeAllMenus() {
        for (UUID playerId : new ArrayList<>(activeSessions.keySet())) {
            MenuSession session = activeSessions.get(playerId);
            if (session != null) {
                // We can't get the player object here easily during shutdown
                // The session will be cleaned up naturally
                activeSessions.remove(playerId);
            }
        }
        containerToPlayer.clear();
    }
    
    /**
     * Get a player's active session.
     */
    public MenuSession getSession(Player player) {
        return activeSessions.get(player.getUUID());
    }
    
    /**
     * Check if a player has an open menu.
     */
    public boolean hasOpenMenu(Player player) {
        return activeSessions.containsKey(player.getUUID());
    }
    
    /**
     * Get all active sessions.
     */
    public Collection<MenuSession> getActiveSessions() {
        return Collections.unmodifiableCollection(activeSessions.values());
    }
    
    /**
     * Get the appropriate MenuType for the row count.
     */
    private MenuType<ChestMenu> getMenuType(int rows) {
        return switch (rows) {
            case 1 -> MenuType.GENERIC_9x1;
            case 2 -> MenuType.GENERIC_9x2;
            case 3 -> MenuType.GENERIC_9x3;
            case 4 -> MenuType.GENERIC_9x4;
            case 5 -> MenuType.GENERIC_9x5;
            case 6 -> MenuType.GENERIC_9x6;
            default -> MenuType.GENERIC_9x3;
        };
    }
    
    // Event handlers
    
    @SubscribeEvent
    public void onContainerClose(PlayerContainerEvent.Close event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            MenuSession session = activeSessions.get(player.getUUID());
            if (session != null) {
                // Check if the closed container was our menu
                int containerId = event.getContainer().containerId;
                UUID trackedPlayer = containerToPlayer.remove(containerId);
                
                if (trackedPlayer != null && trackedPlayer.equals(player.getUUID())) {
                    // Execute close actions but don't force close again
                    MenuDefinition menu = session.getMenu();
                    activeSessions.remove(player.getUUID());
                    
                    if (!menu.getCloseActions().isEmpty()) {
                        ActionContext actionCtx = new ActionContext(player, session, menu, -1);
                        for (Action action : menu.getCloseActions()) {
                            actionExecutor.execute(action, actionCtx);
                        }
                    }
                    
                    ActionMenus.LOGGER.debug("Player {} closed menu {}", 
                            player.getName().getString(), menu.getId());
                }
            }
        }
    }
    
    @SubscribeEvent
    public void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            activeSessions.remove(player.getUUID());
        }
    }
}
