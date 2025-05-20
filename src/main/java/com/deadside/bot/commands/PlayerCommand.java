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
 * Command for viewing player information
 */
public class PlayerCommand implements ICommand {
    private static final Logger logger = Logger.getLogger(PlayerCommand.class.getName());
    private final PlayerRepository playerRepository;

    public PlayerCommand() {
        this.playerRepository = new PlayerRepository();
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        String playerName = event.getOption("name") != null ? event.getOption("name").getAsString() : null;
        String guildId = event.getGuild().getId();
        
        if (playerName == null || playerName.isEmpty()) {
            event.reply("Please provide a valid player name").setEphemeral(true).queue();
            return;
        }
        
        // First try exact match
        Player player = playerRepository.findByNameAndGuildId(playerName, guildId);
        
        // If no exact match, try partial match
        if (player == null) {
            List<Player> matchingPlayers = playerRepository.searchByPartialName(playerName, guildId);
            
            if (matchingPlayers.isEmpty()) {
                event.reply("No player found with name containing: " + playerName).setEphemeral(true).queue();
                return;
            } else if (matchingPlayers.size() > 1) {
                // If multiple matches, show list of possible players
                StringBuilder playerList = new StringBuilder("Multiple players found. Please be more specific:\n");
                for (Player p : matchingPlayers) {
                    playerList.append("- ").append(p.getName()).append("\n");
                }
                event.reply(playerList.toString()).setEphemeral(true).queue();
                return;
            } else {
                // If only one match, use that player
                player = matchingPlayers.get(0);
            }
        }
        
        // Display player information in an embed
        EmbedBuilder embed = new EmbedBuilder()
                .setTitle("Player: " + player.getName())
                .setColor(Color.GREEN)
                .addField("Kills", String.valueOf(player.getKills()), true)
                .addField("Deaths", String.valueOf(player.getDeaths()), true)
                .addField("K/D Ratio", String.format("%.2f", player.getKdRatio()), true)
                .addField("Playtime", formatPlaytime(player.getPlaytimeMinutes()), false)
                .addField("Currency", String.valueOf(player.getCurrency()), true);
        
        event.replyEmbeds(embed.build()).queue();
    }
    
    private String formatPlaytime(int minutes) {
        int hours = minutes / 60;
        int remainingMinutes = minutes % 60;
        return hours + "h " + remainingMinutes + "m";
    }

    @Override
    public String getName() {
        return "player";
    }

    @Override
    public String getDescription() {
        return "View information about a player";
    }

    @Override
    public CommandData getCommandData() {
        return Commands.slash(getName(), getDescription())
                .addOptions(
                        new OptionData(OptionType.STRING, "name", "Player name to search for", true)
                );
    }
}