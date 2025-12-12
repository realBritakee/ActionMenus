package com.britakee.actionmenus.util;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class for text and color handling.
 * Supports both legacy color codes (&a, &b) and hex colors (&#RRGGBB).
 */
public class TextUtil {
    
    private static final Pattern HEX_PATTERN = Pattern.compile("&#([A-Fa-f0-9]{6})");
    private static final Pattern COLOR_PATTERN = Pattern.compile("&([0-9a-fk-or])");
    
    /**
     * Convert a string with color codes to a Component.
     */
    public static Component colorize(String text) {
        if (text == null || text.isEmpty()) {
            return Component.empty();
        }
        
        // Handle hex colors first
        text = translateHexColors(text);
        
        // Handle legacy color codes
        return translateLegacyColors(text);
    }
    
    /**
     * Translate hex color codes (&#RRGGBB) to a parseable format.
     */
    private static String translateHexColors(String text) {
        Matcher matcher = HEX_PATTERN.matcher(text);
        StringBuilder result = new StringBuilder();
        
        while (matcher.find()) {
            String hex = matcher.group(1);
            // Replace with a special marker we can parse later
            matcher.appendReplacement(result, "§x" + 
                    "§" + hex.charAt(0) + "§" + hex.charAt(1) +
                    "§" + hex.charAt(2) + "§" + hex.charAt(3) +
                    "§" + hex.charAt(4) + "§" + hex.charAt(5));
        }
        matcher.appendTail(result);
        
        return result.toString();
    }
    
    /**
     * Translate legacy color codes to Component.
     */
    private static Component translateLegacyColors(String text) {
        // Replace & with § for standard processing
        text = text.replace('&', '§');
        
        MutableComponent result = Component.empty();
        Style currentStyle = Style.EMPTY;
        StringBuilder currentText = new StringBuilder();
        
        int i = 0;
        while (i < text.length()) {
            char c = text.charAt(i);
            
            if (c == '§' && i + 1 < text.length()) {
                char code = Character.toLowerCase(text.charAt(i + 1));
                
                // Flush current text
                if (currentText.length() > 0) {
                    result = result.append(Component.literal(currentText.toString()).withStyle(currentStyle));
                    currentText = new StringBuilder();
                }
                
                // Handle hex colors (§x§R§R§G§G§B§B format)
                if (code == 'x' && i + 13 < text.length()) {
                    StringBuilder hexBuilder = new StringBuilder();
                    boolean valid = true;
                    for (int j = 0; j < 6 && valid; j++) {
                        int hexIndex = i + 3 + (j * 2);
                        if (hexIndex < text.length() && text.charAt(hexIndex - 1) == '§') {
                            hexBuilder.append(text.charAt(hexIndex));
                        } else {
                            valid = false;
                        }
                    }
                    
                    if (valid && hexBuilder.length() == 6) {
                        try {
                            int rgb = Integer.parseInt(hexBuilder.toString(), 16);
                            currentStyle = currentStyle.withColor(TextColor.fromRgb(rgb));
                            i += 14;
                            continue;
                        } catch (NumberFormatException ignored) {}
                    }
                }
                
                // Handle standard color codes
                ChatFormatting formatting = getFormatting(code);
                if (formatting != null) {
                    if (formatting == ChatFormatting.RESET) {
                        currentStyle = Style.EMPTY;
                    } else if (formatting.isColor()) {
                        currentStyle = Style.EMPTY.withColor(formatting);
                    } else {
                        currentStyle = applyFormat(currentStyle, formatting);
                    }
                    i += 2;
                    continue;
                }
            }
            
            currentText.append(c);
            i++;
        }
        
        // Flush remaining text
        if (currentText.length() > 0) {
            result = result.append(Component.literal(currentText.toString()).withStyle(currentStyle));
        }
        
        return result;
    }
    
    /**
     * Get ChatFormatting from color code character.
     */
    private static ChatFormatting getFormatting(char code) {
        return switch (code) {
            case '0' -> ChatFormatting.BLACK;
            case '1' -> ChatFormatting.DARK_BLUE;
            case '2' -> ChatFormatting.DARK_GREEN;
            case '3' -> ChatFormatting.DARK_AQUA;
            case '4' -> ChatFormatting.DARK_RED;
            case '5' -> ChatFormatting.DARK_PURPLE;
            case '6' -> ChatFormatting.GOLD;
            case '7' -> ChatFormatting.GRAY;
            case '8' -> ChatFormatting.DARK_GRAY;
            case '9' -> ChatFormatting.BLUE;
            case 'a' -> ChatFormatting.GREEN;
            case 'b' -> ChatFormatting.AQUA;
            case 'c' -> ChatFormatting.RED;
            case 'd' -> ChatFormatting.LIGHT_PURPLE;
            case 'e' -> ChatFormatting.YELLOW;
            case 'f' -> ChatFormatting.WHITE;
            case 'k' -> ChatFormatting.OBFUSCATED;
            case 'l' -> ChatFormatting.BOLD;
            case 'm' -> ChatFormatting.STRIKETHROUGH;
            case 'n' -> ChatFormatting.UNDERLINE;
            case 'o' -> ChatFormatting.ITALIC;
            case 'r' -> ChatFormatting.RESET;
            default -> null;
        };
    }
    
    /**
     * Apply a formatting style.
     */
    private static Style applyFormat(Style style, ChatFormatting format) {
        return switch (format) {
            case OBFUSCATED -> style.withObfuscated(true);
            case BOLD -> style.withBold(true);
            case STRIKETHROUGH -> style.withStrikethrough(true);
            case UNDERLINE -> style.withUnderlined(true);
            case ITALIC -> style.withItalic(true);
            default -> style;
        };
    }
    
    /**
     * Strip color codes from text.
     */
    public static String stripColors(String text) {
        if (text == null) return null;
        return text.replaceAll("&[0-9a-fk-or]", "")
                   .replaceAll("&#[A-Fa-f0-9]{6}", "")
                   .replaceAll("§[0-9a-fk-orx]", "");
    }
    
    /**
     * Create a simple text component.
     */
    public static Component text(String text) {
        return Component.literal(text);
    }
    
    /**
     * Create a colored text component.
     */
    public static Component text(String text, ChatFormatting color) {
        return Component.literal(text).withStyle(color);
    }
}
