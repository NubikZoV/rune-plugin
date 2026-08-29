package com.example.runes.commands;

import com.example.runes.RunePlugin;
import com.example.runes.RuneType;
import com.example.runes.utils.RuneGUI;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class RuneCommand implements CommandExecutor {

    private final RunePlugin plugin;

    public RuneCommand(RunePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cТолько игрок может использовать эту команду.");
            return true;
        }

        if (args.length == 0) {
            // Open GUI
            RuneGUI.openRuneMenu(player);
            return true;
        }

        // Direct selection via args
        RuneType rune = RuneType.fromString(args[0]);
        if (rune == null) {
            player.sendMessage("§cНеизвестная руна. Доступны: red, blue, green, orange, purple");
            return true;
        }

        plugin.getRuneManager().setPlayerRune(player, rune);
        player.sendMessage("§7Вы выбрали: " + rune.getDisplayName());
        return true;
    }
}
