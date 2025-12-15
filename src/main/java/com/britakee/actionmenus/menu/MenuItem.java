package com.britakee.actionmenus.menu;

import com.britakee.actionmenus.action.Action;
import com.britakee.actionmenus.condition.Condition;

import java.util.*;

/**
 * Represents a single item in a menu.
 */
public class MenuItem {
    
    private final String id;
    private final List<Integer> slots;
    private final String material;
    private final int amount;
    private final int priority;
    private final boolean dynamic;
    
    // Display properties
    private final String displayName;
    private final List<String> lore;
    
    // Item modifiers
    private final int customModelData;
    private final boolean enchanted;
    private final String skullOwner;
    private final String skullTexture;
    private final List<String> itemFlags;
    
    // Permission for clicking this item
    private final String permission;
    private final String permissionMessage;
    
    // Conditions
    private final Condition viewRequirement;
    private final Condition clickRequirement;
    
    // Actions by click type
    private final Map<ClickType, List<Action>> clickActions;
    
    private MenuItem(Builder builder) {
        this.id = builder.id;
        this.slots = Collections.unmodifiableList(new ArrayList<>(builder.slots));
        this.material = builder.material;
        this.amount = builder.amount;
        this.priority = builder.priority;
        this.dynamic = builder.dynamic;
        this.displayName = builder.displayName;
        this.lore = builder.lore != null ? 
                Collections.unmodifiableList(new ArrayList<>(builder.lore)) : null;
        this.customModelData = builder.customModelData;
        this.enchanted = builder.enchanted;
        this.skullOwner = builder.skullOwner;
        this.skullTexture = builder.skullTexture;
        this.itemFlags = builder.itemFlags != null ?
                Collections.unmodifiableList(new ArrayList<>(builder.itemFlags)) : null;
        this.permission = builder.permission;
        this.permissionMessage = builder.permissionMessage;
        this.viewRequirement = builder.viewRequirement;
        this.clickRequirement = builder.clickRequirement;
        this.clickActions = Collections.unmodifiableMap(new EnumMap<>(builder.clickActions));
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    /**
     * Create a copy of this item with a different slot.
     */
    public MenuItem withSlot(int slot) {
        return new Builder()
                .id(this.id)
                .slots(Collections.singletonList(slot))
                .material(this.material)
                .amount(this.amount)
                .priority(this.priority)
                .dynamic(this.dynamic)
                .displayName(this.displayName)
                .lore(this.lore)
                .customModelData(this.customModelData)
                .enchanted(this.enchanted)
                .skullOwner(this.skullOwner)
                .skullTexture(this.skullTexture)
                .itemFlags(this.itemFlags)
                .permission(this.permission)
                .permissionMessage(this.permissionMessage)
                .viewRequirement(this.viewRequirement)
                .clickRequirement(this.clickRequirement)
                .clickActions(this.clickActions)
                .build();
    }
    
    // Getters
    public String getId() {
        return id;
    }
    
    public List<Integer> getSlots() {
        return slots;
    }
    
    public int getFirstSlot() {
        return slots.isEmpty() ? -1 : slots.get(0);
    }
    
    public String getMaterial() {
        return material;
    }
    
    public int getAmount() {
        return amount;
    }
    
    public int getPriority() {
        return priority;
    }
    
    public boolean isDynamic() {
        return dynamic;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public List<String> getLore() {
        return lore;
    }
    
    public int getCustomModelData() {
        return customModelData;
    }
    
    public boolean isEnchanted() {
        return enchanted;
    }
    
    public String getSkullOwner() {
        return skullOwner;
    }
    
    public String getSkullTexture() {
        return skullTexture;
    }
    
    public List<String> getItemFlags() {
        return itemFlags;
    }
    
    public String getPermission() {
        return permission;
    }
    
    public boolean hasPermission() {
        return permission != null && !permission.isEmpty();
    }
    
    public String getPermissionMessage() {
        return permissionMessage;
    }
    
    public Condition getViewRequirement() {
        return viewRequirement;
    }
    
    public boolean hasViewRequirement() {
        return viewRequirement != null && viewRequirement != Condition.ALWAYS_TRUE;
    }
    
    public Condition getClickRequirement() {
        return clickRequirement;
    }
    
    public boolean hasClickRequirement() {
        return clickRequirement != null && clickRequirement != Condition.ALWAYS_TRUE;
    }
    
    public Map<ClickType, List<Action>> getClickActions() {
        return clickActions;
    }
    
    /**
     * Get actions for a specific click type.
     * Falls back to ANY if specific type not found.
     */
    public List<Action> getActionsForClick(ClickType type) {
        List<Action> actions = clickActions.get(type);
        if (actions != null && !actions.isEmpty()) {
            return actions;
        }
        // Fall back to ANY
        return clickActions.getOrDefault(ClickType.ANY, Collections.emptyList());
    }
    
    public boolean hasActions() {
        return !clickActions.isEmpty();
    }
    
    @Override
    public String toString() {
        return "MenuItem{" +
                "id='" + id + '\'' +
                ", material='" + material + '\'' +
                ", slots=" + slots +
                '}';
    }
    
    /**
     * Builder for MenuItem.
     */
    public static class Builder {
        private String id;
        private List<Integer> slots = new ArrayList<>();
        private String material = "minecraft:stone";
        private int amount = 1;
        private int priority = 0;
        private boolean dynamic = false;
        private String displayName;
        private List<String> lore;
        private int customModelData = 0;
        private boolean enchanted = false;
        private String skullOwner;
        private String skullTexture;
        private List<String> itemFlags;
        private String permission;
        private String permissionMessage;
        private Condition viewRequirement = Condition.ALWAYS_TRUE;
        private Condition clickRequirement = Condition.ALWAYS_TRUE;
        private Map<ClickType, List<Action>> clickActions = new EnumMap<>(ClickType.class);
        
        public Builder id(String id) {
            this.id = id;
            return this;
        }
        
        public Builder slot(int slot) {
            this.slots = Collections.singletonList(slot);
            return this;
        }
        
        public Builder slots(List<Integer> slots) {
            this.slots = slots;
            return this;
        }
        
        public Builder material(String material) {
            this.material = material;
            return this;
        }
        
        public Builder amount(int amount) {
            this.amount = Math.max(1, Math.min(64, amount));
            return this;
        }
        
        public Builder priority(int priority) {
            this.priority = priority;
            return this;
        }
        
        public Builder dynamic(boolean dynamic) {
            this.dynamic = dynamic;
            return this;
        }
        
        public Builder displayName(String displayName) {
            this.displayName = displayName;
            return this;
        }
        
        public Builder lore(List<String> lore) {
            this.lore = lore;
            return this;
        }
        
        public Builder customModelData(int customModelData) {
            this.customModelData = customModelData;
            return this;
        }
        
        public Builder enchanted(boolean enchanted) {
            this.enchanted = enchanted;
            return this;
        }
        
        public Builder skullOwner(String skullOwner) {
            this.skullOwner = skullOwner;
            return this;
        }
        
        public Builder skullTexture(String skullTexture) {
            this.skullTexture = skullTexture;
            return this;
        }
        
        public Builder itemFlags(List<String> itemFlags) {
            this.itemFlags = itemFlags;
            return this;
        }
        
        public Builder permission(String permission) {
            this.permission = permission;
            return this;
        }
        
        public Builder permissionMessage(String permissionMessage) {
            this.permissionMessage = permissionMessage;
            return this;
        }
        
        public Builder viewRequirement(Condition viewRequirement) {
            this.viewRequirement = viewRequirement;
            return this;
        }
        
        public Builder clickRequirement(Condition clickRequirement) {
            this.clickRequirement = clickRequirement;
            return this;
        }
        
        public Builder clickActions(Map<ClickType, List<Action>> clickActions) {
            this.clickActions = new EnumMap<>(clickActions);
            return this;
        }
        
        public Builder addClickActions(ClickType type, List<Action> actions) {
            this.clickActions.put(type, actions);
            return this;
        }
        
        public MenuItem build() {
            Objects.requireNonNull(id, "Item ID is required");
            return new MenuItem(this);
        }
    }
}
