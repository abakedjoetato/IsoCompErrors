package com.deadside.bot.commands.economy;

import com.deadside.bot.commands.ICommand;
import com.deadside.bot.db.models.Player;
import com.deadside.bot.db.repositories.PlayerRepository;
import com.deadside.bot.utils.Config;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;

import java.awt.*;
import java.util.logging.Logger;

/**
 * Admin command for managing the economy system
 */
public class AdminEconomyCommand implements ICommand {
    private static final Logger logger = Logger.getLogger(AdminEconomyCommand.class.getName());
    private final PlayerRepository playerRepository;
    private final Config config;

    public AdminEconomyCommand(Config config) {
        this.config = config;
        this.playerRepository = new PlayerRepository();
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        if (!isAdmin(event.getUser().getIdLong())) {
            event.reply("You do not have permission to use this command.")
                    .setEphemeral(true)
                    .queue();
            return;
        }

        String subcommand = event.getSubcommandName();
        if (subcommand == null) {
            event.reply("Invalid subcommand").setEphemeral(true).queue();
            return;
        }

        switch (subcommand) {
            case "give":
                handleGiveCommand(event);
                break;
            case "take":
                handleTakeCommand(event);
                break;
            case "set":
                handleSetCommand(event);
                break;
            case "reset":
                handleResetCommand(event);
                break;
            case "set_daily":
                handleSetDailyCommand(event);
                break;
            case "set_work_max":
                handleSetWorkMaxCommand(event);
                break;
            case "set_work_min":
                handleSetWorkMinCommand(event);
                break;
            default:
                event.reply("Unknown subcommand: " + subcommand).setEphemeral(true).queue();
        }
    }

    private void handleGiveCommand(SlashCommandInteractionEvent event) {
        User targetUser = event.getOption("user").getAsUser();
        long amount = event.getOption("amount").getAsLong();
        String guildId = event.getGuild().getId();

        if (amount <= 0) {
            event.reply("Amount must be greater than zero.").setEphemeral(true).queue();
            return;
        }

        try {
            // Find the player by Discord ID
            Player player = playerRepository.findByDiscordId(targetUser.getId(), Long.parseLong(guildId));

            if (player == null) {
                // If player doesn't exist, create a new one
                player = new Player();
                player.setName(targetUser.getName());
                player.setDiscordId(targetUser.getId());
                player.setGuildId(Long.parseLong(guildId));
                player.setCurrency(0);
            }

            // Update currency
            long newBalance = player.getCurrency() + amount;
            player.setCurrency(newBalance);

            // Save updated player
            playerRepository.save(player);

            // Send success message
            EmbedBuilder embed = new EmbedBuilder()
                    .setTitle("Currency Added")
                    .setColor(Color.GREEN)
                    .setDescription("Added **" + amount + "** credits to " + targetUser.getAsMention() + ".\n" +
                            "New balance: **" + newBalance + "** credits");

            event.replyEmbeds(embed.build()).queue();

        } catch (Exception e) {
            logger.warning("Error giving currency: " + e.getMessage());
            event.reply("An error occurred while giving currency.").setEphemeral(true).queue();
        }
    }

    private void handleTakeCommand(SlashCommandInteractionEvent event) {
        User targetUser = event.getOption("user").getAsUser();
        long amount = event.getOption("amount").getAsLong();
        String guildId = event.getGuild().getId();

        if (amount <= 0) {
            event.reply("Amount must be greater than zero.").setEphemeral(true).queue();
            return;
        }

        try {
            // Find the player by Discord ID
            Player player = playerRepository.findByDiscordId(targetUser.getId(), Long.parseLong(guildId));

            if (player == null) {
                event.reply("Player not found in database.").setEphemeral(true).queue();
                return;
            }

            // Update currency (don't go below 0)
            long newBalance = Math.max(0, player.getCurrency() - amount);
            long actualAmountTaken = player.getCurrency() - newBalance;
            player.setCurrency(newBalance);

            // Save updated player
            playerRepository.save(player);

            // Send success message
            EmbedBuilder embed = new EmbedBuilder()
                    .setTitle("Currency Removed")
                    .setColor(Color.ORANGE)
                    .setDescription("Removed **" + actualAmountTaken + "** credits from " + targetUser.getAsMention() + ".\n" +
                            "New balance: **" + newBalance + "** credits");

            event.replyEmbeds(embed.build()).queue();

        } catch (Exception e) {
            logger.warning("Error taking currency: " + e.getMessage());
            event.reply("An error occurred while taking currency.").setEphemeral(true).queue();
        }
    }

