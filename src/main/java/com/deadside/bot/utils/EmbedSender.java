package com.deadside.bot.utils;

import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * Utility class for sending embeds with proper error handling
 */
public class EmbedSender {
    private static final Logger logger = LoggerFactory.getLogger(EmbedSender.class);
    
    /**
     * Send an embed in response to a slash command
     * 
     * @param event The slash command event
     * @param embed The embed to send
     * @param ephemeral Whether the response should be ephemeral
     */
    public static void sendEmbed(SlashCommandInteractionEvent event, MessageEmbed embed, boolean ephemeral) {
        try {
            if (!event.isAcknowledged()) {
                event.replyEmbeds(embed).setEphemeral(ephemeral).queue(null, 
                    error -> handleError(error, "Failed to send embed response", event.getUser().getName()));
            } else {
                event.getHook().sendMessageEmbeds(embed).queue(null, 
                    error -> handleError(error, "Failed to send embed response", event.getUser().getName()));
            }
        } catch (Exception e) {
            logger.error("Error sending embed to {}", event.getUser().getName(), e);
        }
    }
    
    /**
     * Send an embed to a text channel
     * 
     * @param channel The channel to send the embed to
     * @param embed The embed to send
     */
    public static void sendEmbed(TextChannel channel, MessageEmbed embed) {
        try {
            channel.sendMessageEmbeds(embed).queue(null, 
                error -> handleError(error, "Failed to send embed to channel", channel.getName()));
        } catch (Exception e) {
            logger.error("Error sending embed to channel {}", channel.getName(), e);
        }
    }
    
    /**
     * Send an embed to a text channel and delete it after a delay
     * 
     * @param channel The channel to send the embed to
     * @param embed The embed to send
     * @param deleteAfter The time to wait before deleting
     * @param unit The time unit for the delay
     */
    public static void sendTemporaryEmbed(TextChannel channel, MessageEmbed embed, int deleteAfter, TimeUnit unit) {
        try {
            channel.sendMessageEmbeds(embed).queue(message -> 
                message.delete().queueAfter(deleteAfter, unit, null, 
                    error -> handleError(error, "Failed to delete temporary message", channel.getName())), 
                error -> handleError(error, "Failed to send temporary embed", channel.getName()));
        } catch (Exception e) {
            logger.error("Error sending temporary embed to channel {}", channel.getName(), e);
        }
    }
    
    /**
     * Send an embed and execute a callback after it is sent
     * 
     * @param channel The channel to send the embed to
     * @param embed The embed to send
     * @param callback The callback to execute
     */
    public static void sendEmbedWithCallback(TextChannel channel, MessageEmbed embed, Consumer<Void> callback) {
        try {
            channel.sendMessageEmbeds(embed).queue(message -> callback.accept(null), 
                error -> handleError(error, "Failed to send embed with callback", channel.getName()));
        } catch (Exception e) {
            logger.error("Error sending embed with callback to channel {}", channel.getName(), e);
        }
    }
    
    /**
     * Edit an existing message with a new embed
     * 
     * @param hook The interaction hook
     * @param embed The new embed
     */
    public static void editEmbed(InteractionHook hook, MessageEmbed embed) {
        try {
            hook.editOriginalEmbeds(embed).queue(null, 
                error -> handleError(error, "Failed to edit embed", "interaction"));
        } catch (Exception e) {
            logger.error("Error editing embed", e);
        }
    }
    
    /**
     * Handle an error that occurred while sending an embed
     * 
     * @param error The error
     * @param errorMessage The error message
     * @param target The target (user or channel)
     */
    private static void handleError(Throwable error, String errorMessage, String target) {
        logger.error("{} for {}: {}", errorMessage, target, error.getMessage());
    }
    
    /**
     * Send an embed asynchronously
     * 
     * @param channel The channel to send the embed to
     * @param embed The embed to send
     * @return A CompletableFuture that completes when the message is sent
     */
    public static CompletableFuture<Void> sendEmbedAsync(TextChannel channel, MessageEmbed embed) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        try {
            channel.sendMessageEmbeds(embed).queue(
                message -> future.complete(null),
                error -> {
                    handleError(error, "Failed to send embed asynchronously", channel.getName());
                    future.completeExceptionally(error);
                }
            );
        } catch (Exception e) {
            logger.error("Error sending embed asynchronously to channel {}", channel.getName(), e);
            future.completeExceptionally(e);
        }
        return future;
    }
}