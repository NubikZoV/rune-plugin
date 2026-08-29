package com.example.runes.listeners;

import com.example.runes.RunePlugin;
import com.example.runes.RuneType;
import com.example.runes.utils.RuneGUI;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

public class RuneGUIListener implements Listener {

    private final RunePlugin plugin;

    public RuneGUIListener(RunePlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player player)) return;

        String title = PlainTextComponentSerializer.plainText().serialize(e.getView().title());
        if (!title.equals(RuneGUI.GUI_TITLE)) return;

        e.setCancelled(true);

        if (e.getCurrentItem() == null) return;
        if (e.getCurrentItem().getItemMeta() == null) return;

        String itemName = PlainTextComponentSerializer.plainText().serialize(
                e.getCurrentItem().getItemMeta().displayName());

        // Check for "remove rune" button
        if (itemName.contains("Снять руну")) {
            plugin.getRuneManager().setPlayerRune(player, null);
            player.sendMessage("§7Руна снята.");
            player.closeInventory();
            return;
        }

        // Find matching rune
        for (RuneType rune : RuneType.values()) {
            String display = rune.getDisplayName().replaceAll("§.", "");
            if (itemName.contains(display)) {
                plugin.getRuneManager().setPlayerRune(player, rune);
                player.sendMessage("§7Вы выбрали: " + rune.getDisplayName());
                // Reopen to refresh GUI
                plugin.getServer().getScheduler().runTaskLater(plugin,
                        () -> RuneGUI.openRuneMenu(player), 1L);
                return;
            }
        }
    }
}
