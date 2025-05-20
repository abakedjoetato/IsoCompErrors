package com.deadside.bot.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Configuration loader for the Deadside Discord Bot
 */
public class Config {
    private static final Logger logger = LoggerFactory.getLogger(Config.class);
    private static final String CONFIG_FILE = "config.properties";
    private static Properties properties = new Properties();
    private static boolean isLoaded = false;
    private static Config instance;
    
    private Config() {
        // Private constructor for singleton
    }
    
    /**
     * Get singleton instance of Config
     * @return Config instance
     */
    public static Config getInstance() {
        if (instance == null) {
            instance = new Config();
            try {
                loadConfig();
            } catch (Exception e) {
                logger.error("Failed to load configuration", e);
            }
        }
        return instance;
    }
    
    static {
        try {
            loadConfig();
        } catch (Exception e) {
            logger.error("Failed to load configuration", e);
        }
    }
    
    /**
     * Load configuration from file
     */
    public static void loadConfig() {
        try (FileInputStream fis = new FileInputStream(CONFIG_FILE)) {
            properties.load(fis);
            isLoaded = true;
            logger.info("Loaded configuration from {}", CONFIG_FILE);
        } catch (IOException e) {
            logger.warn("Could not load configuration from {}. Using defaults.", CONFIG_FILE);
        }
    }
    
    /**
     * Get a configuration property
     * @param key Property key
     * @param defaultValue Default value if not found
     * @return Property value or default
     */
    public static String getProperty(String key, String defaultValue) {
        if (!isLoaded) {
            loadConfig();
        }
        return properties.getProperty(key, defaultValue);
    }
    
    /**
     * Get a configuration property
     * @param key Property key
     * @return Property value or null if not found
     */
    public static String getProperty(String key) {
        if (!isLoaded) {
            loadConfig();
        }
        return properties.getProperty(key);
    }
    
    /**
     * Get a configuration property as an integer
     * @param key Property key
     * @param defaultValue Default value if not found or invalid
     * @return Property value as integer or default
     */
    public static int getIntProperty(String key, int defaultValue) {
        String value = getProperty(key);
        if (value == null) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            logger.warn("Invalid integer property {}: {}. Using default: {}", key, value, defaultValue);
            return defaultValue;
        }
    }
    
    /**
     * Get a configuration property as a boolean
     * @param key Property key
     * @param defaultValue Default value if not found
     * @return Property value as boolean or default
     */
    public static boolean getBooleanProperty(String key, boolean defaultValue) {
        String value = getProperty(key);
        if (value == null) {
            return defaultValue;
        }
        return Boolean.parseBoolean(value);
    }
    
    /**
     * Check if a property exists
     * @param key Property key
     * @return True if property exists, false otherwise
     */
    public static boolean hasProperty(String key) {
        if (!isLoaded) {
            loadConfig();
        }
        return properties.containsKey(key);
    }
    
    /**
     * Get the default MongoDB connection string
     * @return MongoDB connection string
     */
    public static String getMongoDBConnectionString() {
        return getProperty("mongodb.connection", "mongodb://localhost:27017/deadside_bot");
    }
    
    /**
     * Get the default MongoDB database name
     * @return MongoDB database name
     */
    public static String getMongoDBDatabase() {
        return getProperty("mongodb.database", "deadside_bot");
    }
    
    /**
     * Gets the update interval for killfeed in seconds
     * @return Update interval in seconds
     */
    public int getKillfeedUpdateInterval() {
        return getIntProperty("killfeed.interval", 60);
    }
    
    /**
     * Gets the maximum amount for work command reward
     * @return Maximum work command reward
     */
    public int getWorkMaxAmount() {
        return getIntProperty("economy.work.max", 500);
    }
    
    /**
     * Gets the Tip4Serv API key
     * @return Tip4Serv API key
     */
    public String getTip4servApiKey() {
        return getProperty("tip4serv.apikey", "");
    }
}