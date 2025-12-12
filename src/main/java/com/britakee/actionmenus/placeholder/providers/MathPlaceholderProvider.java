package com.britakee.actionmenus.placeholder.providers;

import com.britakee.actionmenus.placeholder.PlaceholderContext;
import com.britakee.actionmenus.placeholder.PlaceholderProvider;

import java.util.Stack;
import java.util.regex.Pattern;

/**
 * Provides math expression evaluation placeholders.
 * 
 * Usage: %math_<expression>%
 * Examples:
 *   %math_5+3%         = 8
 *   %math_10/2%        = 5
 *   %math_2*3+4%       = 10
 *   %math_(2+3)*4%     = 20
 *   %math_round(3.7)%  = 4
 *   %math_floor(3.7)%  = 3
 *   %math_ceil(3.2)%   = 4
 */
public class MathPlaceholderProvider implements PlaceholderProvider {
    
    private static final Pattern NUMBER_PATTERN = Pattern.compile("-?\\d+(\\.\\d+)?");
    
    @Override
    public String getIdentifier() {
        return "math";
    }
    
    @Override
    public String resolve(String placeholder, PlaceholderContext context) {
        String lowerPlaceholder = placeholder.toLowerCase();
        
        // Remove "math_" prefix if present
        String expression = lowerPlaceholder;
        if (expression.startsWith("math_")) {
            expression = expression.substring(5);
        }
        
        // Handle special functions
        if (expression.startsWith("round(") && expression.endsWith(")")) {
            String inner = expression.substring(6, expression.length() - 1);
            try {
                double value = evaluate(inner);
                return String.valueOf(Math.round(value));
            } catch (Exception e) {
                return "0";
            }
        }
        
        if (expression.startsWith("floor(") && expression.endsWith(")")) {
            String inner = expression.substring(6, expression.length() - 1);
            try {
                double value = evaluate(inner);
                return String.valueOf((long) Math.floor(value));
            } catch (Exception e) {
                return "0";
            }
        }
        
        if (expression.startsWith("ceil(") && expression.endsWith(")")) {
            String inner = expression.substring(5, expression.length() - 1);
            try {
                double value = evaluate(inner);
                return String.valueOf((long) Math.ceil(value));
            } catch (Exception e) {
                return "0";
            }
        }
        
        if (expression.startsWith("abs(") && expression.endsWith(")")) {
            String inner = expression.substring(4, expression.length() - 1);
            try {
                double value = evaluate(inner);
                return formatNumber(Math.abs(value));
            } catch (Exception e) {
                return "0";
            }
        }
        
        if (expression.startsWith("sqrt(") && expression.endsWith(")")) {
            String inner = expression.substring(5, expression.length() - 1);
            try {
                double value = evaluate(inner);
                return formatNumber(Math.sqrt(value));
            } catch (Exception e) {
                return "0";
            }
        }
        
        if (expression.startsWith("pow(") && expression.endsWith(")")) {
            String inner = expression.substring(4, expression.length() - 1);
            String[] parts = inner.split(",");
            if (parts.length == 2) {
                try {
                    double base = evaluate(parts[0].trim());
                    double exp = evaluate(parts[1].trim());
                    return formatNumber(Math.pow(base, exp));
                } catch (Exception e) {
                    return "0";
                }
            }
        }
        
        if (expression.startsWith("min(") && expression.endsWith(")")) {
            String inner = expression.substring(4, expression.length() - 1);
            String[] parts = inner.split(",");
            if (parts.length >= 2) {
                try {
                    double min = Double.MAX_VALUE;
                    for (String part : parts) {
                        min = Math.min(min, evaluate(part.trim()));
                    }
                    return formatNumber(min);
                } catch (Exception e) {
                    return "0";
                }
            }
        }
        
        if (expression.startsWith("max(") && expression.endsWith(")")) {
            String inner = expression.substring(4, expression.length() - 1);
            String[] parts = inner.split(",");
            if (parts.length >= 2) {
                try {
                    double max = Double.MIN_VALUE;
                    for (String part : parts) {
                        max = Math.max(max, evaluate(part.trim()));
                    }
                    return formatNumber(max);
                } catch (Exception e) {
                    return "0";
                }
            }
        }
        
        if (expression.equals("random") || expression.equals("rand")) {
            return formatNumber(Math.random());
        }
        
        if (expression.startsWith("random(") && expression.endsWith(")")) {
            String inner = expression.substring(7, expression.length() - 1);
            String[] parts = inner.split(",");
            if (parts.length == 2) {
                try {
                    int min = (int) evaluate(parts[0].trim());
                    int max = (int) evaluate(parts[1].trim());
                    return String.valueOf(min + (int) (Math.random() * (max - min + 1)));
                } catch (Exception e) {
                    return "0";
                }
            }
        }
        
        // Regular math expression
        try {
            double result = evaluate(expression);
            return formatNumber(result);
        } catch (Exception e) {
            return "0";
        }
    }
    
