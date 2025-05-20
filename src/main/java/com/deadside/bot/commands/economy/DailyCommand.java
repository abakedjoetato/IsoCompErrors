package com.deadside.bot.commands.economy;

import com.deadside.bot.commands.ICommand;
import com.deadside.bot.db.models.Player;
import com.deadside.bot.db.repositories.PlayerRepository;
import com.deadside.bot.utils.Config;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;

import java.awt.*;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.logging.Logger;

/**
 * Command for claiming daily reward
 */
public class DailyCommand implements ICommand {
    private static final Logger logger = Logger.getLogger(DailyCommand.class.getName());
    private final PlayerRepository playerRepository;
    private final Config config;
    private static final Duration COOLDOWN = Duration.ofHours(24);

    public DailyCommand(Config config) {
        this.config = config;
        this.playerRepository = new PlayerRepository();
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        String discordId = event.getUser().getId();
        String guildId = event.getGuild().getId();
        
        try {
            // Find the player by Discord ID
            Player player = playerRepository.findByDiscordId(discordId, Long.parseLong(guildId));
            
            if (player == null) {
                // If player doesn't exist, create a new one
                player = new Player();
                player.setName(event.getUser().getName());
                player.setDiscordId(discordId);
                player.setGuildId(Long.parseLong(guildId));
                player.setCurrency(0);
                player.setLastDailyReward(Instant.EPOCH); // Set to epoch time for first-time users
            }
            
            // Check if the player can claim a daily reward
            Instant now = Instant.now();
            Instant lastClaim = player.getLastDailyReward();
            Duration timeSinceLastClaim = Duration.between(lastClaim, now);
            
            if (timeSinceLastClaim.compareTo(COOLDOWN) < 0 && !lastClaim.equals(Instant.EPOCH)) {
                // Player cannot claim yet
                long hoursRemaining = COOLDOWN.minus(timeSinceLastClaim).toHours();
                long minutesRemaining = COOLDOWN.minus(timeSinceLastClaim).toMinutes() % 60;
                
                EmbedBuilder embed = new EmbedBuilder()
                        .setTitle("Daily Reward - Not Available")
                        .setColor(Color.RED)
                        .setDescription("You've already claimed your daily reward. Try again in " + 
                                       hoursRemaining + "h " + minutesRemaining + "m");
                
                event.replyEmbeds(embed.build()).setEphemeral(true).queue();
                return;
            }
            
            // Player can claim reward
            long rewardAmount = config.getDailyAmount();
            player.setCurrency(player.getCurrency() + rewardAmount);
            player.setLastDailyReward(now);
            
            // Save updated player
            playerRepository.save(player);
            
            // Send success message
            EmbedBuilder embed = new EmbedBuilder()
                    .setTitle("Daily Reward Claimed!")
                    .setColor(Color.GREEN)
                    .setDescription("You've received **" + rewardAmount + "** credits!\n" +
                                   "Your new balance: **" + player.getCurrency() + "** credits")
                    .setFooter("Come back in 24 hours for your next reward");
            
            event.replyEmbeds(embed.build()).queue();
            
        } catch (Exception e) {
            logger.warning("Error processing daily reward: " + e.getMessage());
            event.reply("An error occurred while processing your daily reward.").setEphemeral(true).queue();
        }
    }

    @Override
    public String getName() {
        return "daily";
    }

    @Override
    public String getDescription() {
        return "Claim your daily currency reward";
    }

    @Override
    public CommandData getCommandData() {
        return Commands.slash(getName(), getDescription());
    }
}