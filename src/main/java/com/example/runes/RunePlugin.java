package com.example.runes;

import com.example.runes.commands.RainCommand;
import com.example.runes.commands.RuneCommand;
import com.example.runes.listeners.*;
import com.example.runes.managers.RuneManager;
import org.bukkit.plugin.java.JavaPlugin;

public class RunePlugin extends JavaPlugin {

    private static RunePlugin instance;
    private RuneManager runeManager;
    private com.example.runes.listeners.PurpleRuneListener purpleRuneListener;

    @Override
    public void onEnable() {
        instance = this;
        runeManager = new RuneManager(this);

        // Register commands
        getCommand("rune").setExecutor(new RuneCommand(this));
        getCommand("rain").setExecutor(new RainCommand(this));

        // Register listeners
        getServer().getPluginManager().registerEvents(new RedRuneListener(this), this);
        getServer().getPluginManager().registerEvents(new BlueRuneListener(this), this);
        getServer().getPluginManager().registerEvents(new GreenRuneListener(this), this);
        getServer().getPluginManager().registerEvents(new OrangeRuneListener(this), this);
        purpleRuneListener = new PurpleRuneListener(this);
        getServer().getPluginManager().registerEvents(purpleRuneListener, this);
        getServer().getPluginManager().registerEvents(new RuneGUIListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerSessionListener(this), this);

        // Start tick tasks
        runeManager.startTasks();

        getLogger().info("RunePlugin enabled!");
    }

    @Override
    public void onDisable() {
        getLogger().info("RunePlugin disabled!");
    }

    public static RunePlugin getInstance() { return instance; }
    public RuneManager getRuneManager() { return runeManager; }
    public com.example.runes.listeners.PurpleRuneListener getPurpleRuneListener() { return purpleRuneListener; }
}