    private void handleSetCommand(SlashCommandInteractionEvent event) {
        User targetUser = event.getOption("user").getAsUser();
        long amount = event.getOption("amount").getAsLong();
        String guildId = event.getGuild().getId();

        if (amount < 0) {
            event.reply("Amount cannot be negative.").setEphemeral(true).queue();
            return;
        }

        try {
            // Find the player by Discord ID
            Player player = playerRepository.findByDiscordId(targetUser.getId(), Long.parseLong(guildId));

            if (player == null) {
                // If player doesn't exist, create a new one
                player = new Player();
                player.setName(targetUser.getName());
                player.setDiscordId(targetUser.getId());
                player.setGuildId(Long.parseLong(guildId));
            }

            // Set currency to exact amount
            player.setCurrency(amount);

            // Save updated player
            playerRepository.save(player);

            // Send success message
            EmbedBuilder embed = new EmbedBuilder()
                    .setTitle("Currency Set")
                    .setColor(Color.BLUE)
                    .setDescription("Set " + targetUser.getAsMention() + "'s balance to **" + amount + "** credits.");

            event.replyEmbeds(embed.build()).queue();

        } catch (Exception e) {
            logger.warning("Error setting currency: " + e.getMessage());
            event.reply("An error occurred while setting currency.").setEphemeral(true).queue();
        }
    }

    private void handleResetCommand(SlashCommandInteractionEvent event) {
        User targetUser = event.getOption("user").getAsUser();
        String guildId = event.getGuild().getId();

        try {
            // Find the player by Discord ID
            Player player = playerRepository.findByDiscordId(targetUser.getId(), Long.parseLong(guildId));

            if (player == null) {
                event.reply("Player not found in database.").setEphemeral(true).queue();
                return;
            }

            // Reset currency to 0
            player.setCurrency(0);

            // Save updated player
            playerRepository.save(player);

            // Send success message
            EmbedBuilder embed = new EmbedBuilder()
                    .setTitle("Currency Reset")
                    .setColor(Color.RED)
                    .setDescription("Reset " + targetUser.getAsMention() + "'s balance to **0** credits.");

            event.replyEmbeds(embed.build()).queue();

        } catch (Exception e) {
            logger.warning("Error resetting currency: " + e.getMessage());
            event.reply("An error occurred while resetting currency.").setEphemeral(true).queue();
        }
    }

    private void handleSetDailyCommand(SlashCommandInteractionEvent event) {
        long amount = event.getOption("amount").getAsLong();

        if (amount <= 0) {
            event.reply("Amount must be greater than zero.").setEphemeral(true).queue();
            return;
        }

        try {
            // Set the new daily reward amount
            config.setDailyRewardAmount(amount);

            // Send success message
            long currentDaily = config.getDailyAmount();
            
            EmbedBuilder embed = new EmbedBuilder()
                    .setTitle("Daily Reward Updated")
                    .setColor(Color.GREEN)
                    .setDescription("Daily reward amount set to **" + currentDaily + "** credits.");

            event.replyEmbeds(embed.build()).queue();

        } catch (Exception e) {
            logger.warning("Error setting daily reward: " + e.getMessage());
            event.reply("An error occurred while setting daily reward amount.").setEphemeral(true).queue();
        }
    }

