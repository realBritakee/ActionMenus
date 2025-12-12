package com.britakee.actionmenus.menu;

import com.britakee.actionmenus.action.Action;
import com.britakee.actionmenus.condition.Condition;

import java.util.*;

/**
 * Represents a complete menu definition.
 * This is the parsed, ready-to-use version of a MenuConfig.
 */
public class MenuDefinition {
    
    private final String id;
    private final String title;
    private final int rows;
    private final int size;
    private final String sourceFile;
    
    private final String permission;
    private final List<String> commands;
    
    private final int updateInterval;
    private final boolean preventClose;
    private final boolean updateOnClick;
    
    private final Condition openRequirement;
    private final List<Action> openActions;
    private final List<Action> closeActions;
    
    private final Map<Integer, MenuItem> items;
    
    private MenuDefinition(Builder builder) {
        this.id = builder.id;
        this.title = builder.title;
        this.rows = builder.rows;
        this.size = builder.rows * 9;
        this.sourceFile = builder.sourceFile;
        this.permission = builder.permission;
        this.commands = Collections.unmodifiableList(new ArrayList<>(builder.commands));
        this.updateInterval = builder.updateInterval;
        this.preventClose = builder.preventClose;
        this.updateOnClick = builder.updateOnClick;
        this.openRequirement = builder.openRequirement;
        this.openActions = Collections.unmodifiableList(new ArrayList<>(builder.openActions));
        this.closeActions = Collections.unmodifiableList(new ArrayList<>(builder.closeActions));
        this.items = Collections.unmodifiableMap(new HashMap<>(builder.items));
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    // Getters
    public String getId() {
        return id;
    }
    
    public String getTitle() {
        return title;
    }
    
    public int getRows() {
        return rows;
    }
    
    public int getSize() {
        return size;
    }
    
    public String getSourceFile() {
        return sourceFile;
    }
    
    public String getPermission() {
        return permission;
    }
    
    public boolean hasPermission() {
        return permission != null && !permission.isBlank();
    }
    
    public List<String> getCommands() {
        return commands;
    }
    
    public int getUpdateInterval() {
        return updateInterval;
    }
    
    public boolean shouldAutoUpdate() {
        return updateInterval > 0 && hasDynamicContent();
    }
    
    public boolean isPreventClose() {
        return preventClose;
    }
    
    public boolean isUpdateOnClick() {
        return updateOnClick;
    }
    
    public Condition getOpenRequirement() {
        return openRequirement;
    }
    
    public boolean hasOpenRequirement() {
        return openRequirement != null && openRequirement != Condition.ALWAYS_TRUE;
    }
    
    public List<Action> getOpenActions() {
        return openActions;
    }
    
    public List<Action> getCloseActions() {
        return closeActions;
    }
    
    public Map<Integer, MenuItem> getItems() {
        return items;
    }
    
    public MenuItem getItem(int slot) {
        return items.get(slot);
    }
    
    /**
     * Check if this menu has any dynamic content that needs refreshing.
     */
    public boolean hasDynamicContent() {
        // Check if title has placeholders
        if (title.contains("%")) {
            return true;
        }
        
        // Check if any item is dynamic
        for (MenuItem item : items.values()) {
            if (item.isDynamic()) {
                return true;
            }
            // Also check for placeholders in display name/lore
            if (item.getDisplayName() != null && item.getDisplayName().contains("%")) {
                return true;
            }
            if (item.getLore() != null) {
                for (String line : item.getLore()) {
                    if (line.contains("%")) {
                        return true;
                    }
                }
            }
        }
        
        return false;
    }
    
    @Override
    public String toString() {
        return "MenuDefinition{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", rows=" + rows +
                ", items=" + items.size() +
                '}';
    }
    
    /**
     * Builder for MenuDefinition.
     */
    public static class Builder {
        private String id;
        private String title;
        private int rows = 3;
        private String sourceFile;
        private String permission;
        private List<String> commands = new ArrayList<>();
        private int updateInterval = 20;
        private boolean preventClose = false;
        private boolean updateOnClick = true;
        private Condition openRequirement = Condition.ALWAYS_TRUE;
        private List<Action> openActions = new ArrayList<>();
        private List<Action> closeActions = new ArrayList<>();
        private Map<Integer, MenuItem> items = new HashMap<>();
        
        public Builder id(String id) {
            this.id = id;
            return this;
        }
        
        public Builder title(String title) {
            this.title = title;
            return this;
        }
        
        public Builder rows(int rows) {
            this.rows = Math.max(1, Math.min(6, rows));
            return this;
        }
        
        public Builder sourceFile(String sourceFile) {
            this.sourceFile = sourceFile;
            return this;
        }
        
        public Builder permission(String permission) {
            this.permission = permission;
            return this;
        }
        
        public Builder commands(List<String> commands) {
            this.commands = commands;
            return this;
        }
        
        public Builder addCommand(String command) {
            this.commands.add(command);
            return this;
        }
        
        public Builder updateInterval(int updateInterval) {
            this.updateInterval = Math.max(1, updateInterval);
            return this;
        }
        
        public Builder preventClose(boolean preventClose) {
            this.preventClose = preventClose;
            return this;
        }
        
        public Builder updateOnClick(boolean updateOnClick) {
            this.updateOnClick = updateOnClick;
            return this;
        }
        
        public Builder openRequirement(Condition requirement) {
            this.openRequirement = requirement;
            return this;
        }
        
        public Builder openActions(List<Action> actions) {
            this.openActions = actions;
            return this;
        }
        
        public Builder closeActions(List<Action> actions) {
            this.closeActions = actions;
            return this;
        }
        
        public Builder items(Map<Integer, MenuItem> items) {
            this.items = items;
            return this;
        }
        
        public Builder addItem(int slot, MenuItem item) {
            this.items.put(slot, item);
            return this;
        }
        
        public MenuDefinition build() {
            Objects.requireNonNull(id, "Menu ID is required");
            Objects.requireNonNull(title, "Menu title is required");
            return new MenuDefinition(this);
        }
    }
}
