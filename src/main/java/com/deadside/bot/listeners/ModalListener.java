package com.deadside.bot.listeners;

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
        String modalId = event.getModalId();
        logger.info("Modal submitted: {}", modalId);
        
        try {
            // Handle server configuration modal
            if (modalId.startsWith("server_config_")) {
                // Extract server ID from modal ID
                String serverId = modalId.substring("server_config_".length());
                
                // Get form values
                String name = event.getValue("name").getAsString();
                String host = event.getValue("host").getAsString();
                String port = event.getValue("port").getAsString();
                
                // Respond to the user
                event.reply("Server configuration updated for: " + name).setEphemeral(true).queue();
                logger.info("Updated server config for {} ({}:{})", name, host, port);
            } 
            // Handle other modal types
            else {
                event.reply("Modal action not implemented").setEphemeral(true).queue();
                logger.warn("Unhandled modal: {}", modalId);
            }
        } catch (Exception e) {
            logger.error("Error handling modal interaction: {}", modalId, e);
            event.reply("An error occurred processing this form").setEphemeral(true).queue();
        }
    }
}