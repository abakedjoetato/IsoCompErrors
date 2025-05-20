package com.deadside.bot.bot;

import com.deadside.bot.commands.CommandManager;
import com.deadside.bot.config.Config;
import com.deadside.bot.db.models.GameServer;
import com.deadside.bot.db.repositories.GameServerRepository;
import com.deadside.bot.db.repositories.PlayerRepository;
import com.deadside.bot.listeners.ButtonListener;
import com.deadside.bot.listeners.CommandListener;
import com.deadside.bot.listeners.ModalListener;
import com.deadside.bot.listeners.StringSelectMenuListener;
import com.deadside.bot.parsers.DeadsideCsvParser;
import com.deadside.bot.parsers.DeadsideLogParser;
import com.deadside.bot.schedulers.KillfeedScheduler;
import com.deadside.bot.schedulers.ServerStatsScheduler;
import com.deadside.bot.sftp.SftpConnector;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Main DeadsideBot implementation
 */
public class DeadsideBot {
    private static final Logger logger = LoggerFactory.getLogger(DeadsideBot.class);
    private final String token;
    private JDA jda;
    private CommandManager commandManager;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(3);
    
    /**
     * Constructor
     * @param token Discord bot token
     */
    public DeadsideBot(String token) {
        this.token = token;
    }
    
    /**
     * Start the bot
     */
    public void start() {
        try {
            // Create repositories
            GameServerRepository gameServerRepository = new GameServerRepository();
            PlayerRepository playerRepository = new PlayerRepository();
            
            // Create SFTP connector
            SftpConnector sftpConnector = new SftpConnector();
            
            // Build JDA
            jda = JDABuilder.createDefault(token)
                .setStatus(OnlineStatus.ONLINE)
                .setActivity(Activity.playing("Deadside"))
                .enableIntents(
                    GatewayIntent.GUILD_MEMBERS,
                    GatewayIntent.GUILD_MESSAGES,
                    GatewayIntent.GUILD_VOICE_STATES,
                    GatewayIntent.GUILD_EMOJIS_AND_STICKERS,
                    GatewayIntent.DIRECT_MESSAGES,
                    GatewayIntent.MESSAGE_CONTENT
                )
                .setMemberCachePolicy(MemberCachePolicy.ALL)
                .setChunkingFilter(ChunkingFilter.ALL)
                .build();
            
            // Create parser instances
            DeadsideCsvParser csvParser = new DeadsideCsvParser(jda, sftpConnector, playerRepository, gameServerRepository);
            DeadsideLogParser logParser = new DeadsideLogParser(jda, gameServerRepository, sftpConnector);
            
            // Create command manager and initialize commands
            commandManager = new CommandManager(jda, gameServerRepository, playerRepository, sftpConnector, csvParser, logParser);
            
            // Register commands with Discord API
            logger.info("Registering commands with Discord API");
            commandManager.registerCommands();
            
            // Add event listeners
            jda.addEventListener(
                new CommandListener(commandManager),
                new ButtonListener(),
                new StringSelectMenuListener(),
                new ModalListener()
            );
            
            // Wait for JDA to be ready
            jda.awaitReady();
            logger.info("JDA initialized and connected to Discord gateway");
            
            // Start schedulers
            startSchedulers();
            
            logger.info("Bot is now online and all systems operational!");
        } catch (Exception e) {
            logger.error("Error starting bot", e);
            throw new RuntimeException("Failed to start bot", e);
        }
    }
    
    /**
     * Start all schedulers for automated tasks
     */
    private void startSchedulers() {
        try {
            logger.info("Starting schedulers...");
            
            // Get refresh intervals from config
            int statsInterval = 60; // Default 60 seconds
            int killfeedInterval = 30; // Default 30 seconds
            
            // Start killfeed scheduler
            KillfeedScheduler killfeedScheduler = new KillfeedScheduler(jda);
            scheduler.scheduleAtFixedRate(killfeedScheduler, 5, killfeedInterval, TimeUnit.SECONDS);
            logger.info("Killfeed scheduler started with interval {} seconds", killfeedInterval);
            
            // Start server stats scheduler
            ServerStatsScheduler serverStatsScheduler = new ServerStatsScheduler(jda);
            scheduler.scheduleAtFixedRate(serverStatsScheduler, 10, statsInterval, TimeUnit.SECONDS);
            logger.info("Server stats scheduler started with interval {} seconds", statsInterval);
        } catch (Exception e) {
            logger.error("Error starting schedulers", e);
        }
    }
    
    /**
     * Shutdown the bot
     */
    public void shutdown() {
        if (scheduler != null) {
            logger.info("Shutting down schedulers...");
            scheduler.shutdown();
            try {
                if (!scheduler.awaitTermination(10, TimeUnit.SECONDS)) {
                    scheduler.shutdownNow();
                }
            } catch (InterruptedException e) {
                scheduler.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
        
        if (jda != null) {
            logger.info("Shutting down JDA...");
            jda.shutdown();
        }
        
        logger.info("Bot shutdown complete");
    }
}