package com.example.runes.managers;

import com.example.runes.RunePlugin;
import com.example.runes.RuneType;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

public class RuneManager {

    private final RunePlugin plugin;

    // Player -> active rune
    private final Map<UUID, RuneType> playerRunes = new HashMap<>();

    // Blue rune: rain cooldown (UUID -> timestamp when /rain was last used)
    private final Map<UUID, Long> rainCooldowns = new HashMap<>();

    // Green rune: axe hit tracking for double-hit shield break
    // UUID -> [firstHitTime, hitCount]
    private final Map<UUID, long[]> greenAxeHits = new HashMap<>();

    // Purple rune: speed/haste attribute modifier keys
    public static final String PURPLE_SPEED_KEY = "rune.purple.speed";
    public static final String PURPLE_HASTE_KEY = "rune.purple.haste";

    // Green rune: extra health modifier key
    public static final String GREEN_HEALTH_KEY = "rune.green.health";

    private BukkitTask tickTask;

    public RuneManager(RunePlugin plugin) {
        this.plugin = plugin;
    }

    public void startTasks() {
        // Main tick: every 10 ticks (0.5s) — applies blue rune water effects
        tickTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                RuneType rune = getPlayerRune(player);
                if (rune == RuneType.BLUE) {
                    applyBlueWaterEffects(player);
                }
            }
        }, 20L, 10L);
    }

    /**
     * Blue rune: apply Strength II, Dolphin's Grace, Water Breathing while in water.
     * These are potion effects (can be removed by milk), as per design spec.
     */
    private void applyBlueWaterEffects(Player player) {
        if (player.isInWater()) {
            org.bukkit.potion.PotionEffect strength = new org.bukkit.potion.PotionEffect(
                    org.bukkit.potion.PotionEffectType.STRENGTH, 40, 1, true, false, true);
            org.bukkit.potion.PotionEffect dolphin = new org.bukkit.potion.PotionEffect(
                    org.bukkit.potion.PotionEffectType.DOLPHINS_GRACE, 40, 0, true, false, true);
            org.bukkit.potion.PotionEffect breath = new org.bukkit.potion.PotionEffect(
                    org.bukkit.potion.PotionEffectType.WATER_BREATHING, 40, 0, true, false, true);
            player.addPotionEffect(strength);
            player.addPotionEffect(dolphin);
            player.addPotionEffect(breath);
        } else {
            // Remove water effects when leaving water
            org.bukkit.potion.PotionEffectType[] waterEffects = {
                    org.bukkit.potion.PotionEffectType.STRENGTH,
                    org.bukkit.potion.PotionEffectType.DOLPHINS_GRACE,
                    org.bukkit.potion.PotionEffectType.WATER_BREATHING
            };
            for (org.bukkit.potion.PotionEffectType type : waterEffects) {
                org.bukkit.potion.PotionEffect active = player.getPotionEffect(type);
                // Only remove if it's the ambient (rune-applied) variant
                if (active != null && active.isAmbient()) {
                    player.removePotionEffect(type);
                }
            }
        }
    }

    public RuneType getPlayerRune(Player player) {
        return playerRunes.get(player.getUniqueId());
    }

    public boolean hasRune(Player player, RuneType type) {
        return type == playerRunes.get(player.getUniqueId());
    }

    public void setPlayerRune(Player player, RuneType rune) {
        RuneType old = playerRunes.get(player.getUniqueId());
        if (old != null) removeRuneEffects(player, old);
        if (rune != null) {
            playerRunes.put(player.getUniqueId(), rune);
            applyRuneEffects(player, rune);
            // Apply purple weapon speed if switching to purple
            if (rune == RuneType.PURPLE) {
                Bukkit.getScheduler().runTaskLater(RunePlugin.getInstance(), () ->
                    RunePlugin.getInstance().getPurpleRuneListener().applyWeaponSpeedModifier(player)
                , 2L);
            }
        } else {
            playerRunes.remove(player.getUniqueId());
        }
    }

    // ──────────────────────────────────────────────────────────────
    // Apply / Remove rune attribute effects
    // ──────────────────────────────────────────────────────────────

    private void applyRuneEffects(Player player, RuneType rune) {
        switch (rune) {
            case GREEN -> applyGreenEffects(player);
            case PURPLE -> applyPurpleEffects(player);
            default -> {} // Other runes are event-driven
        }
    }

    private void removeRuneEffects(Player player, RuneType rune) {
        switch (rune) {
            case GREEN -> removeGreenEffects(player);
            case PURPLE -> removePurpleEffects(player);
            case BLUE -> {
                // Remove water potion effects
                player.removePotionEffect(org.bukkit.potion.PotionEffectType.STRENGTH);
                player.removePotionEffect(org.bukkit.potion.PotionEffectType.DOLPHINS_GRACE);
                player.removePotionEffect(org.bukkit.potion.PotionEffectType.WATER_BREATHING);
            }
            default -> {}
        }
    }

    // ──────────────────────────────────────────────────────────────
    // GREEN RUNE
    // ──────────────────────────────────────────────────────────────

    private void applyGreenEffects(Player player) {
        // +4 hearts = +8 max health
        var attr = player.getAttribute(Attribute.MAX_HEALTH);
        if (attr != null) {
            removeModifier(attr, GREEN_HEALTH_KEY);
            AttributeModifier mod = new AttributeModifier(
                    greenHealthKey(), 8.0, AttributeModifier.Operation.ADD_NUMBER);
            attr.addModifier(mod);
        }
    }

    private void removeGreenEffects(Player player) {
        var attr = player.getAttribute(Attribute.MAX_HEALTH);
        if (attr != null) removeModifier(attr, GREEN_HEALTH_KEY);
        greenAxeHits.remove(player.getUniqueId());
    }

    // ──────────────────────────────────────────────────────────────
    // PURPLE RUNE
    // ──────────────────────────────────────────────────────────────

    private void applyPurpleEffects(Player player) {
        // +15% movement speed
        var speedAttr = player.getAttribute(Attribute.MOVEMENT_SPEED);
        if (speedAttr != null) {
            removeModifier(speedAttr, PURPLE_SPEED_KEY);
            AttributeModifier speedMod = new AttributeModifier(
                    purpleSpeedKey(), 0.15, AttributeModifier.Operation.ADD_SCALAR);
            speedAttr.addModifier(speedMod);
        }

        // +15% attack speed (generic.attack_speed) — "haste" equivalent
        var attackAttr = player.getAttribute(Attribute.ATTACK_SPEED);
        if (attackAttr != null) {
            removeModifier(attackAttr, PURPLE_HASTE_KEY);
            AttributeModifier hasteMod = new AttributeModifier(
                    purpleHasteKey(), 0.15, AttributeModifier.Operation.ADD_SCALAR);
            attackAttr.addModifier(hasteMod);
        }
    }

    private void removePurpleEffects(Player player) {
        var speedAttr = player.getAttribute(Attribute.MOVEMENT_SPEED);
        if (speedAttr != null) removeModifier(speedAttr, PURPLE_SPEED_KEY);
        var attackAttr = player.getAttribute(Attribute.ATTACK_SPEED);
        if (attackAttr != null) {
            removeModifier(attackAttr, PURPLE_HASTE_KEY);
            // Also remove weapon-specific speed modifier
            attackAttr.getModifiers().stream()
                    .filter(m -> m.key().value().equals("purple_weapon_speed"))
                    .forEach(attackAttr::removeModifier);
        }
    }

    // ──────────────────────────────────────────────────────────────
    // Green rune: axe hit tracking
    // ──────────────────────────────────────────────────────────────

    /**
     * Called when a GREEN rune player hits an entity with an axe.
     * Returns true if this hit should disable the target's shield.
     * Rules:
     *  - First hit: record time, hitCount = 1, no disable.
     *  - Second hit: must be > 2 seconds after first. If so, disable shield.
     *  - If second hit is within 2 seconds, reset (treat as new first hit).
     */
    public boolean processGreenAxeHit(Player attacker) {
        UUID id = attacker.getUniqueId();
        long now = System.currentTimeMillis();
        long[] data = greenAxeHits.get(id);

        if (data == null || data[1] == 0) {
            // First hit
            greenAxeHits.put(id, new long[]{now, 1});
            return false;
        }

        long timeSinceFirst = now - data[0];
        if (timeSinceFirst > 2000) {
            // Valid second hit
            greenAxeHits.remove(id);
            return true;
        } else {
            // Too fast — reset, this becomes the new first hit
            greenAxeHits.put(id, new long[]{now, 1});
            return false;
        }
    }

    // ──────────────────────────────────────────────────────────────
    // Rain cooldown (Blue rune)
    // ──────────────────────────────────────────────────────────────

    public boolean isRainOnCooldown(Player player) {
        Long last = rainCooldowns.get(player.getUniqueId());
        if (last == null) return false;
        return System.currentTimeMillis() - last < 60 * 60 * 1000L; // 1 hour
    }

    public long getRainCooldownRemaining(Player player) {
        Long last = rainCooldowns.get(player.getUniqueId());
        if (last == null) return 0;
        return (60 * 60 * 1000L) - (System.currentTimeMillis() - last);
    }

    public void setRainUsed(Player player) {
        rainCooldowns.put(player.getUniqueId(), System.currentTimeMillis());
    }

    // ──────────────────────────────────────────────────────────────
    // Utility
    // ──────────────────────────────────────────────────────────────

    private void removeModifier(org.bukkit.attribute.AttributeInstance attr, String key) {
        attr.getModifiers().stream()
                .filter(m -> m.key().toString().equals(key) ||
                             m.key().value().equals(key))
                .forEach(attr::removeModifier);
    }

    private org.bukkit.NamespacedKey greenHealthKey() {
        return new org.bukkit.NamespacedKey(RunePlugin.getInstance(), "green_health");
    }

    private org.bukkit.NamespacedKey purpleSpeedKey() {
        return new org.bukkit.NamespacedKey(RunePlugin.getInstance(), "purple_speed");
    }

    private org.bukkit.NamespacedKey purpleHasteKey() {
        return new org.bukkit.NamespacedKey(RunePlugin.getInstance(), "purple_haste");
    }
}
