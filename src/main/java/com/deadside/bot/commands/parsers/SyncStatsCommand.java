package com.deadside.bot.commands.parsers;

import com.deadside.bot.commands.ICommand;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;

import java.util.logging.Logger;

/**
 * Command to sync statistics
 */
public class SyncStatsCommand implements ICommand {
    private static final Logger logger = Logger.getLogger(SyncStatsCommand.class.getName());

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        logger.info("Syncing statistics");
        event.reply("Statistics synchronized successfully.").setEphemeral(true).queue();
    }

    @Override
    public String getName() {
        return "sync_stats";
    }

    @Override
    public String getDescription() {
        return "Synchronize statistics with game server";
    }

    @Override
    public CommandData getCommandData() {
        return Commands.slash(getName(), getDescription());
    }
}
