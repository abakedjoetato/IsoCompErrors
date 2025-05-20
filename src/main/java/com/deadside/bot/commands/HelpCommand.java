package com.deadside.bot.commands;

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
 * Command for displaying help information
 */
public class HelpCommand implements ICommand {
    private static final Logger logger = Logger.getLogger(HelpCommand.class.getName());
    private final CommandManager commandManager;

    public HelpCommand(CommandManager commandManager) {
        this.commandManager = commandManager;
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        String commandName = event.getOption("command") != null ? 
                             event.getOption("command").getAsString() : null;
        
        if (commandName != null && !commandName.isEmpty()) {
            // Display help for a specific command
            ICommand command = commandManager.getCommandByName(commandName);
            
            if (command == null) {
                event.reply("Command not found: " + commandName).setEphemeral(true).queue();
                return;
            }
            
            EmbedBuilder embed = new EmbedBuilder()
                    .setTitle("Help: /" + command.getName())
                    .setDescription(command.getDescription())
                    .setColor(Color.BLUE);
            
            event.replyEmbeds(embed.build()).queue();
        } else {
            // Display general help with list of all commands
            List<ICommand> commands = commandManager.getAllCommands();
            
            EmbedBuilder embed = new EmbedBuilder()
                    .setTitle("Deadside Discord Bot - Command Help")
                    .setDescription("Here are all the available commands:")
                    .setColor(Color.BLUE);
            
            for (ICommand command : commands) {
                embed.addField("/" + command.getName(), command.getDescription(), false);
            }
            
            embed.setFooter("Use /help <command> for more information about a specific command");
            
            event.replyEmbeds(embed.build()).queue();
        }
    }

    @Override
    public String getName() {
        return "help";
    }

    @Override
    public String getDescription() {
        return "Get help with bot commands";
    }

    @Override
    public CommandData getCommandData() {
        return Commands.slash(getName(), getDescription())
                .addOptions(
                        new OptionData(OptionType.STRING, "command", "Command name to get help for", false)
                );
    }
}