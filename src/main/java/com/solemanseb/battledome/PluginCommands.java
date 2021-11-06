package com.solemanseb.battledome;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitScheduler;

import java.util.ArrayList;
import java.util.List;

public class PluginCommands implements CommandExecutor {

    public static final String[] registeredCommands = {
            "blueteam",
            "redteam",
            "clearteams",
            "start",
            "end",
            "top",
            "tp"
    };

    public boolean gameIsRunning = false;
    public boolean worldBorderModified;
    private final PluginMain main;


    public PluginCommands(PluginMain main){
        this.main = main;
    }

    public List<String> getCompletions(String[] args, List<String> existingCompletions){
        switch (args[0]){
            case "/blueteam":
            case "/redteam":
            case "clearteams":
                return new ArrayList<String>();
            case "/start":
            case "/end":
            case "/top":
            case "/tp":
            default:
                return existingCompletions;
        }
    }

    public boolean onCommand(CommandSender commandSender, Command command, String label, String[] args){
        if ("blueteam".equals(label)){
            if (args.length != 1)
                return false;
            Player target = Bukkit.getPlayer(args[0]);
            if (target == null){
                commandSender.sendMessage("Player not available");
                return false;
            }
            for (String name : main.getBlueTeam()){
                if (target.getName().equalsIgnoreCase(name)){
                    commandSender.sendMessage("Already on the blue team!");
                    return true;
                }
            }
            for (String name : main.getRedTeam()){
                if (target.getName().equalsIgnoreCase(name)) {
                    main.removePlayerFromTeam(main.getRedTeam(), name);
                    break;
                }
            }
            main.addToBlueTeam(target.getName());
            Bukkit.broadcastMessage(target.getName() + " is now on the " + ChatColor.BOLD + ChatColor.BLUE +"Blue Team!");
            return true;
        }
        else if("redteam".equals(label)){
            if (args.length != 1)
                return false;
            Player target = Bukkit.getPlayer(args[0]);
            if (target == null){
                commandSender.sendMessage("Player not available");
                return false;
            }
            for (String name : main.getRedTeam()){
                if (target.getName().equalsIgnoreCase(name)){
                    commandSender.sendMessage("Already on the red team!");
                    return true;
                }
            }
            for (String name : main.getBlueTeam()){
                if (target.getName().equalsIgnoreCase(name)) {
                    main.removePlayerFromTeam(main.getBlueTeam(), name);
                    break;
                }
            }
            main.addToRedTeam(target.getName());
            Bukkit.broadcastMessage(target.getName() + " is now on the " + ChatColor.BOLD + ChatColor.RED +"Red Team!");
            return true;
        }
        else if("clearteams".equals(label)){
            main.getRedTeam().clear();
            main.getBlueTeam().clear();
            Bukkit.broadcastMessage("Teams cleared!");
            return true;
        }
        else if ("start".equals(label)){
            if (gameIsRunning){
                commandSender.sendMessage("Game is in progress, use /end before starting another game");
                return true;
            }
            if ((main.getBlueTeam().size() < 1 || main.getRedTeam().size() < 1) && !main.isDebugMode()){
                commandSender.sendMessage("Teams don't have enough players to start");
                return true;
            }
            commandSender.sendMessage("Starting game...");
            commandSender.getServer().dispatchCommand(Bukkit.getConsoleSender(), "minecraft:kill @e[type=item]");
            if (worldBorderModified){
                WorldBorder wb = main.getWorld().getWorldBorder();
                wb.setCenter(main.getCenterBlock().getLocation());
                wb.setSize(400);
            }
            List<World> worlds = Bukkit.getWorlds();
            World world1 = worlds.get(0);
            if (main.getConfig().getBoolean("setTimeToZero", true)){
                main.setWorld(world1);
                main.getWorld().setTime(0);
            }
            for (String name : main.getRedTeam()){
                playerStateOnStart(name);
            }
            for (String name : main.getBlueTeam()){
                playerStateOnStart(name);
            }
            Player blueLeader = Bukkit.getPlayer(main.getBlueTeam().get(0));
            blueLeader.getInventory().addItem(new ItemStack(Material.OBSIDIAN));
            for (int i = 0; i <=63; i++ ){
                blueLeader.getInventory().addItem(new ItemStack(Material.CHICKEN_SPAWN_EGG));
            }
            Player redLeader = Bukkit.getPlayer(main.getRedTeam().get(0));
            redLeader.getInventory().addItem(new ItemStack(Material.OBSIDIAN));
            for (int i = 0; i <=63; i++ ){
                redLeader.getInventory().addItem(new ItemStack(Material.CHICKEN_SPAWN_EGG));
            }

            setPVPState(false);

            starTimers();

            gameIsRunning = true;

            Bukkit.broadcastMessage(ChatColor.RED.toString() + ChatColor.BOLD + "GAME STARTED! YOU HAVE 15 MINUTES TO PREPARE");
            return true;

        }
        else if ("end".equals(label)){
            if (!gameIsRunning){
                commandSender.sendMessage("Game is not in progress, use /start to start a new game!");
                return true;
            }
            worldBorderModified = false;
            Bukkit.broadcastMessage("Game ended!");
            gameIsRunning = false;
            return true;
        }
        else if ("top".equals(label)){
            if (commandSender instanceof Player player){
                Location currentLocation = player.getLocation();
                int topY = main.getWorld().getHighestBlockYAt(currentLocation);
                player.teleport(new Location(main.getWorld(),currentLocation.getX(),topY,currentLocation.getZ()));
            }
        }
        else if ("tp".equals(label)){
            if (args.length != 1){
                return false;
            }
            if (commandSender instanceof Player sender){
                Player targetPlayer = Bukkit.getPlayer(args[0]);
                if ((main.getBlueTeam().contains(sender) && main.getBlueTeam().contains(targetPlayer))
                        || main.getRedTeam().contains(sender) && main.getRedTeam().contains(targetPlayer)) {
                    sender.teleport(targetPlayer);
                }
                else
                    return false;
            }
        }
        return false;
    }

