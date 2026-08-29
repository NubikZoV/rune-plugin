package com.example.runes.listeners;

import com.example.runes.RunePlugin;
import com.example.runes.RuneType;
import com.example.runes.managers.RuneManager;
import io.papermc.paper.event.entity.EntityAttemptSmashAttackEvent;
import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import static org.bukkit.Bukkit.getLogger;
import static org.bukkit.Bukkit.getServer;

/**
 * RED RUNE:
 * 1. +40% damage from mace
 * 2. Wind Charge gives 6 wind charges instead of 4
 * 3. Mace has wind burst (on hit, launches target and creates burst)
 */
public class RedRuneListener implements Listener {

    private final RunePlugin plugin;

    public RedRuneListener(RunePlugin plugin) {
        this.plugin = plugin;
    }

    // ── 1. +40% mace damage ──────────────────────────────────────
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onMaceDamage(EntityDamageByEntityEvent e) {
        if (!(e.getDamager() instanceof Player player)) return;
        if (!plugin.getRuneManager().hasRune(player, RuneType.RED)) return;

        ItemStack hand = player.getInventory().getItemInMainHand();
        if (hand.getType() != Material.MACE) return;

        e.setDamage(e.getDamage() * 1.4);
    }

    // ── 3. Wind Burst on mace hit ────────────────────────────────
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onMaceHitWindBurst(EntityDamageByEntityEvent e) {
        if (!(e.getDamager() instanceof Player player)) return;
        if (!plugin.getRuneManager().hasRune(player, RuneType.RED)) return;

        ItemStack hand = player.getInventory().getItemInMainHand();
        if (hand.getType() != Material.MACE) return;

        if (!(e.getEntity() instanceof LivingEntity target)) return;

        Location loc = target.getLocation();
        World world = loc.getWorld();
        if (world == null) return;

        // Launch target upward + away from player
        Vector dir = target.getLocation().toVector()
                .subtract(player.getLocation().toVector())
                .normalize()
                .multiply(1.2)
                .setY(0.8);
        target.setVelocity(dir);

        // Visual: spawn wind burst particles
        //world.spawnParticle(Particle.GUST, loc, 1, 0, 0, 0, 0);
        //world.playSound(loc, Sound.ENTITY_WIND_CHARGE_WIND_BURST, 1.0f, 1.0f);

    }

    // ── 2. Wind Charge gives 6 instead of 4 ─────────────────────
    // This intercepts crafting — handled via PrepareItemCraftEvent
    @EventHandler
    public void onWindChargeCraft(org.bukkit.event.inventory.PrepareItemCraftEvent e) {
        if (!(e.getView().getPlayer() instanceof Player player)) return;
        if (!plugin.getRuneManager().hasRune(player, RuneType.RED)) return;

        org.bukkit.inventory.CraftingInventory inv = e.getInventory();
        if (inv.getResult() == null) return;
        if (inv.getResult().getType() != Material.WIND_CHARGE) return;

        // Give 6 wind charges instead of vanilla amount (4)
        ItemStack result = inv.getResult().clone();
        result.setAmount(6);
        inv.setResult(result);
    }
    @EventHandler
    public void maceAttack(EntityAttemptSmashAttackEvent event){
        if (!(event.getEntity() instanceof Player)) {
            return;
        }
        Player govnoed = (Player) event.getEntity();
        if (!plugin.getRuneManager().hasRune(govnoed, RuneType.RED)) return;
        System.out.println(govnoed.name());
        ItemStack item = govnoed.getInventory().getItemInMainHand();
        Enchantment wind_burst = Registry.ENCHANTMENT.get(NamespacedKey.minecraft("wind_burst"));

    }

}