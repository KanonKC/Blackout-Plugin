package com.jericho.maps;

import org.bukkit.Bukkit;
import org.bukkit.Location;

import com.jericho.blackout.Blackout;
import com.jericho.classes.GameSystem;

public class DevGameSystem extends GameSystem {
    public DevGameSystem(Blackout plugin) {
        super(plugin);

        world = Bukkit.getWorld("blackout_map");

        lobbyLocation = new Location(world, -42,11,-89);
        hunterSpawnLocations = new Location[] {
            new Location(world,-61 ,42, -87)
        };
        survivorSpawnLocations = new Location[] {
            new Location(world,-80, 58,-98),
            new Location(world,-70, 67,-93),
            new Location(world,-72, 50,-76),
            new Location(world,-58, 50,-84),
            new Location(world,-87, 59,-75),
            new Location(world,-74, 47,-82),
            new Location(world,-81, 41,-84),
            new Location(world,-64, 62,-98),
            new Location(world,-84, 62,-78),
            new Location(world,-83, 46,-98)
        };

    }
}
