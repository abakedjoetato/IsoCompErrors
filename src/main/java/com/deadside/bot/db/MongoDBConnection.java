package com.deadside.bot.db;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;

/**
 * MongoDB connection manager
 */
public class MongoDBConnection {
    private static final Logger logger = LoggerFactory.getLogger(MongoDBConnection.class);
    private static MongoClient mongoClient;
    private static MongoDatabase database;
    private static boolean initialized = false;
    private static String connectionString;

    /**
     * Initialize the MongoDB connection
     * @param mongoUri MongoDB connection URI
     */
    public static void initialize(String mongoUri) {
        if (initialized) {
            logger.warn("MongoDB connection already initialized");
            return;
        }

        try {
            logger.info("Initializing MongoDB connection");
            connectionString = mongoUri;
            
            // Configure codec registry for POJO support
            CodecRegistry pojoCodecRegistry = fromRegistries(
                    MongoClientSettings.getDefaultCodecRegistry(),
                    fromProviders(PojoCodecProvider.builder().automatic(true).build())
            );

            // Configure client settings
            MongoClientSettings settings = MongoClientSettings.builder()
                    .applyConnectionString(new ConnectionString(mongoUri))
                    .codecRegistry(pojoCodecRegistry)
                    .build();

            // Create client and get database
            mongoClient = MongoClients.create(settings);
            database = mongoClient.getDatabase("deadside");
            initialized = true;
            logger.info("MongoDB connection successfully initialized");
        } catch (Exception e) {
            logger.error("Failed to initialize MongoDB connection", e);
            throw e;
        }
    }

    /**
     * Get the MongoDB database instance
     * @return MongoDB database
     */
    public static MongoDatabase getDatabase() {
        if (!initialized) {
            logger.error("MongoDB connection not initialized");
            throw new IllegalStateException("MongoDB connection not initialized");
        }
        return database;
    }

    /**
     * Get a collection from the database
     * @param collectionName Name of the collection
     * @return MongoDB collection
     */
    public static MongoCollection<Document> getCollection(String collectionName) {
        return getDatabase().getCollection(collectionName);
    }

    /**
     * Get a typed collection from the database
     * @param collectionName Name of the collection
     * @param documentClass Class of the document
     * @param <T> Type of the document
     * @return MongoDB collection
     */
    public static <T> MongoCollection<T> getCollection(String collectionName, Class<T> documentClass) {
        return getDatabase().getCollection(collectionName, documentClass);
    }

    /**
     * Close the MongoDB connection
     */
    public static void close() {
        if (mongoClient != null) {
            logger.info("Closing MongoDB connection");
            mongoClient.close();
            initialized = false;
        }
    }

    /**
     * Check if the MongoDB connection is initialized
     * @return true if initialized, false otherwise
     */
    public static boolean isInitialized() {
        return initialized;
    }
}