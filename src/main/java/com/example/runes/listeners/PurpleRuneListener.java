package com.example.runes.listeners;

import com.example.runes.RunePlugin;
import com.example.runes.RuneType;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.ItemStack;

/**
 * PURPLE RUNE:
 * 1. +25% movement speed (via attribute modifier)
 * 2. +25% attack speed/haste (via attribute modifier)
 * 3. Sword attack speed → 1.7, Any axe attack speed → 1.1
 */
public class PurpleRuneListener implements Listener {

    private final RunePlugin plugin;
    private static final String WEAPON_SPEED_KEY = "purple_weapon_speed";

    public PurpleRuneListener(RunePlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * When player switches held item, update the ATTACK_SPEED attribute
     * to set weapon-specific attack speeds.
     * 
     * Sword base attack speed = 4.0 → set to 1.7
     * Axe base attack speed = 4.0 → set to 1.1
     */
    @EventHandler
    public void onItemSwitch(PlayerItemHeldEvent e) {
        Player player = e.getPlayer();
        if (!plugin.getRuneManager().hasRune(player, RuneType.PURPLE)) return;

        // Run 1 tick later so the item switch has completed
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            applyWeaponSpeedModifier(player);
        }, 1L);
    }

    public void applyWeaponSpeedModifier(Player player) {
        if (!plugin.getRuneManager().hasRune(player, RuneType.PURPLE)) {
            // Remove weapon speed modifier if rune is no longer active
            removeWeaponSpeedModifier(player);
            return;
        }

        ItemStack item = player.getInventory().getItemInMainHand();
        Material mat = item.getType();

        var attr = player.getAttribute(Attribute.ATTACK_SPEED);
        if (attr == null) return;

        // Remove previous purple weapon modifier
        removeWeaponSpeedModifier(player);

        double targetSpeed = -1;

        if (isSword(mat)) {
            targetSpeed = 1.7;
        } else if (isAxe(mat)) {
            targetSpeed = 1.1;
        }

        if (targetSpeed < 0) return;

        // Base attack speed for player is 4.0
        // We want final value = targetSpeed
        // Using ADD_NUMBER: finalValue = base + mod → mod = targetSpeed - base
        double base = attr.getBaseValue(); // should be 4.0
        double flatMod = targetSpeed - base;

        NamespacedKey key = new NamespacedKey(plugin, WEAPON_SPEED_KEY);
        AttributeModifier mod = new AttributeModifier(key, flatMod,
                AttributeModifier.Operation.ADD_NUMBER);
        attr.addModifier(mod);
    }

    public void removeWeaponSpeedModifier(Player player) {
        var attr = player.getAttribute(Attribute.ATTACK_SPEED);
        if (attr == null) return;

        attr.getModifiers().stream()
                .filter(m -> m.key().value().equals(WEAPON_SPEED_KEY))
                .forEach(attr::removeModifier);
    }

    private boolean isSword(Material mat) {
        return mat == Material.WOODEN_SWORD || mat == Material.STONE_SWORD
                || mat == Material.IRON_SWORD || mat == Material.GOLDEN_SWORD
                || mat == Material.DIAMOND_SWORD || mat == Material.NETHERITE_SWORD;
    }

    private boolean isAxe(Material mat) {
        return mat == Material.WOODEN_AXE || mat == Material.STONE_AXE
                || mat == Material.IRON_AXE || mat == Material.GOLDEN_AXE
                || mat == Material.DIAMOND_AXE || mat == Material.NETHERITE_AXE;
    }
}
