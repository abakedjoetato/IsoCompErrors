package com.deadside.bot.db.repositories;

import com.deadside.bot.db.MongoDBConnection;
import com.deadside.bot.db.models.GameServer;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Repository for GameServer entity
 */
public class GameServerRepository {
    private static final Logger logger = LoggerFactory.getLogger(GameServerRepository.class);
    private static final String COLLECTION_NAME = "servers";

    /**
     * Find server by ID
     * @param id Server ID
     * @return GameServer or null if not found
     */
    public GameServer findById(String id) {
        try {
            MongoCollection<Document> collection = MongoDBConnection.getCollection(COLLECTION_NAME);
            Document doc = collection.find(Filters.eq("_id", new ObjectId(id))).first();
            
            if (doc == null) {
                return null;
            }
            
            return documentToServer(doc);
        } catch (Exception e) {
            logger.error("Error finding server by ID: {}", id, e);
            return null;
        }
    }

    /**
     * Find server by name and guild ID
     * @param name Server name
     * @param guildId Guild ID
     * @return GameServer or null if not found
     */
    public GameServer findByNameAndGuildId(String name, long guildId) {
        try {
            MongoCollection<Document> collection = MongoDBConnection.getCollection(COLLECTION_NAME);
            Document doc = collection.find(
                Filters.and(
                    Filters.eq("name", name),
                    Filters.eq("guildId", guildId)
                )
            ).first();
            
            if (doc == null) {
                return null;
            }
            
            return documentToServer(doc);
        } catch (Exception e) {
            logger.error("Error finding server by name and guild ID: {}, {}", name, guildId, e);
            return null;
        }
    }

    /**
     * Find all servers for a guild
     * @param guildId Guild ID
     * @return List of servers
     */
    public List<GameServer> findByGuildId(long guildId) {
        List<GameServer> servers = new ArrayList<>();
        
        try {
            MongoCollection<Document> collection = MongoDBConnection.getCollection(COLLECTION_NAME);
            FindIterable<Document> docs = collection.find(Filters.eq("guildId", guildId));
            
            for (Document doc : docs) {
                servers.add(documentToServer(doc));
            }
        } catch (Exception e) {
            logger.error("Error finding servers by guild ID: {}", guildId, e);
        }
        
        return servers;
    }

    /**
     * Find all active servers for a guild
     * @param guildId Guild ID
     * @return List of active servers
     */
    public List<GameServer> findActiveByGuildId(long guildId) {
        List<GameServer> servers = new ArrayList<>();
        
        try {
            MongoCollection<Document> collection = MongoDBConnection.getCollection(COLLECTION_NAME);
            FindIterable<Document> docs = collection.find(
                Filters.and(
                    Filters.eq("guildId", guildId),
                    Filters.eq("active", true)
                )
            );
            
            for (Document doc : docs) {
                servers.add(documentToServer(doc));
            }
        } catch (Exception e) {
            logger.error("Error finding active servers by guild ID: {}", guildId, e);
        }
        
        return servers;
    }

    /**
     * Save a server (insert or update)
     * @param server Server to save
     * @return Saved server
     */
    public GameServer save(GameServer server) {
        try {
            MongoCollection<Document> collection = MongoDBConnection.getCollection(COLLECTION_NAME);
            Document doc = serverToDocument(server);
            
            if (server.getId() == null) {
                // Insert new server
                collection.insertOne(doc);
                
                // Get the ID
                server.setId(doc.getObjectId("_id").toString());
            } else {
                // Update existing server
                UpdateResult result = collection.replaceOne(
                    Filters.eq("_id", new ObjectId(server.getId())),
                    doc
                );
                
                if (result.getMatchedCount() == 0) {
                    logger.warn("No server found with ID: {}", server.getId());
                    return null;
                }
            }
            
            return server;
        } catch (Exception e) {
            logger.error("Error saving server: {}", server.getName(), e);
            return null;
        }
    }

    /**
     * Update server status
     * @param serverId Server ID
     * @param online Online status
     * @param playerCount Current player count
     * @param maxPlayers Maximum player count
     * @return true if updated, false otherwise
     */
    public boolean updateServerStatus(String serverId, boolean online, int playerCount, int maxPlayers) {
        try {
            MongoCollection<Document> collection = MongoDBConnection.getCollection(COLLECTION_NAME);
            UpdateResult result = collection.updateOne(
                Filters.eq("_id", new ObjectId(serverId)),
                Updates.combine(
                    Updates.set("online", online),
                    Updates.set("playerCount", playerCount),
                    Updates.set("maxPlayers", maxPlayers)
                )
            );
            
            return result.getModifiedCount() > 0;
        } catch (Exception e) {
            logger.error("Error updating server status: {}", serverId, e);
            return false;
        }
    }

    /**
     * Set server active status
     * @param serverId Server ID
     * @param active Active status
     * @return true if updated, false otherwise
     */
    public boolean setServerActive(String serverId, boolean active) {
        try {
            MongoCollection<Document> collection = MongoDBConnection.getCollection(COLLECTION_NAME);
            UpdateResult result = collection.updateOne(
                Filters.eq("_id", new ObjectId(serverId)),
                Updates.set("active", active)
            );
            
            return result.getModifiedCount() > 0;
        } catch (Exception e) {
            logger.error("Error setting server active status: {}", serverId, e);
            return false;
        }
    }

