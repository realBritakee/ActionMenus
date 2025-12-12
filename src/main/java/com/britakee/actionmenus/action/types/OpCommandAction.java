package com.britakee.actionmenus.action.types;

import com.britakee.actionmenus.ActionMenus;
import com.britakee.actionmenus.action.Action;
import com.britakee.actionmenus.action.ActionContext;
import com.britakee.actionmenus.placeholder.PlaceholderContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

/**
 * Action that executes a command with OP permissions temporarily.
 */
public class OpCommandAction extends Action {
    
    private final String command;
    
    public OpCommandAction(String command) {
        this.command = command.startsWith("/") ? command.substring(1) : command;
    }
    
    @Override
    public void execute(ActionContext context) {
        ServerPlayer player = context.getPlayer();
        MinecraftServer server = player.getServer();
        
        if (server == null) return;
        
        // Parse placeholders
        PlaceholderContext placeholderCtx = new PlaceholderContext(player, context.getSession(), context.getArguments());
        String parsedCommand = ActionMenus.getInstance().getPlaceholderManager().parse(command, placeholderCtx);
        
        // Create command source with elevated permissions
        CommandSourceStack source = player.createCommandSourceStack()
                .withPermission(4); // OP level 4
        
        // Execute command
        try {
            server.getCommands().performPrefixedCommand(source, parsedCommand);
            ActionMenus.LOGGER.debug("Executed OP command '{}' for {}", parsedCommand, player.getName().getString());
        } catch (Exception e) {
            ActionMenus.LOGGER.error("Failed to execute OP command '{}': {}", parsedCommand, e.getMessage());
        }
    }
    
    @Override
    public String getType() {
        return "op";
    }
    
    @Override
    public String toString() {
        return "op{" + command + "}";
    }
}
