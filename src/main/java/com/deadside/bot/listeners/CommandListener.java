package com.deadside.bot.listeners;

import com.deadside.bot.commands.CommandManager;
import com.deadside.bot.commands.ICommand;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Listener for slash command interactions
 */
public class CommandListener extends ListenerAdapter {
    private static final Logger logger = LoggerFactory.getLogger(CommandListener.class);
    private final CommandManager commandManager;
    
    public CommandListener(CommandManager commandManager) {
        this.commandManager = commandManager;
    }
    
    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        String commandName = event.getName();
        String userName = event.getUser().getName();
        
        logger.info("Received command '{}' from user {}", commandName, userName);
        
        try {
            // Get the command from the manager
            ICommand command = commandManager.getCommandByName(commandName);
            
            if (command != null) {
                // Execute the command
                command.execute(event);
                logger.debug("Command '{}' executed for user {}", commandName, userName);
            } else {
                // Command not found
                event.reply("Unknown command: " + commandName).setEphemeral(true).queue();
                logger.warn("Unknown command '{}' attempted by user {}", commandName, userName);
            }
        } catch (Exception e) {
            // Handle any errors during command execution
            logger.error("Error executing command '{}' for user {}: {}", commandName, userName, e.getMessage(), e);
            event.reply("An error occurred while processing your command. Please try again later.").setEphemeral(true).queue();
        }
    }
}