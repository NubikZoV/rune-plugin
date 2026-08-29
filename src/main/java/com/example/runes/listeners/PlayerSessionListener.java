package com.example.runes.listeners;

import com.example.runes.RunePlugin;
import com.example.runes.RuneType;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.io.File;
import java.io.IOException;

public class PlayerSessionListener implements Listener {

    private final RunePlugin plugin;
    private final File dataFile;
    private final FileConfiguration dataConfig;

    public PlayerSessionListener(RunePlugin plugin) {
        this.plugin = plugin;
        this.dataFile = new File(plugin.getDataFolder(), "playerdata.yml");
        if (!plugin.getDataFolder().exists()) plugin.getDataFolder().mkdirs();
        this.dataConfig = YamlConfiguration.loadConfiguration(dataFile);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Player player = e.getPlayer();
        String runeStr = dataConfig.getString(player.getUniqueId().toString());
        if (runeStr != null) {
            RuneType rune = RuneType.fromString(runeStr);
            if (rune != null) {
                plugin.getRuneManager().setPlayerRune(player, rune);
                player.sendMessage("§7Ваша руна восстановлена: " + rune.getDisplayName());
            }
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        Player player = e.getPlayer();
        RuneType rune = plugin.getRuneManager().getPlayerRune(player);
        if (rune != null) {
            dataConfig.set(player.getUniqueId().toString(), rune.getId());
        } else {
            dataConfig.set(player.getUniqueId().toString(), null);
        }
        try {
            dataConfig.save(dataFile);
        } catch (IOException ex) {
            plugin.getLogger().warning("Failed to save player rune data: " + ex.getMessage());
        }
    }
}
