package com.britakee.actionmenus.action.types;

import com.britakee.actionmenus.ActionMenus;
import com.britakee.actionmenus.action.Action;
import com.britakee.actionmenus.action.ActionContext;
import com.britakee.actionmenus.placeholder.PlaceholderContext;
import com.britakee.actionmenus.util.TextUtil;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import net.minecraft.server.level.ServerPlayer;

/**
 * Action that displays a title and subtitle.
 * Format: title|subtitle|fadeIn|stay|fadeOut
 */
public class TitleAction extends Action {
    
    private final String title;
    private final String subtitle;
    private final int fadeIn;
    private final int stay;
    private final int fadeOut;
    
    public TitleAction(String title, String subtitle, int fadeIn, int stay, int fadeOut) {
        this.title = title;
        this.subtitle = subtitle;
        this.fadeIn = fadeIn;
        this.stay = stay;
        this.fadeOut = fadeOut;
    }
    
    public static TitleAction parse(String value) {
        String[] parts = value.split("\\|");
        
        String title = parts.length > 0 ? parts[0] : "";
        String subtitle = parts.length > 1 ? parts[1] : "";
        int fadeIn = parts.length > 2 ? parseIntSafe(parts[2], 10) : 10;
        int stay = parts.length > 3 ? parseIntSafe(parts[3], 70) : 70;
        int fadeOut = parts.length > 4 ? parseIntSafe(parts[4], 20) : 20;
        
        return new TitleAction(title, subtitle, fadeIn, stay, fadeOut);
    }
    
    private static int parseIntSafe(String s, int defaultValue) {
        try {
            return Integer.parseInt(s.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
    
    @Override
    public void execute(ActionContext context) {
        ServerPlayer player = context.getPlayer();
        
        // Parse placeholders
        PlaceholderContext placeholderCtx = new PlaceholderContext(player, context.getSession(), context.getArguments());
        String parsedTitle = ActionMenus.getInstance().getPlaceholderManager().parse(title, placeholderCtx);
        String parsedSubtitle = ActionMenus.getInstance().getPlaceholderManager().parse(subtitle, placeholderCtx);
        
        // Send timing packet first
        player.connection.send(new ClientboundSetTitlesAnimationPacket(fadeIn, stay, fadeOut));
        
        // Send subtitle if present
        if (!parsedSubtitle.isEmpty()) {
            player.connection.send(new ClientboundSetSubtitleTextPacket(TextUtil.colorize(parsedSubtitle)));
        }
        
        // Send title
        player.connection.send(new ClientboundSetTitleTextPacket(TextUtil.colorize(parsedTitle)));
    }
    
    @Override
    public String getType() {
        return "title";
    }
    
    @Override
    public String toString() {
        return "title{" + title + "|" + subtitle + "}";
    }
}