    /**
     * Delete a server
     * @param id Server ID
     * @return true if deleted, false otherwise
     */
    public boolean delete(String id) {
        try {
            MongoCollection<Document> collection = MongoDBConnection.getCollection(COLLECTION_NAME);
            DeleteResult result = collection.deleteOne(Filters.eq("_id", new ObjectId(id)));
            
            return result.getDeletedCount() > 0;
        } catch (Exception e) {
            logger.error("Error deleting server with ID: {}", id, e);
            return false;
        }
    }

    /**
     * Convert document to server
     * @param doc MongoDB document
     * @return GameServer object
     */
    private GameServer documentToServer(Document doc) {
        GameServer server = new GameServer();
        
        server.setId(doc.getObjectId("_id").toString());
        server.setServerName(doc.getString("serverName"));
        server.setServerIp(doc.getString("serverIp"));
        server.setGamePort(doc.getInteger("gamePort", 0));
        server.setServerVersion(doc.getString("serverVersion"));
        server.setGuildId(doc.getLong("guildId"));
        server.setFtpHost(doc.getString("ftpHost"));
        server.setFtpPort(doc.getInteger("ftpPort", 21));
        server.setFtpUsername(doc.getString("ftpUsername"));
        server.setFtpPassword(doc.getString("ftpPassword"));
        server.setLogPath(doc.getString("logPath"));
        server.setActive(doc.getBoolean("active", true));
        server.setReadOnly(doc.getBoolean("readOnly", false));
        server.setName(doc.getString("name"));
        server.setHost(doc.getString("host"));
        server.setSftpHost(doc.getString("sftpHost"));
        server.setSftpPort(doc.getInteger("sftpPort", 22));
        server.setSftpUsername(doc.getString("sftpUsername"));
        server.setSftpPassword(doc.getString("sftpPassword"));
        server.setPlayerCount(doc.getInteger("playerCount", 0));
        server.setMaxPlayers(doc.getInteger("maxPlayers", 0));
        server.setOnline(doc.getBoolean("online", false));
        server.setLogChannelId(doc.getLong("logChannelId", 0));
        server.setUseSftpForLogs(doc.getBoolean("useSftpForLogs", false));
        server.setLastProcessedTimestamp(doc.getLong("lastProcessedTimestamp", 0));
        server.setUsername(doc.getString("username"));
        server.setPassword(doc.getString("password"));
        
        return server;
    }

    /**
     * Convert server to document
     * @param server GameServer object
     * @return MongoDB document
     */
    private Document serverToDocument(GameServer server) {
        Document doc = new Document();
        
        if (server.getId() != null) {
            doc.append("_id", new ObjectId(server.getId()));
        }
        
        doc.append("serverName", server.getServerName())
           .append("serverIp", server.getServerIp())
           .append("gamePort", server.getGamePort())
           .append("serverVersion", server.getServerVersion())
           .append("guildId", server.getGuildId())
           .append("ftpHost", server.getFtpHost())
           .append("ftpPort", server.getFtpPort())
           .append("ftpUsername", server.getFtpUsername())
           .append("ftpPassword", server.getFtpPassword())
           .append("logPath", server.getLogPath())
           .append("active", server.isActive())
           .append("readOnly", server.isReadOnly())
           .append("name", server.getName())
           .append("host", server.getHost())
           .append("sftpHost", server.getSftpHost())
           .append("sftpPort", server.getSftpPort())
           .append("sftpUsername", server.getSftpUsername())
           .append("sftpPassword", server.getSftpPassword())
           .append("playerCount", server.getPlayerCount())
           .append("maxPlayers", server.getMaxPlayers())
           .append("online", server.isOnline())
           .append("logChannelId", server.getLogChannelId())
           .append("useSftpForLogs", server.isUseSftpForLogs())
           .append("lastProcessedTimestamp", server.getLastProcessedTimestamp())
           .append("username", server.getUsername())
           .append("password", server.getPassword());
        
        return doc;
    }
    
    /**
     * Check if any servers exist for a guild
     * @param guildId Guild ID
     * @return true if servers exist, false otherwise
     */
    public boolean existsByGuildId(long guildId) {
        try {
            MongoCollection<Document> collection = MongoDBConnection.getCollection(COLLECTION_NAME);
            return collection.countDocuments(Filters.eq("guildId", guildId)) > 0;
        } catch (Exception e) {
            logger.error("Error checking if servers exist for guild: {}", guildId, e);
            return false;
        }
    }
    
    /**
     * Count all servers in the database
     * @return Number of servers
     */
    public long countAll() {
        try {
            MongoCollection<Document> collection = MongoDBConnection.getCollection(COLLECTION_NAME);
            return collection.countDocuments();
        } catch (Exception e) {
            logger.error("Error counting all servers", e);
            return 0;
        }
    }
}