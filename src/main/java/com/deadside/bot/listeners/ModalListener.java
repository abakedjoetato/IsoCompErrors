package com.deadside.bot.listeners;

import com.deadside.bot.utils.EmbedUtils;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Listener for modal interactions
 */
public class ModalListener extends ListenerAdapter {
    private static final Logger logger = LoggerFactory.getLogger(ModalListener.class);

    @Override
    public void onModalInteraction(ModalInteractionEvent event) {
        try {
            String modalId = event.getModalId();
            logger.info("Received modal interaction: {}", modalId);
            
            // Handle different modal IDs
            if (modalId.startsWith("server_add")) {
                handleServerAddModal(event);
            } else if (modalId.startsWith("faction_create")) {
                handleFactionCreateModal(event);
            } else if (modalId.startsWith("bounty_create")) {
                handleBountyCreateModal(event);
            } else {
                logger.warn("Unknown modal ID: {}", modalId);
                event.replyEmbeds(
                    EmbedUtils.createErrorEmbed("Error", "Unknown modal type. Please try again.")
                ).setEphemeral(true).queue();
            }
        } catch (Exception e) {
            logger.error("Error handling modal interaction", e);
            
            // Reply with error if interaction hasn't been replied to
            if (!event.isAcknowledged()) {
                event.replyEmbeds(
                    EmbedUtils.createErrorEmbed("Error", 
                        "There was an error processing your input. Please try again later.")
                ).setEphemeral(true).queue();
            }
        }
    }
    
    private void handleServerAddModal(ModalInteractionEvent event) {
        // For Phase 0, just acknowledge the modal submission
        event.replyEmbeds(
            EmbedUtils.createInfoEmbed("Server Management", 
                "Server add request received. This functionality will be implemented in a future phase.")
        ).setEphemeral(true).queue();
    }
    
    private void handleFactionCreateModal(ModalInteractionEvent event) {
        // For Phase 0, just acknowledge the modal submission
        event.replyEmbeds(
            EmbedUtils.createInfoEmbed("Faction Management", 
                "Faction create request received. This functionality will be implemented in a future phase.")
        ).setEphemeral(true).queue();
    }
    
    private void handleBountyCreateModal(ModalInteractionEvent event) {
        // For Phase 0, just acknowledge the modal submission
        event.replyEmbeds(
            EmbedUtils.createInfoEmbed("Bounty System", 
                "Bounty create request received. This functionality will be implemented in a future phase.")
        ).setEphemeral(true).queue();
    }
}