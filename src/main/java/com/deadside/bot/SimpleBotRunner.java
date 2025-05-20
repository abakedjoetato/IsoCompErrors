package com.deadside.bot;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.util.logging.Logger;
import java.util.logging.Level;

public class SimpleBotRunner extends ListenerAdapter {
    private static final Logger logger = Logger.getLogger(SimpleBotRunner.class.getName());
    
    public static void main(String[] args) {
        try {
            logger.info("Starting Deadside Discord Bot...");
            
            // Get Discord token from environment
            String token = System.getenv("DISCORD_TOKEN");
            if (token == null || token.isEmpty()) {
                logger.severe("Discord token is not set! Please set the DISCORD_TOKEN environment variable.");
                return;
            }
            
            // Initialize JDA
            JDA jda = JDABuilder.createDefault(token)
                    .setActivity(Activity.playing("Deadside"))
                    .enableIntents(
                            GatewayIntent.GUILD_MEMBERS,
                            GatewayIntent.GUILD_MESSAGES,
                            GatewayIntent.MESSAGE_CONTENT
                    )
                    .addEventListeners(new SimpleBotRunner())
                    .build();
            
            // Register simple test command
            jda.upsertCommand("ping", "Check if the bot is working").queue();
            
            logger.info("Bot is starting...");
            jda.awaitReady();
            logger.info("Bot is now ready!");
            
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to start bot: " + e.getMessage(), e);
        }
    }
    
    @Override
    public void onReady(ReadyEvent event) {
        logger.info("Bot is connected to Discord. Ready to receive commands!");
    }
    
    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (event.getName().equals("ping")) {
            long gatewayPing = event.getJDA().getGatewayPing();
            event.reply("Pong! Gateway ping: " + gatewayPing + "ms").queue();
        }
    }
}
