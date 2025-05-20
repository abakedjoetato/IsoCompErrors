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
        // Check if user is admin
        if (!isAdmin(event.getUser().getIdLong())) {
            event.reply("You do not have permission to use this command.")
                    .setEphemeral(true)
                    .queue();
            return;
        }

        boolean enabled = event.getOption("enabled").getAsBoolean();
        
        // Save the setting to config
        config.setProperty("startup.cleanup.enabled", String.valueOf(enabled));
        
        event.reply("Automatic cleanup on startup has been " + (enabled ? "enabled" : "disabled") + ".")
                .setEphemeral(true)
                .queue();
        
        logger.info("Automatic cleanup on startup " + (enabled ? "enabled" : "disabled") + " by " + event.getUser().getAsTag());
    }

    private boolean isAdmin(long userId) {
        try {
            // Get the bot owner ID
            String ownerIdStr = config.getBotOwnerId();
            long ownerId = ownerIdStr != null && !ownerIdStr.isEmpty() ? Long.parseLong(ownerIdStr) : 0;
            
            // Check if user is owner or in admin list
            if (ownerIdStr != null && !ownerIdStr.isEmpty()) {
                return String.valueOf(userId).equals(ownerIdStr) || config.getAdminUserIds().contains(userId);
            }
            return userId == ownerId || config.getAdminUserIds().contains(userId);
        } catch (Exception e) {
            logger.warning("Error checking admin permission: " + e.getMessage());
            return false;
        }
    }

    @Override
    public String getName() {
        return "setup_startup_cleanup";
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