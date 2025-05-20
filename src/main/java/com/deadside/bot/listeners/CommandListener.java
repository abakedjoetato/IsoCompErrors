package com.deadside.bot.listeners;

import com.deadside.bot.commands.CommandManager;
import com.deadside.bot.commands.ICommand;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.Command.Choice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;

/**
 * Listener for slash command interactions
 */
public class CommandListener extends ListenerAdapter {
    private static final Logger logger = LoggerFactory.getLogger(CommandListener.class);
    
    private final CommandManager commandManager;
    
    /**
     * Constructor
     * @param commandManager The command manager
     */
    public CommandListener(CommandManager commandManager) {
        this.commandManager = commandManager;
    }
    
    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        String commandName = event.getName();
        logger.info("Received slash command: {}", commandName);
        
        try {
            // Get the command from the command manager
            ICommand command = commandManager.getCommand(commandName);
            
            if (command != null) {
                // Execute the command
                command.execute(event);
                logger.debug("Command {} executed successfully", commandName);
            } else {
                logger.warn("Command not found: {}", commandName);
                event.reply("Command not found: " + commandName).setEphemeral(true).queue();
            }
        } catch (Exception e) {
            logger.error("Error handling slash command {}: {}", 
                commandName, e.getMessage(), e);
            
            // Reply with error message
            String errorMessage = "An error occurred while processing the command: " + e.getMessage();
            if (!event.isAcknowledged()) {
                event.reply(errorMessage).setEphemeral(true).queue();
            }
        }
    }
    
    @Override
    public void onCommandAutoCompleteInteraction(CommandAutoCompleteInteractionEvent event) {
        String commandName = event.getName();
        String optionName = event.getFocusedOption().getName();
        
        logger.debug("Received autocomplete for command: {}, option: {}", commandName, optionName);
        
        try {
            // Get the command from the command manager
            ICommand command = commandManager.getCommand(commandName);
            
            if (command != null) {
                // Handle autocomplete
                List<Choice> choices = command.handleAutoComplete(event);
                event.replyChoices(choices).queue();
                logger.debug("Autocomplete for command {} option {} handled successfully", commandName, optionName);
            } else {
                logger.warn("Command not found for autocomplete: {}", commandName);
                event.replyChoices(Collections.emptyList()).queue();
            }
        } catch (Exception e) {
            logger.error("Error handling autocomplete for command {}, option {}: {}", 
                commandName, optionName, e.getMessage(), e);
            // Reply with empty choices on error
            event.replyChoices(Collections.emptyList()).queue();
        }
    }
}