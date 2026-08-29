package com.example.runes.listeners;

import com.example.runes.RunePlugin;
import com.example.runes.RuneType;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class GreenRuneListener implements Listener {

    private final RunePlugin plugin;

    // Хранилище состояния: UUID атакующего -> (UUID цели -> Время последнего удара в мс)
    private final Map<UUID, Map<UUID, Long>> shieldHitTracker = new HashMap<>();

    public GreenRuneListener(RunePlugin plugin) {
        this.plugin = plugin;
    }

    // ── 1. Снижение урона ОТ ВЗРЫВОВ на 24% ─────────────────────────────────
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onExplosionDamage(EntityDamageEvent e) {
        if (!(e.getEntity() instanceof Player player)) return;
        if (!plugin.getRuneManager().hasRune(player, RuneType.GREEN)) return;

        EntityDamageEvent.DamageCause cause = e.getCause();

        // Проверяем, является ли причина урона взрывом (динамит или крипер)
        if (cause == EntityDamageEvent.DamageCause.ENTITY_EXPLOSION ||
                cause == EntityDamageEvent.DamageCause.BLOCK_EXPLOSION) {

            double currentDamage = e.getDamage();
            // Уменьшаем урон на 24% (оставляем 76% от исходного значения)
            // Math.max(0, ...) гарантирует, что урон не станет отрицательным
            e.setDamage(Math.max(0, currentDamage * 0.76));
        }
    }

    // ── 2. Механика щита: 2 удара топором с интервалом > 2 секунд ──────────
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onAxeHit(EntityDamageByEntityEvent e) {
        if (!(e.getDamager() instanceof Player attacker)) return;
        if (!plugin.getRuneManager().hasRune(attacker, RuneType.GREEN)) return;

        ItemStack hand = attacker.getInventory().getItemInMainHand();
        if (!isAxe(hand.getType())) return;

        if (!(e.getEntity() instanceof Player target)) return;

        // Проверяем, блокирует ли цель удар щитом
        if (!target.isBlocking()) return;

        UUID attackerUuid = attacker.getUniqueId();
        UUID targetUuid = target.getUniqueId();
        long currentTime = System.currentTimeMillis();

        Map<UUID, Long> targetMap = shieldHitTracker.computeIfAbsent(attackerUuid, k -> new HashMap<>());
        Long lastHitTime = targetMap.get(targetUuid);

        if (lastHitTime == null) {
            // ПЕРВЫЙ УДАР
            targetMap.put(targetUuid, currentTime);
            attacker.sendMessage("§eПервый удар! Ударьте снова через 2+ секунды, чтобы пробить щит.");

            // Устанавливаем урон в 0. В Paper это предотвращает снятие прочности со щита,
            e.setDamage(0);

        } else {
            // ВТОРОЙ (или последующий) УДАР
            long timeDiff = currentTime - lastHitTime;

            if (timeDiff >= 2000) {
                // УСПЕХ: Прошло 2 или более секунд. Пробиваем щит.
                target.setCooldown(Material.SHIELD, 100); // Кулдаун на постановку щита 5 секунд (100 тиков)
                target.getWorld().playSound(target.getLocation(), Sound.ITEM_SHIELD_BREAK, 1.0f, 1.0f);
                attacker.sendMessage("§aЩит пробит!");

                // Сбрасываем запись для этой пары
                targetMap.remove(targetUuid);
            } else {
                // ПРОВАЛ: Игрок спамит (прошло меньше 2 секунд).
                // Перезаписываем время, заставляя ждать 2 секунды от этого момента спама.
                targetMap.put(targetUuid, currentTime);
                attacker.sendMessage("§cСлишком быстро! Подождите 2 секунды между ударами.");
            }
        }
    }

    // ── Вспомогательный метод проверки топора ───────────────────────────────
    private boolean isAxe(Material mat) {
        return mat == Material.WOODEN_AXE || mat == Material.STONE_AXE
                || mat == Material.IRON_AXE || mat == Material.GOLDEN_AXE
                || mat == Material.DIAMOND_AXE || mat == Material.NETHERITE_AXE;
    }

    // ── Очистка памяти при выходе игрока (предотвращает утечки) ─────────────
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e) {
        shieldHitTracker.remove(e.getPlayer().getUniqueId());
    }
}
