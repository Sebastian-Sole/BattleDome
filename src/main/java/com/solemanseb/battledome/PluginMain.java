package com.solemanseb.battledome;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class PluginMain extends JavaPlugin {
    // Fields
    private ArrayList<String> redTeam = new ArrayList<>();
    private ArrayList<String> blueTeam = new ArrayList<>();
    private Logger logger;
    public PluginCommands commands;

    private Block blueObsidian;
    private Block redObsidian;

    private boolean debugMode = false;
    private World world;

    private boolean pvpState = false;

    private Block centerBlock;


    @Override
    public void onDisable() {
        logger.info("BattleDome plugin disabled");
    }

    @Override
    public void onEnable() {
        logger = Logger.getLogger("com.solemanseb.battledome.PluginMain");
        logger = getLogger();
        logger.info("BattleDome Plugin Enabled!");
        saveDefaultConfig();
        debugMode = getConfig().getBoolean("debugMode", false);
        getServer().getPluginManager().registerEvents(new PluginListener(this), this);

        commands = new PluginCommands(this);
        for (String command : PluginCommands.registeredCommands) {
            this.getCommand(command).setExecutor(commands);
        }

        List<World> worlds = Bukkit.getWorlds();
        if (worlds.size() < 1) {
            logger.warning("Could not detect main world! Plugin will not work.");
        }
        world = worlds.get(0);

    }


    public void removePlayerFromTeam(ArrayList<String> team, String name) {
        if (!(team.equals(redTeam) || team.equals(blueTeam)))
            logger.info("Cannot remove player from non-existent team");
        if (!team.contains(name)) {
            logger.info("Player not in this team");
        }
        team.remove(name);
    }


    public ArrayList<String> getRedTeam() {
        return redTeam;
    }

    public void setRedTeam(ArrayList<String> redTeam) {
        this.redTeam = redTeam;
    }

    public ArrayList<String> getBlueTeam() {
        return blueTeam;
    }

    public void setBlueTeam(ArrayList<String> blueTeam) {
        this.blueTeam = blueTeam;
    }


    public void setLogger(Logger logger) {
        this.logger = logger;
    }

    public PluginCommands getCommands() {
        return commands;
    }

    public void setCommands(PluginCommands commands) {
        this.commands = commands;
    }

    public boolean isDebugMode() {
        return debugMode;
    }

    public void setDebugMode(boolean debugMode) {
        this.debugMode = debugMode;
    }

    public World getWorld() {
        return world;
    }

    public void setWorld(World world) {
        this.world = world;
    }

    public void addToBlueTeam(String name) {
        blueTeam.add(name);
    }

    public void addToRedTeam(String name) {
        redTeam.add(name);
    }

    public boolean getPvpState() {
        return pvpState;
    }

    public void setPvpState(boolean pvpState) {
        this.pvpState = pvpState;
    }

    public Block getBlueObsidian() {
        return blueObsidian;
    }

    public void setBlueObsidian(Block blueObsidian) {
        this.blueObsidian = blueObsidian;
    }

    public Block getRedObsidian() {
        return redObsidian;
    }

    public void setRedObsidian(Block redObsidian) {
        this.redObsidian = redObsidian;
    }

    public Block getCenterBlock() {
        return centerBlock;
    }

    public void setCenterBlock(Block centerBlock) {
        this.centerBlock = centerBlock;
    }
}
