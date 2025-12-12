package com.britakee.actionmenus.config.model;

import java.util.List;
import java.util.Map;

/**
 * Configuration model for an action.
 * Actions can be commands, messages, menu operations, etc.
 * 
 * Supported action types:
 * - [command] <command>       - Run command as player
 * - [console] <command>       - Run command as console
 * - [message] <message>       - Send message to player
 * - [broadcast] <message>     - Broadcast to all players
 * - [sound] <sound> [volume] [pitch] - Play sound
 * - [open] <menu>             - Open another menu
 * - [close]                   - Close current menu
 * - [refresh]                 - Refresh current menu
 * - [give] <item> [amount]    - Give item to player
 * - [take] <item> [amount]    - Take item from player
 * - [delay] <ticks>           - Delay next action
 * - [title] <title>|<subtitle>|<fadeIn>|<stay>|<fadeOut>
 * - [actionbar] <message>     - Show action bar message
 * - [connect] <server>        - BungeeCord server switch (if supported)
 */
public class ActionConfig {
    
    // Simple action string format: "[type] value"
    private String action;
    
    // Or detailed action object
    private String type;
    private String value;
    private Map<String, Object> data;
    
    // Conditional execution
    private ConditionConfig requirement;
    private List<ActionConfig> denyActions;
    
    // Delay before this action (in ticks)
    private int delay = 0;
    
    // Chance to execute (0.0 - 1.0, 1.0 = always)
    private double chance = 1.0;
    
    // Sequence of actions (for complex flows)
    private List<ActionConfig> actions;
    
    // Default constructor for Gson
    public ActionConfig() {}
    
    // Constructor for simple actions
    public ActionConfig(String action) {
        this.action = action;
    }
    
    // Getters and setters
    public String getAction() {
        return action;
    }
    
    public void setAction(String action) {
        this.action = action;
    }
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public String getValue() {
        return value;
    }
    
    public void setValue(String value) {
        this.value = value;
    }
    
    public Map<String, Object> getData() {
        return data;
    }
    
    public void setData(Map<String, Object> data) {
        this.data = data;
    }
    
    public ConditionConfig getRequirement() {
        return requirement;
    }
    
    public void setRequirement(ConditionConfig requirement) {
        this.requirement = requirement;
    }
    
    public List<ActionConfig> getDenyActions() {
        return denyActions;
    }
    
    public void setDenyActions(List<ActionConfig> denyActions) {
        this.denyActions = denyActions;
    }
    
    public int getDelay() {
        return delay;
    }
    
    public void setDelay(int delay) {
        this.delay = Math.max(0, delay);
    }
    
    public double getChance() {
        return chance;
    }
    
    public void setChance(double chance) {
        this.chance = Math.max(0.0, Math.min(1.0, chance));
    }
    
    public List<ActionConfig> getActions() {
        return actions;
    }
    
    public void setActions(List<ActionConfig> actions) {
        this.actions = actions;
    }
    
    /**
     * Check if this is a simple action string.
     */
    public boolean isSimpleAction() {
        return action != null && !action.isBlank();
    }
    
    /**
     * Check if this is an action sequence.
     */
    public boolean isSequence() {
        return actions != null && !actions.isEmpty();
    }
    
    /**
     * Get the resolved action string.
     * Either from the 'action' field or constructed from type/value.
     */
    public String getResolvedAction() {
        if (action != null && !action.isBlank()) {
            return action;
        }
        if (type != null && !type.isBlank()) {
            if (value != null && !value.isBlank()) {
                return "[" + type + "] " + value;
            }
            return "[" + type + "]";
        }
        return null;
    }
    
    @Override
    public String toString() {
        if (isSimpleAction()) {
            return "Action{" + action + "}";
        } else if (isSequence()) {
            return "ActionSequence{" + actions.size() + " actions}";
        } else {
            return "Action{type=" + type + ", value=" + value + "}";
        }
    }
}
