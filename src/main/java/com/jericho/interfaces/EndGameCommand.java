package com.jericho.interfaces;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.jericho.blackout.Blackout;
import com.jericho.modules.GameSystem;

public class EndGameCommand implements CommandExecutor {
    Blackout plugin;
    
    public EndGameCommand(Blackout plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        
        if (sender instanceof Player) {
            Player player = (Player) sender;
            GameSystem gameSystem  = new GameSystem(plugin, player.getWorld());
            gameSystem.end(0);
        }
        
        return false;
    }
}
