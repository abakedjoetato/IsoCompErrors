package com.deadside.bot.listeners;

import com.deadside.bot.commands.CommandManager;
import com.deadside.bot.commands.ICommand;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

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
        logger.info("Command received: {}", event.getName());
        
        try {
            String commandName = event.getName();
            ICommand command = commandManager.getCommandByName(commandName);
            
            if (command != null) {
                command.execute(event);
            } else {
                event.reply("Unknown command: " + commandName).setEphemeral(true).queue();
                logger.warn("Unknown command executed: {}", commandName);
            }
        } catch (Exception e) {
            logger.error("Error executing command", e);
            String errorMessage = "An error occurred while executing the command";
            if (e.getMessage() != null) {
                errorMessage += ": " + e.getMessage();
            }
            
            try {
                if (event.isAcknowledged()) {
                    event.getHook().sendMessage(errorMessage).setEphemeral(true).queue();
                } else {
                    event.reply(errorMessage).setEphemeral(true).queue();
                }
            } catch (Exception ex) {
                logger.error("Error sending error message", ex);
            }
        }
    }
    
    @Override
    public void onCommandAutoCompleteInteraction(CommandAutoCompleteInteractionEvent event) {
        logger.debug("Autocomplete request: {} - {}", 
            event.getName(), event.getFocusedOption().getName());
            
        try {
            String commandName = event.getName();
            ICommand command = commandManager.getCommandByName(commandName);
            
            if (command != null) {
                List<net.dv8tion.jda.api.interactions.commands.Command.Choice> choices = 
                    command.handleAutoComplete(event);
                    
                if (choices != null && !choices.isEmpty()) {
                    event.replyChoices(choices).queue();
                }
            }
        } catch (Exception e) {
            logger.error("Error handling autocomplete", e);
        }
    }
}