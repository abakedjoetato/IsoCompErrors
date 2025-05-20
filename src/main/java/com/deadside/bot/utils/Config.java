package com.deadside.bot.utils;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Configuration manager for the bot
 */
public class Config {
    private static final Logger logger = Logger.getLogger(Config.class.getName());
    private static Config instance;
    private final Properties properties;
    private final String configFilePath = "config.properties";
    
    // Default values
    private static final long DEFAULT_DAILY_AMOUNT = 100;
    private static final long DEFAULT_WORK_MIN_AMOUNT = 10;
    private static final long DEFAULT_WORK_MAX_AMOUNT = 50;
    
    private Config() {
        properties = new Properties();
        try {
            properties.load(new FileInputStream(configFilePath));
            logger.info("Configuration loaded from " + configFilePath);
        } catch (IOException e) {
            logger.log(Level.WARNING, "Could not load configuration file. Using default values.", e);
        }
    }
    
    public static synchronized Config getInstance() {
        if (instance == null) {
            instance = new Config();
        }
        return instance;
    }
    
    public String getProperty(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }
    
    public void setProperty(String key, String value) {
        properties.setProperty(key, value);
        saveProperties();
    }
    
    public int getIntProperty(String key, int defaultValue) {
        try {
            return Integer.parseInt(properties.getProperty(key, String.valueOf(defaultValue)));
        } catch (NumberFormatException e) {
            logger.warning("Invalid number format for property " + key + ". Using default value: " + defaultValue);
            return defaultValue;
        }
    }
    
    public boolean getBooleanProperty(String key, boolean defaultValue) {
        return Boolean.parseBoolean(properties.getProperty(key, String.valueOf(defaultValue)));
    }
    
    private void saveProperties() {
        try (FileOutputStream out = new FileOutputStream(configFilePath)) {
            properties.store(out, "Deadside Bot Configuration");
            logger.info("Configuration saved to " + configFilePath);
        } catch (IOException e) {
            logger.log(Level.WARNING, "Could not save configuration file", e);
        }
    }
    
    // Economy methods
    public long getDailyAmount() {
        return Long.parseLong(properties.getProperty("economy.daily.amount", String.valueOf(DEFAULT_DAILY_AMOUNT)));
    }
    
    public void setDailyRewardAmount(long amount) {
        properties.setProperty("economy.daily.amount", String.valueOf(amount));
        saveProperties();
    }
    
    public long getWorkMinAmount() {
        return Long.parseLong(properties.getProperty("economy.work.min_amount", String.valueOf(DEFAULT_WORK_MIN_AMOUNT)));
    }
    
    public void setWorkMinAmount(long amount) {
        properties.setProperty("economy.work.min_amount", String.valueOf(amount));
        saveProperties();
    }
    
    public long getWorkMaxAmount() {
        return Long.parseLong(properties.getProperty("economy.work.max_amount", String.valueOf(DEFAULT_WORK_MAX_AMOUNT)));
    }
    
    public void setWorkMaxAmount(long amount) {
        properties.setProperty("economy.work.max_amount", String.valueOf(amount));
        saveProperties();
    }
    
    // Admin and permissions
    public String getBotOwnerId() {
        return properties.getProperty("bot.owner_id", "");
    }
    
    public List<String> getAdminUserIds() {
        String admins = properties.getProperty("bot.admin_ids", "");
        if (admins.isEmpty()) {
            return new ArrayList<>();
        }
        return Arrays.asList(admins.split(","));
    }
}