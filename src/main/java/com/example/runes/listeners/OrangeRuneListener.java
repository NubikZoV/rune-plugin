package com.example.runes.listeners;

import com.example.runes.RunePlugin;
import com.example.runes.RuneType;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;

/**
 * ORANGE RUNE:
 * 1. Simplified TNT craft: 2 gunpowder + 2 sand
 * 2. Arrow + TNT craft = explosive arrow (explodes on hit entity/block)
 * 3. Crossbow shoots fire arrows
 */
public class OrangeRuneListener implements Listener {

    private final RunePlugin plugin;
    private final NamespacedKey EXPLOSIVE_KEY;

    public OrangeRuneListener(RunePlugin plugin) {
        this.plugin = plugin;
        this.EXPLOSIVE_KEY = new NamespacedKey(plugin, "explosive_arrow");
    }

    // ── 1. Simplified TNT craft ─────────────────────────────────
    @EventHandler
    public void onPrepareTNTCraft(PrepareItemCraftEvent e) {
        if (!(e.getView().getPlayer() instanceof Player player)) return;
        if (!plugin.getRuneManager().hasRune(player, RuneType.ORANGE)) return;

        ItemStack[] matrix = e.getInventory().getMatrix();
        int gunpowder = 0, sand = 0, other = 0;
        for (ItemStack item : matrix) {
            if (item == null || item.getType() == Material.AIR) continue;
            if (item.getType() == Material.GUNPOWDER) gunpowder++;
            else if (item.getType() == Material.SAND || item.getType() == Material.RED_SAND) sand++;
            else other++;
        }
        if (gunpowder == 2 && sand == 2 && other == 0) {
            e.getInventory().setResult(new ItemStack(Material.TNT, 1));
        }
    }

    @EventHandler
    public void onCraftTNT(CraftItemEvent e) {
        if (!(e.getWhoClicked() instanceof Player player)) return;
        if (!plugin.getRuneManager().hasRune(player, RuneType.ORANGE)) return;

        ItemStack result = e.getCurrentItem();
        if (result == null || result.getType() != Material.TNT) return;

        ItemStack[] matrix = e.getInventory().getMatrix();
        int gunpowder = 0, sand = 0, other = 0;
        for (ItemStack item : matrix) {
            if (item == null || item.getType() == Material.AIR) continue;
            if (item.getType() == Material.GUNPOWDER) gunpowder++;
            else if (item.getType() == Material.SAND || item.getType() == Material.RED_SAND) sand++;
            else other++;
        }
        if (!(gunpowder == 2 && sand == 2 && other == 0)) {
            e.setCancelled(true);
        }
    }

    // ── 2. Explosive arrow craft ─────────────────────────────────
    @EventHandler
    public void onPrepareExplosiveArrowCraft(PrepareItemCraftEvent e) {
        if (!(e.getView().getPlayer() instanceof Player player)) return;
        if (!plugin.getRuneManager().hasRune(player, RuneType.ORANGE)) return;

        ItemStack[] matrix = e.getInventory().getMatrix();
        int arrows = 0, tnt = 0, other = 0;
        for (ItemStack item : matrix) {
            if (item == null || item.getType() == Material.AIR) continue;
            if (item.getType() == Material.ARROW) arrows++;
            else if (item.getType() == Material.TNT) tnt++;
            else other++;
        }

        if (arrows == 1 && tnt == 1 && other == 0) {
            ItemStack result = new ItemStack(Material.ARROW, 1);
            ItemMeta meta = result.getItemMeta();
            meta.displayName(net.kyori.adventure.text.Component.text("§6Взрывная стрела")
                    .decoration(net.kyori.adventure.text.format.TextDecoration.ITALIC, false));
            meta.lore(List.of(net.kyori.adventure.text.Component.text("§7Взрывается при попадании")
                    .decoration(net.kyori.adventure.text.format.TextDecoration.ITALIC, false)));
            meta.getPersistentDataContainer().set(EXPLOSIVE_KEY, PersistentDataType.BYTE, (byte) 1);
            result.setItemMeta(meta);
            e.getInventory().setResult(result);
        }
    }

    // Исправление бага с миллиардами ТНТ
    @EventHandler
    public void onCraftExplosiveArrow(CraftItemEvent e) {
        if (!(e.getWhoClicked() instanceof Player player)) return;
        if (!plugin.getRuneManager().hasRune(player, RuneType.ORANGE)) return;

        ItemStack result = e.getCurrentItem();
        if (result == null) return;

        ItemMeta meta = result.getItemMeta();
        if (meta == null) return;
        if (!meta.getPersistentDataContainer().has(EXPLOSIVE_KEY, PersistentDataType.BYTE)) return;

        // Находим TNT в сетке и уменьшаем его количество
        ItemStack[] matrix = e.getInventory().getMatrix();
        for (int i = 0; i < matrix.length; i++) {
            if (matrix[i] != null && matrix[i].getType() == Material.TNT) {
                matrix[i].setAmount(matrix[i].getAmount() - 1);
                if (matrix[i].getAmount() <= 0) {
                    matrix[i] = null;
                }
                e.getInventory().setMatrix(matrix);
                break;
            }
        }
    }

    // ── 2. Explosive arrow hit ───────────────────────────────────
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onArrowHit(ProjectileHitEvent e) {
        if (!(e.getEntity() instanceof Arrow arrow)) return;
        if (!(arrow.getShooter() instanceof Player player)) return;
        if (!plugin.getRuneManager().hasRune(player, RuneType.ORANGE)) return;

        ItemStack arrowItem = arrow.getItemStack();
        if (arrowItem == null) return;
        ItemMeta meta = arrowItem.getItemMeta();
        if (meta == null) return;
        if (!meta.getPersistentDataContainer().has(EXPLOSIVE_KEY, PersistentDataType.BYTE)) return;

        Location loc = arrow.getLocation();
        arrow.getWorld().createExplosion(loc, 2.5f, true, true, player);
        arrow.remove();
    }

    // ── 3. Crossbow fires flaming arrows ─────────────────────────
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onCrossbowShoot(ProjectileLaunchEvent e) {
        if (!(e.getEntity() instanceof Arrow arrow)) return;
        if (!(arrow.getShooter() instanceof Player player)) return;
        if (!plugin.getRuneManager().hasRune(player, RuneType.ORANGE)) return;

        ItemStack hand = player.getInventory().getItemInMainHand();
        ItemStack offHand = player.getInventory().getItemInOffHand();

        boolean hasCrossbow = hand.getType() == Material.CROSSBOW
                || offHand.getType() == Material.CROSSBOW;
        if (!hasCrossbow) return;

        arrow.setFireTicks(200);
    }
}