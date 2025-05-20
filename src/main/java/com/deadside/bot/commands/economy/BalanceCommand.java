package com.deadside.bot.commands.economy;

import com.deadside.bot.commands.ICommand;
import com.deadside.bot.db.models.Player;
import com.deadside.bot.db.repositories.PlayerRepository;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.awt.*;
import java.util.logging.Logger;

/**
 * Command for checking player balance
 */
public class BalanceCommand implements ICommand {
    private static final Logger logger = Logger.getLogger(BalanceCommand.class.getName());
    private final PlayerRepository playerRepository;

    public BalanceCommand() {
        this.playerRepository = new PlayerRepository();
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        User targetUser = event.getOption("user") != null ? 
                          event.getOption("user").getAsUser() : event.getUser();
        String guildId = event.getGuild().getId();
        
        try {
            // Get the Discord ID as a string
            String discordId = targetUser.getId();
            
            // Find the player by Discord ID
            Player player = playerRepository.findByDiscordId(discordId, Long.parseLong(guildId));
            
            if (player == null) {
                // If the player doesn't exist, create a new one with default values
                player = new Player();
                player.setName(targetUser.getName());
                player.setDiscordId(discordId);
                player.setGuildId(Long.parseLong(guildId));
                player.setCurrency(0);
                
                // Save the new player
                playerRepository.save(player);
            }
            
            // Display the balance in an embed
            EmbedBuilder embed = new EmbedBuilder()
                    .setTitle(targetUser.getName() + "'s Balance")
                    .setColor(Color.GOLD)
                    .setDescription(targetUser.getAsMention() + " has **" + player.getCurrency() + "** credits")
                    .setThumbnail(targetUser.getEffectiveAvatarUrl());
            
            event.replyEmbeds(embed.build()).queue();
            
        } catch (Exception e) {
            logger.warning("Error checking balance: " + e.getMessage());
            event.reply("An error occurred while checking the balance.").setEphemeral(true).queue();
        }
    }

    @Override
    public String getName() {
        return "balance";
    }

    @Override
    public String getDescription() {
        return "Check your or another player's currency balance";
    }

    @Override
    public CommandData getCommandData() {
        return Commands.slash(getName(), getDescription())
                .addOptions(
                        new OptionData(OptionType.USER, "user", "User to check (defaults to yourself)", false)
                );
    }
}