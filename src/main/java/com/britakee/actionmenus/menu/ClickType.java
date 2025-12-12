package com.britakee.actionmenus.menu;

/**
 * Enum representing different types of inventory clicks.
 */
public enum ClickType {
    /** Any click type */
    ANY,
    /** Left click */
    LEFT,
    /** Right click */
    RIGHT,
    /** Shift + left click */
    SHIFT_LEFT,
    /** Shift + right click */
    SHIFT_RIGHT,
    /** Middle click (mouse wheel) */
    MIDDLE,
    /** Number key press (1-9) */
    NUMBER_KEY,
    /** Drop key (Q) */
    DROP,
    /** Ctrl + Drop (drop all) */
    CONTROL_DROP,
    /** Double click */
    DOUBLE_CLICK;
    
    /**
     * Convert a Minecraft ClickType to our ClickType.
     */
    public static ClickType fromMinecraft(net.minecraft.world.inventory.ClickType mcType, int button, boolean shiftDown) {
        return switch (mcType) {
            case PICKUP -> {
                if (shiftDown) {
                    yield button == 0 ? SHIFT_LEFT : SHIFT_RIGHT;
                }
                yield button == 0 ? LEFT : RIGHT;
            }
            case QUICK_MOVE -> shiftDown ? (button == 0 ? SHIFT_LEFT : SHIFT_RIGHT) : LEFT;
            case SWAP -> NUMBER_KEY;
            case CLONE -> MIDDLE;
            case THROW -> shiftDown ? CONTROL_DROP : DROP;
            case QUICK_CRAFT -> ANY;
            case PICKUP_ALL -> DOUBLE_CLICK;
        };
    }
    
    /**
     * Check if this click type matches the given type.
     * ANY matches everything, and specific types also match ANY.
     */
    public boolean matches(ClickType other) {
        if (this == ANY || other == ANY) {
            return true;
        }
        return this == other;
    }
}
