package com.britakee.actionmenus.action.types;

import com.britakee.actionmenus.ActionMenus;
import com.britakee.actionmenus.action.Action;
import com.britakee.actionmenus.action.ActionContext;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;

/**
 * Action that plays a sound.
 * Format: sound [volume] [pitch]
 */
public class SoundAction extends Action {
    
    private final String soundId;
    private final float volume;
    private final float pitch;
    
    public SoundAction(String soundId, float volume, float pitch) {
        this.soundId = soundId;
        this.volume = volume;
        this.pitch = pitch;
    }
    
    public static SoundAction parse(String value) {
        String[] parts = value.trim().split("\\s+");
        
        String soundId = parts.length > 0 ? parts[0] : "minecraft:ui.button.click";
        float volume = parts.length > 1 ? parseFloatSafe(parts[1], 1.0f) : 1.0f;
        float pitch = parts.length > 2 ? parseFloatSafe(parts[2], 1.0f) : 1.0f;
        
        return new SoundAction(soundId, volume, pitch);
    }
    
    private static float parseFloatSafe(String s, float defaultValue) {
        try {
            return Float.parseFloat(s);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
    
    @Override
    public void execute(ActionContext context) {
        ServerPlayer player = context.getPlayer();
        
        ResourceLocation soundLocation = ResourceLocation.tryParse(soundId);
        if (soundLocation == null) {
            soundLocation = ResourceLocation.withDefaultNamespace(soundId);
        }
        
        SoundEvent soundEvent = BuiltInRegistries.SOUND_EVENT.get(soundLocation);
        if (soundEvent != null) {
            player.playNotifySound(soundEvent, SoundSource.MASTER, volume, pitch);
        } else {
            // Try playing as custom sound
            player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvent.createVariableRangeEvent(soundLocation), 
                    SoundSource.MASTER, volume, pitch);
        }
    }
    
    @Override
    public String getType() {
        return "sound";
    }
    
    @Override
    public String toString() {
        return "sound{" + soundId + ", " + volume + ", " + pitch + "}";
    }
}
