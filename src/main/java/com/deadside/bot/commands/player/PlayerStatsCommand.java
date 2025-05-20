package com.deadside.bot.commands.player;

import com.deadside.bot.commands.ICommand;
import com.deadside.bot.db.models.Player;
import com.deadside.bot.db.repositories.PlayerRepository;
import com.deadside.bot.utils.EmbedUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Command to display player statistics
 */
public class PlayerStatsCommand implements ICommand {
    private static final Logger logger = LoggerFactory.getLogger(PlayerStatsCommand.class);
    private final PlayerRepository playerRepository;

    public PlayerStatsCommand() {
        this.playerRepository = new PlayerRepository();
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        OptionMapping steamIdOption = event.getOption("steam_id");
        OptionMapping nameOption = event.getOption("name");
        
        if (steamIdOption == null && nameOption == null) {
            event.reply("Please provide either a Steam ID or player name.").setEphemeral(true).queue();
            return;
        }
        
        event.deferReply().queue();
        
        Player player = null;
        if (steamIdOption != null) {
            String steamId = steamIdOption.getAsString();
            player = playerRepository.findBySteamId(steamId);
        } else {
            String name = nameOption.getAsString();
            player = playerRepository.findByName(name);
        }
        
        if (player == null) {
            event.getHook().editOriginalEmbeds(
                EmbedUtils.createErrorEmbed("Player Not Found", "No player found with the provided information.")
            ).queue();
            return;
        }
        
        displayPlayerStats(event, player);
    }
    
    private void displayPlayerStats(SlashCommandInteractionEvent event, Player player) {
        try {
            // Calculate K/D ratio to show clean value
            double kdr = player.getKdr();
            if (Double.isNaN(kdr) || Double.isInfinite(kdr)) {
                kdr = player.getKillCount(); // If no deaths, show kills as KDR
            }
            
            // Format time played
            int totalSeconds = player.getTotalPlaytime();
            int hours = totalSeconds / 3600;
            int minutes = (totalSeconds % 3600) / 60;
            
            // Build the embed
            EmbedBuilder embed = new EmbedBuilder()
                .setTitle("Player Statistics: " + player.getDisplayName())
                .setDescription("Steam ID: " + player.getSteamId())
                .addField("Kills", String.valueOf(player.getKillCount()), true)
                .addField("Deaths", String.valueOf(player.getDeathCount()), true)
                .addField("K/D Ratio", String.format("%.2f", kdr), true)
                .addField("Time Played", hours + "h " + minutes + "m", true)
                .addField("Coins", String.valueOf(player.getCoins()), true)
                .setColor(0x2ecc71)
                .setTimestamp(Instant.now());
            
            // Add weapon stats if available
            if (!player.getWeaponStats().isEmpty()) {
                StringBuilder weaponStatsText = new StringBuilder();
                player.getWeaponStats().entrySet().stream()
                    .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
                    .limit(5) // Top 5 weapons
                    .forEach(entry -> 
                        weaponStatsText.append(entry.getKey()).append(": ")
                                      .append(entry.getValue()).append(" kills\n")
                    );
                
                if (weaponStatsText.length() > 0) {
                    embed.addField("Top Weapons", weaponStatsText.toString(), false);
                }
            }
            
            // Set footer with last seen info
            if (player.getLastSeen() > 0) {
                String lastSeenDate = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                    .format(new java.util.Date(player.getLastSeen()));
                embed.setFooter("Last seen: " + lastSeenDate);
            }
            
            event.getHook().editOriginalEmbeds(embed.build()).queue();
        } catch (Exception e) {
            logger.error("Error displaying player stats for {}", player.getDisplayName(), e);
            event.getHook().editOriginalEmbeds(
                EmbedUtils.createErrorEmbed("Error", "Failed to retrieve player statistics.")
            ).queue();
        }
    }

    @Override
    public CommandData getCommandData() {
        return Commands.slash("player", "View player statistics")
            .addOptions(
                new OptionData(OptionType.STRING, "steam_id", "Player's Steam ID", false),
                new OptionData(OptionType.STRING, "name", "Player's in-game name", false)
            );
    }
    
    @Override
    public List<String> getAliases() {
        List<String> aliases = new ArrayList<>();
        aliases.add("player-stats");
        aliases.add("playerstats");
        return aliases;
    }
    
    @Override
    public boolean isGuildOnly() {
        return false;
    }
    
    @Override
    public boolean isAdminOnly() {
        return false;
    }
    
    @Override
    public String getDescription() {
        return "View statistics for a player";
    }
}