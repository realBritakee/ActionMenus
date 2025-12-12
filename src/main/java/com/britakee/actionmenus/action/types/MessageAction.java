package com.britakee.actionmenus.action.types;

import com.britakee.actionmenus.ActionMenus;
import com.britakee.actionmenus.action.Action;
import com.britakee.actionmenus.action.ActionContext;
import com.britakee.actionmenus.placeholder.PlaceholderContext;
import com.britakee.actionmenus.util.TextUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import net.minecraft.server.level.ServerPlayer;

/**
 * Action that sends a message to the player.
 */
public class MessageAction extends Action {
    
    private final String message;
    
    public MessageAction(String message) {
        this.message = message;
    }
    
    @Override
    public void execute(ActionContext context) {
        ServerPlayer player = context.getPlayer();
        
        // Parse placeholders
        PlaceholderContext placeholderCtx = new PlaceholderContext(player, context.getSession(), context.getArguments());
        String parsedMessage = ActionMenus.getInstance().getPlaceholderManager().parse(message, placeholderCtx);
        
        // Send message
        player.sendSystemMessage(TextUtil.colorize(parsedMessage));
    }
    
    @Override
    public String getType() {
        return "message";
    }
    
    @Override
    public String toString() {
        return "message{" + message + "}";
    }
}
