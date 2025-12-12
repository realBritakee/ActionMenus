package com.britakee.actionmenus.condition;

import com.britakee.actionmenus.ActionMenus;
import com.britakee.actionmenus.placeholder.PlaceholderManager;

/**
 * Evaluates conditions with proper context.
 */
public class ConditionEvaluator {
    
    private final PlaceholderManager placeholderManager;
    
    public ConditionEvaluator(PlaceholderManager placeholderManager) {
        this.placeholderManager = placeholderManager;
    }
    
    /**
     * Evaluate a condition.
     */
    public boolean evaluate(Condition condition, ConditionContext context) {
        if (condition == null || condition == Condition.ALWAYS_TRUE) {
            return true;
        }
        
        try {
            return condition.evaluate(context);
        } catch (Exception e) {
            ActionMenus.LOGGER.error("Error evaluating condition: {}", condition, e);
            return false;
        }
    }
    
    /**
     * Get the placeholder manager.
     */
    public PlaceholderManager getPlaceholderManager() {
        return placeholderManager;
    }
}
