package com.deadside.bot.commands;

import com.deadside.bot.commands.economy.AdminEconomyCommand;
import com.deadside.bot.commands.economy.BalanceCommand;
import com.deadside.bot.commands.economy.DailyCommand;
import com.deadside.bot.commands.economy.WorkCommand;
import com.deadside.bot.utils.Config;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Manages all slash commands for the bot
 */
public class CommandManager {
    private static final Logger logger = Logger.getLogger(CommandManager.class.getName());
    private final Map<String, ICommand> commands = new HashMap<>();
    private final JDA jda;
    private final Config config;

    public CommandManager(JDA jda, Config config) {
        this.jda = jda;
        this.config = config;
        registerCommands();
    }

    private void registerCommands() {
        // Server management commands
        registerCommand(new ServerCommand(config));
        registerCommand(new PathFixCommand());
        
        // Player commands
        registerCommand(new PlayerCommand());
        registerCommand(new PlayerListCommand());
        registerCommand(new TopPlayersCommand());
        
        // Economy commands
        registerCommand(new BalanceCommand());
        registerCommand(new DailyCommand(config));
        registerCommand(new WorkCommand(config));
        registerCommand(new AdminEconomyCommand(config));
        
        // Help commands
        registerCommand(new HelpCommand(this));
        
        logger.info("Registered " + commands.size() + " commands");
    }

    private void registerCommand(ICommand command) {
        commands.put(command.getName(), command);
    }

    public ICommand getCommandByName(String name) {
        return commands.get(name);
    }

    public List<CommandData> getCommandData() {
        List<CommandData> commandData = new ArrayList<>();
        for (ICommand command : commands.values()) {
            commandData.add(command.getCommandData());
        }
        return commandData;
    }
    
    public List<ICommand> getAllCommands() {
        return new ArrayList<>(commands.values());
    }
    
    public void executeCommand(SlashCommandInteractionEvent event) {
        String commandName = event.getName();
        ICommand command = getCommandByName(commandName);
        
        if (command != null) {
            try {
                command.execute(event);
            } catch (Exception e) {
                logger.severe("Error executing command: " + commandName + " - " + e.getMessage());
                event.reply("An error occurred while executing this command.").setEphemeral(true).queue();
            }
        } else {
            event.reply("Unknown command: " + commandName).setEphemeral(true).queue();
        }
    }
}