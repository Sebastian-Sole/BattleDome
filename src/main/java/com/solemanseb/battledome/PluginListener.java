package com.solemanseb.battledome;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.server.TabCompleteEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.List;

public class PluginListener implements Listener {

    PluginMain main;

    public PluginListener(PluginMain main){
        this.main = main;
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

        //generateSpawnCircle();
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
    public void onBlockPlace(BlockPlaceEvent event){
        if (event.getBlock().getType().equals(Material.OBSIDIAN)){
            String player = event.getPlayer().getName();
            if (main.getBlueTeam().contains(player)){
                main.setBlueObsidian(event.getBlock());
            }
            else if (main.getRedTeam().contains(player)){
                main.setRedObsidian(event.getBlock());
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







}
