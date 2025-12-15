package com.britakee.actionmenus.util;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class for text and color handling.
 * Supports multiple color formats:
 * - Legacy codes: &a, &b, &l, &o, etc.
 * - Hex colors: &#RRGGBB, {#RRGGBB}, <#RRGGBB>, #RRGGBB
 * - Spigot hex: &x&R&R&G&G&B&B
 * - MiniMessage tags: <red>, <bold>, <gradient:color1:color2>
 * - Gradients: <gradient:#RRGGBB:#RRGGBB>text</gradient>
 */
public class TextUtil {
    
    // Hex patterns - various formats
    private static final Pattern HEX_PATTERN = Pattern.compile("&#([A-Fa-f0-9]{6})");
    private static final Pattern HEX_BRACKET_PATTERN = Pattern.compile("\\{#([A-Fa-f0-9]{6})}");
    private static final Pattern HEX_ANGLE_PATTERN = Pattern.compile("<#([A-Fa-f0-9]{6})>");
    private static final Pattern HEX_PLAIN_PATTERN = Pattern.compile("(?<![&{<])#([A-Fa-f0-9]{6})(?![}>])");
    private static final Pattern SPIGOT_HEX_PATTERN = Pattern.compile("&x(&[A-Fa-f0-9]){6}");
    
    // MiniMessage-style patterns
    private static final Pattern MINIMESSAGE_COLOR_PATTERN = Pattern.compile("<(black|dark_blue|dark_green|dark_aqua|dark_red|dark_purple|gold|gray|dark_gray|blue|green|aqua|red|light_purple|yellow|white)>");
    private static final Pattern MINIMESSAGE_FORMAT_PATTERN = Pattern.compile("<(bold|italic|underlined|strikethrough|obfuscated|reset)>");
    private static final Pattern MINIMESSAGE_CLOSE_PATTERN = Pattern.compile("</(bold|italic|underlined|strikethrough|obfuscated|[a-z_]+)>");
    private static final Pattern GRADIENT_PATTERN = Pattern.compile("<gradient:?(#[A-Fa-f0-9]{6}):(#[A-Fa-f0-9]{6})>(.*?)</gradient>", Pattern.DOTALL);
    private static final Pattern RAINBOW_PATTERN = Pattern.compile("<rainbow>(.*?)</rainbow>", Pattern.DOTALL);
    
    /**
     * Convert a string with color codes to a Component.
     */
    public static Component colorize(String text) {
        if (text == null || text.isEmpty()) {
            return Component.empty();
        }
        
        // Handle gradients first (they contain text that needs special processing)
        text = processGradients(text);
        text = processRainbow(text);
        
        // Handle all hex color formats
        text = translateHexColors(text);
        text = translateBracketHex(text);
        text = translateAngleHex(text);
        text = translatePlainHex(text);
        text = translateSpigotHex(text);
        
        // Handle MiniMessage-style tags
        text = translateMiniMessageColors(text);
        text = translateMiniMessageFormats(text);
        
        // Handle legacy color codes
        return translateLegacyColors(text);
    }
    
    /**
     * Process gradient tags and convert to individual hex colors per character.
     */
    private static String processGradients(String text) {
        Matcher matcher = GRADIENT_PATTERN.matcher(text);
        StringBuilder result = new StringBuilder();
        
        while (matcher.find()) {
            String startHex = matcher.group(1).substring(1); // Remove #
            String endHex = matcher.group(2).substring(1);
            String content = matcher.group(3);
            
            String gradientText = applyGradient(content, startHex, endHex);
            matcher.appendReplacement(result, Matcher.quoteReplacement(gradientText));
        }
        matcher.appendTail(result);
        
        return result.toString();
    }
    
