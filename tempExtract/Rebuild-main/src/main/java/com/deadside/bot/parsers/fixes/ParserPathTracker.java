package com.deadside.bot.parsers.fixes;

import com.deadside.bot.db.models.GameServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Tracks successful and failed paths for parsers
 * This class provides a registry for tracking which paths have been
 * successfully used for different servers and categories
 */
public class ParserPathTracker {
    private static final Logger logger = LoggerFactory.getLogger(ParserPathTracker.class);
    
    // Static constants for path categories
    public static final String CATEGORY_CSV = "csv";
    public static final String CATEGORY_LOG = "log";
    
    // Common path patterns to try
    private static final String[] COMMON_CSV_PATHS = {
        "/home/deadside/server/actual/deathlogs",
        "/home/deadside/server/actual1/deathlogs",
        "C:\\deadside\\server\\actual\\deathlogs",
        "C:\\deadside\\server\\actual1\\deathlogs"
    };
    
    private static final String[] COMMON_LOG_PATHS = {
        "/home/deadside/server/Logs",
        "/home/deadside/server/actual/Logs",
        "/home/deadside/server/actual1/Logs",
        "C:\\deadside\\server\\Logs",
        "C:\\deadside\\server\\actual\\Logs",
        "C:\\deadside\\server\\actual1\\Logs"
    };
    
    // The singleton instance
    private static ParserPathTracker instance;
    
    // Maps to track successful paths
    // Structure: serverId -> category -> List<successful paths>
    private final Map<String, Map<String, List<String>>> successfulPaths = new ConcurrentHashMap<>();
    
    /**
     * Private constructor for singleton pattern
     */
    private ParserPathTracker() {
        logger.info("ParserPathTracker initialized");
    }
    
    /**
     * Get the singleton instance
     * @return The singleton instance
     */
    public static synchronized ParserPathTracker getInstance() {
        if (instance == null) {
            instance = new ParserPathTracker();
        }
        return instance;
    }
    
    /**
     * Record a successful path for a server and category
     * @param server The game server
     * @param category The path category (e.g., "csv", "log")
     * @param path The successful path
     */
    public void recordSuccessfulPath(GameServer server, String category, String path) {
        if (server == null || category == null || path == null || path.isEmpty()) {
            return;
        }
        
        String serverId = server.getId() != null ? server.getId().toString() : server.getServerId();
        
        // Get or create the category map for this server
        Map<String, List<String>> categoryMap = successfulPaths.computeIfAbsent(
            serverId, k -> new ConcurrentHashMap<>());
        
        // Get or create the list of successful paths for this category
        List<String> paths = categoryMap.computeIfAbsent(
            category, k -> Collections.synchronizedList(new ArrayList<>()));
        
        // Add the path if it's not already in the list
        if (!paths.contains(path)) {
            paths.add(path);
            logger.debug("Recorded successful {} path for server {}: {}", 
                category, server.getName(), path);
        }
    }
    
    /**
     * Get the most recently successful path for a server and category
     * @param server The game server
     * @param category The path category
     * @return The most recent successful path, or null if none
     */
    public String getSuccessfulPath(GameServer server, String category) {
        if (server == null || category == null) {
            return null;
        }
        
        String serverId = server.getId() != null ? server.getId().toString() : server.getServerId();
        
        // Get the category map for this server
        Map<String, List<String>> categoryMap = successfulPaths.get(serverId);
        if (categoryMap == null) {
            return null;
        }
        
        // Get the list of successful paths for this category
        List<String> paths = categoryMap.get(category);
        if (paths == null || paths.isEmpty()) {
            return null;
        }
        
        // Return the most recent path (last in the list)
        return paths.get(paths.size() - 1);
    }
    
    /**
     * Get recommended paths to try for a server and category
     * @param server The game server
     * @param category The path category
     * @return List of recommended paths to try
     */
    public List<String> getRecommendedPaths(GameServer server, String category) {
        if (server == null || category == null) {
            return Collections.emptyList();
        }
        
        List<String> recommended = new ArrayList<>();
        
        // Add the current path from the server as the first option
        String currentPath = null;
        if (CATEGORY_CSV.equals(category)) {
            currentPath = server.getDeathlogsDirectory();
        } else if (CATEGORY_LOG.equals(category)) {
            currentPath = server.getLogDirectory();
        }
        
        if (currentPath != null && !currentPath.isEmpty()) {
            recommended.add(currentPath);
        }
        
        // Add previously successful paths for this server and category
        String serverId = server.getId() != null ? server.getId().toString() : server.getServerId();
        Map<String, List<String>> categoryMap = successfulPaths.get(serverId);
        if (categoryMap != null) {
            List<String> paths = categoryMap.get(category);
            if (paths != null) {
                for (String path : paths) {
                    if (!recommended.contains(path)) {
                        recommended.add(path);
                    }
                }
            }
        }
        
        // Add common patterns based on the current path
        if (currentPath != null && !currentPath.isEmpty()) {
            // Try variations of the current path
            if (currentPath.contains("actual1")) {
                // If the current path has "actual1", try with "actual"
                String altPath = currentPath.replace("actual1", "actual");
                if (!recommended.contains(altPath)) {
                    recommended.add(altPath);
                }
            } else if (currentPath.contains("actual")) {
                // If the current path has "actual", try with "actual1"
                String altPath = currentPath.replace("actual", "actual1");
                if (!recommended.contains(altPath)) {
                    recommended.add(altPath);
                }
            }
        }
        
        // Add standard common paths
        String[] commonPaths = CATEGORY_CSV.equals(category) ? COMMON_CSV_PATHS : COMMON_LOG_PATHS;
        for (String path : commonPaths) {
            if (!recommended.contains(path)) {
                recommended.add(path);
            }
        }
        
        return recommended;
    }
    
    /**
     * Clear all tracked paths
     * This is mainly for testing purposes
     */
    public void clearAllPaths() {
        successfulPaths.clear();
        logger.info("Cleared all tracked paths");
    }
    
    /**
     * Clear tracked paths for a specific server
     * @param server The game server
     */
    public void clearServerPaths(GameServer server) {
        if (server == null) {
            return;
        }
        
        String serverId = server.getId() != null ? server.getId().toString() : server.getServerId();
        successfulPaths.remove(serverId);
        logger.info("Cleared tracked paths for server {}", server.getName());
    }
}