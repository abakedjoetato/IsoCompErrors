package com.deadside.bot.db;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * MongoDB connection manager
 */
public class MongoDBConnection {
    private static final Logger logger = Logger.getLogger(MongoDBConnection.class.getName());
    private static MongoClient mongoClient;
    private static MongoDatabase database;
    private static String databaseName = "deadside_bot";
    
    /**
     * Initialize the MongoDB connection
     * @param mongoUri The MongoDB connection URI
     */
    public static void initialize(String mongoUri) {
        try {
            logger.info("Initializing MongoDB connection...");
            
            // Close any existing connection
            if (mongoClient != null) {
                mongoClient.close();
            }
            
            // Create new connection
            mongoClient = MongoClients.create(mongoUri);
            database = mongoClient.getDatabase(databaseName);
            
            logger.info("MongoDB connection initialized successfully");
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to initialize MongoDB connection", e);
            throw e;
        }
    }
    
    /**
     * Get the MongoDB database
     * @return The MongoDB database
     */
    public static MongoDatabase getDatabase() {
        if (database == null) {
            throw new IllegalStateException("MongoDB connection not initialized. Call initialize() first.");
        }
        return database;
    }
    
    /**
     * Get the MongoDB client
     * @return The MongoDB client
     */
    public static MongoClient getClient() {
        if (mongoClient == null) {
            throw new IllegalStateException("MongoDB connection not initialized. Call initialize() first.");
        }
        return mongoClient;
    }
    
    /**
     * Set the database name
     * @param name The database name
     */
    public static void setDatabaseName(String name) {
        databaseName = name;
        if (mongoClient != null) {
            database = mongoClient.getDatabase(databaseName);
        }
    }
    
    /**
     * Close the MongoDB connection
     */
    public static void close() {
        if (mongoClient != null) {
            mongoClient.close();
            mongoClient = null;
            database = null;
            logger.info("MongoDB connection closed");
        }
    }
}