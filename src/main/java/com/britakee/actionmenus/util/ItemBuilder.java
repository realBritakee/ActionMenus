package com.britakee.actionmenus.util;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomModelData;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.item.enchantment.ItemEnchantments;

import java.util.*;

/**
 * Builder for creating ItemStacks with various properties.
 */
public class ItemBuilder {
    
    private Item item;
    private int amount = 1;
    private Component displayName;
    private List<Component> lore;
    private int customModelData = 0;
    private boolean enchantGlint = false;
    private String skullOwner;
    private String skullTexture;
    private Set<String> hideFlags = new HashSet<>();
    
    public ItemBuilder(String materialId) {
        this.item = parseItem(materialId);
    }
    
    public ItemBuilder(Item item) {
        this.item = item;
    }
    
    private Item parseItem(String materialId) {
        if (materialId == null || materialId.isEmpty()) {
            return Items.STONE;
        }
        
        // Add minecraft namespace if not present
        ResourceLocation location = ResourceLocation.tryParse(materialId);
        if (location == null) {
            location = ResourceLocation.withDefaultNamespace(materialId.toLowerCase());
        }
        
        Item parsedItem = BuiltInRegistries.ITEM.get(location);
        return parsedItem != null ? parsedItem : Items.STONE;
    }
    
    public ItemBuilder amount(int amount) {
        this.amount = Math.max(1, Math.min(64, amount));
        return this;
    }
    
    public ItemBuilder name(String name) {
        if (name != null && !name.isEmpty()) {
            this.displayName = TextUtil.colorize(name);
        }
        return this;
    }
    
    public ItemBuilder name(Component name) {
        this.displayName = name;
        return this;
    }
    
    public ItemBuilder lore(List<String> lore) {
        if (lore != null && !lore.isEmpty()) {
            this.lore = new ArrayList<>();
            for (String line : lore) {
                this.lore.add(TextUtil.colorize(line));
            }
        }
        return this;
    }
    
    public ItemBuilder loreComponents(List<Component> lore) {
        this.lore = lore;
        return this;
    }
    
    public ItemBuilder addLore(String line) {
        if (this.lore == null) {
            this.lore = new ArrayList<>();
        }
        this.lore.add(TextUtil.colorize(line));
        return this;
    }
    
    public ItemBuilder customModelData(int data) {
        this.customModelData = data;
        return this;
    }
    
    public ItemBuilder enchantGlint() {
        this.enchantGlint = true;
        return this;
    }
    
    public ItemBuilder enchantGlint(boolean glint) {
        this.enchantGlint = glint;
        return this;
    }
    
    public ItemBuilder skullOwner(String owner) {
        this.skullOwner = owner;
        return this;
    }
    
    public ItemBuilder skullTexture(String texture) {
        this.skullTexture = texture;
        return this;
    }
    
    public ItemBuilder hideFlags(List<String> flags) {
        if (flags != null) {
            this.hideFlags.addAll(flags);
        }
        return this;
    }
    
    public ItemBuilder hideFlag(String flag) {
        this.hideFlags.add(flag);
        return this;
    }
    
    public ItemStack build() {
        ItemStack stack = new ItemStack(item, amount);
        
        // Set display name
        if (displayName != null) {
            stack.set(DataComponents.CUSTOM_NAME, displayName);
        }
        
        // Set lore
        if (lore != null && !lore.isEmpty()) {
            stack.set(DataComponents.LORE, new ItemLore(lore));
        }
        
        // Set custom model data
        if (customModelData > 0) {
            stack.set(DataComponents.CUSTOM_MODEL_DATA, new CustomModelData(customModelData));
        }
        
        // Set enchant glint
        if (enchantGlint) {
            stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        }
        
        // Handle player head
        if (item == Items.PLAYER_HEAD) {
            applySkullData(stack);
        }
        
        // Handle hide flags - in 1.21.1 these are handled differently
        applyHideFlags(stack);
        
        return stack;
    }
    
    private void applySkullData(ItemStack stack) {
        if (skullTexture != null && !skullTexture.isEmpty()) {
            // Create profile with texture
            GameProfile profile = new GameProfile(UUID.randomUUID(), "");
            profile.getProperties().put("textures", new Property("textures", skullTexture));
            stack.set(DataComponents.PROFILE, new ResolvableProfile(profile));
        } else if (skullOwner != null && !skullOwner.isEmpty()) {
            // Create profile with just name (will be resolved by client)
            try {
                UUID uuid = UUID.fromString(skullOwner);
                GameProfile profile = new GameProfile(uuid, "");
                stack.set(DataComponents.PROFILE, new ResolvableProfile(profile));
            } catch (IllegalArgumentException e) {
                // Not a UUID, treat as name
                GameProfile profile = new GameProfile(null, skullOwner);
                stack.set(DataComponents.PROFILE, new ResolvableProfile(profile));
            }
        }
    }
    
    private void applyHideFlags(ItemStack stack) {
        // In 1.21.1, hiding attributes is done via components
        // This is simplified - full implementation would handle each flag type
        if (hideFlags.contains("HIDE_ATTRIBUTES") || hideFlags.contains("attributes")) {
            stack.set(DataComponents.ATTRIBUTE_MODIFIERS, 
                    stack.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS, 
                            net.minecraft.world.item.component.ItemAttributeModifiers.EMPTY)
                    .withTooltip(false));
        }
        
        if (hideFlags.contains("HIDE_ENCHANTS") || hideFlags.contains("enchants")) {
            ItemEnchantments enchants = stack.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
            // Create a mutable copy, set tooltip visibility, and apply
            ItemEnchantments.Mutable mutable = new ItemEnchantments.Mutable(enchants);
            stack.set(DataComponents.ENCHANTMENTS, mutable.toImmutable().withTooltip(false));
        }
    }
}
