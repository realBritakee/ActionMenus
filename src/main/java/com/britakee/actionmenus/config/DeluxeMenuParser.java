package com.britakee.actionmenus.config;

import com.britakee.actionmenus.ActionMenus;
import com.britakee.actionmenus.action.Action;
import com.britakee.actionmenus.action.ActionRegistry;
import com.britakee.actionmenus.condition.Condition;
import com.britakee.actionmenus.menu.ClickType;
import com.britakee.actionmenus.menu.MenuDefinition;
import com.britakee.actionmenus.menu.MenuItem;
import com.google.gson.*;

import java.util.*;

/**
 * Parses DeluxeMenus-style JSON menu configurations.
 * 
 * Format example:
 * {
 *   "menu_title": "&8Warps",
 *   "open_command": ["warps", "warp"],
 *   "size": 36,
 *   "update_interval": 1,
 *   "items": {
 *     "item_id": {
 *       "material": "DIAMOND",
 *       "slot": 13,
 *       "display_name": "&bDiamond",
 *       "lore": ["Line 1", "Line 2"],
 *       "left_click_commands": ["[player] say Hello"],
 *       "right_click_commands": ["[close]"]
 *     }
 *   }
 * }
 */
public class DeluxeMenuParser {
    
    private final ActionRegistry actionRegistry;
    
    public DeluxeMenuParser(ActionRegistry actionRegistry) {
        this.actionRegistry = actionRegistry;
    }
    
    /**
     * Parse a DeluxeMenus-style JSON object into a MenuDefinition.
     */
    public MenuDefinition parse(String menuId, JsonObject json, String sourceFile) {
        try {
            // Get menu title
            String title = getStringOrDefault(json, "menu_title", "&8Menu");
            
            // Get size (in slots, convert to rows)
            int size = getIntOrDefault(json, "size", 27);
            int rows = Math.max(1, Math.min(6, size / 9));
            
            // Get update interval
            int updateInterval = getIntOrDefault(json, "update_interval", 0) * 20; // Convert seconds to ticks
            
            MenuDefinition.Builder builder = MenuDefinition.builder()
                    .id(menuId)
                    .title(title)
                    .rows(rows)
                    .sourceFile(sourceFile)
                    .updateInterval(updateInterval);
            
            // Parse open commands
            if (json.has("open_command")) {
                List<String> commands = parseStringList(json.get("open_command"));
                if (!commands.isEmpty()) {
                    builder.commands(commands);
                }
            }
            
            // Also support "open_commands" plural
            if (json.has("open_commands")) {
                List<String> commands = parseStringList(json.get("open_commands"));
                if (!commands.isEmpty()) {
                    builder.commands(commands);
                }
            }
            
            // Parse open requirement
            if (json.has("open_requirement") && json.get("open_requirement").isJsonObject()) {
                builder.openRequirement(parseRequirement(json.getAsJsonObject("open_requirement")));
            }
            
            // Parse open actions
            if (json.has("open_commands")) {
                builder.openActions(parseActions(json.get("open_commands")));
            }
            
            // Parse close actions  
            if (json.has("close_commands")) {
                builder.closeActions(parseActions(json.get("close_commands")));
            }
            
            // Parse items
            Map<Integer, MenuItem> items = new HashMap<>();
            if (json.has("items") && json.get("items").isJsonObject()) {
                JsonObject itemsObj = json.getAsJsonObject("items");
                
                for (Map.Entry<String, JsonElement> entry : itemsObj.entrySet()) {
                    String itemId = entry.getKey();
                    if (!entry.getValue().isJsonObject()) continue;
                    
                    JsonObject itemJson = entry.getValue().getAsJsonObject();
                    MenuItem item = parseItem(itemId, itemJson);
                    
                    if (item != null) {
                        // Place item in all slots
                        for (int slot : item.getSlots()) {
                            if (slot >= 0 && slot < size) {
                                items.put(slot, item);
                            }
                        }
                    }
                }
            }
            
            builder.items(items);
            
            return builder.build();
            
        } catch (Exception e) {
            ActionMenus.LOGGER.error("Failed to parse menu {}: {}", menuId, e.getMessage());
            return null;
        }
    }
    
