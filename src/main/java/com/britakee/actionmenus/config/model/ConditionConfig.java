package com.britakee.actionmenus.config.model;

import java.util.List;
import java.util.Map;

/**
 * Configuration model for conditions.
 * Conditions determine when items are shown or actions are executed.
 * 
 * Supported condition types:
 * - permission: Check if player has permission
 * - !permission: Check if player does NOT have permission
 * - string equals: Compare strings
 * - string contains: Check if string contains substring
 * - regex: Match string against regex
 * - ==, !=, >, <, >=, <=: Numeric comparisons
 * - javascript: JavaScript expression evaluation
 * - has item: Check if player has item
 * - has money: Check if player has money (requires economy mod)
 * 
 * Conditions can be combined with AND/OR logic.
 */
public class ConditionConfig {
    
    // Simple condition string format
    private String condition;
    
    // Or detailed condition object
    private String type;
    private String input;
    private String output;
    private String operator;
    
    // For permission checks
    private String permission;
    
    // For JavaScript expressions
    private String expression;
    
    // Combined conditions (AND logic by default)
    private List<ConditionConfig> conditions;
    
    // OR logic (any must match)
    private List<ConditionConfig> any;
    
    // NOT logic (invert result)
    private boolean negate = false;
    
    // Deny actions when condition fails
    private List<ActionConfig> denyActions;
    
    // Message to show when condition fails
    private String denyMessage;
    
    // Additional data for complex conditions
    private Map<String, Object> data;
    
    // Default constructor for Gson
    public ConditionConfig() {}
    
    // Constructor for simple conditions
    public ConditionConfig(String condition) {
        this.condition = condition;
    }
    
    // Getters and setters
    public String getCondition() {
        return condition;
    }
    
    public void setCondition(String condition) {
        this.condition = condition;
    }
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public String getInput() {
        return input;
    }
    
    public void setInput(String input) {
        this.input = input;
    }
    
    public String getOutput() {
        return output;
    }
    
    public void setOutput(String output) {
        this.output = output;
    }
    
    public String getOperator() {
        return operator;
    }
    
    public void setOperator(String operator) {
        this.operator = operator;
    }
    
    public String getPermission() {
        return permission;
    }
    
    public void setPermission(String permission) {
        this.permission = permission;
    }
    
    public String getExpression() {
        return expression;
    }
    
    public void setExpression(String expression) {
        this.expression = expression;
    }
    
    public List<ConditionConfig> getConditions() {
        return conditions;
    }
    
    public void setConditions(List<ConditionConfig> conditions) {
        this.conditions = conditions;
    }
    
    public List<ConditionConfig> getAny() {
        return any;
    }
    
    public void setAny(List<ConditionConfig> any) {
        this.any = any;
    }
    
    public boolean isNegate() {
        return negate;
    }
    
    public void setNegate(boolean negate) {
        this.negate = negate;
    }
    
    public List<ActionConfig> getDenyActions() {
        return denyActions;
    }
    
    public void setDenyActions(List<ActionConfig> denyActions) {
        this.denyActions = denyActions;
    }
    
    public String getDenyMessage() {
        return denyMessage;
    }
    
    public void setDenyMessage(String denyMessage) {
        this.denyMessage = denyMessage;
    }
    
    public Map<String, Object> getData() {
        return data;
    }
    
    public void setData(Map<String, Object> data) {
        this.data = data;
    }
    
    /**
     * Check if this is a simple condition string.
     */
    public boolean isSimpleCondition() {
        return condition != null && !condition.isBlank();
    }
    
    /**
     * Check if this is a combined (AND) condition.
     */
    public boolean isAndCondition() {
        return conditions != null && !conditions.isEmpty();
    }
    
    /**
     * Check if this is an OR condition.
     */
    public boolean isOrCondition() {
        return any != null && !any.isEmpty();
    }
    
    /**
     * Check if this is a permission condition.
     */
    public boolean isPermissionCondition() {
        return permission != null && !permission.isBlank();
    }
    
    /**
     * Check if this is a JavaScript expression condition.
     */
    public boolean isExpressionCondition() {
        return expression != null && !expression.isBlank();
    }
    
    @Override
    public String toString() {
        if (isSimpleCondition()) {
            return "Condition{" + condition + "}";
        } else if (isPermissionCondition()) {
            return "Condition{permission=" + permission + "}";
        } else if (isExpressionCondition()) {
            return "Condition{expression=" + expression + "}";
        } else if (isAndCondition()) {
            return "Condition{AND: " + conditions.size() + " conditions}";
        } else if (isOrCondition()) {
            return "Condition{OR: " + any.size() + " conditions}";
        } else {
            return "Condition{type=" + type + ", input=" + input + ", operator=" + operator + ", output=" + output + "}";
        }
    }
}
