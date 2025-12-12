package com.britakee.actionmenus.action.types;

import com.britakee.actionmenus.ActionMenus;
import com.britakee.actionmenus.action.Action;
import com.britakee.actionmenus.action.ActionContext;
import com.britakee.actionmenus.placeholder.PlaceholderContext;
import com.britakee.actionmenus.util.TextUtil;
import net.minecraft.server.level.ServerPlayer;

/**
 * Action that broadcasts a message to all players.
 */
public class BroadcastAction extends Action {
    
    private final String message;
    
    public BroadcastAction(String message) {
        this.message = message;
    }
    
    @Override
    public void execute(ActionContext context) {
        ServerPlayer player = context.getPlayer();
        
        if (player.getServer() == null) return;
        
        // Parse placeholders
        PlaceholderContext placeholderCtx = new PlaceholderContext(player, context.getSession(), context.getArguments());
        String parsedMessage = ActionMenus.getInstance().getPlaceholderManager().parse(message, placeholderCtx);
        
        // Broadcast to all players
        player.getServer().getPlayerList().broadcastSystemMessage(TextUtil.colorize(parsedMessage), false);
    }
    
    @Override
    public String getType() {
        return "broadcast";
    }
    
    @Override
    public String toString() {
        return "broadcast{" + message + "}";
    }
}
