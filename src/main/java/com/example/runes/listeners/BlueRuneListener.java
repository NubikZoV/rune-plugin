package com.example.runes.listeners;

import com.example.runes.RunePlugin;
import com.example.runes.RuneType;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.entity.Trident;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

/**
 * BLUE RUNE:
 * 1. Strength 2 + Dolphin Grace + Water Breathing while in water (handled in RuneManager tick)
 * 2. /rain command (handled in RainCommand)
 * 3. Trident attack speed 1.5 + 15% bonus damage
 */
public class BlueRuneListener implements Listener {

    private final RunePlugin plugin;

    public BlueRuneListener(RunePlugin plugin) {
        this.plugin = plugin;
    }

    // ── 3. +15% trident damage ───────────────────────────────────
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onTridentDamage(EntityDamageByEntityEvent e) {
        if (!(e.getDamager() instanceof Player player)) {
            // Could be thrown trident — check shooter
            if (e.getDamager() instanceof Trident trident) {
                if (trident.getShooter() instanceof Player shooter) {
                    if (plugin.getRuneManager().hasRune(shooter, RuneType.BLUE)) {
                        e.setDamage(e.getDamage() * 1.15);
                    }
                }
            }
            return;
        }

        if (!plugin.getRuneManager().hasRune(player, RuneType.BLUE)) return;

        ItemStack hand = player.getInventory().getItemInMainHand();
        if (hand.getType() == Material.TRIDENT) {
            e.setDamage(e.getDamage() * 1.15);
        }
    }

    // ── 3. Trident attack speed boost (simulate via velocity on throw) ──
    // We boost trident projectile speed to simulate 1.5x attack speed feel
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onTridentLaunch(ProjectileLaunchEvent e) {
        if (!(e.getEntity() instanceof Trident trident)) return;
        if (!(trident.getShooter() instanceof Player player)) return;
        if (!plugin.getRuneManager().hasRune(player, RuneType.BLUE)) return;

        // Boost projectile velocity to 1.5x
        Vector vel = trident.getVelocity();
        trident.setVelocity(vel.multiply(1.5));
    }
}
