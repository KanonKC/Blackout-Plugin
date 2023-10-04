package com.jericho.interfaces;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.jericho.modules.GameSystemController;

public class StartGameCommand implements CommandExecutor {
    
    GameSystemController controller;
    
    public StartGameCommand(GameSystemController controller) {
        this.controller = controller;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        
        if (sender instanceof Player) {
            Player player = (Player) sender;
            controller.getGameSystem(player.getWorld()).start();
        }
        
        return false;
    }
}
