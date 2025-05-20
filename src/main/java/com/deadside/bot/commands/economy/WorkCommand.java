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
import java.util.Random;
import java.util.logging.Logger;

/**
 * Command for working to earn currency
 */
public class WorkCommand implements ICommand {
    private static final Logger logger = Logger.getLogger(WorkCommand.class.getName());
    private final PlayerRepository playerRepository;
    private final Config config;
    private final Random random = new Random();
    private static final Duration COOLDOWN = Duration.ofHours(3);
    
    // Work messages for flavor text
    private static final String[] WORK_MESSAGES = {
            "You patrolled the area and found some loot",
            "You scavenged through abandoned buildings",
            "You hunted wildlife and sold the meat",
            "You helped escort traders through dangerous territory",
            "You eliminated some zombies and collected a bounty",
            "You found and sold valuable crafting materials",
            "You repaired equipment for other survivors",
            "You cleared out a bandit camp",
            "You salvaged parts from abandoned vehicles",
            "You helped defend a settlement from attackers"
    };

    public WorkCommand(Config config) {
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
                player.setLastWorkTime(Instant.EPOCH); // Set to epoch time for first-time users
            }
            
            // Check if the player can work
            Instant now = Instant.now();
            Instant lastWork = player.getLastWorkTime();
            Duration timeSinceLastWork = Duration.between(lastWork, now);
            
            if (timeSinceLastWork.compareTo(COOLDOWN) < 0 && !lastWork.equals(Instant.EPOCH)) {
                // Player cannot work yet
                long hoursRemaining = COOLDOWN.minus(timeSinceLastWork).toHours();
                long minutesRemaining = COOLDOWN.minus(timeSinceLastWork).toMinutes() % 60;
                
                EmbedBuilder embed = new EmbedBuilder()
                        .setTitle("Work - Cooldown Active")
                        .setColor(Color.RED)
                        .setDescription("You're too tired to work right now. Try again in " + 
                                       hoursRemaining + "h " + minutesRemaining + "m");
                
                event.replyEmbeds(embed.build()).setEphemeral(true).queue();
                return;
            }
            
            // Player can work
            long minReward = config.getWorkMinAmount();
            long maxReward = config.getWorkMaxAmount();
            long earnedAmount = minReward + random.nextInt((int)(maxReward - minReward + 1));
            
            player.setCurrency(player.getCurrency() + earnedAmount);
            player.setLastWorkTime(now);
            
            // Save updated player
            playerRepository.save(player);
            
            // Get a random work message
            String workMessage = WORK_MESSAGES[random.nextInt(WORK_MESSAGES.length)];
            
            // Send success message
            EmbedBuilder embed = new EmbedBuilder()
                    .setTitle("Work Completed")
                    .setColor(Color.GREEN)
                    .setDescription(workMessage + ".\n\n" +
                                    "You've earned **" + earnedAmount + "** credits!\n" +
                                    "Your new balance: **" + player.getCurrency() + "** credits")
                    .setFooter("You can work again in 3 hours");
            
            event.replyEmbeds(embed.build()).queue();
            
        } catch (Exception e) {
            logger.warning("Error processing work command: " + e.getMessage());
            event.reply("An error occurred while processing your work.").setEphemeral(true).queue();
        }
    }

    @Override
    public String getName() {
        return "work";
    }

    @Override
    public String getDescription() {
        return "Work to earn currency (cooldown: 3 hours)";
    }

    @Override
    public CommandData getCommandData() {
        return Commands.slash(getName(), getDescription());
    }
}