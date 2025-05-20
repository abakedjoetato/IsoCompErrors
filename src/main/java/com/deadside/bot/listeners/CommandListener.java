package com.deadside.bot.listeners;

import com.deadside.bot.commands.CommandManager;
import com.deadside.bot.utils.EmbedUtils;
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
        try {
            String commandName = event.getName();
            logger.info("Received slash command: {}", commandName);
            
            // Handle slash command through command manager
            commandManager.handle(event);
        } catch (Exception e) {
            logger.error("Error handling slash command", e);
            
            // Reply with error if interaction hasn't been replied to
            if (!event.isAcknowledged()) {
                event.replyEmbeds(
                    EmbedUtils.createErrorEmbed("Command Error", 
                        "There was an error processing your command. Please try again later.")
                ).setEphemeral(true).queue();
            }
        }
    }
}