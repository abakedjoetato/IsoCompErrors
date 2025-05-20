package com.deadside.bot.commands;

import com.deadside.bot.commands.admin.*;
import com.deadside.bot.commands.economy.*;
import com.deadside.bot.commands.player.*;
import com.deadside.bot.commands.stats.*;
import com.deadside.bot.db.repositories.GameServerRepository;
import com.deadside.bot.db.repositories.PlayerRepository;
import com.deadside.bot.parsers.DeadsideCsvParser;
import com.deadside.bot.parsers.DeadsideLogParser;
import com.deadside.bot.sftp.SftpConnector;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Command manager for Discord slash commands
 */
public class CommandManager {
    private static final Logger logger = LoggerFactory.getLogger(CommandManager.class);
    
    private final JDA jda;
    private final GameServerRepository gameServerRepository;
    private final PlayerRepository playerRepository;
    private final SftpConnector sftpConnector;
    private final DeadsideCsvParser csvParser;
    private final DeadsideLogParser logParser;
    
    private final Map<String, ICommand> commandMap = new HashMap<>();
    private final List<ICommand> commands = new ArrayList<>();
    
    /**
     * Constructor
     * @param jda The JDA instance
     * @param gameServerRepository The game server repository
     * @param playerRepository The player repository
     * @param sftpConnector The SFTP connector
     * @param csvParser The CSV parser
     * @param logParser The log parser
     */
    public CommandManager(JDA jda, GameServerRepository gameServerRepository, 
                         PlayerRepository playerRepository, SftpConnector sftpConnector,
                         DeadsideCsvParser csvParser, DeadsideLogParser logParser) {
        this.jda = jda;
        this.gameServerRepository = gameServerRepository;
        this.playerRepository = playerRepository;
        this.sftpConnector = sftpConnector;
        this.csvParser = csvParser;
        this.logParser = logParser;
        
        initializeCommands();
    }
    
    /**
     * Initialize all commands
     */
    private void initializeCommands() {
        try {
            // Admin commands
            registerCommand(new ServerCommand());
            registerCommand(new SftpConfigCommand());
            registerCommand(new SetLogChannelsCommand());
            registerCommand(new PathFixCommand(gameServerRepository, sftpConnector));
            registerCommand(new ProcessHistoricalDataCommand());
            registerCommand(new SyncStatsCommand());
            registerCommand(new TestCommand());
            registerCommand(new SetVoiceChannelCommand());
            
            // Player commands
            registerCommand(new PlayerStatsCommand(playerRepository));
            registerCommand(new TopPlayersCommand(playerRepository));
            
            // Statistics commands
            registerCommand(new ServerStatsCommand(gameServerRepository));
        } catch (Exception e) {
            logger.error("Error initializing commands: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Register slash commands with Discord API
     */
    public void registerCommands() {
        logger.info("Registering {} slash commands with Discord API", commands.size());
        
        if (commands.isEmpty()) {
            logger.warn("No commands to register");
            return;
        }
        
        List<CommandData> commandData = new ArrayList<>();
        for (ICommand command : commands) {
            commandData.add(command.getCommandData());
        }
        
        try {
            // Global commands
            jda.updateCommands().addCommands(commandData).queue(
                success -> logger.info("Successfully registered {} global commands", commandData.size()),
                error -> logger.error("Failed to register global commands: {}", error.getMessage())
            );
            
            logger.info("Commands registration queued successfully");
        } catch (Exception e) {
            logger.error("Error registering commands", e);
        }
    }
    
    /**
     * Register a command with the command manager
     * @param command The command to register
     */
    public void registerCommand(ICommand command) {
        String name = command.getName();
        commandMap.put(name, command);
        commands.add(command);
        
        logger.info("Registered command: {}", name);
    }
    
    /**
     * Get a command by name
     * @param name The name of the command
     * @return The command, or null if not found
     */
    public ICommand getCommand(String name) {
        return commandMap.get(name);
    }
    
    /**
     * Get all registered commands
     * @return List of all commands
     */
    public List<ICommand> getAllCommands() {
        return commands;
    }
    
    /**
     * Get the game server repository
     * @return The game server repository
     */
    public GameServerRepository getGameServerRepository() {
        return gameServerRepository;
    }
    
    /**
     * Get the player repository
     * @return The player repository
     */
    public PlayerRepository getPlayerRepository() {
        return playerRepository;
    }
    
    /**
     * Get the SFTP connector
     * @return The SFTP connector
     */
    public SftpConnector getSftpConnector() {
        return sftpConnector;
    }
    
    /**
     * Get the CSV parser
     * @return The CSV parser
     */
    public DeadsideCsvParser getCsvParser() {
        return csvParser;
    }
    
    /**
     * Get the log parser
     * @return The log parser
     */
    public DeadsideLogParser getLogParser() {
        return logParser;
    }
    
    /**
     * Get the JDA instance
     * @return The JDA instance
     */
    public JDA getJda() {
        return jda;
    }
}