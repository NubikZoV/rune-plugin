package com.example.runes.utils;

import com.example.runes.RunePlugin;
import com.example.runes.RuneType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class RuneGUI {

    public static final String GUI_TITLE = "§5✦ Выбор руны ✦";

    public static void openRuneMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, Component.text(GUI_TITLE));

        RuneType current = RunePlugin.getInstance().getRuneManager().getPlayerRune(player);

        inv.setItem(10, buildRuneItem(Material.REDSTONE, RuneType.RED, current,
                List.of("§7+40% к урону от булавы",
                        "§74 ветра → 6 ветров",
                        "§7Порыв ветра на булаве")));

        inv.setItem(12, buildRuneItem(Material.PRISMARINE_SHARD, RuneType.BLUE, current,
                List.of("§7Сила 2 + Грация дельфина + Дыхание под водой",
                        "§7/rain — вызов дождя (кд 1 час)",
                        "§7+15% к урону трезубца, атака 1.5")));

        inv.setItem(14, buildRuneItem(Material.EMERALD, RuneType.GREEN, current,
                List.of("§7+4 дополнительных сердца",
                        "§7Взрывоустойчивость 2 на броне",
                        "§7Щит сносится двумя ударами топора (>2 сек)")));

        inv.setItem(16, buildRuneItem(Material.GUNPOWDER, RuneType.ORANGE, current,
                List.of("§7Упрощённый крафт ТНТ (2 порох + 2 песок)",
                        "§7Стрела + ТНТ = взрывная стрела",
                        "§7Арбалет стреляет огненными стрелами")));

        inv.setItem(13, buildRuneItem(Material.AMETHYST_SHARD, RuneType.PURPLE, current,
                List.of("§7+15% к скорости передвижения",
                        "§7+15% к скорости атаки",
                        "§7Меч: атака 1.7 | Топор: атака 1.1")));

        // Remove rune slot
        ItemStack remove = new ItemStack(Material.BARRIER);
        ItemMeta removeMeta = remove.getItemMeta();
        removeMeta.displayName(Component.text("§cСнять руну").decoration(TextDecoration.ITALIC, false));
        removeMeta.lore(List.of(Component.text("§7Убрать текущую руну").decoration(TextDecoration.ITALIC, false)));
        remove.setItemMeta(removeMeta);
        inv.setItem(22, remove);

        player.openInventory(inv);
    }

    private static ItemStack buildRuneItem(Material mat, RuneType rune, RuneType current, List<String> desc) {
        boolean isActive = rune == current;
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();

        String prefix = isActive ? "§a✔ " : "";
        meta.displayName(Component.text(prefix + rune.getDisplayName()).decoration(TextDecoration.ITALIC, false));

        List<Component> lore = new java.util.ArrayList<>();
        for (String line : desc) {
            lore.add(Component.text(line).decoration(TextDecoration.ITALIC, false));
        }
        if (isActive) {
            lore.add(Component.text("").decoration(TextDecoration.ITALIC, false));
            lore.add(Component.text("§a★ Активна").decoration(TextDecoration.ITALIC, false));
        } else {
            lore.add(Component.text("").decoration(TextDecoration.ITALIC, false));
            lore.add(Component.text("§eНажмите чтобы выбрать").decoration(TextDecoration.ITALIC, false));
        }
        meta.lore(lore);

        if (isActive) {
            meta.addEnchant(org.bukkit.enchantments.Enchantment.UNBREAKING, 1, true);
            meta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ENCHANTS);
        }

        item.setItemMeta(meta);
        return item;
    }
}
