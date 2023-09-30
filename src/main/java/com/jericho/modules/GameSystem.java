package com.jericho.modules;

import java.util.HashMap;
import java.util.List;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Entity;
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
import org.bukkit.scoreboard.Team;
import org.bukkit.scoreboard.Team.Option;
import org.bukkit.scoreboard.Team.OptionStatus;

import com.jericho.blackout.Blackout;

import net.md_5.bungee.api.ChatColor;

public class GameSystem implements Listener {

    Blackout plugin;
    World world ;

    NamespacedKey taskIdKey;
    NamespacedKey timerKey;
    NamespacedKey inGameKey;

    String HUNTER_TAG = "hunter";
    String SURVIVOR_TAG = "survivor";

    Location lobbyLocation = new Location(world, -72,-34,-88);

    Location hunterSpawnLocations[] = {
        new Location(world,-61 ,42, -87)
    };
    Location survivorSpawnLocations[] = {
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

    double gameDurationSeconds = 300;
    
    BossBar timerBar;
    BukkitTask timerTask;
    
    Random random = new Random();

    public GameSystem(Blackout plugin,World world) {
        this.plugin = plugin;
        this.world = world;
        taskIdKey = new NamespacedKey(plugin, "taskId");
        timerKey = new NamespacedKey(plugin, "timerKey");
        inGameKey = new NamespacedKey(plugin, "inGameKey");

        timerBar = Bukkit.createBossBar(timerKey,"Time Left", BarColor.RED, BarStyle.SEGMENTED_20);
    }

    private Team getBlackoutTeam() {
        Team team = Bukkit.getScoreboardManager().getMainScoreboard().getTeam("blackout");
        if (team == null) {
            team = Bukkit.getScoreboardManager().getMainScoreboard().registerNewTeam("blackout");
            team.setAllowFriendlyFire(false);
            team.setOption(Option.NAME_TAG_VISIBILITY, OptionStatus.NEVER);
            team.allowFriendlyFire();
        }
        return team;
    }
    
    /**
    Start the game session.
    */
    public void start() {
        // Pick a random player to be the hunter
        List<Player> allPlayers = world.getPlayers();
        allPlayers.removeIf(player -> player.getGameMode() == GameMode.SPECTATOR);

        if (allPlayers.size() < 2) {
            Bukkit.broadcastMessage(ChatColor.RED + "Not enough players to start the game");
            return;
        }

        Team blackoutTeam = getBlackoutTeam();

        Player hunter = allPlayers.get(random.nextInt(allPlayers.size()));
        hunter.addScoreboardTag(HUNTER_TAG);

        List<Player> survivors = world.getPlayers();
        survivors.removeIf(player -> player.getGameMode() == GameMode.SPECTATOR);
        
        survivors.remove(hunter);
        survivors.forEach(player -> {
            player.addScoreboardTag(SURVIVOR_TAG);
            blackoutTeam.addEntry(player.getName());
        });

        blackoutTeam.addEntry(hunter.getName());

        // Applied effect to all survivor
        survivors.forEach(player -> {
            player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 999999*20, 0, false, false, false));

            // Random teleport survivors to a survivor spawn location
            Location survivorSpawnLocation = this.survivorSpawnLocations[random.nextInt(this.survivorSpawnLocations.length)];
            player.teleport(world.getBlockAt(survivorSpawnLocation).getLocation());

            player.sendTitle(ChatColor.AQUA + "You are a Survivor", "Stay alive for 5 minutes", 10, 70, 20);
            player.sendMessage(ChatColor.RED + hunter.getName() + " is the Hunter");
            player.setGameMode(GameMode.ADVENTURE);
        });

