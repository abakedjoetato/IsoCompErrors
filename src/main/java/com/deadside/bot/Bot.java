package com.deadside.bot;

import com.deadside.bot.commands.CommandManager;
import com.deadside.bot.commands.ICommand;
import com.deadside.bot.listeners.CommandListener;
import com.deadside.bot.utils.Config;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Simple Discord bot implementation for direct use
 */
public class Bot extends ListenerAdapter {
    private static final Logger logger = Logger.getLogger(Bot.class.getName());
    private final JDA jda;
    private final CommandManager commandManager;
    private final Config config;

    public Bot(String token) throws Exception {
        this.config = Config.getInstance();
        
        // Initialize JDA
        this.jda = JDABuilder.createDefault(token)
                .setActivity(Activity.playing("Deadside"))
                .enableIntents(GatewayIntent.GUILD_MEMBERS, 
                        GatewayIntent.GUILD_MESSAGES,
                        GatewayIntent.MESSAGE_CONTENT)
                .build();
        
        // Initialize command manager
        this.commandManager = new CommandManager(jda, config);
        
        // Register event listeners
        this.jda.addEventListener(new CommandListener(commandManager));
        this.jda.addEventListener(this);
        
        // Register slash commands
        List<ICommand> commands = commandManager.getAllCommands();
        this.jda.updateCommands().addCommands(commandManager.getCommandData()).queue(
                success -> logger.info("Successfully registered " + commands.size() + " slash commands"),
                failure -> logger.log(Level.SEVERE, "Failed to register slash commands", failure)
        );
    }
    
    public JDA getJda() {
        return jda;
    }
    
    public void shutdown() {
        jda.shutdown();
    }
}