    /**
     * Parse a single item from JSON.
     */
    private MenuItem parseItem(String id, JsonObject json) {
        // Get material
        String material = getStringOrDefault(json, "material", "STONE");
        // Convert to minecraft namespace if needed
        if (!material.contains(":")) {
            material = "minecraft:" + material.toLowerCase();
        }
        
        // Get slot(s)
        List<Integer> slots = new ArrayList<>();
        if (json.has("slot")) {
            slots.add(json.get("slot").getAsInt());
        }
        if (json.has("slots")) {
            slots.addAll(parseIntList(json.get("slots")));
        }
        if (slots.isEmpty()) {
            slots.add(0); // Default to slot 0
        }
        
        MenuItem.Builder builder = MenuItem.builder()
                .id(id)
                .slots(slots)
                .material(material)
                .amount(getIntOrDefault(json, "amount", 1))
                .priority(getIntOrDefault(json, "priority", 0));
        
        // Display name
        if (json.has("display_name")) {
            builder.displayName(json.get("display_name").getAsString());
        }
        
        // Lore
        if (json.has("lore")) {
            builder.lore(parseStringList(json.get("lore")));
        }
        
        // Data / custom model data
        if (json.has("data")) {
            builder.customModelData(json.get("data").getAsInt());
        }
        if (json.has("custom_model_data")) {
            builder.customModelData(json.get("custom_model_data").getAsInt());
        }
        
        // Enchanted glow
        if (json.has("enchanted")) {
            builder.enchanted(json.get("enchanted").getAsBoolean());
        }
        
        // Skull owner
        if (json.has("skull_owner")) {
            builder.skullOwner(json.get("skull_owner").getAsString());
        }
        
        // Dynamic (for placeholders)
        if (json.has("update")) {
            builder.dynamic(json.get("update").getAsBoolean());
        }
        
        // View requirement
        if (json.has("view_requirement") && json.get("view_requirement").isJsonObject()) {
            builder.viewRequirement(parseRequirement(json.getAsJsonObject("view_requirement")));
        }
        
        // Click actions by type
        Map<ClickType, List<Action>> clickActions = new EnumMap<>(ClickType.class);
        
        // Generic click commands (any click)
        if (json.has("click_commands")) {
            clickActions.put(ClickType.ANY, parseActions(json.get("click_commands")));
        }
        
        // Left click
        if (json.has("left_click_commands")) {
            clickActions.put(ClickType.LEFT, parseActions(json.get("left_click_commands")));
        }
        
        // Right click
        if (json.has("right_click_commands")) {
            clickActions.put(ClickType.RIGHT, parseActions(json.get("right_click_commands")));
        }
        
        // Shift left click
        if (json.has("shift_left_click_commands")) {
            clickActions.put(ClickType.SHIFT_LEFT, parseActions(json.get("shift_left_click_commands")));
        }
        
        // Shift right click
        if (json.has("shift_right_click_commands")) {
            clickActions.put(ClickType.SHIFT_RIGHT, parseActions(json.get("shift_right_click_commands")));
        }
        
        // Middle click
        if (json.has("middle_click_commands")) {
            clickActions.put(ClickType.MIDDLE, parseActions(json.get("middle_click_commands")));
        }
        
        builder.clickActions(clickActions);
        
        return builder.build();
    }
    
