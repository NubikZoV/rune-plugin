package com.example.runes.commands;

import com.example.runes.RunePlugin;
import com.example.runes.RuneType;
import com.example.runes.managers.RuneManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class RainCommand implements CommandExecutor {

    private final RunePlugin plugin;

    public RainCommand(RunePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cТолько игрок может использовать эту команду.");
            return true;
        }

        RuneManager rm = plugin.getRuneManager();

        if (!rm.hasRune(player, RuneType.BLUE)) {
            player.sendMessage("§cЭта команда доступна только с Голубой руной.");
            return true;
        }

        if (rm.isRainOnCooldown(player)) {
            long remaining = rm.getRainCooldownRemaining(player);
            long minutes = remaining / 60000;
            long seconds = (remaining % 60000) / 1000;
            player.sendMessage(String.format("§bДождь на кулдауне! Осталось: %d мин %d сек", minutes, seconds));
            return true;
        }

        // Start rain for 10 minutes (12000 ticks)
        player.getWorld().setStorm(true);
        player.getWorld().setWeatherDuration(12000);
        player.getWorld().setThundering(false);
        rm.setRainUsed(player);

        player.sendMessage("§b☁ Вы вызвали дождь на 10 минут!");

        // Schedule rain stop
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            if (player.getWorld().hasStorm()) {
                player.getWorld().setStorm(false);
                player.sendMessage("§bДождь закончился.");
            }
        }, 12000L);

        return true;
    }
}
