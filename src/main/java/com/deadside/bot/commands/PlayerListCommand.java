package com.deadside.bot.commands;

import com.deadside.bot.db.models.Player;
import com.deadside.bot.db.repositories.PlayerRepository;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;

import java.awt.*;
import java.util.List;
import java.util.logging.Logger;

/**
 * Command for listing all players on the server
 */
public class PlayerListCommand implements ICommand {
    private static final Logger logger = Logger.getLogger(PlayerListCommand.class.getName());
    private final PlayerRepository playerRepository;
    private static final int PLAYERS_PER_PAGE = 10;

    public PlayerListCommand() {
        this.playerRepository = new PlayerRepository();
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        String guildId = event.getGuild().getId();
        
        // Get all players for this guild/server
        List<Player> players = playerRepository.findAllByGuildId(guildId);
        
        if (players.isEmpty()) {
            event.reply("No players found for this server.").queue();
            return;
        }
        
        // Build the player list embed
        EmbedBuilder embed = new EmbedBuilder()
                .setTitle("Player List - " + players.size() + " Players Total")
                .setColor(Color.BLUE);
        
        // Add up to PLAYERS_PER_PAGE players to the embed
        StringBuilder playerList = new StringBuilder();
        int count = 0;
        
        for (Player player : players) {
            if (count < PLAYERS_PER_PAGE) {
                playerList.append(player.getName())
                        .append(" | K: ").append(player.getKills())
                        .append(" | D: ").append(player.getDeaths())
                        .append("\n");
                count++;
            } else {
                break;
            }
        }
        
        embed.setDescription(playerList.toString());
        
        // Add note if there are more players
        if (players.size() > PLAYERS_PER_PAGE) {
            embed.setFooter("Showing " + PLAYERS_PER_PAGE + " of " + players.size() + " players. Use /topplayers for rankings.");
        }
        
        event.replyEmbeds(embed.build()).queue();
    }

    @Override
    public String getName() {
        return "playerlist";
    }

    @Override
    public String getDescription() {
        return "List all players on the server";
    }

    @Override
    public CommandData getCommandData() {
        return Commands.slash(getName(), getDescription());
    }
}