package com.jericho.modules;

import java.util.List;
import java.util.Random;
import java.util.jar.Attributes.Name;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import com.jericho.blackout.Blackout;

public class GameSystem implements Listener {

    Blackout plugin;
    World world ;

    NamespacedKey taskIdKey;
    NamespacedKey timerKey;

    String HUNTER_TAG = "hunter";
    String SURVIVOR_TAG = "survivor";

    Location lobbyLocation = new Location(world, -42,11,-89);

    Location hunterSpawnLocations[] = {
        new Location(world,-95, 43, -89),
        new Location(world,-82, 42, -43),
        new Location(world,0, 39, -47),
        new Location(world,-4, 42, -127),
    };
    Location survivorSpawnLocations[] = {
        new Location(world,-64, 47, -98)
    };

    double gameDurationSeconds = 10;
    
    BossBar timerBar;
    BukkitTask timerTask;
    
    Random random = new Random();

    public GameSystem(Blackout plugin,World world) {
        this.plugin = plugin;
        this.world = world;
        taskIdKey = new NamespacedKey(plugin, "taskId");
        timerKey = new NamespacedKey(plugin, "timerKey");

        timerBar = Bukkit.createBossBar(timerKey,"Time Left", BarColor.RED, BarStyle.SEGMENTED_20);
        // taskIdKey = new NamespacedKey(plugin, "worldKey");
    }
    
    /**
    Start the game session.
    @param world The world to end the game in
    */
    public void start() {
        // Pick a random player to be the hunter
        List<Player> allPlayers = world.getPlayers();

        Player hunter = allPlayers.get(random.nextInt(allPlayers.size()));
        hunter.addScoreboardTag(HUNTER_TAG);

        List<Player> survivors = world.getPlayers();
        
        survivors.remove(hunter);
        survivors.forEach(player -> {
            player.addScoreboardTag(SURVIVOR_TAG);
        });

        // Applied blindness to all players
        survivors.forEach(player -> {
            player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 999999*20, 0, false, false, false));
        });
        hunter.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 999999*20, 0, false, false, false));
        hunter.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 999999*20, 5, false, false, false));


        // Random teleport hunter to a hunter spawn location
        Location hunterSpawnLocation = this.hunterSpawnLocations[random.nextInt(this.hunterSpawnLocations.length)];
        // hunter.teleport(world.getBlockAt(hunterSpawnLocation).getLocation());

        // Random teleport survivors to a survivor spawn location
        survivors.forEach(player -> {
            Location survivorSpawnLocation = this.survivorSpawnLocations[random.nextInt(this.survivorSpawnLocations.length)];
            // player.teleport(world.getBlockAt(survivorSpawnLocation).getLocation());
        });

        timerBar.setProgress(1);
        
        for (Player player: allPlayers) {
            timerBar.addPlayer(player);
        }
        timerBar.addPlayer(hunter);

        timerTask = new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    timerBar.setProgress(timerBar.getProgress() - (1/gameDurationSeconds));
                }
                catch(Exception e) {
                    end(2);
                }
            }
        }.runTaskTimer(plugin, 0, 20);

        PersistentDataContainer dataContainer = world.getPersistentDataContainer();
        dataContainer.set(taskIdKey, PersistentDataType.INTEGER, timerTask.getTaskId());
        // dataContainer.set(worldKey, PersistentDataType.STRING, world.getUID().toString());
    }

    /**
    Terminate the game session.
    @param endCode 0 - Force to end the game, 1 - Game ended due to no survivor left, 2 - Game ended due to time is running out.
    */
    public void end(Integer endCode) {
        timerBar = Bukkit.getBossBar(timerKey);
        PersistentDataContainer dataContainer = world.getPersistentDataContainer();
        timerTask = Bukkit.getScheduler().getPendingTasks().stream().filter(task -> task.getTaskId() == dataContainer.get(taskIdKey, PersistentDataType.INTEGER)).findFirst().orElse(null);

        if (timerTask != null) {
            timerTask.cancel();
        }
        
        for (Player player: world.getPlayers()) {
            if (player.getScoreboardTags().contains(HUNTER_TAG)) {
                player.removeScoreboardTag(HUNTER_TAG);
            }
            if (player.getScoreboardTags().contains(SURVIVOR_TAG)) {
                player.removeScoreboardTag(SURVIVOR_TAG);
            }
            player.removePotionEffect(PotionEffectType.BLINDNESS);
            player.removePotionEffect(PotionEffectType.INCREASE_DAMAGE);
            timerBar.removePlayer(player);
            // player.teleport(world.getBlockAt(lobbyLocation).getLocation());
        }

        if (endCode == 0) {
            Bukkit.broadcastMessage("Game interrupted");
        }
        else if (endCode == 1) {
            Bukkit.broadcastMessage("Hunter Win");
        }
        else if (endCode == 2) {
            Bukkit.broadcastMessage("Survivors win");
        }
        
    }

    private void endGameIfNoSurvivorLeft() {
        for (Player player: world.getPlayers()) {
            if (player.getScoreboardTags().contains(SURVIVOR_TAG)) {
                return;
            }
        }
        end(1);
    }

    @EventHandler
    public void survivorDeathEvent(PlayerDeathEvent e) {
        if (e.getEntity().getScoreboardTags().contains(SURVIVOR_TAG)) {
            e.getEntity().removeScoreboardTag(SURVIVOR_TAG);
        }

        endGameIfNoSurvivorLeft();
    }
}
