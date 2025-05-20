package com.deadside.bot.commands;

import com.deadside.bot.db.models.Player;
import com.deadside.bot.db.repositories.PlayerRepository;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.awt.*;
import java.util.List;
import java.util.logging.Logger;

/**
 * Command for viewing top players by kills
 */
public class TopPlayersCommand implements ICommand {
    private static final Logger logger = Logger.getLogger(TopPlayersCommand.class.getName());
    private final PlayerRepository playerRepository;
    private static final int DEFAULT_LIMIT = 10;

    public TopPlayersCommand() {
        this.playerRepository = new PlayerRepository();
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        int limit = event.getOption("limit") != null ? 
                    (int) Math.min(event.getOption("limit").getAsLong(), 20) : DEFAULT_LIMIT;
        String guildId = event.getGuild().getId();
        
        try {
            // Convert guildId to long for repository method
            long guildIdLong = Long.parseLong(guildId);
            
            // Get top players by kills for this guild
            List<Player> topPlayers = playerRepository.findTopPlayersByKills(guildIdLong, limit);
            
            if (topPlayers.isEmpty()) {
                event.reply("No player statistics found for this server.").queue();
                return;
            }
            
            // Build the top players embed
            EmbedBuilder embed = new EmbedBuilder()
                    .setTitle("Top " + limit + " Players - By Kills")
                    .setColor(Color.RED);
            
            // Add top players to the embed
            StringBuilder playerList = new StringBuilder();
            int rank = 1;
            
            for (Player player : topPlayers) {
                playerList.append(rank).append(". **").append(player.getName()).append("**")
                        .append(" | K: ").append(player.getKills())
                        .append(" | D: ").append(player.getDeaths())
                        .append(" | K/D: ").append(String.format("%.2f", player.getKdRatio()))
                        .append("\n");
                rank++;
            }
            
            embed.setDescription(playerList.toString());
            event.replyEmbeds(embed.build()).queue();
            
        } catch (NumberFormatException e) {
            logger.warning("Error parsing guild ID: " + guildId);
            event.reply("An error occurred while retrieving top players.").setEphemeral(true).queue();
        } catch (Exception e) {
            logger.warning("Error retrieving top players: " + e.getMessage());
            event.reply("An error occurred while retrieving top players.").setEphemeral(true).queue();
        }
    }

    @Override
    public String getName() {
        return "topplayers";
    }

    @Override
    public String getDescription() {
        return "View the top players by kills";
    }

    @Override
    public CommandData getCommandData() {
        return Commands.slash(getName(), getDescription())
                .addOptions(
                        new OptionData(OptionType.INTEGER, "limit", "Number of players to show (max 20)", false)
                                .setMinValue(1)
                                .setMaxValue(20)
                );
    }
}