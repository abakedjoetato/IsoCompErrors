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

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Command for displaying top players leaderboard
 */
public class TopPlayersCommand implements ICommand {
    private static final Logger logger = LoggerFactory.getLogger(TopPlayersCommand.class);
    private final PlayerRepository playerRepository = new PlayerRepository();
    
    @Override
    public String getName() {
        return "topplayers";
    }
    
    @Override
    public CommandData getCommandData() {
        return Commands.slash(getName(), "View top players leaderboard")
                .addOption(OptionType.STRING, "category", "The category to rank by", true)
                .addOption(OptionType.INTEGER, "limit", "Number of players to show (max 25)", false);
    }
    
    @Override
    public void execute(SlashCommandInteractionEvent event) {
        String category = event.getOption("category", "kills", OptionMapping::getAsString);
        int limit = event.getOption("limit", 10, OptionMapping::getAsInt);
        
        if (limit > 25) limit = 25; // Cap at 25 entries
        if (limit < 1) limit = 10;  // Minimum 1 entry
        
        Guild guild = event.getGuild();
        if (guild == null) {
            event.reply("This command can only be used in a server.").setEphemeral(true).queue();
            return;
        }
        
        long guildId = guild.getIdLong();
        
        event.deferReply().queue();
        
        try {
            List<Player> players;
            String title;
            
            switch (category.toLowerCase()) {
                case "kills":
                    players = playerRepository.findTopPlayersByKills(guildId, limit);
                    title = "Top Players by Kills";
                    break;
                case "kd":
                case "kdratio":
                    players = playerRepository.findTopPlayersByKDRatio(guildId, limit);
                    title = "Top Players by K/D Ratio";
                    break;
                case "playtime":
                    players = playerRepository.findTopPlayersByPlaytime(guildId, limit);
                    title = "Top Players by Playtime";
                    break;
                default:
                    players = playerRepository.findTopPlayersByKills(guildId, limit);
                    title = "Top Players by Kills";
                    break;
            }
            
            if (players.isEmpty()) {
                event.getHook().sendMessageEmbeds(
                    EmbedThemes.infoEmbed("No Player Data", 
                        "No player statistics available yet.")
                ).queue();
                return;
            }
            
            StringBuilder sb = new StringBuilder();
            int rank = 1;
            
            for (Player player : players) {
                switch (category.toLowerCase()) {
                    case "kills":
                        sb.append(String.format("%d. **%s** - %d kills\n", 
                                rank++, player.getName(), player.getKills()));
                        break;
                    case "kd":
                    case "kdratio":
                        float kd = player.getDeaths() > 0 ? 
                                (float) player.getKills() / player.getDeaths() : player.getKills();
                        sb.append(String.format("%d. **%s** - %.2f K/D\n", 
                                rank++, player.getName(), kd));
                        break;
                    case "playtime":
                        sb.append(String.format("%d. **%s** - %s\n", 
                                rank++, player.getName(), formatPlaytime(player.getPlaytimeMinutes())));
                        break;
                    default:
                        sb.append(String.format("%d. **%s** - %d kills\n", 
                                rank++, player.getName(), player.getKills()));
                        break;
                }
            }
            
            event.getHook().sendMessageEmbeds(
                EmbedThemes.infoEmbed(title, sb.toString())
            ).queue();
            
            logger.info("Retrieved top players for category: {}", category);
        } catch (Exception e) {
            logger.error("Error retrieving top players for category {}: {}", category, e.getMessage(), e);
            event.getHook().sendMessageEmbeds(
                EmbedThemes.errorEmbed("Error", "Error retrieving top players: " + e.getMessage())
            ).queue();
        }
    }
    
    @Override
    public List<Choice> handleAutoComplete(CommandAutoCompleteInteractionEvent event) {
        if (event.getFocusedOption().getName().equals("category")) {
            String current = event.getFocusedOption().getValue().toLowerCase();
            List<String> categories = Arrays.asList("kills", "kdratio", "playtime");
            
            return categories.stream()
                    .filter(cat -> cat.startsWith(current))
                    .map(cat -> new Choice(cat, cat))
                    .collect(Collectors.toList());
        }
        return List.of();
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
        
        return String.format("%d days, %d hours", days, hours);
    }
}