package com.jericho.modules;

import org.bukkit.World;

import com.jericho.blackout.Blackout;
import com.jericho.classes.GameSystem;
import com.jericho.maps.DevServer;
import com.jericho.maps.SnowVillage;

public class GameSystemController {
    
    Blackout plugin;
    
    GameSystem gameSystems[];
    

    public GameSystemController(Blackout plugin) {
        this.plugin = plugin;
        gameSystems = new GameSystem[] {
            new DevServer(plugin),
            new SnowVillage(plugin)
        };
    }

    public GameSystem getGameSystem(World world) {
        for (GameSystem gameSystem : gameSystems) {
            if (gameSystem.getWorld().equals(world)) {
                return gameSystem;
            }
        }
        return null;
    }
}
