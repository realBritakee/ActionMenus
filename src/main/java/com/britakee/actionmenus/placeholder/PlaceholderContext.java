package com.britakee.actionmenus.placeholder;

import com.britakee.actionmenus.menu.MenuSession;
import net.minecraft.server.level.ServerPlayer;

/**
 * Context for placeholder resolution.
 * Contains the player, session, and any arguments.
 */
public class PlaceholderContext {
    
    private final ServerPlayer player;
    private final MenuSession session;
    private final String[] arguments;
    
    public PlaceholderContext(ServerPlayer player) {
        this(player, null, null);
    }
    
    public PlaceholderContext(ServerPlayer player, MenuSession session) {
        this(player, session, null);
    }
    
    public PlaceholderContext(ServerPlayer player, MenuSession session, String[] arguments) {
        this.player = player;
        this.session = session;
        this.arguments = arguments;
    }
    
    public ServerPlayer getPlayer() {
        return player;
    }
    
    public MenuSession getSession() {
        return session;
    }
    
    public String[] getArguments() {
        return arguments;
    }
    
    public String getArgument(int index) {
        if (arguments == null || index < 0 || index >= arguments.length) {
            return "";
        }
        return arguments[index];
    }
    
    public String getArgumentsString() {
        if (arguments == null || arguments.length == 0) {
            return "";
        }
        return String.join(" ", arguments);
    }
    
    public boolean hasPlayer() {
        return player != null;
    }
    
    public boolean hasSession() {
        return session != null;
    }
    
    public boolean hasArguments() {
        return arguments != null && arguments.length > 0;
    }
}
