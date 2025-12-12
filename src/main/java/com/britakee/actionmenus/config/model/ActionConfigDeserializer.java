package com.britakee.actionmenus.config.model;

import com.google.gson.*;
import java.lang.reflect.Type;

/**
 * Custom Gson deserializer for ActionConfig that handles both:
 * - Simple string format: "[message] Hello!"
 * - Object format: { "action": "[message] Hello!", "delay": 20 }
 */
public class ActionConfigDeserializer implements JsonDeserializer<ActionConfig> {
    
    @Override
    public ActionConfig deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) 
            throws JsonParseException {
        
        // Handle simple string format
        if (json.isJsonPrimitive() && json.getAsJsonPrimitive().isString()) {
            return new ActionConfig(json.getAsString());
        }
        
        // Handle object format
        if (json.isJsonObject()) {
            JsonObject obj = json.getAsJsonObject();
            ActionConfig config = new ActionConfig();
            
            // Action string
            if (obj.has("action") && obj.get("action").isJsonPrimitive()) {
                config.setAction(obj.get("action").getAsString());
            }
            
            // Type and value (alternative format)
            if (obj.has("type") && obj.get("type").isJsonPrimitive()) {
                config.setType(obj.get("type").getAsString());
            }
            if (obj.has("value") && obj.get("value").isJsonPrimitive()) {
                config.setValue(obj.get("value").getAsString());
            }
            
            // Delay
            if (obj.has("delay") && obj.get("delay").isJsonPrimitive()) {
                config.setDelay(obj.get("delay").getAsInt());
            }
            
            // Chance
            if (obj.has("chance") && obj.get("chance").isJsonPrimitive()) {
                config.setChance(obj.get("chance").getAsDouble());
            }
            
            // Requirement condition
            if (obj.has("requirement") && obj.get("requirement").isJsonObject()) {
                config.setRequirement(context.deserialize(obj.get("requirement"), ConditionConfig.class));
            }
            
            // Deny actions
            if (obj.has("denyActions") && obj.get("denyActions").isJsonArray()) {
                java.util.List<ActionConfig> denyActions = new java.util.ArrayList<>();
                for (JsonElement elem : obj.getAsJsonArray("denyActions")) {
                    denyActions.add(context.deserialize(elem, ActionConfig.class));
                }
                config.setDenyActions(denyActions);
            }
            
            // Sequence of actions
            if (obj.has("actions") && obj.get("actions").isJsonArray()) {
                java.util.List<ActionConfig> actions = new java.util.ArrayList<>();
                for (JsonElement elem : obj.getAsJsonArray("actions")) {
                    actions.add(context.deserialize(elem, ActionConfig.class));
                }
                config.setActions(actions);
            }
            
            return config;
        }
        
        throw new JsonParseException("ActionConfig must be a string or object");
    }
}
