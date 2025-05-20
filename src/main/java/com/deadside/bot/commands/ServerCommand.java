package com.deadside.bot.commands;

import com.deadside.bot.db.models.GameServer;
import com.deadside.bot.db.repositories.GameServerRepository;
import com.deadside.bot.sftp.SftpConnector;
import com.deadside.bot.utils.EmbedUtils;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Command for server-related operations
 */
public class ServerCommand implements ICommand {
    private static final Logger logger = LoggerFactory.getLogger(ServerCommand.class);
    private final GameServerRepository serverRepository;
    private final SftpConnector sftpConnector;

    public ServerCommand() {
        this.serverRepository = new GameServerRepository();
        this.sftpConnector = new SftpConnector();
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        String subCommand = event.getSubcommandName();
        
        if (subCommand == null) {
            event.reply("Invalid server command").setEphemeral(true).queue();
            return;
        }
        
        if ("list".equals(subCommand)) {
            handleListServers(event);
        } else if ("add".equals(subCommand)) {
            handleAddServer(event);
        } else if ("test".equals(subCommand)) {
            handleTestServer(event);
        } else if ("reload".equals(subCommand)) {
            handleReloadServer(event);
        } else if ("remove".equals(subCommand)) {
            handleRemoveServer(event);
        } else {
            event.reply("Unknown server subcommand: " + subCommand).setEphemeral(true).queue();
        }
    }

    private void handleListServers(SlashCommandInteractionEvent event) {
        Guild guild = event.getGuild();
        if (guild == null) {
            event.reply("This command must be used in a server").setEphemeral(true).queue();
            return;
        }
        
        List<GameServer> servers = serverRepository.findByGuildId(guild.getIdLong());
        if (servers.isEmpty()) {
            event.replyEmbeds(
                EmbedUtils.createInfoEmbed("Server List", 
                    "No servers configured for this Discord server")
            ).setEphemeral(true).queue();
            return;
        }
        
        StringBuilder description = new StringBuilder();
        for (GameServer server : servers) {
            description.append("**").append(server.getName()).append("**\n");
            description.append("ID: ").append(server.getId()).append("\n");
            description.append("Status: ").append(server.isActive() ? "Active" : "Inactive").append("\n\n");
        }
        
        event.replyEmbeds(
            EmbedUtils.createInfoEmbed("Server List", description.toString())
        ).setEphemeral(true).queue();
    }

    private void handleAddServer(SlashCommandInteractionEvent event) {
        // For Phase 0, just acknowledge the command
        event.reply("The add server command will be implemented in a future phase.").setEphemeral(true).queue();
    }

    private void handleTestServer(SlashCommandInteractionEvent event) {
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
        
        event.deferReply().setEphemeral(true).queue();
        
        boolean testResult = sftpConnector.testConnection(server);
        
        if (testResult) {
            event.getHook().editOriginalEmbeds(
                EmbedUtils.createSuccessEmbed("Connection Test", 
                    "Successfully connected to server: " + server.getName())
            ).queue();
        } else {
            event.getHook().editOriginalEmbeds(
                EmbedUtils.createErrorEmbed("Connection Test Failed", 
                    "Could not connect to server: " + server.getName() + 
                    "\nPlease check the server credentials and try again.")
            ).queue();
        }
    }

    private void handleReloadServer(SlashCommandInteractionEvent event) {
        // For Phase 0, just acknowledge the command
        event.reply("The reload server command will be implemented in a future phase.").setEphemeral(true).queue();
    }

    private void handleRemoveServer(SlashCommandInteractionEvent event) {
        // For Phase 0, just acknowledge the command
        event.reply("The remove server command will be implemented in a future phase.").setEphemeral(true).queue();
    }

    @Override
    public CommandData getCommandData() {
        // Build the command with subcommands
        return Commands.slash("server", "Manage game servers")
            .addSubcommands(
                new SubcommandData("list", "List all configured servers"),
                new SubcommandData("add", "Add a new server")
                    .addOption(OptionType.STRING, "name", "Server name", true)
                    .addOption(OptionType.STRING, "ip", "Server IP address", true)
                    .addOption(OptionType.INTEGER, "port", "Game server port", true)
                    .addOption(OptionType.STRING, "ftp_host", "FTP host", true)
                    .addOption(OptionType.INTEGER, "ftp_port", "FTP port", false)
                    .addOption(OptionType.STRING, "ftp_user", "FTP username", true)
                    .addOption(OptionType.STRING, "ftp_pass", "FTP password", true)
                    .addOption(OptionType.STRING, "log_path", "Path to server logs", true),
                new SubcommandData("test", "Test server connection")
                    .addOptions(
                        new OptionData(OptionType.STRING, "server_id", "Server ID", true)
                    ),
                new SubcommandData("reload", "Reload server information")
                    .addOptions(
                        new OptionData(OptionType.STRING, "server_id", "Server ID", true)
                    ),
                new SubcommandData("remove", "Remove a server")
                    .addOptions(
                        new OptionData(OptionType.STRING, "server_id", "Server ID", true)
                    )
            );
    }
    
    @Override
    public List<String> getAliases() {
        List<String> aliases = new ArrayList<>();
        aliases.add("servers");
        return aliases;
    }
    
    @Override
    public boolean isGuildOnly() {
        return true;
    }
    
    @Override
    public boolean isAdminOnly() {
        return true;
    }
    
    @Override
    public String getDescription() {
        return "Manage game servers";
    }
}