    /**
     * Apply gradient colors to text.
     */
    private static String applyGradient(String text, String startHex, String endHex) {
        if (text.isEmpty()) return text;
        
        // Strip existing color codes from content for clean gradient
        String cleanText = stripColors(text);
        if (cleanText.isEmpty()) return text;
        
        int[] startRGB = hexToRGB(startHex);
        int[] endRGB = hexToRGB(endHex);
        
        StringBuilder result = new StringBuilder();
        int length = cleanText.length();
        
        for (int i = 0; i < length; i++) {
            float ratio = length == 1 ? 0 : (float) i / (length - 1);
            int r = (int) (startRGB[0] + ratio * (endRGB[0] - startRGB[0]));
            int g = (int) (startRGB[1] + ratio * (endRGB[1] - startRGB[1]));
            int b = (int) (startRGB[2] + ratio * (endRGB[2] - startRGB[2]));
            
            result.append(String.format("&#%02X%02X%02X%c", r, g, b, cleanText.charAt(i)));
        }
        
        return result.toString();
    }
    
    /**
     * Process rainbow tags.
     */
    private static String processRainbow(String text) {
        Matcher matcher = RAINBOW_PATTERN.matcher(text);
        StringBuilder result = new StringBuilder();
        
        while (matcher.find()) {
            String content = matcher.group(1);
            String rainbowText = applyRainbow(content);
            matcher.appendReplacement(result, Matcher.quoteReplacement(rainbowText));
        }
        matcher.appendTail(result);
        
        return result.toString();
    }
    
    /**
     * Apply rainbow colors to text.
     */
    private static String applyRainbow(String text) {
        String cleanText = stripColors(text);
        if (cleanText.isEmpty()) return text;
        
        StringBuilder result = new StringBuilder();
        int length = cleanText.length();
        
        for (int i = 0; i < length; i++) {
            float hue = (float) i / length;
            int rgb = java.awt.Color.HSBtoRGB(hue, 1.0f, 1.0f);
            int r = (rgb >> 16) & 0xFF;
            int g = (rgb >> 8) & 0xFF;
            int b = rgb & 0xFF;
            
            result.append(String.format("&#%02X%02X%02X%c", r, g, b, cleanText.charAt(i)));
        }
        
        return result.toString();
    }
    
    /**
     * Convert hex string to RGB array.
     */
    private static int[] hexToRGB(String hex) {
        return new int[] {
            Integer.parseInt(hex.substring(0, 2), 16),
            Integer.parseInt(hex.substring(2, 4), 16),
            Integer.parseInt(hex.substring(4, 6), 16)
        };
    }
    
    /**
     * Translate hex color codes (&#RRGGBB) to internal format.
     */
    private static String translateHexColors(String text) {
        Matcher matcher = HEX_PATTERN.matcher(text);
        StringBuilder result = new StringBuilder();
        
        while (matcher.find()) {
            String hex = matcher.group(1);
            matcher.appendReplacement(result, "§x" + 
                    "§" + hex.charAt(0) + "§" + hex.charAt(1) +
                    "§" + hex.charAt(2) + "§" + hex.charAt(3) +
                    "§" + hex.charAt(4) + "§" + hex.charAt(5));
        }
        matcher.appendTail(result);
        
        return result.toString();
    }
    
    /**
     * Translate bracket hex ({#RRGGBB}) to internal format.
     */
    private static String translateBracketHex(String text) {
        Matcher matcher = HEX_BRACKET_PATTERN.matcher(text);
        StringBuilder result = new StringBuilder();
        
        while (matcher.find()) {
            String hex = matcher.group(1);
            matcher.appendReplacement(result, "§x" + 
                    "§" + hex.charAt(0) + "§" + hex.charAt(1) +
                    "§" + hex.charAt(2) + "§" + hex.charAt(3) +
                    "§" + hex.charAt(4) + "§" + hex.charAt(5));
        }
        matcher.appendTail(result);
        
        return result.toString();
    }
    
    /**
     * Translate angle bracket hex (<#RRGGBB>) to internal format.
     */
    private static String translateAngleHex(String text) {
        Matcher matcher = HEX_ANGLE_PATTERN.matcher(text);
        StringBuilder result = new StringBuilder();
        
        while (matcher.find()) {
            String hex = matcher.group(1);
            matcher.appendReplacement(result, "§x" + 
                    "§" + hex.charAt(0) + "§" + hex.charAt(1) +
                    "§" + hex.charAt(2) + "§" + hex.charAt(3) +
                    "§" + hex.charAt(4) + "§" + hex.charAt(5));
        }
        matcher.appendTail(result);
        
        return result.toString();
    }
    
