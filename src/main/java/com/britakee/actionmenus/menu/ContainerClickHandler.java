package com.britakee.actionmenus.menu;

import com.britakee.actionmenus.ActionMenus;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.ChestMenu;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerContainerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

/**
 * Handles container click events for ActionMenus.
 * 
 * Note: NeoForge doesn't have a direct container click event like Bukkit.
 * We need to use a different approach - either mixins or packet interception.
 * For simplicity, we handle this through the container menu itself.
 */
@EventBusSubscriber(modid = ActionMenus.MOD_ID)
public class ContainerClickHandler {
    
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onContainerOpen(PlayerContainerEvent.Open event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            // Menu open is handled in MenuManager
        }
    }
}
