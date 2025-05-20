package com.deadside.bot.commands.admin;

import com.deadside.bot.commands.ICommand;
import com.deadside.bot.db.MongoDBConnection;
import com.deadside.bot.db.models.GameServer;
import com.deadside.bot.db.repositories.GameServerRepository;
import com.deadside.bot.db.repositories.PlayerRepository;
import com.deadside.bot.utils.EmbedUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.util.ArrayList;
import java.util.List;

/**
 * Command for displaying statistics about the bot and game servers
 */
public class StatsCommand implements ICommand {
    private static final Logger logger = LoggerFactory.getLogger(StatsCommand.class);
    private final GameServerRepository serverRepository;
    private final PlayerRepository playerRepository;

    public StatsCommand() {
        this.serverRepository = new GameServerRepository();
        this.playerRepository = new PlayerRepository();
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        String subCommand = event.getSubcommandName();
        
        if (subCommand == null) {
            event.reply("Invalid stats command").setEphemeral(true).queue();
            return;
        }
        
        switch (subCommand) {
            case "bot":
                handleBotStats(event);
                break;
            case "server":
                handleServerStats(event);
                break;
            case "player":
                handlePlayerStats(event);
                break;
            default:
                event.reply("Unknown stats subcommand: " + subCommand).setEphemeral(true).queue();
                break;
        }
    }

    private void handleBotStats(SlashCommandInteractionEvent event) {
        JDA jda = event.getJDA();
        event.deferReply().queue();
        
        try {
            MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
            MemoryUsage heapMemoryUsage = memoryBean.getHeapMemoryUsage();
            
            long usedMemory = heapMemoryUsage.getUsed() / (1024 * 1024);
            long maxMemory = heapMemoryUsage.getMax() / (1024 * 1024);
            
            long uptime = ManagementFactory.getRuntimeMXBean().getUptime();
            long days = uptime / (1000 * 60 * 60 * 24);
            long hours = (uptime / (1000 * 60 * 60)) % 24;
            long minutes = (uptime / (1000 * 60)) % 60;
            long seconds = (uptime / 1000) % 60;
            
            String uptimeString = String.format("%d days, %d hours, %d minutes, %d seconds", 
                    days, hours, minutes, seconds);
            
            // Count some database statistics
            long serverCount = serverRepository.countAll();
            
            EmbedBuilder embedBuilder = new EmbedBuilder()
                    .setTitle("DeadsideBot Statistics")
                    .setDescription("Overall bot statistics and health metrics")
                    .addField("Uptime", uptimeString, false)
                    .addField("Memory Usage", usedMemory + "MB / " + maxMemory + "MB", false)
                    .addField("Servers Connected", String.valueOf(jda.getGuilds().size()), true)
                    .addField("Game Servers Tracked", String.valueOf(serverCount), true)
                    .addField("Library Version", "JDA " + JDA.class.getPackage().getImplementationVersion(), true)
                    .addField("Database Status", MongoDBConnection.isInitialized() ? "Connected" : "Disconnected", true)
                    .setColor(0x3498db)
                    .setTimestamp(java.time.Instant.now());
            
            event.getHook().editOriginalEmbeds(embedBuilder.build()).queue();
        } catch (Exception e) {
            logger.error("Error retrieving bot stats", e);
            event.getHook().editOriginalEmbeds(
                EmbedUtils.createErrorEmbed("Error", "Failed to retrieve bot statistics")
            ).queue();
        }
    }

    private void handleServerStats(SlashCommandInteractionEvent event) {
        String serverId = event.getOption("server_id") != null ? event.getOption("server_id").getAsString() : null;
        
        if (serverId == null) {
            event.reply("Please specify a server ID").setEphemeral(true).queue();
            return;
        }
        
        GameServer server = serverRepository.findById(serverId);
        if (server == null) {
            event.reply("Server not found with ID: " + serverId).setEphemeral(true).queue();
            return;
        }
        
        event.deferReply().queue();
        
        try {
            EmbedBuilder embedBuilder = new EmbedBuilder()
                    .setTitle("Server Statistics: " + server.getName())
                    .addField("Status", server.isOnline() ? "Online" : "Offline", true)
                    .addField("Players", server.getPlayerCount() + "/" + server.getMaxPlayers(), true)
                    .addField("IP:Port", server.getServerIp() + ":" + server.getGamePort(), true)
                    .addField("Version", server.getServerVersion() != null ? server.getServerVersion() : "Unknown", true)
                    .setColor(server.isOnline() ? 0x2ecc71 : 0xe74c3c)
                    .setTimestamp(java.time.Instant.now());
            
            event.getHook().editOriginalEmbeds(embedBuilder.build()).queue();
        } catch (Exception e) {
            logger.error("Error retrieving server stats for {}", serverId, e);
            event.getHook().editOriginalEmbeds(
                EmbedUtils.createErrorEmbed("Error", "Failed to retrieve server statistics")
            ).queue();
        }
    }

    private void handlePlayerStats(SlashCommandInteractionEvent event) {
        // For Phase 0, just acknowledge the command
        event.reply("Player stats command will be implemented in a future phase.").setEphemeral(true).queue();
    }

    @Override
    public CommandData getCommandData() {
        return Commands.slash("stats", "View statistics")
            .addSubcommands(
                new SubcommandData("bot", "View bot statistics"),
                new SubcommandData("server", "View server statistics")
                    .addOptions(
                        new OptionData(OptionType.STRING, "server_id", "Server ID", true)
                    ),
                new SubcommandData("player", "View player statistics")
                    .addOptions(
                        new OptionData(OptionType.STRING, "steam_id", "Player Steam ID", false),
                        new OptionData(OptionType.STRING, "name", "Player Name", false)
                    )
            );
    }
    
    @Override
    public List<String> getAliases() {
        List<String> aliases = new ArrayList<>();
        aliases.add("stat");
        aliases.add("statistics");
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
        return "View statistics about the bot, servers, or players";
    }
}