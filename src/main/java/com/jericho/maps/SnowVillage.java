package com.jericho.maps;

import org.bukkit.Bukkit;
import org.bukkit.Location;

import com.jericho.blackout.Blackout;
import com.jericho.classes.GameSystem;

public class SnowVillage extends GameSystem {
    public SnowVillage(Blackout plugin) {
        super(plugin);
        world = Bukkit.getWorld("blackout_beta");

        lobbyLocation = new Location(world, 20,-48,4);
        hunterSpawnLocations = new Location[] {
            new Location(world,64,-41,62)
        };
        survivorSpawnLocations = new Location[] {
            new Location(world,77,-44,87),
            new Location(world,38,-48,99),
            new Location(world,38,-34,132),
            new Location(world,77,-48,113),
            new Location(world,52,-42,68),
            new Location(world,10,-37,112),
            new Location(world,55,-48,109),
            new Location(world,49,-48,64),
            new Location(world,26,-38,94)
        };
    }
}
