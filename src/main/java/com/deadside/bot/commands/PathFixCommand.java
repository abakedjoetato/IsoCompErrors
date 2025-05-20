package com.deadside.bot.commands;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.logging.Logger;

/**
 * Command for fixing file paths for Deadside server log files
 */
public class PathFixCommand implements ICommand {
    private static final Logger logger = Logger.getLogger(PathFixCommand.class.getName());

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        String path = event.getOption("path") != null ? event.getOption("path").getAsString() : null;
        
        if (path == null || path.isEmpty()) {
            event.reply("Please provide a valid path").setEphemeral(true).queue();
            return;
        }
        
        // Process the path fixing logic
        String fixedPath = fixPath(path);
        
        event.reply("Fixed path: " + fixedPath).queue();
    }
    
    private String fixPath(String path) {
        // Replace Windows-style backslashes with forward slashes
        path = path.replace('\\', '/');
        
        // Ensure path starts with a slash
        if (!path.startsWith("/")) {
            path = "/" + path;
        }
        
        // Remove any duplicate slashes
        while (path.contains("//")) {
            path = path.replace("//", "/");
        }
        
        return path;
    }

    @Override
    public String getName() {
        return "pathfix";
    }

    @Override
    public String getDescription() {
        return "Fix file paths for compatibility with Deadside server";
    }

    @Override
    public CommandData getCommandData() {
        return Commands.slash(getName(), getDescription())
                .addOptions(
                        new OptionData(OptionType.STRING, "path", "The path to fix", true)
                );
    }
}