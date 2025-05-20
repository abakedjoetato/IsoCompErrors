package com.deadside.bot.commands.admin;

import com.deadside.bot.commands.ICommand;
import com.deadside.bot.db.MongoDBConnection;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.bson.Document;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

/**
 * Admin command to reset/clear the database
 */
public class DatabaseResetCommand implements ICommand {
    private static final Logger logger = Logger.getLogger(DatabaseResetCommand.class.getName());
    private static final List<String> COLLECTIONS = Arrays.asList("players", "servers", "economy_logs");

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        // Check if user has administrator permission
        if (!event.getMember().hasPermission(Permission.ADMINISTRATOR)) {
            event.reply("You need administrator permissions to use this command.").setEphemeral(true).queue();
            return;
        }
        
        String collection = event.getOption("collection") != null ? 
                event.getOption("collection").getAsString() : "all";
        
        boolean confirm = event.getOption("confirm") != null && 
                event.getOption("confirm").getAsBoolean();
        
        if (!confirm) {
            event.reply("⚠️ This command will delete data from the database. " +
                    "Please run the command again with confirm=true to proceed.")
                    .setEphemeral(true).queue();
            return;
        }
        
        try {
            MongoDatabase db = MongoDBConnection.getDatabase();
            
            if ("all".equalsIgnoreCase(collection)) {
                // Reset all collections
                for (String collName : COLLECTIONS) {
                    MongoCollection<Document> coll = db.getCollection(collName);
                    coll.deleteMany(new Document());
                    logger.info("Reset collection: " + collName);
                }
                event.reply("Successfully reset all collections in the database.").setEphemeral(true).queue();
            } else if (COLLECTIONS.contains(collection.toLowerCase())) {
                // Reset specific collection
                MongoCollection<Document> coll = db.getCollection(collection);
                coll.deleteMany(new Document());
                logger.info("Reset collection: " + collection);
                event.reply("Successfully reset the " + collection + " collection.").setEphemeral(true).queue();
            } else {
                event.reply("Invalid collection name. Valid options are: " + String.join(", ", COLLECTIONS) + ", or 'all'.")
                        .setEphemeral(true).queue();
            }
        } catch (Exception e) {
            logger.severe("Error resetting database: " + e.getMessage());
            event.reply("An error occurred while resetting the database: " + e.getMessage())
                    .setEphemeral(true).queue();
        }
    }

    @Override
    public String getName() {
        return "dbreset";
    }

    @Override
    public String getDescription() {
        return "Reset/clear database collections (admin only)";
    }

    @Override
    public CommandData getCommandData() {
        return Commands.slash(getName(), getDescription())
                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR))
                .addOptions(
                        new OptionData(OptionType.STRING, "collection", "Collection to reset ('all' for all collections)", true)
                                .addChoice("All Collections", "all")
                                .addChoice("Players", "players")
                                .addChoice("Servers", "servers")
                                .addChoice("Economy Logs", "economy_logs"),
                        new OptionData(OptionType.BOOLEAN, "confirm", "Confirm the reset operation", true)
                );
    }
}