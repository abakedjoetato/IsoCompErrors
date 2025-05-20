package com.deadside.bot.commands.admin;

import com.deadside.bot.commands.CommandManager;
import com.deadside.bot.commands.ICommand;
import com.deadside.bot.utils.EmbedUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Command to display help information about available commands
 */
public class HelpCommand implements ICommand {
    private static final Logger logger = LoggerFactory.getLogger(HelpCommand.class);
    private final CommandManager commandManager;

    public HelpCommand() {
        // Create a temporary command manager for initialization
        // The real one will be injected later by the CommandManager
        this.commandManager = null;
    }

    public HelpCommand(CommandManager commandManager) {
        this.commandManager = commandManager;
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        if (commandManager == null) {
            event.reply("Help command is not properly initialized").setEphemeral(true).queue();
            return;
        }

        String commandName = null;
        if (event.getOption("command") != null) {
            commandName = event.getOption("command").getAsString();
        }

        if (commandName != null) {
            // Show help for specific command
            showCommandHelp(event, commandName);
        } else {
            // Show general help with all commands
            showGeneralHelp(event);
        }
    }

    private void showCommandHelp(SlashCommandInteractionEvent event, String commandName) {
        Map<String, ICommand> commands = commandManager.getCommands();
        ICommand command = commands.get(commandName.toLowerCase());

        if (command == null) {
            event.replyEmbeds(EmbedUtils.createErrorEmbed("Unknown Command", 
                "Command not found: " + commandName)).setEphemeral(true).queue();
            return;
        }

        CommandData commandData = command.getCommandData();
        if (commandData == null) {
            event.replyEmbeds(EmbedUtils.createErrorEmbed("Command Error", 
                "Command data not available for: " + commandName)).setEphemeral(true).queue();
            return;
        }

        // Build command help embed
        EmbedBuilder embed = new EmbedBuilder()
                .setTitle("Command Help: /" + commandData.getName())
                .setDescription(command.getDescription())
                .setColor(0x3498db)
                .setTimestamp(Instant.now());

        // Add command options if any
        if (!commandData.getOptions().isEmpty()) {
            StringBuilder optionsText = new StringBuilder();
            commandData.getOptions().forEach(option -> {
                optionsText.append("**").append(option.getName()).append("**");
                if (option.isRequired()) {
                    optionsText.append(" (Required)");
                }
                optionsText.append(": ").append(option.getDescription()).append("\n");
            });
            embed.addField("Options", optionsText.toString(), false);
        }

        // Add subcommands if any
        if (!commandData.getSubcommands().isEmpty()) {
            StringBuilder subcommandsText = new StringBuilder();
            commandData.getSubcommands().forEach(subcommand -> {
                subcommandsText.append("**").append(subcommand.getName()).append("**: ")
                        .append(subcommand.getDescription()).append("\n");
            });
            embed.addField("Subcommands", subcommandsText.toString(), false);
        }

        // Add additional information
        if (command.isAdminOnly()) {
            embed.addField("Permissions", "Admin Only", true);
        }
        if (command.isGuildOnly()) {
            embed.addField("Usage", "Server Only", true);
        }

        event.replyEmbeds(embed.build()).setEphemeral(true).queue();
    }

    private void showGeneralHelp(SlashCommandInteractionEvent event) {
        if (commandManager == null) {
            event.reply("Help system is not properly initialized").setEphemeral(true).queue();
            return;
        }

        Map<String, ICommand> commands = commandManager.getCommands();
        
        EmbedBuilder embed = new EmbedBuilder()
                .setTitle("DeadsideBot Help")
                .setDescription("Use /help [command] to get detailed information about a specific command.")
                .setColor(0x3498db)
                .setTimestamp(Instant.now());

        // Admin commands section
        StringBuilder adminCommandsText = new StringBuilder();
        commands.values().stream()
                .distinct()
                .filter(ICommand::isAdminOnly)
                .forEach(cmd -> {
                    adminCommandsText.append("**/").append(cmd.getCommandData().getName()).append("**");
                    adminCommandsText.append(": ").append(cmd.getDescription()).append("\n");
                });
        if (adminCommandsText.length() > 0) {
            embed.addField("Admin Commands", adminCommandsText.toString(), false);
        }

        // General commands section
        StringBuilder generalCommandsText = new StringBuilder();
        commands.values().stream()
                .distinct()
                .filter(cmd -> !cmd.isAdminOnly())
                .forEach(cmd -> {
                    generalCommandsText.append("**/").append(cmd.getCommandData().getName()).append("**");
                    generalCommandsText.append(": ").append(cmd.getDescription()).append("\n");
                });
        if (generalCommandsText.length() > 0) {
            embed.addField("General Commands", generalCommandsText.toString(), false);
        }

        event.replyEmbeds(embed.build()).setEphemeral(true).queue();
    }

    @Override
    public CommandData getCommandData() {
        return Commands.slash("help", "Get help with bot commands")
                .addOptions(
                        new OptionData(OptionType.STRING, "command", "Command name to get help with", false)
                );
    }

    @Override
    public List<String> getAliases() {
        List<String> aliases = new ArrayList<>();
        aliases.add("commands");
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
        return "Get help with bot commands and features";
    }
    
    public void setCommandManager(CommandManager commandManager) {
        // This method will be called by CommandManager to inject itself
        if (this.commandManager == null) {
            // Only set if not already set
            // Using reflection to set a private field is not ideal, but it's needed for the cyclic dependency
            try {
                java.lang.reflect.Field field = HelpCommand.class.getDeclaredField("commandManager");
                field.setAccessible(true);
                field.set(this, commandManager);
            } catch (Exception e) {
                logger.error("Failed to set command manager for help command", e);
            }
        }
    }
}