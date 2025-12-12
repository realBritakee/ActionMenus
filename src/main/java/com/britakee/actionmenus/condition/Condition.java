package com.britakee.actionmenus.condition;

import com.britakee.actionmenus.ActionMenus;

import java.util.List;
import java.util.regex.Pattern;

/**
 * Represents a condition that can be evaluated.
 * Conditions are used for view requirements, click requirements, and action conditions.
 */
public abstract class Condition {
    
    /** A condition that always returns true */
    public static final Condition ALWAYS_TRUE = new Condition() {
        @Override
        public boolean evaluate(ConditionContext context) {
            return true;
        }
        
        @Override
        public String toString() {
            return "ALWAYS_TRUE";
        }
    };
    
    /** A condition that always returns false */
    public static final Condition ALWAYS_FALSE = new Condition() {
        @Override
        public boolean evaluate(ConditionContext context) {
            return false;
        }
        
        @Override
        public String toString() {
            return "ALWAYS_FALSE";
        }
    };
    
    /**
     * Evaluate this condition.
     */
    public abstract boolean evaluate(ConditionContext context);
    
    /**
     * Create a negated version of this condition.
     */
    public Condition negate() {
        Condition original = this;
        return new Condition() {
            @Override
            public boolean evaluate(ConditionContext context) {
                return !original.evaluate(context);
            }
            
            @Override
            public String toString() {
                return "NOT(" + original + ")";
            }
        };
    }
    
    // Factory methods
    
    /**
     * Create a permission condition.
     */
    public static Condition permission(String permission) {
        return new PermissionCondition(permission);
    }
    
    /**
     * Create a string equals condition.
     */
    public static Condition stringEquals(String expression) {
        String[] parts = expression.toLowerCase().split(" equals ");
        if (parts.length != 2) {
            return ALWAYS_TRUE;
        }
        return new StringEqualsCondition(parts[0].trim(), parts[1].trim());
    }
    
    public static Condition stringEquals(String input, String expected) {
        return new StringEqualsCondition(input, expected);
    }
    
    /**
     * Create a string contains condition.
     */
    public static Condition stringContains(String expression) {
        String[] parts = expression.toLowerCase().split(" contains ");
        if (parts.length != 2) {
            return ALWAYS_TRUE;
        }
        return new StringContainsCondition(parts[0].trim(), parts[1].trim());
    }
    
    public static Condition stringContains(String input, String substring) {
        return new StringContainsCondition(input, substring);
    }
    
    /**
     * Create a regex condition.
     */
    public static Condition regex(String input, String pattern) {
        return new RegexCondition(input, pattern);
    }
    
    /**
     * Create a comparison condition from a string like "5 > 3" or "%player_level% >= 10".
     */
    public static Condition comparison(String expression) {
        return ComparisonCondition.parse(expression);
    }
    
    public static Condition comparison(String input, String operator, String output) {
        return new ComparisonCondition(input, operator, output);
    }
    
    /**
     * Create an expression/JavaScript condition.
     */
    public static Condition expression(String expression) {
        return new ExpressionCondition(expression);
    }
    
    /**
     * Create an AND condition (all must be true).
     */
    public static Condition and(List<Condition> conditions) {
        return new AndCondition(conditions);
    }
    
    /**
     * Create an OR condition (any must be true).
     */
    public static Condition or(List<Condition> conditions) {
        return new OrCondition(conditions);
    }
    
    // Inner condition classes
    
    /**
     * Permission check condition.
     */
    public static class PermissionCondition extends Condition {
        private final String permission;
        
        public PermissionCondition(String permission) {
            this.permission = permission;
        }
        
        @Override
        public boolean evaluate(ConditionContext context) {
            if (context.getPlayer() == null) return false;
            return ActionMenus.getInstance().getPermissionManager()
                    .hasPermission(context.getPlayer(), permission);
        }
        
        @Override
        public String toString() {
            return "Permission{" + permission + "}";
        }
    }
    
    /**
     * String equality condition.
     */
    public static class StringEqualsCondition extends Condition {
        private final String input;
        private final String expected;
        
        public StringEqualsCondition(String input, String expected) {
            this.input = input;
            this.expected = expected;
        }
        
        @Override
        public boolean evaluate(ConditionContext context) {
            String resolvedInput = context.resolvePlaceholders(input);
            String resolvedExpected = context.resolvePlaceholders(expected);
            return resolvedInput.equalsIgnoreCase(resolvedExpected);
        }
        
        @Override
        public String toString() {
            return "StringEquals{" + input + " == " + expected + "}";
        }
    }
    
    /**
     * String contains condition.
     */
    public static class StringContainsCondition extends Condition {
        private final String input;
        private final String substring;
        
        public StringContainsCondition(String input, String substring) {
            this.input = input;
            this.substring = substring;
        }
        
