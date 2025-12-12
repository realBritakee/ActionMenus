package com.britakee.actionmenus.action.types;

import com.britakee.actionmenus.ActionMenus;
import com.britakee.actionmenus.action.Action;
import com.britakee.actionmenus.action.ActionContext;
import com.britakee.actionmenus.util.ItemBuilder;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

/**
 * Action that gives an item to the player.
 * Format: item [amount]
 */
public class GiveItemAction extends Action {
    
    private final String itemId;
    private final int amount;
    
    public GiveItemAction(String itemId, int amount) {
        this.itemId = itemId;
        this.amount = amount;
    }
    
    public static GiveItemAction parse(String value) {
        String[] parts = value.trim().split("\\s+");
        
        String itemId = parts.length > 0 ? parts[0] : "minecraft:stone";
        int amount = parts.length > 1 ? parseIntSafe(parts[1], 1) : 1;
        
        return new GiveItemAction(itemId, amount);
    }
    
    private static int parseIntSafe(String s, int defaultValue) {
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
    
    @Override
    public void execute(ActionContext context) {
        ServerPlayer player = context.getPlayer();
        
        ItemStack stack = new ItemBuilder(itemId).amount(amount).build();
        
        if (!stack.isEmpty()) {
            // Try to add to inventory
            if (!player.getInventory().add(stack)) {
                // Drop at player's feet if inventory full
                player.drop(stack, false);
            }
        }
    }
    
    @Override
    public String getType() {
        return "give";
    }
    
    @Override
    public String toString() {
        return "give{" + itemId + " x" + amount + "}";
    }
}
