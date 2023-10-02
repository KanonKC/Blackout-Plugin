package com.jericho.interfaces;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.jericho.modules.GameSystem;

public class EndGameCommand implements CommandExecutor {
    GameSystem gameSystem;
    
    public EndGameCommand(GameSystem gameSystem) {
        this.gameSystem = gameSystem;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        
        if (sender instanceof Player) {
            gameSystem.end(0);
        }
        
        return false;
    }
}
