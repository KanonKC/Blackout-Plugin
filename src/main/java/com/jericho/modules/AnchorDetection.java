package com.jericho.modules;

import java.util.HashMap;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import net.md_5.bungee.api.ChatColor;

public class AnchorDetection {

    String HUNTER_TAG = "hunter";
    String SURVIVOR_TAG = "survivor";

    Integer count = 0;
    Integer threshold = 10;
    HashMap<Player,Location[]> playerPreviousLocations = new HashMap<Player,Location[]>();
    HashMap<Player,Boolean[]> hunterNearbyPlayers = new HashMap<Player,Boolean[]>();

    List<Player> survivors;
    Player hunter;

    public AnchorDetection(List<Player> survivors, Player hunter) {
        this.survivors = survivors;
        this.hunter = hunter;
    }

    public void impluse() {
        if (count >= threshold) {
            survivors.forEach(player -> {
                Double averageMovingDistance = movingAverageDistance(playerPreviousLocations.get(player));
                if (averageMovingDistance < 1 && noHunterNearby(hunterNearbyPlayers.get(player)) && player.getScoreboardTags().contains(SURVIVOR_TAG)) {
                    
                    PotionEffect glowEffect = player.getPotionEffect(PotionEffectType.GLOWING);
                    if (glowEffect == null) {
                        player.sendMessage("You are now being " + ChatColor.RED +  "EXPOSED" + ChatColor.WHITE + " after standing still for too long!");
                        hunter.sendMessage(ChatColor.AQUA + player.getName() + ChatColor.WHITE + "'s location has been revealed!");
                    }
                    
                    player.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 4*20, 0, false, false, false));
                    
                }
            });
        }

        survivors.forEach(player -> {

            Location[] previousLocations = playerPreviousLocations.get(player);
            if (previousLocations == null) {
                previousLocations = new Location[threshold];
            }
            previousLocations[count % threshold] = player.getLocation();
            playerPreviousLocations.put(player, previousLocations);

            Boolean[] previousHunterNearbyPlayers = hunterNearbyPlayers.get(player);
            if (previousHunterNearbyPlayers == null) {
                previousHunterNearbyPlayers = new Boolean[threshold];
            }

            Boolean hunterNearby = false;
            for (Entity entity: player.getNearbyEntities(10,10,10)) {
                if (entity instanceof Player) {
                    Player nearbyPlayer = (Player) entity;
                    if (nearbyPlayer.getScoreboardTags().contains(HUNTER_TAG)) {
                        previousHunterNearbyPlayers[count % threshold] = true;
                        hunterNearby = true;
                        break;
                    }
                }
            }
            if (!hunterNearby) {
                previousHunterNearbyPlayers[count % threshold] = false;
            }

            hunterNearbyPlayers.put(player, previousHunterNearbyPlayers);
            
        });
        count++;
    }

    private Double movingAverageDistance(Location[] locations) {
        Double totalDistance = 0.0;
        for (int i = 0; i < locations.length - 1; i++) {
            totalDistance += locations[i].distance(locations[i+1]);
        }
        return totalDistance / (locations.length - 1);
    }

    private Boolean noHunterNearby(Boolean[] booleans) {
        for (Boolean bool: booleans) {
            if (bool) {
                return false;
            }
        }
        return true;
    }


}
