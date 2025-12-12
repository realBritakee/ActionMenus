package com.britakee.actionmenus.menu;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Represents an active menu session for a player.
 * Tracks state like arguments, page number, and custom data.
 */
public class MenuSession {
    
    private final UUID playerId;
    private final String playerName;
    private final MenuDefinition menu;
    private final long openedAt;
    
    // Session state
    private int currentPage = 0;
    private String[] arguments;
    private Map<String, Object> data = new HashMap<>();
    
    // Cached rendered items (for comparison during updates)
    private Map<Integer, ItemStack> cachedItems = new HashMap<>();
    
    // Last update tick
    private long lastUpdateTick = 0;
    
    public MenuSession(ServerPlayer player, MenuDefinition menu) {
        this.playerId = player.getUUID();
        this.playerName = player.getName().getString();
        this.menu = menu;
        this.openedAt = System.currentTimeMillis();
    }
    
    public MenuSession(ServerPlayer player, MenuDefinition menu, String[] arguments) {
        this(player, menu);
        this.arguments = arguments;
    }
    
    // Getters
    public UUID getPlayerId() {
        return playerId;
    }
    
    public String getPlayerName() {
        return playerName;
    }
    
    public MenuDefinition getMenu() {
        return menu;
    }
    
    public String getMenuId() {
        return menu.getId();
    }
    
    public long getOpenedAt() {
        return openedAt;
    }
    
    public long getOpenDuration() {
        return System.currentTimeMillis() - openedAt;
    }
    
    // Page handling
    public int getCurrentPage() {
        return currentPage;
    }
    
    public void setCurrentPage(int page) {
        this.currentPage = Math.max(0, page);
    }
    
    public void nextPage() {
        this.currentPage++;
    }
    
    public void previousPage() {
        this.currentPage = Math.max(0, currentPage - 1);
    }
    
    // Arguments
    public String[] getArguments() {
        return arguments;
    }
    
    public void setArguments(String[] arguments) {
        this.arguments = arguments;
    }
    
    public String getArgument(int index) {
        if (arguments == null || index < 0 || index >= arguments.length) {
            return "";
        }
        return arguments[index];
    }
    
    public String getArgumentsString() {
        if (arguments == null || arguments.length == 0) {
            return "";
        }
        return String.join(" ", arguments);
    }
    
    // Custom data storage
    public Map<String, Object> getData() {
        return data;
    }
    
    public void setData(String key, Object value) {
        data.put(key, value);
    }
    
    @SuppressWarnings("unchecked")
    public <T> T getData(String key, T defaultValue) {
        Object value = data.get(key);
        if (value == null) {
            return defaultValue;
        }
        try {
            return (T) value;
        } catch (ClassCastException e) {
            return defaultValue;
        }
    }
    
    public boolean hasData(String key) {
        return data.containsKey(key);
    }
    
    public void removeData(String key) {
        data.remove(key);
    }
    
    // Cached items for smart updates
    public Map<Integer, ItemStack> getCachedItems() {
        return cachedItems;
    }
    
    public void setCachedItems(Map<Integer, ItemStack> items) {
        this.cachedItems = new HashMap<>(items);
    }
    
    public void setCachedItem(int slot, ItemStack item) {
        this.cachedItems.put(slot, item.copy());
    }
    
    public ItemStack getCachedItem(int slot) {
        return cachedItems.get(slot);
    }
    
    // Update tracking
    public long getLastUpdateTick() {
        return lastUpdateTick;
    }
    
    public void setLastUpdateTick(long tick) {
        this.lastUpdateTick = tick;
    }
    
    public boolean shouldUpdate(long currentTick) {
        return currentTick - lastUpdateTick >= menu.getUpdateInterval();
    }
    
    @Override
    public String toString() {
        return "MenuSession{" +
                "player=" + playerName +
                ", menu=" + menu.getId() +
                ", page=" + currentPage +
                '}';
    }
}
