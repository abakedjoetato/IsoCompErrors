package com.deadside.bot.utils;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;

import java.awt.Color;
import java.time.Instant;
import java.util.List;

/**
 * Utility class for creating and formatting Discord embeds
 */
public class EmbedUtils {
    // Default color for embeds (Deadside theme color)
    private static final Color DEFAULT_COLOR = new Color(227, 66, 52);
    private static final Color SUCCESS_COLOR = new Color(46, 204, 113);
    private static final Color ERROR_COLOR = new Color(231, 76, 60);
    private static final Color INFO_COLOR = new Color(52, 152, 219);
    private static final Color WARNING_COLOR = new Color(241, 196, 15);
    
    /**
     * Create a default embed with the given title and description
     * 
     * @param title The title of the embed
     * @param description The description of the embed
     * @return The created embed
     */
    public static MessageEmbed createDefaultEmbed(String title, String description) {
        return new EmbedBuilder()
                .setTitle(title)
                .setDescription(description)
                .setColor(DEFAULT_COLOR)
                .setTimestamp(Instant.now())
                .build();
    }
    
    /**
     * Create a success embed with the given title and description
     * 
     * @param title The title of the embed
     * @param description The description of the embed
     * @return The created embed
     */
    public static MessageEmbed createSuccessEmbed(String title, String description) {
        return new EmbedBuilder()
                .setTitle(title)
                .setDescription(description)
                .setColor(SUCCESS_COLOR)
                .setTimestamp(Instant.now())
                .build();
    }
    
    /**
     * Create an error embed with the given title and description
     * 
     * @param title The title of the embed
     * @param description The description of the embed
     * @return The created embed
     */
    public static MessageEmbed createErrorEmbed(String title, String description) {
        return new EmbedBuilder()
                .setTitle(title)
                .setDescription(description)
                .setColor(ERROR_COLOR)
                .setTimestamp(Instant.now())
                .build();
    }
    
    /**
     * Create an info embed with the given title and description
     * 
     * @param title The title of the embed
     * @param description The description of the embed
     * @return The created embed
     */
    public static MessageEmbed createInfoEmbed(String title, String description) {
        return new EmbedBuilder()
                .setTitle(title)
                .setDescription(description)
                .setColor(INFO_COLOR)
                .setTimestamp(Instant.now())
                .build();
    }
    
    /**
     * Create a warning embed with the given title and description
     * 
     * @param title The title of the embed
     * @param description The description of the embed
     * @return The created embed
     */
    public static MessageEmbed createWarningEmbed(String title, String description) {
        return new EmbedBuilder()
                .setTitle(title)
                .setDescription(description)
                .setColor(WARNING_COLOR)
                .setTimestamp(Instant.now())
                .build();
    }
    
    /**
     * Create a player statistics embed
     * 
     * @param playerName The name of the player
     * @param stats A list of strings containing the player's statistics
     * @return The created embed
     */
    public static MessageEmbed createPlayerStatsEmbed(String playerName, List<String> stats) {
        EmbedBuilder builder = new EmbedBuilder()
                .setTitle("Statistics for " + playerName)
                .setColor(DEFAULT_COLOR)
                .setTimestamp(Instant.now());
        
        if (stats.isEmpty()) {
            builder.setDescription("No statistics found for this player.");
        } else {
            StringBuilder statsBuilder = new StringBuilder();
            for (String stat : stats) {
                statsBuilder.append(stat).append("\n");
            }
            builder.setDescription(statsBuilder.toString());
        }
        
        return builder.build();
    }
    
    /**
     * Create a server statistics embed
     * 
     * @param serverName The name of the server
     * @param stats A list of strings containing the server's statistics
     * @return The created embed
     */
    public static MessageEmbed createServerStatsEmbed(String serverName, List<String> stats) {
        EmbedBuilder builder = new EmbedBuilder()
                .setTitle("Statistics for " + serverName)
                .setColor(DEFAULT_COLOR)
                .setTimestamp(Instant.now());
        
        if (stats.isEmpty()) {
            builder.setDescription("No statistics found for this server.");
        } else {
            StringBuilder statsBuilder = new StringBuilder();
            for (String stat : stats) {
                statsBuilder.append(stat).append("\n");
            }
            builder.setDescription(statsBuilder.toString());
        }
        
        return builder.build();
    }
    
    /**
     * Set the footer of an embed builder with the user's name and avatar
     * 
     * @param builder The embed builder
     * @param user The user
     * @return The modified embed builder
     */
    public static EmbedBuilder setFooter(EmbedBuilder builder, User user) {
        return builder.setFooter("Requested by " + user.getName(), user.getEffectiveAvatarUrl());
    }
}