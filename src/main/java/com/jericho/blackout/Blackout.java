package com.jericho.blackout;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import com.jericho.interfaces.EndGameCommand;
import com.jericho.interfaces.StartGameCommand;
import com.jericho.modules.GameSystem;

public final class Blackout extends JavaPlugin {

    GameSystem gameSystem = new GameSystem(this,Bukkit.getWorld("blackout_map"));

    @Override
    public void onEnable() {
        // Plugin startup logic

        Bukkit.getPluginManager().registerEvents(gameSystem,this);
        
        getCommand("start").setExecutor(new StartGameCommand(gameSystem));
        getCommand("end").setExecutor(new EndGameCommand(gameSystem));

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
