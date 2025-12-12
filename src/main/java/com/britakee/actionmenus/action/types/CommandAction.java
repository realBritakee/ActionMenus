package com.britakee.actionmenus.action.types;

import com.britakee.actionmenus.ActionMenus;
import com.britakee.actionmenus.action.Action;
import com.britakee.actionmenus.action.ActionContext;
import com.britakee.actionmenus.placeholder.PlaceholderContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

/**
 * Action that executes a command.
 * Can run as player or as console.
 */
public class CommandAction extends Action {
    
    private final String command;
    private final boolean asConsole;
    
    public CommandAction(String command, boolean asConsole) {
        this.command = command.startsWith("/") ? command.substring(1) : command;
        this.asConsole = asConsole;
    }
    
    @Override
    public void execute(ActionContext context) {
        ServerPlayer player = context.getPlayer();
        MinecraftServer server = player.getServer();
        
        if (server == null) return;
        
        // Parse placeholders
        PlaceholderContext placeholderCtx = new PlaceholderContext(player, context.getSession(), context.getArguments());
        String parsedCommand = ActionMenus.getInstance().getPlaceholderManager().parse(command, placeholderCtx);
        
        // Get command source
        CommandSourceStack source;
        if (asConsole) {
            source = server.createCommandSourceStack();
        } else {
            source = player.createCommandSourceStack();
        }
        
        // Execute command
        try {
            server.getCommands().performPrefixedCommand(source, parsedCommand);
            ActionMenus.LOGGER.debug("Executed command '{}' as {}", parsedCommand, asConsole ? "console" : player.getName().getString());
        } catch (Exception e) {
            ActionMenus.LOGGER.error("Failed to execute command '{}': {}", parsedCommand, e.getMessage());
        }
    }
    
    @Override
    public String getType() {
        return asConsole ? "console" : "command";
    }
    
    @Override
    public String toString() {
        return getType() + "{" + command + "}";
    }
}
