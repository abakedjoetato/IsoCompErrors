package com.deadside.bot.bot;

import com.deadside.bot.commands.CommandManager;
import com.deadside.bot.commands.ICommand;
import com.deadside.bot.db.repositories.GameServerRepository;
import com.deadside.bot.db.repositories.PlayerRepository;
import com.deadside.bot.listeners.CommandListener;
import com.deadside.bot.listeners.ModalListener;
import com.deadside.bot.utils.Config;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Main bot implementation that handles Discord connection
 */
public class DeadsideBot {
    private static final Logger logger = Logger.getLogger(DeadsideBot.class.getName());
    private final String token;
    private JDA jda;
    private CommandManager commandManager;
    private final Config config;
    private final GameServerRepository serverRepository;
    private final PlayerRepository playerRepository;
    private final AutoStartupCleanup autoStartupCleanup;
    
    public DeadsideBot(String token) {
        this.token = token;
        this.config = Config.getInstance();
        this.serverRepository = new GameServerRepository();
        this.playerRepository = new PlayerRepository();
        this.autoStartupCleanup = new AutoStartupCleanup(config);
    }
    
    /**
     * Initialize and start the bot
     * @throws Exception If an error occurs during startup
     */
    public void start() throws Exception {
        logger.info("Starting DeadsideBot...");
        
        // Run startup cleanup if enabled
        autoStartupCleanup.runIfEnabled();
        
        // Build the JDA instance
        jda = JDABuilder.createDefault(token)
                .setActivity(Activity.playing("Deadside"))
                .enableIntents(
                        GatewayIntent.GUILD_MEMBERS,
                        GatewayIntent.GUILD_PRESENCES,
                        GatewayIntent.GUILD_MESSAGES,
                        GatewayIntent.MESSAGE_CONTENT
                )
                .setMemberCachePolicy(MemberCachePolicy.ALL)
                .setChunkingFilter(ChunkingFilter.ALL)
                .build();
        
        // Initialize command manager
        commandManager = new CommandManager(jda, config);
        
        // Register event listeners
        jda.addEventListener(new CommandListener(commandManager));
        jda.addEventListener(new ModalListener());
        
        // Register all slash commands with Discord
        List<ICommand> commands = commandManager.getAllCommands();
        jda.updateCommands().addCommands(commandManager.getCommandData()).queue(
                success -> logger.info("Successfully registered " + commands.size() + " slash commands"),
                failure -> logger.log(Level.SEVERE, "Failed to register slash commands", failure)
        );
        
        logger.info("Bot started successfully!");
    }
    
    /**
     * Shut down the bot
     */
    public void shutdown() {
        if (jda != null) {
            jda.shutdown();
            logger.info("Bot has been shut down");
        }
    }
    
    /**
     * Get the JDA instance
     * @return JDA instance
     */
    public JDA getJda() {
        return jda;
    }
}