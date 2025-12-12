package com.britakee.actionmenus.action.types;

import com.britakee.actionmenus.ActionMenus;
import com.britakee.actionmenus.action.Action;
import com.britakee.actionmenus.action.ActionContext;
import com.britakee.actionmenus.placeholder.PlaceholderContext;
import net.minecraft.server.level.ServerPlayer;

/**
 * Action that teleports the player.
 * Format: x y z [world]
 */
public class TeleportAction extends Action {
    
    private final String x;
    private final String y;
    private final String z;
    private final String world;
    
    public TeleportAction(String x, String y, String z, String world) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.world = world;
    }
    
    public static TeleportAction parse(String value) {
        String[] parts = value.trim().split("\\s+");
        
        String x = parts.length > 0 ? parts[0] : "~";
        String y = parts.length > 1 ? parts[1] : "~";
        String z = parts.length > 2 ? parts[2] : "~";
        String world = parts.length > 3 ? parts[3] : null;
        
        return new TeleportAction(x, y, z, world);
    }
    
    @Override
    public void execute(ActionContext context) {
        ServerPlayer player = context.getPlayer();
        
        // Parse placeholders
        PlaceholderContext placeholderCtx = new PlaceholderContext(player, context.getSession(), context.getArguments());
        String parsedX = ActionMenus.getInstance().getPlaceholderManager().parse(x, placeholderCtx);
        String parsedY = ActionMenus.getInstance().getPlaceholderManager().parse(y, placeholderCtx);
        String parsedZ = ActionMenus.getInstance().getPlaceholderManager().parse(z, placeholderCtx);
        
        double targetX = parseCoord(parsedX, player.getX());
        double targetY = parseCoord(parsedY, player.getY());
        double targetZ = parseCoord(parsedZ, player.getZ());
        
        player.teleportTo(targetX, targetY, targetZ);
    }
    
    private double parseCoord(String coord, double current) {
        if (coord.startsWith("~")) {
            if (coord.length() == 1) {
                return current;
            }
            try {
                return current + Double.parseDouble(coord.substring(1));
            } catch (NumberFormatException e) {
                return current;
            }
        }
        try {
            return Double.parseDouble(coord);
        } catch (NumberFormatException e) {
            return current;
        }
    }
    
    @Override
    public String getType() {
        return "teleport";
    }
    
    @Override
    public String toString() {
        return "teleport{" + x + " " + y + " " + z + "}";
    }
}