    private void handleSetWorkMaxCommand(SlashCommandInteractionEvent event) {
        long amount = event.getOption("amount").getAsLong();

        if (amount <= 0) {
            event.reply("Amount must be greater than zero.").setEphemeral(true).queue();
            return;
        }

        try {
            // Set the new work max amount
            long currentMin = config.getWorkMinAmount();
            
            if (amount < currentMin) {
                event.reply("Maximum work reward must be greater than or equal to minimum work reward ("
                        + currentMin + ").").setEphemeral(true).queue();
                return;
            }
            
            config.setWorkMaxAmount(amount);

            // Send success message
            EmbedBuilder embed = new EmbedBuilder()
                    .setTitle("Work Max Reward Updated")
                    .setColor(Color.GREEN)
                    .setDescription("Maximum work reward amount set to **" + amount + "** credits.");

            event.replyEmbeds(embed.build()).queue();

        } catch (Exception e) {
            logger.warning("Error setting work max reward: " + e.getMessage());
            event.reply("An error occurred while setting work max reward amount.").setEphemeral(true).queue();
        }
    }

    private void handleSetWorkMinCommand(SlashCommandInteractionEvent event) {
        long amount = event.getOption("amount").getAsLong();

        if (amount <= 0) {
            event.reply("Amount must be greater than zero.").setEphemeral(true).queue();
            return;
        }

        try {
            // Set the new work min amount
            long currentMin = config.getWorkMinAmount();
            long currentMax = config.getWorkMaxAmount();
            
            if (amount > currentMax) {
                event.reply("Minimum work reward must be less than or equal to maximum work reward ("
                        + currentMax + ").").setEphemeral(true).queue();
                return;
            }
            
            config.setWorkMinAmount(amount);

            // Send success message
            EmbedBuilder embed = new EmbedBuilder()
                    .setTitle("Work Min Reward Updated")
                    .setColor(Color.GREEN)
                    .setDescription("Minimum work reward amount set to **" + amount + "** credits.");

            event.replyEmbeds(embed.build()).queue();

        } catch (Exception e) {
            logger.warning("Error setting work min reward: " + e.getMessage());
            event.reply("An error occurred while setting work min reward amount.").setEphemeral(true).queue();
        }
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
        return "adminecon";
    }

    @Override
    public String getDescription() {
        return "Admin commands for economy management";
    }

    @Override
    public CommandData getCommandData() {
        return Commands.slash(getName(), getDescription())
                .addSubcommands(
                        new SubcommandData("give", "Give currency to a player")
                                .addOptions(
                                        new OptionData(OptionType.USER, "user", "User to give currency to", true),
                                        new OptionData(OptionType.INTEGER, "amount", "Amount to give", true)
                                                .setMinValue(1)
                                ),
                        new SubcommandData("take", "Take currency from a player")
                                .addOptions(
                                        new OptionData(OptionType.USER, "user", "User to take currency from", true),
                                        new OptionData(OptionType.INTEGER, "amount", "Amount to take", true)
                                                .setMinValue(1)
                                ),
                        new SubcommandData("set", "Set a player's currency to a specific amount")
                                .addOptions(
                                        new OptionData(OptionType.USER, "user", "User to set currency for", true),
                                        new OptionData(OptionType.INTEGER, "amount", "Amount to set", true)
                                                .setMinValue(0)
                                ),
                        new SubcommandData("reset", "Reset a player's currency to zero")
                                .addOptions(
                                        new OptionData(OptionType.USER, "user", "User to reset currency for", true)
                                ),
                        new SubcommandData("set_daily", "Set the daily reward amount")
                                .addOptions(
                                        new OptionData(OptionType.INTEGER, "amount", "New daily reward amount", true)
                                                .setMinValue(1)
                                ),
                        new SubcommandData("set_work_max", "Set the maximum work reward amount")
                                .addOptions(
                                        new OptionData(OptionType.INTEGER, "amount", "New maximum work reward amount", true)
                                                .setMinValue(1)
                                ),
                        new SubcommandData("set_work_min", "Set the minimum work reward amount")
                                .addOptions(
                                        new OptionData(OptionType.INTEGER, "amount", "New minimum work reward amount", true)
                                                .setMinValue(1)
                                )
                );
    }
}