        @Override
        public boolean evaluate(ConditionContext context) {
            String resolvedInput = context.resolvePlaceholders(input);
            String resolvedSubstring = context.resolvePlaceholders(substring);
            return resolvedInput.toLowerCase().contains(resolvedSubstring.toLowerCase());
        }
        
        @Override
        public String toString() {
            return "StringContains{" + input + " contains " + substring + "}";
        }
    }
    
    /**
     * Regex matching condition.
     */
    public static class RegexCondition extends Condition {
        private final String input;
        private final String patternStr;
        private Pattern pattern;
        
        public RegexCondition(String input, String pattern) {
            this.input = input;
            this.patternStr = pattern;
            try {
                this.pattern = Pattern.compile(pattern);
            } catch (Exception e) {
                this.pattern = null;
            }
        }
        
        @Override
        public boolean evaluate(ConditionContext context) {
            if (pattern == null) return false;
            String resolved = context.resolvePlaceholders(input);
            return pattern.matcher(resolved).matches();
        }
        
        @Override
        public String toString() {
            return "Regex{" + input + " matches " + patternStr + "}";
        }
    }
    
    /**
     * Numeric comparison condition.
     */
    public static class ComparisonCondition extends Condition {
        private final String left;
        private final String operator;
        private final String right;
        
        public ComparisonCondition(String left, String operator, String right) {
            this.left = left;
            this.operator = operator;
            this.right = right;
        }
        
        public static ComparisonCondition parse(String expression) {
            // Try different operators
            String[] operators = {">=", "<=", "!=", "==", ">", "<"};
            
            for (String op : operators) {
                int idx = expression.indexOf(op);
                if (idx > 0) {
                    String left = expression.substring(0, idx).trim();
                    String right = expression.substring(idx + op.length()).trim();
                    return new ComparisonCondition(left, op, right);
                }
            }
            
            return new ComparisonCondition(expression, "==", "true");
        }
        
        @Override
        public boolean evaluate(ConditionContext context) {
            String resolvedLeft = context.resolvePlaceholders(left);
            String resolvedRight = context.resolvePlaceholders(right);
            
            try {
                double leftNum = Double.parseDouble(resolvedLeft);
                double rightNum = Double.parseDouble(resolvedRight);
                
                return switch (operator) {
                    case "==" -> leftNum == rightNum;
                    case "!=" -> leftNum != rightNum;
                    case ">" -> leftNum > rightNum;
                    case "<" -> leftNum < rightNum;
                    case ">=" -> leftNum >= rightNum;
                    case "<=" -> leftNum <= rightNum;
                    default -> false;
                };
            } catch (NumberFormatException e) {
                // Fall back to string comparison
                return switch (operator) {
                    case "==" -> resolvedLeft.equalsIgnoreCase(resolvedRight);
                    case "!=" -> !resolvedLeft.equalsIgnoreCase(resolvedRight);
                    default -> false;
                };
            }
        }
        
        @Override
        public String toString() {
            return "Comparison{" + left + " " + operator + " " + right + "}";
        }
    }
    
    /**
     * Expression/JavaScript-like condition.
     * Evaluates simple boolean expressions.
     */
    public static class ExpressionCondition extends Condition {
        private final String expression;
        
        public ExpressionCondition(String expression) {
            this.expression = expression;
        }
        
        @Override
        public boolean evaluate(ConditionContext context) {
            String resolved = context.resolvePlaceholders(expression);
            
            // Simple true/false check
            if (resolved.equalsIgnoreCase("true")) return true;
            if (resolved.equalsIgnoreCase("false")) return false;
            
            // Try to parse as comparison
            ComparisonCondition comp = ComparisonCondition.parse(resolved);
            return comp.evaluate(context);
        }
        
        @Override
        public String toString() {
            return "Expression{" + expression + "}";
        }
    }
    
    /**
     * AND condition - all must be true.
     */
    public static class AndCondition extends Condition {
        private final List<Condition> conditions;
        
        public AndCondition(List<Condition> conditions) {
            this.conditions = conditions;
        }
        
        @Override
        public boolean evaluate(ConditionContext context) {
            for (Condition condition : conditions) {
                if (!condition.evaluate(context)) {
                    return false;
                }
            }
            return true;
        }
        
        @Override
        public String toString() {
            return "AND" + conditions;
        }
    }
    
    /**
     * OR condition - any must be true.
     */
    public static class OrCondition extends Condition {
        private final List<Condition> conditions;
        
        public OrCondition(List<Condition> conditions) {
            this.conditions = conditions;
        }
        
        @Override
        public boolean evaluate(ConditionContext context) {
            for (Condition condition : conditions) {
                if (condition.evaluate(context)) {
                    return true;
                }
            }
            return false;
        }
        
        @Override
        public String toString() {
            return "OR" + conditions;
        }
    }
}