    /**
     * Evaluate a mathematical expression.
     * Supports: +, -, *, /, %, ^, ()
     */
    private double evaluate(String expression) {
        expression = expression.replaceAll("\\s+", "");
        return parseExpression(expression, new int[]{0});
    }
    
    private double parseExpression(String expr, int[] pos) {
        double result = parseTerm(expr, pos);
        
        while (pos[0] < expr.length()) {
            char op = expr.charAt(pos[0]);
            if (op != '+' && op != '-') break;
            pos[0]++;
            double term = parseTerm(expr, pos);
            result = (op == '+') ? result + term : result - term;
        }
        
        return result;
    }
    
    private double parseTerm(String expr, int[] pos) {
        double result = parseFactor(expr, pos);
        
        while (pos[0] < expr.length()) {
            char op = expr.charAt(pos[0]);
            if (op != '*' && op != '/' && op != '%') break;
            pos[0]++;
            double factor = parseFactor(expr, pos);
            if (op == '*') result *= factor;
            else if (op == '/') result /= factor;
            else result %= factor;
        }
        
        return result;
    }
    
    private double parseFactor(String expr, int[] pos) {
        double result = parsePower(expr, pos);
        
        while (pos[0] < expr.length() && expr.charAt(pos[0]) == '^') {
            pos[0]++;
            double power = parsePower(expr, pos);
            result = Math.pow(result, power);
        }
        
        return result;
    }
    
    private double parsePower(String expr, int[] pos) {
        if (pos[0] >= expr.length()) return 0;
        
        char c = expr.charAt(pos[0]);
        
        // Handle unary minus
        if (c == '-') {
            pos[0]++;
            return -parsePower(expr, pos);
        }
        
        // Handle parentheses
        if (c == '(') {
            pos[0]++;
            double result = parseExpression(expr, pos);
            if (pos[0] < expr.length() && expr.charAt(pos[0]) == ')') {
                pos[0]++;
            }
            return result;
        }
        
        // Parse number
        StringBuilder numStr = new StringBuilder();
        while (pos[0] < expr.length()) {
            c = expr.charAt(pos[0]);
            if (Character.isDigit(c) || c == '.') {
                numStr.append(c);
                pos[0]++;
            } else {
                break;
            }
        }
        
        if (numStr.length() == 0) return 0;
        return Double.parseDouble(numStr.toString());
    }
    
    private String formatNumber(double value) {
        if (value == (long) value) {
            return String.valueOf((long) value);
        }
        return String.format("%.2f", value);
    }
    
    @Override
    public String[] getPlaceholders() {
        return new String[] {
                "<expression>", "round(<expr>)", "floor(<expr>)", "ceil(<expr>)",
                "abs(<expr>)", "sqrt(<expr>)", "pow(<base>,<exp>)",
                "min(<a>,<b>)", "max(<a>,<b>)", "random", "random(<min>,<max>)"
        };
    }
}
