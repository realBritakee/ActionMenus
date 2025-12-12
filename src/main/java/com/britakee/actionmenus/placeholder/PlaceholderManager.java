package com.britakee.actionmenus.placeholder;

import com.britakee.actionmenus.ActionMenus;
import com.britakee.actionmenus.placeholder.providers.*;
import net.minecraft.server.MinecraftServer;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Central manager for placeholder resolution.
 * Handles registration of providers and parsing of placeholder strings.
 * 
 * Placeholder format: %category_placeholder% or %placeholder%
 * Examples:
 *   %player_name% - Player's name
 *   %player_health% - Player's current health
 *   %server_online% - Number of online players
 *   %math_1+1% - Math expression result
 */
public class PlaceholderManager {
    
    // Pattern to match placeholders: %...%
    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("%([^%]+)%");
    
    // Registered placeholder providers
    private final Map<String, PlaceholderProvider> providers = new ConcurrentHashMap<>();
    
    // Cached resolved values (optional, for performance)
    private final Map<String, CachedValue> cache = new ConcurrentHashMap<>();
    private static final long CACHE_DURATION_MS = 100; // 100ms cache
    
    // Server reference
    private MinecraftServer server;
    
    public PlaceholderManager() {
    }
    
    /**
     * Register built-in placeholder providers.
     */
    public void registerBuiltInProviders() {
        register(new PlayerPlaceholderProvider());
        register(new ServerPlaceholderProvider());
        register(new MathPlaceholderProvider());
        register(new DateTimePlaceholderProvider());
        register(new SessionPlaceholderProvider());
        
        ActionMenus.LOGGER.info("Registered {} built-in placeholder providers", providers.size());
    }
    
    /**
     * Register a placeholder provider.
     */
    public void register(PlaceholderProvider provider) {
        providers.put(provider.getIdentifier().toLowerCase(), provider);
        ActionMenus.LOGGER.debug("Registered placeholder provider: {}", provider.getIdentifier());
    }
    
    /**
     * Unregister a placeholder provider.
     */
    public void unregister(String identifier) {
        providers.remove(identifier.toLowerCase());
    }
    
    /**
     * Parse a string and replace all placeholders.
     */
    public String parse(String input, PlaceholderContext context) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        
        if (!input.contains("%")) {
            return input;
        }
        
        Matcher matcher = PLACEHOLDER_PATTERN.matcher(input);
        StringBuilder result = new StringBuilder();
        
        while (matcher.find()) {
            String placeholder = matcher.group(1);
            String replacement = resolve(placeholder, context);
            matcher.appendReplacement(result, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(result);
        
        return result.toString();
    }
    
    /**
     * Resolve a single placeholder.
     */
    public String resolve(String placeholder, PlaceholderContext context) {
        // Check cache first
        String cacheKey = placeholder + "_" + (context.getPlayer() != null ? context.getPlayer().getUUID() : "null");
        CachedValue cached = cache.get(cacheKey);
        if (cached != null && !cached.isExpired()) {
            return cached.getValue();
        }
        
        String result = resolveInternal(placeholder, context);
        
        // Cache the result
        cache.put(cacheKey, new CachedValue(result));
        
        return result;
    }
    
    /**
     * Internal resolution logic.
     */
    private String resolveInternal(String placeholder, PlaceholderContext context) {
        // Try to parse category_placeholder format
        String category;
        String param;
        
        int underscoreIndex = placeholder.indexOf('_');
        if (underscoreIndex > 0) {
            category = placeholder.substring(0, underscoreIndex).toLowerCase();
            param = placeholder.substring(underscoreIndex + 1);
        } else {
            // No category, try all providers
            category = null;
            param = placeholder;
        }
        
        // If category specified, use that provider
        if (category != null) {
            PlaceholderProvider provider = providers.get(category);
            if (provider != null) {
                String result = provider.resolve(param, context);
                if (result != null) {
                    return result;
                }
            }
        }
        
        // Try all providers
        for (PlaceholderProvider provider : providers.values()) {
            String result = provider.resolve(placeholder, context);
            if (result != null) {
                return result;
            }
            
            // Also try without category prefix
            if (category != null) {
                result = provider.resolve(param, context);
                if (result != null) {
                    return result;
                }
            }
        }
        
        // Return original if no provider found
        return "%" + placeholder + "%";
    }
    
    /**
     * Get all registered provider identifiers.
     */
    public Set<String> getProviderIdentifiers() {
        return Collections.unmodifiableSet(providers.keySet());
    }
    
    /**
     * Get a specific provider.
     */
    public PlaceholderProvider getProvider(String identifier) {
        return providers.get(identifier.toLowerCase());
    }
    
    /**
     * Set the server reference.
     */
    public void setServer(MinecraftServer server) {
        this.server = server;
        
        // Update server reference in providers that need it
        for (PlaceholderProvider provider : providers.values()) {
            if (provider instanceof ServerAwarePlaceholderProvider sap) {
                sap.setServer(server);
            }
        }
    }
    
    /**
     * Get the server reference.
     */
    public MinecraftServer getServer() {
        return server;
    }
    
    /**
     * Clear the placeholder cache.
     */
    public void clearCache() {
        cache.clear();
    }
    
    /**
     * Cache entry for placeholder values.
     */
    private static class CachedValue {
        private final String value;
        private final long timestamp;
        
        public CachedValue(String value) {
            this.value = value;
            this.timestamp = System.currentTimeMillis();
        }
        
        public String getValue() {
            return value;
        }
        
        public boolean isExpired() {
            return System.currentTimeMillis() - timestamp > CACHE_DURATION_MS;
        }
    }
}
