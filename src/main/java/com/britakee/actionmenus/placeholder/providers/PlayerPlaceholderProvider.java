package com.britakee.actionmenus.placeholder.providers;

import com.britakee.actionmenus.placeholder.PlaceholderContext;
import com.britakee.actionmenus.placeholder.PlaceholderProvider;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;

/**
 * Provides player-related placeholders.
 * 
 * Supported placeholders:
 * - player_name: Player's display name
 * - player_uuid: Player's UUID
 * - player_health: Current health
 * - player_max_health: Maximum health
 * - player_food: Food level
 * - player_saturation: Saturation level
 * - player_level: Experience level
 * - player_exp: Total experience points
 * - player_x, player_y, player_z: Position coordinates
 * - player_world: Current dimension name
 * - player_gamemode: Current game mode
 * - player_ping: Network ping
 * - player_playtime: Total playtime in ticks
 * - player_playtime_formatted: Formatted playtime
 */
public class PlayerPlaceholderProvider implements PlaceholderProvider {
    
    @Override
    public String getIdentifier() {
        return "player";
    }
    
    @Override
    public String resolve(String placeholder, PlaceholderContext context) {
        if (!context.hasPlayer()) {
            return null;
        }
        
        ServerPlayer player = context.getPlayer();
        String lowerPlaceholder = placeholder.toLowerCase();
        
        return switch (lowerPlaceholder) {
            case "name", "player_name", "displayname" -> player.getName().getString();
            case "uuid", "player_uuid" -> player.getUUID().toString();
            
            case "health", "player_health" -> String.format("%.1f", player.getHealth());
            case "max_health", "player_max_health", "maxhealth" -> String.format("%.1f", player.getMaxHealth());
            case "health_percent", "player_health_percent" -> 
                    String.format("%.0f", (player.getHealth() / player.getMaxHealth()) * 100);
            
            case "food", "player_food", "hunger" -> String.valueOf(player.getFoodData().getFoodLevel());
            case "saturation", "player_saturation" -> String.format("%.1f", player.getFoodData().getSaturationLevel());
            
            case "level", "player_level", "exp_level" -> String.valueOf(player.experienceLevel);
            case "exp", "player_exp", "experience" -> String.valueOf(player.totalExperience);
            case "exp_to_level", "player_exp_to_level" -> String.valueOf(player.getXpNeededForNextLevel());
            case "exp_percent", "player_exp_percent" -> String.format("%.0f", player.experienceProgress * 100);
            
            case "x", "player_x" -> String.format("%.0f", player.getX());
            case "y", "player_y" -> String.format("%.0f", player.getY());
            case "z", "player_z" -> String.format("%.0f", player.getZ());
            case "x_precise", "player_x_precise" -> String.format("%.2f", player.getX());
            case "y_precise", "player_y_precise" -> String.format("%.2f", player.getY());
            case "z_precise", "player_z_precise" -> String.format("%.2f", player.getZ());
            case "yaw", "player_yaw" -> String.format("%.1f", player.getYRot());
            case "pitch", "player_pitch" -> String.format("%.1f", player.getXRot());
            case "location", "player_location" -> 
                    String.format("%.0f, %.0f, %.0f", player.getX(), player.getY(), player.getZ());
            
            case "world", "player_world", "dimension" -> 
                    player.level().dimension().location().toString();
            case "world_name", "player_world_name" -> 
                    player.level().dimension().location().getPath();
            
            case "gamemode", "player_gamemode" -> getGameModeName(player.gameMode.getGameModeForPlayer());
            case "is_creative" -> String.valueOf(player.isCreative());
            case "is_spectator" -> String.valueOf(player.isSpectator());
            case "is_survival" -> String.valueOf(player.gameMode.getGameModeForPlayer() == GameType.SURVIVAL);
            
            case "ping", "player_ping" -> String.valueOf(player.connection.latency());
            
            case "playtime", "player_playtime" -> String.valueOf(player.getStats().getValue(
                    net.minecraft.stats.Stats.CUSTOM.get(net.minecraft.stats.Stats.PLAY_TIME)));
            case "playtime_formatted", "player_playtime_formatted" -> formatPlaytime(player.getStats().getValue(
                    net.minecraft.stats.Stats.CUSTOM.get(net.minecraft.stats.Stats.PLAY_TIME)));
            case "playtime_seconds" -> String.valueOf(player.getStats().getValue(
                    net.minecraft.stats.Stats.CUSTOM.get(net.minecraft.stats.Stats.PLAY_TIME)) / 20);
            case "playtime_minutes" -> String.valueOf(player.getStats().getValue(
                    net.minecraft.stats.Stats.CUSTOM.get(net.minecraft.stats.Stats.PLAY_TIME)) / 20 / 60);
            case "playtime_hours" -> String.valueOf(player.getStats().getValue(
                    net.minecraft.stats.Stats.CUSTOM.get(net.minecraft.stats.Stats.PLAY_TIME)) / 20 / 60 / 60);
            
            case "is_sneaking" -> String.valueOf(player.isShiftKeyDown());
            case "is_sprinting" -> String.valueOf(player.isSprinting());
            case "is_flying" -> String.valueOf(player.getAbilities().flying);
            case "is_swimming" -> String.valueOf(player.isSwimming());
            case "is_sleeping" -> String.valueOf(player.isSleeping());
            
            case "armor", "player_armor" -> String.valueOf(player.getArmorValue());
            case "air", "player_air" -> String.valueOf(player.getAirSupply());
            case "max_air", "player_max_air" -> String.valueOf(player.getMaxAirSupply());
            
            case "main_hand", "player_main_hand" -> player.getMainHandItem().getHoverName().getString();
            case "off_hand", "player_off_hand" -> player.getOffhandItem().getHoverName().getString();
            
            default -> null;
        };
    }
    
    private String getGameModeName(GameType gameType) {
        return switch (gameType) {
            case SURVIVAL -> "Survival";
            case CREATIVE -> "Creative";
            case ADVENTURE -> "Adventure";
            case SPECTATOR -> "Spectator";
        };
    }
    
    private String formatPlaytime(int ticks) {
        int seconds = ticks / 20;
        int minutes = seconds / 60;
        int hours = minutes / 60;
        int days = hours / 24;
        
        seconds %= 60;
        minutes %= 60;
        hours %= 24;
        
        if (days > 0) {
            return String.format("%dd %dh %dm", days, hours, minutes);
        } else if (hours > 0) {
            return String.format("%dh %dm %ds", hours, minutes, seconds);
        } else if (minutes > 0) {
            return String.format("%dm %ds", minutes, seconds);
        } else {
            return String.format("%ds", seconds);
        }
    }
    
    @Override
    public String[] getPlaceholders() {
        return new String[] {
                "name", "uuid", "health", "max_health", "health_percent",
                "food", "saturation", "level", "exp", "exp_to_level", "exp_percent",
                "x", "y", "z", "x_precise", "y_precise", "z_precise", "yaw", "pitch", "location",
                "world", "world_name", "gamemode", "is_creative", "is_spectator", "is_survival",
                "ping", "playtime", "playtime_formatted", "playtime_seconds", "playtime_minutes", "playtime_hours",
                "is_sneaking", "is_sprinting", "is_flying", "is_swimming", "is_sleeping",
                "armor", "air", "max_air", "main_hand", "off_hand"
        };
    }
}
