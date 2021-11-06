package com.solemanseb.battledome;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.server.TabCompleteEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.List;

public class PluginListener implements Listener {

    PluginMain main;
    private Block centerBlock;
    private final int spawnDiameter = 31;

    public PluginListener(PluginMain main){
        this.main = main;
    }

    @EventHandler
    public void onClick(PlayerInteractEvent e){
        Player player = e.getPlayer();
        Block targetBlock = e.getClickedBlock();

        if(player.getEquipment().getItemInMainHand().getType() == Material.WOODEN_HOE){
            if (main.isOnBlueTeam(player)){
                if (main.getBlueObsidian() != null){
                    if (targetBlock.getType() == Material.OBSIDIAN){
                        if (targetBlock.getY() > 45){
                            main.setBlueObsidian(targetBlock);
                            Bukkit.broadcastMessage("BLUE TEAM OBSIDIAN IS SET");
                        }
                        else player.sendMessage("Please place your obsidian ABOVE ground. You will be disqualified if it is buried");
                    }
                    else player.sendMessage("Cannot set your team's obsidian as a different block");
                }
                else player.sendMessage("Your obsidian is already set!");
            }
            if (main.isOnRedTeam(player)){
                if (main.getRedObsidian() != null){
                    if (targetBlock.getType() == Material.OBSIDIAN){
                        if (targetBlock.getY() > 45){
                            main.setRedObsidian(targetBlock);
                            Bukkit.broadcastMessage("RED TEAM OBSIDIAN IS SET");
                        }
                        else player.sendMessage("Please place your obsidian ABOVE ground. You will be disqualified if it is buried");
                    }
                    else player.sendMessage("Cannot set your team's obsidian as a different block");
                }
                else player.sendMessage("Your obsidian is already set!");
            }
        }
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event){
        if (main.getCommands().gameIsRunning){
            if (main.getPvpState()){
                event.getPlayer().setGameMode(GameMode.SPECTATOR);
                event.getPlayer().sendTitle(ChatColor.DARK_RED.toString() + ChatColor.BOLD + "YOU ARE ELIMINATED", "Better luck next time!", 20,60,20);
            }
            else{
                event.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 600, 2));
            }
        }
        else {
            main.getLogger().info("Game isn't running");
        }
    }


    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        main.getLogger().info("Player Joined event");
        Location joinLoc = event.getPlayer().getLocation();
        WorldBorder wb = main.getWorld().getWorldBorder();

        wb.setDamageAmount(0);
        wb.setWarningDistance(0);
        wb.setCenter(joinLoc);
        wb.setSize(main.getConfig().getInt("preGameBorderSize", 300));

        main.commands.worldBorderModified = true;

        generateSpawnCircle();
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event){
        if (event.getBlock().getLocation().equals(new Location(
                main.getWorld(),
                main.getCenterBlock().getX(),
                main.getCenterBlock().getY(),
                main.getCenterBlock().getZ()))){
            event.setCancelled(true);
            return;
        }
        if (event.getBlock().equals(main.getBlueObsidian())){
            Bukkit.broadcastMessage(ChatColor.BOLD.toString() + ChatColor.RED + "RED TEAM" + "WINS!");
            for (String name : main.getBlueTeam()){
                Bukkit.getPlayer(name).sendTitle(ChatColor.DARK_RED.toString() + ChatColor.BOLD + "YOU LOSE!", "Red Team wins!", 20, 60, 20);
            }
            for (String name : main.getRedTeam()){
                Bukkit.getPlayer(name).sendTitle(ChatColor.GREEN.toString() + ChatColor.BOLD + "YOU WIN!", "Obsidian was broken!", 20, 60, 20 );
            }
        }
        else if (event.getBlock().equals(main.getRedObsidian())){
            Bukkit.broadcastMessage(ChatColor.BOLD.toString() + ChatColor.BLUE + "BLUE TEAM" + "WINS!");
            for (String name : main.getRedTeam()){
                Bukkit.getPlayer(name).sendTitle(ChatColor.DARK_RED.toString() + ChatColor.BOLD + "YOU LOSE!", "Blue Team wins!", 20, 60, 20);
            }
            for (String name : main.getBlueTeam()){
                Bukkit.getPlayer(name).sendTitle(ChatColor.GREEN.toString() + ChatColor.BOLD + "YOU WIN!", "Obsidian was broken!", 20, 60, 20 );
            }
        }
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event){
        if (main.getPvpState()){
            String name = event.getEntity().getName();
            if (main.getBlueTeam().contains(name)) {
                main.addDeadTeam(name);
                Bukkit.broadcastMessage(name + ChatColor.DARK_RED + ChatColor.BOLD + " is ELIMINATED!" +ChatColor.BLUE + " BLUE TEAM " + "members remaining: " + main.getBlueTeamDeathCount() );

            }
            else if (main.getRedTeam().contains(name)) {
                main.addDeadTeam(name);
                Bukkit.broadcastMessage(name + ChatColor.DARK_RED + ChatColor.BOLD + " is ELIMINATED!" +ChatColor.RED + " RED TEAM " + "members remaining: " + main.getRedTeamDeathCount() );
            }
        }
    }


    @EventHandler
    public void onAutocomplete(TabCompleteEvent event){
        String buffer = event.getBuffer();
        if(!buffer.startsWith("/")) return;
        String[] args = buffer.split(" ");

        List<String> completions = main.getCommands().getCompletions(args, event.getCompletions());

        event.setCompletions(completions);
    }


    private void generateSpawnCircle(){
        Location corner = main.getWorld().getSpawnLocation();
        var cornerX = (int) corner.getX();
        var cornerY = (int) corner.getY();
        var cornerZ = (int) corner.getZ();
        for (int i = 0; i<this.spawnDiameter; i++){
            for (int j = 0; j<this.spawnDiameter; j++){
                Block block = main.getWorld().getBlockAt(cornerX+i,cornerY,cornerZ+j);
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





}
