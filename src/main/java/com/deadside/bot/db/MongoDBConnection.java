package com.deadside.bot.db;

import com.deadside.bot.config.Config;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.mongodb.client.model.ReplaceOptions;

import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;

/**
 * Singleton connection to MongoDB
 */
public class MongoDBConnection {
    private static final Logger logger = LoggerFactory.getLogger(MongoDBConnection.class);
    private static MongoDBConnection instance;
    private MongoClient mongoClient;
    private MongoDatabase database;
    private static final String DEFAULT_DATABASE_NAME = "deadside_bot";
    
    private MongoDBConnection() {
        // Private constructor for singleton
    }
    
    /**
     * Get instance of MongoDB connection
     * @return Singleton instance
     */
    public static MongoDBConnection getInstance() {
        if (instance == null) {
            instance = new MongoDBConnection();
        }
        return instance;
    }
    
    /**
     * Initialize MongoDB connection (static method for Main class)
     * @param connectionString MongoDB connection string
     */
    public static void initialize(String connectionString) {
        getInstance().initialize(connectionString, DEFAULT_DATABASE_NAME);
    }
    
    // This method is handled by the static initialize method
    
    /**
     * Initialize MongoDB connection with specific database name
     * @param connectionString MongoDB connection string
     * @param databaseName Database name to use
     */
    public void initialize(String connectionString, String databaseName) {
        try {
            // Create codec registry for POJOs
            CodecRegistry pojoCodecRegistry = fromRegistries(
                    MongoClientSettings.getDefaultCodecRegistry(),
                    fromProviders(PojoCodecProvider.builder().automatic(true).build())
            );
            
            // Configure MongoDB client settings
            MongoClientSettings settings = MongoClientSettings.builder()
                    .applyConnectionString(new ConnectionString(connectionString))
                    .codecRegistry(pojoCodecRegistry)
                    .build();
            
            // Create MongoDB client and get database
            mongoClient = MongoClients.create(settings);
            database = mongoClient.getDatabase(databaseName);
            
            logger.info("Connected to MongoDB database: {}", databaseName);
        } catch (Exception e) {
            logger.error("Failed to connect to MongoDB", e);
            throw new RuntimeException("Failed to connect to MongoDB", e);
        }
    }
    
    /**
     * Get MongoDB database instance
     * @return MongoDB database
     */
    public MongoDatabase getDatabase() {
        if (database == null) {
            throw new IllegalStateException("MongoDB connection not initialized. Call initialize() first.");
        }
        return database;
    }
    
    /**
     * Close MongoDB connection
     */
    public void close() {
        if (mongoClient != null) {
            mongoClient.close();
            mongoClient = null;
            database = null;
            logger.info("MongoDB connection closed");
        }
    }
    
    /**
     * Get ReplaceOptions with upsert enabled
     * @return ReplaceOptions with upsert=true
     */
    public ReplaceOptions getUpsertOptions() {
        return new ReplaceOptions().upsert(true);
    }
}