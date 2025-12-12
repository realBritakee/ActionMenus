package com.britakee.actionmenus.action.types;

import com.britakee.actionmenus.action.Action;
import com.britakee.actionmenus.action.ActionContext;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * Action that takes/removes items from the player.
 * Format: item [amount]
 */
public class TakeItemAction extends Action {
    
    private final String itemId;
    private final int amount;
    
    public TakeItemAction(String itemId, int amount) {
        this.itemId = itemId;
        this.amount = amount;
    }
    
    public static TakeItemAction parse(String value) {
        String[] parts = value.trim().split("\\s+");
        
        String itemId = parts.length > 0 ? parts[0] : "minecraft:stone";
        int amount = parts.length > 1 ? parseIntSafe(parts[1], 1) : 1;
        
        return new TakeItemAction(itemId, amount);
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
        
        ResourceLocation itemLocation = ResourceLocation.tryParse(itemId);
        if (itemLocation == null) {
            itemLocation = ResourceLocation.withDefaultNamespace(itemId);
        }
        
        Item item = BuiltInRegistries.ITEM.get(itemLocation);
        if (item == Items.AIR) {
            return;
        }
        
        int remaining = amount;
        
        // Search through inventory and remove items
        for (int i = 0; i < player.getInventory().getContainerSize() && remaining > 0; i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.is(item)) {
                int toRemove = Math.min(remaining, stack.getCount());
                stack.shrink(toRemove);
                remaining -= toRemove;
                
                if (stack.isEmpty()) {
                    player.getInventory().setItem(i, ItemStack.EMPTY);
                }
            }
        }
    }
    
    @Override
    public String getType() {
        return "take";
    }
    
    @Override
    public String toString() {
        return "take{" + itemId + " x" + amount + "}";
    }
}
