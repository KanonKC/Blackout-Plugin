package com.jericho.classes;

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
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.Team;
import org.bukkit.scoreboard.Team.Option;
import org.bukkit.scoreboard.Team.OptionStatus;

import com.jericho.blackout.Blackout;
import com.jericho.modules.AnchorDetection;

import net.md_5.bungee.api.ChatColor;

public class GameSystem implements Listener {

    Blackout plugin; 
    
    NamespacedKey taskIdKey;
    NamespacedKey timerKey;
    NamespacedKey inGameKey;

    String HUNTER_TAG = "hunter";
    String SURVIVOR_TAG = "survivor";

    protected World world;
    protected Location lobbyLocation;
    protected Location hunterSpawnLocations[];
    protected Location survivorSpawnLocations[];

    double gameDurationSeconds = 300;
    
    BossBar timerBar;
    BukkitTask timerTask;
    Boolean gameIsRunning = false;
    Team blackoutTeam;
    
    Random random = new Random();

    public GameSystem(Blackout plugin) {
        this.plugin = plugin;

        Bukkit.getPluginManager().registerEvents(this, plugin);

        taskIdKey = new NamespacedKey(plugin, "taskId");
        timerKey = new NamespacedKey(plugin, "timerKey");
        inGameKey = new NamespacedKey(plugin, "inGameKey");

        blackoutTeam = Bukkit.getScoreboardManager().getMainScoreboard().getTeam("blackout");
        if (blackoutTeam == null) {
            blackoutTeam = Bukkit.getScoreboardManager().getMainScoreboard().registerNewTeam("blackout");
            blackoutTeam.setAllowFriendlyFire(false);
            blackoutTeam.setOption(Option.NAME_TAG_VISIBILITY, OptionStatus.NEVER);
            blackoutTeam.allowFriendlyFire();
        }

        timerBar = Bukkit.createBossBar(timerKey,"Time Left", BarColor.RED, BarStyle.SEGMENTED_20);
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

        gameIsRunning = true;
    }

    /**
    Terminate the game session.
    @param endCode 0 - Force to end the game, 1 - Game ended due to no survivor left, 2 - Game ended due to time is running out.
    */
    public void end(Integer endCode) {
       
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
            System.out.println("Remove timer from" + player.getName());

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

        gameIsRunning = false;
    }

    private void endGameIfNoSurvivorLeft() {
        if (!gameIsRunning) {
            return;
        }
        for (Player player: world.getPlayers()) {
            if (player.getScoreboardTags().contains(SURVIVOR_TAG)) {
                return;
            }
        }
        end(1);
    }

    public World getWorld() {
        return world;
    }

    @EventHandler
    public void survivorDeathEvent(PlayerDeathEvent e) {
        Player player = e.getEntity();
        // System.out.println(gameIsRunning);
        // System.out.println(gameIsRunning == null);
        // System.out.println(!gameIsRunning);
        if (!gameIsRunning) {
            // System.out.println("Return");
            return;
        }
        // Edit death message
        // System.out.println("Should not work");
        e.setDeathMessage(ChatColor.RED + player.getName() + ChatColor.WHITE + " has been slained");
        if (player.getScoreboardTags().contains(SURVIVOR_TAG)) {
            player.removeScoreboardTag(SURVIVOR_TAG);
            player.sendTitle(ChatColor.RED + "You have been slained", "You are now a spectator", 10, 70, 20);
            player.setGameMode(GameMode.SPECTATOR);
        }

        endGameIfNoSurvivorLeft();
    }
}