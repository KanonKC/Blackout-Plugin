package com.jericho.blackout;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import com.jericho.interfaces.EndGameCommand;
import com.jericho.interfaces.StartGameCommand;
import com.jericho.modules.GameSystem;

public final class Blackout extends JavaPlugin {

    @Override
    public void onEnable() {
        // Plugin startup logic

        Bukkit.getPluginManager().registerEvents(new GameSystem(this,Bukkit.getWorld("blackout_map")),this);
        
        getCommand("start").setExecutor(new StartGameCommand(this));
        getCommand("end").setExecutor(new EndGameCommand(this));

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
