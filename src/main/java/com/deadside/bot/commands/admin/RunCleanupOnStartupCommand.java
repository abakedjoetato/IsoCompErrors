package com.deadside.bot.commands.admin;

import com.deadside.bot.commands.ICommand;
import com.deadside.bot.utils.Config;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.logging.Logger;

/**
 * Admin command to enable/disable automatic cleanup on startup
 */
public class RunCleanupOnStartupCommand implements ICommand {
    private static final Logger logger = Logger.getLogger(RunCleanupOnStartupCommand.class.getName());
    private final Config config;

    public RunCleanupOnStartupCommand(Config config) {
        this.config = config;
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        boolean enabled = event.getOption("enabled").getAsBoolean();
        config.setProperty("startup.cleanup.enabled", String.valueOf(enabled));
        event.reply("Automatic cleanup on startup has been " + (enabled ? "enabled" : "disabled") + ".").setEphemeral(true).queue();
        logger.info("Automatic cleanup on startup {} by {}", enabled ? "enabled" : "disabled", event.getUser().getAsTag());
    }

    @Override
    public String getName() {
        return "cleanup_startup";
    }

    @Override
    public String getDescription() {
        return "Enable or disable automatic cleanup on startup";
    }

    @Override
    public CommandData getCommandData() {
        return Commands.slash(getName(), getDescription())
                .addOptions(
                        new OptionData(OptionType.BOOLEAN, "enabled", "Enable or disable automatic cleanup", true)
                );
    }
}
