package com.britakee.actionmenus.condition;

import com.britakee.actionmenus.placeholder.PlaceholderContext;
import com.britakee.actionmenus.placeholder.PlaceholderManager;
import net.minecraft.server.level.ServerPlayer;

/**
 * Context for condition evaluation.
 * Provides access to player, placeholders, and the condition evaluator.
 */
public class ConditionContext {
    
    private final ServerPlayer player;
    private final PlaceholderContext placeholderContext;
    private final PlaceholderManager placeholderManager;
    private final ConditionEvaluator conditionEvaluator;
    
    public ConditionContext(ServerPlayer player, PlaceholderContext placeholderContext,
                            PlaceholderManager placeholderManager, ConditionEvaluator conditionEvaluator) {
        this.player = player;
        this.placeholderContext = placeholderContext;
        this.placeholderManager = placeholderManager;
        this.conditionEvaluator = conditionEvaluator;
    }
    
    public ServerPlayer getPlayer() {
        return player;
    }
    
    public PlaceholderContext getPlaceholderContext() {
        return placeholderContext;
    }
    
    public PlaceholderManager getPlaceholderManager() {
        return placeholderManager;
    }
    
    public ConditionEvaluator getConditionEvaluator() {
        return conditionEvaluator;
    }
    
    /**
     * Resolve placeholders in a string.
     */
    public String resolvePlaceholders(String input) {
        if (input == null || !input.contains("%")) {
            return input;
        }
        return placeholderManager.parse(input, placeholderContext);
    }
}
