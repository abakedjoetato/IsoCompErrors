package com.deadside.bot.commands.player;

import com.deadside.bot.commands.ICommand;
import com.deadside.bot.db.models.Player;
import com.deadside.bot.db.repositories.PlayerRepository;
import com.deadside.bot.utils.EmbedThemes;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.Command.Choice;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Command for viewing player statistics
 */
public class PlayerStatsCommand implements ICommand {
    private static final Logger logger = LoggerFactory.getLogger(PlayerStatsCommand.class);
    private final PlayerRepository playerRepository = new PlayerRepository();
    
    @Override
    public String getName() {
        return "playerstats";
    }
    
    @Override
    public CommandData getCommandData() {
        return Commands.slash(getName(), "View player statistics")
                .addOption(OptionType.STRING, "playername", "The name of the player", true, true);
    }
    
    @Override
    public void execute(SlashCommandInteractionEvent event) {
        String playerName = event.getOption("playername", "", OptionMapping::getAsString);
        
        Guild guild = event.getGuild();
        if (guild == null) {
            event.reply("This command can only be used in a server.").setEphemeral(true).queue();
            return;
        }
        
        long guildId = guild.getIdLong();
        
        event.deferReply().queue();
        
        try {
            Player player = playerRepository.findByNameAndGuildId(playerName, guildId);
            
            if (player == null) {
                event.getHook().sendMessageEmbeds(
                    EmbedThemes.errorEmbed("Player Not Found", 
                        "No player found with name: " + playerName)
                ).queue();
                return;
            }
            
            // Build player stats embed
            event.getHook().sendMessageEmbeds(
                EmbedThemes.infoEmbed("Player Stats: " + player.getName(),
                    "**Kills:** " + player.getKills() + "\n" +
                    "**Deaths:** " + player.getDeaths() + "\n" +
                    "**K/D Ratio:** " + formatKD(player.getKills(), player.getDeaths()) + "\n" +
                    "**Playtime:** " + formatPlaytime(player.getPlaytimeMinutes()) + "\n" +
                    "**Last Seen:** " + (player.getLastSeen() > 0 ? player.getLastSeen() : "Unknown"))
            ).queue();
            
            logger.info("Retrieved player stats for {}", playerName);
        } catch (Exception e) {
            logger.error("Error retrieving player stats for {}: {}", playerName, e.getMessage(), e);
            event.getHook().sendMessageEmbeds(
                EmbedThemes.errorEmbed("Error", "Error retrieving player stats: " + e.getMessage())
            ).queue();
        }
    }
    
    @Override
    public List<Choice> handleAutoComplete(CommandAutoCompleteInteractionEvent event) {
        if (event.getFocusedOption().getName().equals("playername")) {
            Guild guild = event.getGuild();
            if (guild == null) {
                return List.of();
            }
            
            String current = event.getFocusedOption().getValue().toLowerCase();
            return playerRepository.searchByPartialName(current, guild.getIdLong(), 25).stream()
                    .map(player -> new Choice(player.getName(), player.getName()))
                    .collect(Collectors.toList());
        }
        return List.of();
    }
    
    private String formatKD(int kills, int deaths) {
        if (deaths == 0) {
            return kills > 0 ? String.valueOf(kills) : "0";
        }
        return String.format("%.2f", (float) kills / deaths);
    }
    
    private String formatPlaytime(long minutesPlayed) {
        if (minutesPlayed < 60) {
            return minutesPlayed + " minutes";
        }
        
        long hours = minutesPlayed / 60;
        long minutes = minutesPlayed % 60;
        
        if (hours < 24) {
            return String.format("%d hours, %d minutes", hours, minutes);
        }
        
        long days = hours / 24;
        hours = hours % 24;
        
        return String.format("%d days, %d hours, %d minutes", days, hours, minutes);
    }
}