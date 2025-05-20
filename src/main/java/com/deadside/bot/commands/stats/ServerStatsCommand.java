package com.deadside.bot.commands.stats;

import com.deadside.bot.commands.ICommand;
import com.deadside.bot.db.models.GameServer;
import com.deadside.bot.db.repositories.GameServerRepository;
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
 * Command for viewing server statistics
 */
public class ServerStatsCommand implements ICommand {
    private static final Logger logger = LoggerFactory.getLogger(ServerStatsCommand.class);
    private final GameServerRepository serverRepository = new GameServerRepository();
    private final PlayerRepository playerRepository = new PlayerRepository();
    
    @Override
    public String getName() {
        return "serverstats";
    }
    
    @Override
    public CommandData getCommandData() {
        return Commands.slash(getName(), "View Deadside server statistics")
                .addOption(OptionType.STRING, "server", "The server to view stats for", true, true);
    }
    
    @Override
    public void execute(SlashCommandInteractionEvent event) {
        String serverName = event.getOption("server", "", OptionMapping::getAsString);
        
        Guild guild = event.getGuild();
        if (guild == null) {
            event.reply("This command can only be used in a server.").setEphemeral(true).queue();
            return;
        }
        
        long guildId = guild.getIdLong();
        
        event.deferReply().queue();
        
        try {
            GameServer server = serverRepository.findByNameAndGuildId(serverName, guildId);
            
            if (server == null) {
                event.getHook().sendMessageEmbeds(
                    EmbedThemes.errorEmbed("Server Not Found", 
                        "No server found with name: " + serverName)
                ).queue();
                return;
            }
            
            // Get player count, unique players, etc.
            int totalPlayers = playerRepository.countUniquePlayersByServerId(server.getServerId());
            int activePlayersLast24h = playerRepository.countActivePlayersInLastHours(server.getServerId(), 24);
            int totalKills = playerRepository.countTotalKillsByServerId(server.getServerId());
            int onlinePlayers = server.getCurrentPlayers();
            int maxPlayers = server.getMaxPlayers();
            
            // Build server stats embed
            StringBuilder stats = new StringBuilder();
            stats.append("**Players Online:** ").append(onlinePlayers).append("/").append(maxPlayers).append("\n");
            stats.append("**Total Unique Players:** ").append(totalPlayers).append("\n");
            stats.append("**Active Last 24h:** ").append(activePlayersLast24h).append("\n");
            stats.append("**Total Kills:** ").append(totalKills).append("\n");
            
            if (server.getServerVersion() != null && !server.getServerVersion().isEmpty()) {
                stats.append("**Server Version:** ").append(server.getServerVersion()).append("\n");
            }
            
            if (server.getServerIp() != null && !server.getServerIp().isEmpty() && 
                server.getGamePort() > 0) {
                stats.append("**Connection:** ").append(server.getServerIp())
                     .append(":").append(server.getGamePort()).append("\n");
            }
            
            // Status update
            stats.append("\n**Status:** ");
            if (server.isOnline()) {
                stats.append("✅ Online");
                if (server.getLastUpdated() > 0) {
                    stats.append("\n**Last Updated:** ").append(server.getLastUpdated());
                }
            } else {
                stats.append("❌ Offline");
                if (server.getLastUpdated() > 0) {
                    stats.append("\n**Last Seen:** ").append(server.getLastUpdated());
                }
            }
            
            event.getHook().sendMessageEmbeds(
                EmbedThemes.infoEmbed("Server Stats: " + server.getName(), stats.toString())
            ).queue();
            
            logger.info("Retrieved server stats for {}", serverName);
        } catch (Exception e) {
            logger.error("Error retrieving server stats for {}: {}", serverName, e.getMessage(), e);
            event.getHook().sendMessageEmbeds(
                EmbedThemes.errorEmbed("Error", "Error retrieving server stats: " + e.getMessage())
            ).queue();
        }
    }
    
    @Override
    public List<Choice> handleAutoComplete(CommandAutoCompleteInteractionEvent event) {
        if (event.getFocusedOption().getName().equals("server")) {
            Guild guild = event.getGuild();
            if (guild == null) {
                return List.of();
            }
            
            String current = event.getFocusedOption().getValue().toLowerCase();
            List<GameServer> servers = serverRepository.findAllByGuildId(guild.getIdLong());
            
            return servers.stream()
                    .filter(server -> server.getName().toLowerCase().contains(current))
                    .map(server -> new Choice(server.getName(), server.getName()))
                    .collect(Collectors.toList());
        }
        return List.of();
    }
}