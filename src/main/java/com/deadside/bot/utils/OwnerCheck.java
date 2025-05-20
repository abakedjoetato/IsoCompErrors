package com.deadside.bot.utils;

/**
 * Utility for checking if a user is the bot owner
 */
public class OwnerCheck {
    private static final long OWNER_ID = 123456789012345678L; // Default owner ID
    
    public static boolean isOwner(long userId) {
        return userId == OWNER_ID;
    }
}