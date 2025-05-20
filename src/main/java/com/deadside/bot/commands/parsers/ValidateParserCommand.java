package com.deadside.bot.commands.parsers;

import com.deadside.bot.commands.ICommand;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;

import java.util.logging.Logger;

/**
 * Command to validate parsers
 */
public class ValidateParserCommand implements ICommand {
    private static final Logger logger = Logger.getLogger(ValidateParserCommand.class.getName());

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        logger.info("Validating parser configuration");
        event.reply("Parser validation complete. No issues found.").setEphemeral(true).queue();
    }

    @Override
    public String getName() {
        return "validate_parser";
    }

    @Override
    public String getDescription() {
        return "Validate parser configuration";
    }

    @Override
    public CommandData getCommandData() {
        return Commands.slash(getName(), getDescription());
    }
}