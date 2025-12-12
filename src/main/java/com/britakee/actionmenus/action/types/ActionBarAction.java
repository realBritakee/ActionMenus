package com.britakee.actionmenus.action.types;

import com.britakee.actionmenus.ActionMenus;
import com.britakee.actionmenus.action.Action;
import com.britakee.actionmenus.action.ActionContext;
import com.britakee.actionmenus.placeholder.PlaceholderContext;
import com.britakee.actionmenus.util.TextUtil;
import net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket;
import net.minecraft.server.level.ServerPlayer;

/**
 * Action that displays a message in the action bar.
 */
public class ActionBarAction extends Action {
    
    private final String message;
    
    public ActionBarAction(String message) {
        this.message = message;
    }
    
    @Override
    public void execute(ActionContext context) {
        ServerPlayer player = context.getPlayer();
        
        // Parse placeholders
        PlaceholderContext placeholderCtx = new PlaceholderContext(player, context.getSession(), context.getArguments());
        String parsedMessage = ActionMenus.getInstance().getPlaceholderManager().parse(message, placeholderCtx);
        
        // Send action bar packet
        player.connection.send(new ClientboundSetActionBarTextPacket(TextUtil.colorize(parsedMessage)));
    }
    
    @Override
    public String getType() {
        return "actionbar";
    }
    
    @Override
    public String toString() {
        return "actionbar{" + message + "}";
    }
}
