package com.britakee.actionmenus.placeholder.providers;

import com.britakee.actionmenus.placeholder.PlaceholderContext;
import com.britakee.actionmenus.placeholder.ServerAwarePlaceholderProvider;
import net.minecraft.server.MinecraftServer;

/**
 * Provides server-related placeholders.
 * 
 * Supported placeholders:
 * - server_online: Number of online players
 * - server_max: Maximum player slots
 * - server_tps: Server TPS (approximated)
 * - server_name: Server name (MOTD)
 * - server_uptime: Server uptime
 */
public class ServerPlaceholderProvider implements ServerAwarePlaceholderProvider {
    
    private MinecraftServer server;
    private long lastTickTime = 0;
    private double estimatedTps = 20.0;
    
    @Override
    public String getIdentifier() {
        return "server";
    }
    
    @Override
    public void setServer(MinecraftServer server) {
        this.server = server;
    }
    
    @Override
    public MinecraftServer getServer() {
        return server;
    }
    
    @Override
    public String resolve(String placeholder, PlaceholderContext context) {
        if (server == null) {
            return null;
        }
        
        String lowerPlaceholder = placeholder.toLowerCase();
        
        return switch (lowerPlaceholder) {
            case "online", "server_online", "players_online" -> 
                    String.valueOf(server.getPlayerCount());
            
            case "max", "server_max", "max_players", "server_max_players" -> 
                    String.valueOf(server.getMaxPlayers());
            
            case "online_percent", "server_online_percent" -> {
                int max = server.getMaxPlayers();
                if (max <= 0) yield "0";
                yield String.format("%.0f", ((double) server.getPlayerCount() / max) * 100);
            }
            
            case "tps", "server_tps" -> String.format("%.1f", getEstimatedTps());
            
            case "tps_color", "server_tps_color" -> getTpsColor(getEstimatedTps());
            
            case "mspt", "server_mspt" -> String.format("%.2f", getAverageTickTime());
            
            case "name", "server_name", "motd" -> server.getMotd();
            
            case "version", "server_version" -> server.getServerVersion();
            
            case "uptime", "server_uptime" -> formatUptime(server.getTickCount());
            
            case "uptime_seconds", "server_uptime_seconds" -> 
                    String.valueOf(server.getTickCount() / 20);
            
            case "uptime_minutes", "server_uptime_minutes" -> 
                    String.valueOf(server.getTickCount() / 20 / 60);
            
            case "uptime_hours", "server_uptime_hours" -> 
                    String.valueOf(server.getTickCount() / 20 / 60 / 60);
            
            case "ram_used", "server_ram_used" -> 
                    formatBytes(Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory());
            
            case "ram_free", "server_ram_free" -> 
                    formatBytes(Runtime.getRuntime().freeMemory());
            
            case "ram_total", "server_ram_total" -> 
                    formatBytes(Runtime.getRuntime().totalMemory());
            
            case "ram_max", "server_ram_max" -> 
                    formatBytes(Runtime.getRuntime().maxMemory());
            
            case "ram_percent", "server_ram_percent" -> {
                long used = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
                long max = Runtime.getRuntime().maxMemory();
                yield String.format("%.0f", ((double) used / max) * 100);
            }
            
            case "world_count", "server_world_count" -> 
                    String.valueOf(server.getAllLevels().spliterator().getExactSizeIfKnown());
            
            case "tick", "server_tick", "current_tick" -> 
                    String.valueOf(server.getTickCount());
            
            default -> null;
        };
    }
    
    private double getEstimatedTps() {
        if (server == null) return 20.0;
        
        // Get average tick time in nanoseconds
        double avgTickTime = getAverageTickTime();
        
        // Calculate TPS (capped at 20)
        if (avgTickTime <= 0) return 20.0;
        double tps = 1000.0 / avgTickTime;
        return Math.min(20.0, tps);
    }
    
    private double getAverageTickTime() {
        if (server == null) return 0.0;
        
        // Get tick times from server
        long[] tickTimes = server.getTickTimesNanos();
        if (tickTimes == null || tickTimes.length == 0) return 0.0;
        
        long sum = 0;
        for (long time : tickTimes) {
            sum += time;
        }
        
        // Convert from nanoseconds to milliseconds
        return (sum / (double) tickTimes.length) / 1_000_000.0;
    }
    
    private String getTpsColor(double tps) {
        if (tps >= 18.0) return "&a"; // Green
        if (tps >= 15.0) return "&e"; // Yellow
        if (tps >= 10.0) return "&6"; // Gold
        return "&c"; // Red
    }
    
    private String formatUptime(int ticks) {
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
    
    private String formatBytes(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
        if (bytes < 1024 * 1024 * 1024) return String.format("%.1f MB", bytes / (1024.0 * 1024.0));
        return String.format("%.2f GB", bytes / (1024.0 * 1024.0 * 1024.0));
    }
    
    @Override
    public String[] getPlaceholders() {
        return new String[] {
                "online", "max", "online_percent", "tps", "tps_color", "mspt",
                "name", "version", "uptime", "uptime_seconds", "uptime_minutes", "uptime_hours",
                "ram_used", "ram_free", "ram_total", "ram_max", "ram_percent",
                "world_count", "tick"
        };
    }
}
