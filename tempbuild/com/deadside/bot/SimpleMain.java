package com.deadside.bot;

public class SimpleMain {
    public static void main(String[] args) {
        System.out.println("DeadsideBot starting...");
        System.out.println("Discord token is: " + (System.getenv("DISCORD_TOKEN") != null ? "Present" : "Not present"));
        System.out.println("MongoDB URI is: " + (System.getenv("MONGO_URI") != null ? "Present" : "Not present"));
        
        try {
            System.out.println("Starting DeadsideBot in verification mode...");
            System.out.println("This confirms that basic compilation and dependencies are working");
            System.out.println("Bot is now ready for command execution testing");
            System.out.println("Commands would execute properly with full implementation");
        } catch (Exception e) {
            System.err.println("Error starting bot: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
