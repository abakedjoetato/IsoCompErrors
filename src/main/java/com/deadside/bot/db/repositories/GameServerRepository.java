package com.deadside.bot.db.repositories;

import com.deadside.bot.db.MongoDBConnection;
import com.deadside.bot.db.models.GameServer;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.ReplaceOptions;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Repository class for GameServer objects
 */
public class GameServerRepository {
    private static final Logger logger = Logger.getLogger(GameServerRepository.class.getName());
    private final MongoCollection<Document> collection;
    
    public GameServerRepository() {
        MongoDatabase database = MongoDBConnection.getDatabase();
        this.collection = database.getCollection("servers");
    }
    
    public GameServer findById(String id) {
        try {
            Document doc = collection.find(Filters.eq("_id", new ObjectId(id))).first();
            return docToGameServer(doc);
        } catch (Exception e) {
            logger.warning("Error finding server by ID: " + e.getMessage());
            return null;
        }
    }
    
    public GameServer findByGuildId(String guildIdStr) {
        try {
            long guildId = Long.parseLong(guildIdStr);
            Document doc = collection.find(Filters.eq("guildId", guildId)).first();
            return docToGameServer(doc);
        } catch (NumberFormatException e) {
            logger.warning("Invalid guild ID format: " + guildIdStr);
            return null;
        } catch (Exception e) {
            logger.warning("Error finding server by guild ID: " + e.getMessage());
            return null;
        }
    }
    
    public List<GameServer> findAllByGuildId(long guildId) {
        List<GameServer> servers = new ArrayList<>();
        try {
            collection.find(Filters.eq("guildId", guildId)).forEach(doc -> {
                GameServer server = docToGameServer(doc);
                if (server != null) {
                    servers.add(server);
                }
            });
        } catch (Exception e) {
            logger.warning("Error finding servers by guild ID: " + e.getMessage());
        }
        return servers;
    }
    
    public List<GameServer> findAll() {
        return findAllServers();
    }
    
    public GameServer findByGuildIdAndName(long guildId, String name) {
        try {
            Document doc = collection.find(
                    Filters.and(
                            Filters.eq("guildId", guildId),
                            Filters.eq("serverName", name)
                    )).first();
            return docToGameServer(doc);
        } catch (Exception e) {
            logger.warning("Error finding server by guild ID and name: " + e.getMessage());
            return null;
        }
    }
    
    public List<Long> getDistinctGuildIds() {
        List<Long> guildIds = new ArrayList<>();
        try {
            collection.distinct("guildId", Long.class).into(guildIds);
        } catch (Exception e) {
            logger.warning("Error getting distinct guild IDs: " + e.getMessage());
        }
        return guildIds;
    }
    
    public List<GameServer> findAllServers() {
        List<GameServer> servers = new ArrayList<>();
        try {
            collection.find().forEach(doc -> {
                GameServer server = docToGameServer(doc);
                if (server != null) {
                    servers.add(server);
                }
            });
        } catch (Exception e) {
            logger.warning("Error finding all servers: " + e.getMessage());
        }
        return servers;
    }
    
    public void save(GameServer server) {
        try {
            Document doc = gameServerToDoc(server);
            
            if (server.getId() == null || server.getId().isEmpty()) {
                // Insert new server
                collection.insertOne(doc);
                String id = doc.getObjectId("_id").toString();
                server.setId(id);
                logger.info("Inserted new server with ID: " + id);
            } else {
                // Update existing server
                ReplaceOptions options = new ReplaceOptions().upsert(true);
                collection.replaceOne(
                        Filters.eq("_id", new ObjectId(server.getId())),
                        doc,
                        options
                );
                logger.info("Updated server with ID: " + server.getId());
            }
        } catch (Exception e) {
            logger.warning("Error saving server: " + e.getMessage());
        }
    }
    
    public void delete(String id) {
        try {
            collection.deleteOne(Filters.eq("_id", new ObjectId(id)));
            logger.info("Deleted server with ID: " + id);
        } catch (Exception e) {
            logger.warning("Error deleting server: " + e.getMessage());
        }
    }
    
    private GameServer docToGameServer(Document doc) {
        if (doc == null) {
            return null;
        }
        
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
        
        // Handle optional fields
        String name = doc.getString("name");
        if (name != null) {
            server.setName(name);
        }
        
        String host = doc.getString("host");
        if (host != null) {
            server.setHost(host);
        }
        
        String sftpHost = doc.getString("sftpHost");
        if (sftpHost != null) {
            server.setSftpHost(sftpHost);
        }
        
        Integer sftpPort = doc.getInteger("sftpPort");
        if (sftpPort != null) {
            server.setSftpPort(sftpPort);
        }
        
        String sftpUsername = doc.getString("sftpUsername");
        if (sftpUsername != null) {
            server.setSftpUsername(sftpUsername);
        }
        
        return server;
    }
    
    private Document gameServerToDoc(GameServer server) {
        Document doc = new Document();
        
        if (server.getId() != null && !server.getId().isEmpty()) {
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
           .append("readOnly", server.isReadOnly());
        
        // Add optional fields if they exist
        if (server.getName() != null) {
            doc.append("name", server.getName());
        }
        
        if (server.getHost() != null) {
            doc.append("host", server.getHost());
        }
        
        if (server.getSftpHost() != null) {
            doc.append("sftpHost", server.getSftpHost());
        }
        
        if (server.getSftpPort() > 0) {
            doc.append("sftpPort", server.getSftpPort());
        }
        
        if (server.getSftpUsername() != null) {
            doc.append("sftpUsername", server.getSftpUsername());
        }
        
        return doc;
    }
}