    /**
     * Parse a requirement/condition from JSON.
     */
    private Condition parseRequirement(JsonObject json) {
        // Simple permission requirement
        if (json.has("permission")) {
            String perm = json.get("permission").getAsString();
            return Condition.permission(perm);
        }
        
        // Has permission requirement
        if (json.has("has_permission")) {
            String perm = json.get("has_permission").getAsString();
            return Condition.permission(perm);
        }
        
        // Expression requirement
        if (json.has("expression")) {
            String expr = json.get("expression").getAsString();
            return Condition.expression(expr);
        }
        
        // Requirements list (AND logic)
        if (json.has("requirements") && json.get("requirements").isJsonObject()) {
            JsonObject reqs = json.getAsJsonObject("requirements");
            List<Condition> conditions = new ArrayList<>();
            
            for (Map.Entry<String, JsonElement> entry : reqs.entrySet()) {
                if (entry.getValue().isJsonObject()) {
                    JsonObject reqObj = entry.getValue().getAsJsonObject();
                    
                    String type = getStringOrDefault(reqObj, "type", "");
                    
                    switch (type.toLowerCase()) {
                        case "has permission" -> {
                            String perm = getStringOrDefault(reqObj, "permission", "");
                            if (!perm.isEmpty()) {
                                conditions.add(Condition.permission(perm));
                            }
                        }
                        case "!has permission" -> {
                            String perm = getStringOrDefault(reqObj, "permission", "");
                            if (!perm.isEmpty()) {
                                conditions.add(Condition.permission(perm).negate());
                            }
                        }
                        case "string equals" -> {
                            String input = getStringOrDefault(reqObj, "input", "");
                            String output = getStringOrDefault(reqObj, "output", "");
                            conditions.add(Condition.stringEquals(input, output));
                        }
                        case "string contains" -> {
                            String input = getStringOrDefault(reqObj, "input", "");
                            String output = getStringOrDefault(reqObj, "output", "");
                            conditions.add(Condition.stringContains(input, output));
                        }
                        case ">", ">=", "<", "<=", "==", "!=" -> {
                            String input = getStringOrDefault(reqObj, "input", "0");
                            String output = getStringOrDefault(reqObj, "output", "0");
                            conditions.add(Condition.comparison(input, type, output));
                        }
                        case "javascript", "expression" -> {
                            String expr = getStringOrDefault(reqObj, "expression", "true");
                            conditions.add(Condition.expression(expr));
                        }
                    }
                }
            }
            
            if (!conditions.isEmpty()) {
                return conditions.size() == 1 ? conditions.get(0) : Condition.and(conditions);
            }
        }
        
        return Condition.ALWAYS_TRUE;
    }
    
    /**
     * Parse actions from a JSON element (array of strings or objects).
     */
    private List<Action> parseActions(JsonElement element) {
        List<Action> actions = new ArrayList<>();
        
        if (element == null || element.isJsonNull()) {
            return actions;
        }
        
        List<String> actionStrings = parseStringList(element);
        
        for (String actionStr : actionStrings) {
            Action action = actionRegistry.parseAction(actionStr);
            if (action != null) {
                actions.add(action);
            } else {
                // If not a recognized action format, try as player command
                if (!actionStr.startsWith("[")) {
                    action = actionRegistry.parseAction("[player] " + actionStr);
                    if (action != null) {
                        actions.add(action);
                    }
                }
            }
        }
        
        return actions;
    }
    
    /**
     * Parse a JSON element as a list of strings.
     */
    private List<String> parseStringList(JsonElement element) {
        List<String> list = new ArrayList<>();
        
        if (element == null || element.isJsonNull()) {
            return list;
        }
        
        if (element.isJsonArray()) {
            for (JsonElement e : element.getAsJsonArray()) {
                if (e.isJsonPrimitive()) {
                    list.add(e.getAsString());
                }
            }
        } else if (element.isJsonPrimitive()) {
            list.add(element.getAsString());
        }
        
        return list;
    }
    
    /**
     * Parse a JSON element as a list of integers.
     */
    private List<Integer> parseIntList(JsonElement element) {
        List<Integer> list = new ArrayList<>();
        
        if (element == null || element.isJsonNull()) {
            return list;
        }
        
        if (element.isJsonArray()) {
            for (JsonElement e : element.getAsJsonArray()) {
                if (e.isJsonPrimitive()) {
                    list.add(e.getAsInt());
                }
            }
        } else if (element.isJsonPrimitive()) {
            list.add(element.getAsInt());
        }
        
        return list;
    }
    
    /**
     * Get a string value or default.
     */
    private String getStringOrDefault(JsonObject json, String key, String defaultValue) {
        if (json.has(key) && json.get(key).isJsonPrimitive()) {
            return json.get(key).getAsString();
        }
        return defaultValue;
    }
    
    /**
     * Get an int value or default.
     */
    private int getIntOrDefault(JsonObject json, String key, int defaultValue) {
        if (json.has(key) && json.get(key).isJsonPrimitive()) {
            return json.get(key).getAsInt();
        }
        return defaultValue;
    }
}