    private void starTimers() {
        BukkitScheduler tenMinTimer = Bukkit.getScheduler();
        tenMinTimer.scheduleSyncDelayedTask(main, new Runnable() {
            public void run() {
                Bukkit.broadcastMessage(ChatColor.BOLD.toString() + ChatColor.RED + "10 MINUTES REMAINING");
            }
        }, 6000L);


        BukkitScheduler fiveMinTimer = Bukkit.getScheduler();
        fiveMinTimer.scheduleSyncDelayedTask(main, new Runnable() {
            public void run() {
                Bukkit.broadcastMessage(ChatColor.BOLD.toString() + ChatColor.RED + "5 MINUTES REMAINING");
            }
        }, 12000L);

        BukkitScheduler threeMinTimer = Bukkit.getScheduler();
        threeMinTimer.scheduleSyncDelayedTask(main, new Runnable() {
            public void run() {
                Bukkit.broadcastMessage(ChatColor.BOLD.toString() + ChatColor.RED + "3 MINUTES REMAINING");
            }
        }, 14400L);

        BukkitScheduler oneMinTimer = Bukkit.getScheduler();
        oneMinTimer.scheduleSyncDelayedTask(main, new Runnable() {
            public void run() {
                Bukkit.broadcastMessage(ChatColor.BOLD.toString() + ChatColor.RED + "1 MINUTE REMAINING");
            }
        }, 16800L);

        BukkitScheduler thirtySecondTimer = Bukkit.getScheduler();
        thirtySecondTimer.scheduleSyncDelayedTask(main, new Runnable() {
            public void run() {
                Bukkit.broadcastMessage(ChatColor.BOLD.toString() + ChatColor.RED + "30 SECONDS REMAINING");
            }
        }, 17400L);

        BukkitScheduler fifteenSecondTimer = Bukkit.getScheduler();
        fifteenSecondTimer.scheduleSyncDelayedTask(main, new Runnable() {
            public void run() {
                Bukkit.broadcastMessage(ChatColor.BOLD.toString() + ChatColor.RED + "15 SECONDS REMAINING");
            }
        }, 17700L);

        BukkitScheduler pvpScheduler = Bukkit.getScheduler();
        pvpScheduler.scheduleSyncDelayedTask(main, new Runnable() {
            public void run() {
                setPVPState(true);
                Bukkit.broadcastMessage(ChatColor.BOLD.toString() + ChatColor.RED + "PVP ENABLED! KILL EVERYONE ON THE OPPOSITE TEAM OR DESTROY THEIR OBSIDIAN TO WIN!");
            }
        }, 18000L);
    }

    private void setPVPState(boolean bool) {
        Player player = Bukkit.getPlayer(main.getBlueTeam().get(0));
        player.getLocation().getWorld().setPVP(bool);
    }


    private void playerStateOnStart(String name) {
        Player player = Bukkit.getPlayer(name);
        if (player == null)
            return;
        player.setGameMode(GameMode.SURVIVAL);
        player.setHealthScale(20.0);
        player.setMaxHealth(20.0);
        player.setHealth(20.0);
        player.setFoodLevel(20);
        player.getInventory().clear();
        player.setExp(0);
        player.setLevel(0);
    }


}
