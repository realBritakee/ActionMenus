package com.britakee.actionmenus.action;

import com.britakee.actionmenus.condition.Condition;

import java.util.Collections;
import java.util.List;

/**
 * Base class for all actions.
 * Actions are executed when items are clicked or menus are opened/closed.
 */
public abstract class Action {
    
    private int delay = 0;
    private double chance = 1.0;
    private Condition condition = null;
    private List<Action> denyActions = Collections.emptyList();
    
    /**
     * Execute this action.
     */
    public abstract void execute(ActionContext context);
    
    /**
     * Get the action type identifier.
     */
    public abstract String getType();
    
    // Modifiers
    
    public int getDelay() {
        return delay;
    }
    
    public Action withDelay(int delay) {
        this.delay = delay;
        return this;
    }
    
    public double getChance() {
        return chance;
    }
    
    public Action withChance(double chance) {
        this.chance = Math.max(0.0, Math.min(1.0, chance));
        return this;
    }
    
    public Condition getCondition() {
        return condition;
    }
    
    public Action withCondition(Condition condition) {
        this.condition = condition;
        return this;
    }
    
    public List<Action> getDenyActions() {
        return denyActions;
    }
    
    public Action withDenyActions(List<Action> denyActions) {
        this.denyActions = denyActions;
        return this;
    }
    
    public boolean hasDelay() {
        return delay > 0;
    }
    
    public boolean hasChance() {
        return chance < 1.0;
    }
    
    public boolean hasCondition() {
        return condition != null && condition != Condition.ALWAYS_TRUE;
    }
    
    @Override
    public String toString() {
        return getType();
    }
}
