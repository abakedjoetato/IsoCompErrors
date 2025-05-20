package com.deadside.bot.commands;

import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.Command.Choice;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

import java.util.List;

/**
 * Interface for all bot commands
 */
public interface ICommand {
    /**
     * Get the name of the command
     * @return Command name
     */
    String getName();
    
    /**
     * Get command data for registration
     * @return Command data
     */
    CommandData getCommandData();
    
    /**
     * Execute the command
     * @param event The slash command event
     */
    void execute(SlashCommandInteractionEvent event);
    
    /**
     * Handle autocomplete for this command
     * @param event The autocomplete event
     * @return List of choices for autocomplete
     */
    default List<Choice> handleAutoComplete(CommandAutoCompleteInteractionEvent event) {
        return List.of();
    }
}