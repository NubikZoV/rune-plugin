package com.example.runes.listeners;

import com.example.runes.RunePlugin;
import com.example.runes.RuneType;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.UUID;

/**
 * PURPLE RUNE:
 * 1. +15% movement speed (via attribute, handled in RuneManager)
 * 2. +15% attack speed (via attribute, handled in RuneManager)
 * 3. Sword attack speed → 1.7, Any axe attack speed → 1.1
 *    (done via item attribute on held item — applied per slot switch)
 */
public class PurpleRuneListener implements Listener {

    private final RunePlugin plugin;
    private static final String SWORD_SPEED_KEY = "rune.purple.sword_speed";
    private static final String AXE_SPEED_KEY = "rune.purple.axe_speed";

    public PurpleRuneListener(RunePlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * When player switches held item, update the ATTACK_SPEED attribute
     * directly on the Player to simulate weapon-specific speeds.
     *
     * Sword base attack speed in vanilla = 1.6 (per tick recharge).
     * Axe base varies: 0.8–1.0 depending on tier.
     * We override ATTACK_SPEED attribute: swords → 1.7, axes → 1.1
     *
     * We remove the previous weapon modifier and add the new one.
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
        if (!plugin.getRuneManager().hasRune(player, RuneType.PURPLE)) return;

        ItemStack item = player.getInventory().getItemInMainHand();
        Material mat = item.getType();

        var attr = player.getAttribute(Attribute.ATTACK_SPEED);
        if (attr == null) return;

        // Remove previous purple weapon modifier
        attr.getModifiers().stream()
                .filter(m -> m.key().value().equals("purple_weapon_speed"))
                .forEach(attr::removeModifier);

        double targetSpeed = -1;

        if (isSword(mat)) {
            targetSpeed = 1.7;
        } else if (isAxe(mat)) {
            targetSpeed = 1.1;
        }

        if (targetSpeed < 0) return;

        // Calculate modifier needed:
        // base attack speed for player = 4.0
        // targetSpeed = base + (base * scalar) or base + flat
        // AttributeModifier.Operation.ADD_NUMBER: finalValue = base + mod
        // We want: finalValue = targetSpeed → mod = targetSpeed - base (4.0)
        double base = attr.getBaseValue(); // should be 4.0
        double flatMod = targetSpeed - base;

        NamespacedKey key = new NamespacedKey(plugin, "purple_weapon_speed");
        AttributeModifier mod = new AttributeModifier(key, flatMod,
                AttributeModifier.Operation.ADD_NUMBER);
        attr.addModifier(mod);
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