        // Applied effect to hunter
        hunter.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 999999*20, 0, false, false, false));
        hunter.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 999999*20, 4, false, false, false));
        hunter.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 999999*20, 2, false, false, false));
        hunter.setGameMode(GameMode.ADVENTURE);

        // Random teleport hunter to a hunter spawn location
        Location hunterSpawnLocation = this.hunterSpawnLocations[random.nextInt(this.hunterSpawnLocations.length)];
        hunter.teleport(world.getBlockAt(hunterSpawnLocation).getLocation());

        hunter.sendTitle(ChatColor.RED + "You are the Hunter", "Take down all survivors in 5 minutes", 10, 70, 20);

        // Display timer bar
        timerBar.setProgress(1);
        
        for (Player player: allPlayers) {
            timerBar.addPlayer(player);
        }
        timerBar.addPlayer(hunter);

        // Start timer
        timerTask = new BukkitRunnable() {

            AnchorDetection anchorDetection = new AnchorDetection(survivors, hunter);

            @Override
            public void run() {
                anchorDetection.impluse();
                try {
                    timerBar.setProgress(timerBar.getProgress() - (1/gameDurationSeconds));
                }
                catch(Exception e) {
                    end(2);
                }
            }
        }.runTaskTimer(plugin, 0, 20);

        // Save timer task id to world persistent data
        PersistentDataContainer dataContainer = world.getPersistentDataContainer();
        dataContainer.set(taskIdKey, PersistentDataType.INTEGER, timerTask.getTaskId());
        dataContainer.set(inGameKey, PersistentDataType.BOOLEAN, true);
    }

    /**
    Terminate the game session.
    @param endCode 0 - Force to end the game, 1 - Game ended due to no survivor left, 2 - Game ended due to time is running out.
    */
    public void end(Integer endCode) {
        timerBar = Bukkit.getBossBar(timerKey);
        System.out.println(timerBar);

        PersistentDataContainer dataContainer = world.getPersistentDataContainer();
        timerTask = Bukkit.getScheduler().getPendingTasks().stream().filter(task -> task.getTaskId() == dataContainer.get(taskIdKey, PersistentDataType.INTEGER)).findFirst().orElse(null);

        Team blackoutTeam = getBlackoutTeam();

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
            player.removePotionEffect(PotionEffectType.SPEED);
            player.setGameMode(GameMode.ADVENTURE);
            player.teleport(world.getBlockAt(lobbyLocation).getLocation());
            blackoutTeam.removeEntry(player.getName());
            
            timerBar.removePlayer(player);

            if (endCode == 0) {
                Bukkit.broadcastMessage("Game interrupted");
            }
            else if (endCode == 1) {
                player.sendTitle(ChatColor.RED + "Hunter Win","No survivors left!", 10, 70, 20);
            }
            else if (endCode == 2) {
                player.sendTitle(ChatColor.AQUA + "Survivor Win","The time ran out!", 10, 70, 20);
            }

            
        }

        dataContainer.set(inGameKey, PersistentDataType.BOOLEAN, false);

    }

    private void endGameIfNoSurvivorLeft() {
        PersistentDataContainer dataContainer = world.getPersistentDataContainer();
        if (dataContainer.get(inGameKey, PersistentDataType.BOOLEAN) == null || dataContainer.get(inGameKey, PersistentDataType.BOOLEAN) == false) {
            return;
        }
        for (Player player: world.getPlayers()) {
            if (player.getScoreboardTags().contains(SURVIVOR_TAG)) {
                return;
            }
        }
        end(1);
    }

    @EventHandler
    public void survivorDeathEvent(PlayerDeathEvent e) {
        Player player = e.getEntity();
        PersistentDataContainer dataContainer = world.getPersistentDataContainer();
        if (dataContainer.get(inGameKey, PersistentDataType.BOOLEAN) == null || dataContainer.get(inGameKey, PersistentDataType.BOOLEAN) == false) {
            return;
        }
        // Edit death message
        e.setDeathMessage(ChatColor.RED + player.getName() + ChatColor.WHITE + " has been slained");
        if (player.getScoreboardTags().contains(SURVIVOR_TAG)) {
            player.removeScoreboardTag(SURVIVOR_TAG);
            player.sendTitle(ChatColor.RED + "You have been slained", "You are now a spectator", 10, 70, 20);
            player.setGameMode(GameMode.SPECTATOR);
        }

        endGameIfNoSurvivorLeft();
    }
}
