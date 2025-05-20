package com.deadside.bot.listeners;

import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Listener for button interactions
 */
public class ButtonListener extends ListenerAdapter {
    private static final Logger logger = LoggerFactory.getLogger(ButtonListener.class);
    
    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        String buttonId = event.getComponentId();
        logger.info("Button clicked: {}", buttonId);
        
        try {
            // Handle button interactions
            if (buttonId.startsWith("confirm_")) {
                // Handle confirmation buttons
                event.reply("Confirmed!").setEphemeral(true).queue();
            } else if (buttonId.startsWith("cancel_")) {
                // Handle cancellation buttons
                event.reply("Cancelled").setEphemeral(true).queue();
            } else {
                // Unknown button
                event.reply("Button action not implemented").setEphemeral(true).queue();
            }
        } catch (Exception e) {
            logger.error("Error handling button interaction: {}", buttonId, e);
            event.reply("An error occurred processing this button").setEphemeral(true).queue();
        }
    }
}