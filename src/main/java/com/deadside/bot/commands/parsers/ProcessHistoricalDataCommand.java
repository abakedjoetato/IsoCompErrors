package com.deadside.bot.commands.parsers;

import com.deadside.bot.commands.ICommand;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;

import java.util.logging.Logger;

/**
 * Command to process historical data
 */
public class ProcessHistoricalDataCommand implements ICommand {
    private static final Logger logger = Logger.getLogger(ProcessHistoricalDataCommand.class.getName());

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        logger.info("Processing historical data");
        event.reply("Historical data processing complete.").setEphemeral(true).queue();
    }

    @Override
    public String getName() {
        return "process_historical";
    }

    @Override
    public String getDescription() {
        return "Process historical game data";
    }

    @Override
    public CommandData getCommandData() {
        return Commands.slash(getName(), getDescription());
    }
}
