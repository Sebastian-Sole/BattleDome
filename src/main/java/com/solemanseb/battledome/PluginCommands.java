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
            "createspawn",
            "top",
            "tp"
    };

    public boolean gameIsRunning = false;
    public boolean worldBorderModified;
    private final PluginMain main;
    private Block centerBlock;
    private final int spawnDiameter = 31;


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
            case "/createspawn":
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
                wb.setCenter(0.5, 0.5);
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

            //todo; test this
            BukkitScheduler scheduler = Bukkit.getScheduler();
            scheduler.scheduleSyncDelayedTask(main, new Runnable() {
                public void run() {
                    setPVPState(true);
                }
            }, 18000L);

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
        else if ("createspawn".equals(label)){
            generateSpawnCircle();
            commandSender.sendMessage("Spawn created");
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



    private void generateSpawnCircle(){
        Location corner = main.getWorld().getSpawnLocation();
        var cornerX = (int) corner.getX();
        var cornerY = (int) corner.getY();
        var cornerZ = (int) corner.getZ();
        for (int i = 0; i<this.spawnDiameter; i++){
            for (int j = 0; j<this.spawnDiameter; j++){
                Block block = main.getWorld().getBlockAt(cornerX-i,cornerY,cornerZ-j);
                block = main.getWorld().getHighestBlockAt(block.getLocation()); // Make block the highest block at its location
                if (isLiquid(block) || isTreeElement(block)){ // If it's a liquid block or tree block, push it down until it isn't
                    boolean bool = true;
                    while (bool){
                        block = main.getWorld().getBlockAt(block.getX(), block.getY() - 1, block.getZ());
                        // Check if block isn't tree or liquid, and is solid.
                        if ((!isLiquid(block) && (!isTreeElement(block) && block.getType().isSolid() ))){
                            bool = false;
                        }
                    }
                }
                changeBlock(i, j, block);
            }
        }

        Block enchantTable = main.getWorld().getBlockAt(this.centerBlock.getX(),this.centerBlock.getY()+1,this.centerBlock.getZ());
        main.setCenterBlock(enchantTable);
        enchantTable.setType(Material.ENCHANTING_TABLE);
        Bukkit.broadcastMessage("Spawn created at: " + cornerX + ", " + cornerY + ", " + cornerZ);
    }


    private void changeBlock(int i, int j, Block block1) {
        if(i == diameterFloor() && j == diameterFloor()){
            this.centerBlock = block1;
        }
        if (i == diameterFloor()){
            block1.setType(Material.BEDROCK);
        }
        else if (i < diameterFloor())
            block1.setType(Material.RED_WOOL);
        else block1.setType(Material.BLUE_WOOL);
    }

    private int diameterFloor(){
        return this.spawnDiameter / 2;
    }

    public boolean isLeaves(Block block){
        return block.getType().equals(Material.ACACIA_LEAVES)
                || block.getType().equals(Material.BIRCH_LEAVES)
                || block.getType().equals(Material.OAK_LEAVES)
                || block.getType().equals(Material.DARK_OAK_LEAVES)
                || block.getType().equals(Material.JUNGLE_LEAVES)
                || block.getType().equals(Material.SPRUCE_LEAVES);
    }


    public boolean isWood(Block block){
        return block.getType().equals(Material.ACACIA_LOG)
                || block.getType().equals(Material.BIRCH_LOG)
                || block.getType().equals(Material.DARK_OAK_LOG)
                || block.getType().equals(Material.JUNGLE_LOG)
                || block.getType().equals(Material.OAK_LOG)
                || block.getType().equals(Material.SPRUCE_LOG);
    }

    public boolean isTreeElement(Block block){
        return isWood(block) || isLeaves(block);
    }

    public boolean isLiquid(Block block){
        return block.getType().equals(Material.WATER) || block.getType().equals(Material.LAVA);
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
