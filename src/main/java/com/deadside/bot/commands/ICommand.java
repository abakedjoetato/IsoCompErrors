package com.deadside.bot.commands;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

/**
 * Interface for all commands
 */
public interface ICommand {
    /**
     * Execute the command
     * @param event The slash command event
     */
    void execute(SlashCommandInteractionEvent event);
    
    /**
     * Get the command name
     * @return Command name
     */
    String getName();
    
    /**
     * Get the command description
     * @return Command description
     */
    String getDescription();
    
    /**
     * Get the command data for registration with Discord API
     * @return CommandData object
     */
    CommandData getCommandData();
}