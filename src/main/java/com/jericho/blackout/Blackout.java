package com.jericho.blackout;

import org.bukkit.plugin.java.JavaPlugin;

import com.jericho.interfaces.EndGameCommand;
import com.jericho.interfaces.StartGameCommand;
import com.jericho.modules.GameSystemController;

public final class Blackout extends JavaPlugin {

    GameSystemController gameSystemController;// = new GameSystemController(this);

    @Override
    public void onEnable() {
        // Plugin startup logic
        gameSystemController = new GameSystemController(this);
        // Bukkit.getPluginManager().registerEvents(gameSystemController,this);
        
        getCommand("start").setExecutor(new StartGameCommand(gameSystemController));
        getCommand("end").setExecutor(new EndGameCommand(gameSystemController));

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
