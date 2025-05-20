package com.deadside.bot;

import com.deadside.bot.bot.DeadsideBot;
import java.util.logging.Logger;

/**
 * Main Bot class for compatibility with original code
 */
public class Bot {
    private static final Logger logger = Logger.getLogger(Bot.class.getName());
    private static DeadsideBot instance;
    
    public static void main(String[] args) {
        // Forward to main implementation
        Main.main(args);
    }
    
    public static DeadsideBot getInstance() {
        if (instance == null) {
            throw new IllegalStateException("Bot not initialized. Call initialize() first.");
        }
        return instance;
    }
    
    public static void setInstance(DeadsideBot bot) {
        instance = bot;
    }
}
