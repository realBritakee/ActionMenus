package com.britakee.actionmenus.placeholder;

/**
 * Interface for placeholder providers.
 * Implement this to add custom placeholders to ActionMenus.
 */
public interface PlaceholderProvider {
    
    /**
     * Get the identifier for this provider.
     * This is used as the prefix in placeholders (e.g., "player" for %player_name%).
     */
    String getIdentifier();
    
    /**
     * Get the author of this provider.
     */
    default String getAuthor() {
        return "ActionMenus";
    }
    
    /**
     * Get the version of this provider.
     */
    default String getVersion() {
        return "1.0.0";
    }
    
    /**
     * Resolve a placeholder.
     * 
     * @param placeholder The placeholder without the category prefix (e.g., "name" for %player_name%)
     * @param context The context containing player and session info
     * @return The resolved value, or null if this provider cannot handle the placeholder
     */
    String resolve(String placeholder, PlaceholderContext context);
    
    /**
     * Get a list of all placeholders this provider supports.
     * Used for documentation and tab completion.
     */
    default String[] getPlaceholders() {
        return new String[0];
    }
}
