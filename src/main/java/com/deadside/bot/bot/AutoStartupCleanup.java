package com.deadside.bot.bot;

import com.deadside.bot.commands.admin.RunCleanupOnStartupCommand;
import com.deadside.bot.utils.Config;

import java.io.File;
import java.util.logging.Logger;

/**
 * Handles automatic cleanup of temporary files on bot startup
 */
public class AutoStartupCleanup {
    private static final Logger logger = Logger.getLogger(AutoStartupCleanup.class.getName());
    private final Config config;
    
    public AutoStartupCleanup(Config config) {
        this.config = config;
    }
    
    /**
     * Run cleanup if enabled in configuration
     */
    public void runIfEnabled() {
        boolean enabled = config.getBooleanProperty("startup.cleanup.enabled", false);
        
        if (enabled) {
            logger.info("Automatic cleanup on startup is enabled. Running cleanup...");
            int filesDeleted = runCleanup();
            logger.info("Cleanup completed. Deleted " + filesDeleted + " temporary files.");
        } else {
            logger.info("Automatic cleanup on startup is disabled. Skipping.");
        }
    }
    
    /**
     * Run the cleanup process
     * @return Number of files deleted
     */
    public int runCleanup() {
        int filesDeleted = 0;
        
        // Clean temp directory
        File tempDir = new File("temp");
        if (tempDir.exists() && tempDir.isDirectory()) {
            filesDeleted += deleteDirectory(tempDir);
        }
        
        // Clean logs older than 7 days
        File logsDir = new File("logs");
        if (logsDir.exists() && logsDir.isDirectory()) {
            long cutoffTime = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000); // 7 days in milliseconds
            
            File[] logFiles = logsDir.listFiles();
            if (logFiles != null) {
                for (File logFile : logFiles) {
                    if (logFile.isFile() && logFile.lastModified() < cutoffTime) {
                        if (logFile.delete()) {
                            filesDeleted++;
                        }
                    }
                }
            }
        }
        
        return filesDeleted;
    }
    
    /**
     * Delete a directory and all its contents
     * @param directory Directory to delete
     * @return Number of files deleted
     */
    private int deleteDirectory(File directory) {
        int filesDeleted = 0;
        File[] files = directory.listFiles();
        
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    filesDeleted += deleteDirectory(file);
                } else {
                    if (file.delete()) {
                        filesDeleted++;
                    }
                }
            }
        }
        
        directory.delete(); // Delete the empty directory
        return filesDeleted;
    }
}