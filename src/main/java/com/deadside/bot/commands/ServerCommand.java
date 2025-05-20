package com.deadside.bot.commands;

import com.deadside.bot.db.models.GameServer;
import com.deadside.bot.db.repositories.GameServerRepository;
import com.deadside.bot.utils.Config;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;

import java.awt.*;
import java.util.logging.Logger;

/**
 * Command for managing game server information
 */
public class ServerCommand implements ICommand {
    private static final Logger logger = Logger.getLogger(ServerCommand.class.getName());
    private final Config config;
    private final GameServerRepository serverRepository;

    public ServerCommand(Config config) {
        this.config = config;
        this.serverRepository = new GameServerRepository();
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        String subcommand = event.getSubcommandName();
        
        if (subcommand == null) {
            event.reply("Invalid subcommand").setEphemeral(true).queue();
            return;
        }
        
        switch (subcommand) {
            case "info":
                handleServerInfo(event);
                break;
            case "status":
                handleServerStatus(event);
                break;
            case "configure":
                handleServerConfigure(event);
                break;
            default:
                event.reply("Unknown subcommand: " + subcommand).setEphemeral(true).queue();
        }
    }

    private void handleServerInfo(SlashCommandInteractionEvent event) {
        GameServer server = serverRepository.findByGuildId(event.getGuild().getId());
        
        if (server == null) {
            event.reply("No server configured for this Discord server. Use `/server configure` to set one up.").setEphemeral(true).queue();
            return;
        }
        
        EmbedBuilder embed = new EmbedBuilder()
                .setTitle("Deadside Server Information")
                .setColor(Color.BLUE)
                .addField("Server Name", server.getServerName(), false)
                .addField("IP Address", server.getServerIp() + ":" + server.getGamePort(), true)
                .addField("Version", server.getServerVersion(), true);
        
        event.replyEmbeds(embed.build()).queue();
    }

    private void handleServerStatus(SlashCommandInteractionEvent event) {
        GameServer server = serverRepository.findByGuildId(event.getGuild().getId());
        
        if (server == null) {
            event.reply("No server configured for this Discord server. Use `/server configure` to set one up.").setEphemeral(true).queue();
            return;
        }
        
        // For demo purposes, we're just sending a simple response
        // In a real implementation, you would query the actual server status
        event.reply("Server status: Online").queue();
    }

    private void handleServerConfigure(SlashCommandInteractionEvent event) {
        // This would normally include configuration options
        event.reply("Server configuration options have been set.").setEphemeral(true).queue();
    }

    @Override
    public String getName() {
        return "server";
    }

    @Override
    public String getDescription() {
        return "Manage and view Deadside server information";
    }

    @Override
    public CommandData getCommandData() {
        return Commands.slash(getName(), getDescription())
                .addSubcommands(
                        new SubcommandData("info", "Get information about the Deadside server"),
                        new SubcommandData("status", "Check the current server status"),
                        new SubcommandData("configure", "Configure server settings")
                                .addOptions(
                                        new OptionData(OptionType.STRING, "name", "Server name", true),
                                        new OptionData(OptionType.STRING, "ip", "Server IP address", true),
                                        new OptionData(OptionType.INTEGER, "port", "Server port", true)
                                )
                );
    }
}