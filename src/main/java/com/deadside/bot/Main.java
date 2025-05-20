package com.deadside.bot;

import com.deadside.bot.bot.DeadsideBot;
import com.deadside.bot.db.MongoDBConnection;
import com.deadside.bot.utils.Config;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Main entry point for the Deadside Discord Bot
 */
public class Main {
    private static final Logger logger = Logger.getLogger(Main.class.getName());
    
    public static void main(String[] args) {
        try {
            logger.info("Starting Deadside Discord Bot...");
            
            // Initialize configuration
            Config config = Config.getInstance();
            
            // Initialize MongoDB connection
            String mongoUri = System.getenv("MONGO_URI");
            if (mongoUri == null || mongoUri.isEmpty()) {
                logger.severe("MongoDB URI environment variable (MONGO_URI) is not set");
                return;
            }
            
            MongoDBConnection.initialize(mongoUri);
            logger.info("MongoDB connection initialized");
            
            // Get Discord token from environment variable
            String token = System.getenv("DISCORD_TOKEN");
            if (token == null || token.isEmpty()) {
                logger.severe("Discord token environment variable (DISCORD_TOKEN) is not set");
                return;
            }
            
            // Initialize and start the bot
            DeadsideBot bot = new DeadsideBot(token);
            bot.start();
            
            logger.info("Bot started successfully");
            
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to start the bot", e);
        }
    }
}