    /**
     * Translate plain hex (#RRGGBB) to internal format.
     */
    private static String translatePlainHex(String text) {
        Matcher matcher = HEX_PLAIN_PATTERN.matcher(text);
        StringBuilder result = new StringBuilder();
        
        while (matcher.find()) {
            String hex = matcher.group(1);
            matcher.appendReplacement(result, "§x" + 
                    "§" + hex.charAt(0) + "§" + hex.charAt(1) +
                    "§" + hex.charAt(2) + "§" + hex.charAt(3) +
                    "§" + hex.charAt(4) + "§" + hex.charAt(5));
        }
        matcher.appendTail(result);
        
        return result.toString();
    }
    
    /**
     * Translate Spigot-style hex (&x&R&R&G&G&B&B) to internal format.
     */
    private static String translateSpigotHex(String text) {
        Matcher matcher = SPIGOT_HEX_PATTERN.matcher(text);
        StringBuilder result = new StringBuilder();
        
        while (matcher.find()) {
            String match = matcher.group();
            // Convert &x&R&R&G&G&B&B to §x§R§R§G§G§B§B
            String converted = match.replace('&', '§');
            matcher.appendReplacement(result, Matcher.quoteReplacement(converted));
        }
        matcher.appendTail(result);
        
        return result.toString();
    }
    
    /**
     * Translate MiniMessage color tags to legacy codes.
     */
    private static String translateMiniMessageColors(String text) {
        text = text.replace("<black>", "&0");
        text = text.replace("<dark_blue>", "&1");
        text = text.replace("<dark_green>", "&2");
        text = text.replace("<dark_aqua>", "&3");
        text = text.replace("<dark_red>", "&4");
        text = text.replace("<dark_purple>", "&5");
        text = text.replace("<gold>", "&6");
        text = text.replace("<gray>", "&7");
        text = text.replace("<grey>", "&7");
        text = text.replace("<dark_gray>", "&8");
        text = text.replace("<dark_grey>", "&8");
        text = text.replace("<blue>", "&9");
        text = text.replace("<green>", "&a");
        text = text.replace("<aqua>", "&b");
        text = text.replace("<red>", "&c");
        text = text.replace("<light_purple>", "&d");
        text = text.replace("<pink>", "&d");
        text = text.replace("<yellow>", "&e");
        text = text.replace("<white>", "&f");
        return text;
    }
    
    /**
     * Translate MiniMessage format tags to legacy codes.
     */
    private static String translateMiniMessageFormats(String text) {
        text = text.replace("<bold>", "&l");
        text = text.replace("<b>", "&l");
        text = text.replace("<italic>", "&o");
        text = text.replace("<i>", "&o");
        text = text.replace("<em>", "&o");
        text = text.replace("<underlined>", "&n");
        text = text.replace("<u>", "&n");
        text = text.replace("<strikethrough>", "&m");
        text = text.replace("<st>", "&m");
        text = text.replace("<obfuscated>", "&k");
        text = text.replace("<obf>", "&k");
        text = text.replace("<reset>", "&r");
        text = text.replace("<r>", "&r");
        
        // Remove closing tags (they don't map to legacy codes directly)
        text = MINIMESSAGE_CLOSE_PATTERN.matcher(text).replaceAll("");
        
        return text;
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
        return text
                .replaceAll("&[0-9a-fk-or]", "")
                .replaceAll("&#[A-Fa-f0-9]{6}", "")
                .replaceAll("\\{#[A-Fa-f0-9]{6}}", "")
                .replaceAll("<#[A-Fa-f0-9]{6}>", "")
                .replaceAll("#[A-Fa-f0-9]{6}", "")
                .replaceAll("§[0-9a-fk-orx]", "")
                .replaceAll("<[a-z_]+>", "")
                .replaceAll("</[a-z_]+>", "")
                .replaceAll("<gradient:[^>]+>", "")
                .replaceAll("</gradient>", "")
                .replaceAll("<rainbow>", "")
                .replaceAll("</rainbow>", "");
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
