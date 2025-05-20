package com.deadside.bot.db.repositories;

import com.deadside.bot.db.MongoDBConnection;
import com.deadside.bot.db.models.GameServer;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
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
        this.collection = database.getCollection("game_servers");
    }
    
    public GameServer findById(String id) {
        Document doc = collection.find(Filters.eq("_id", new ObjectId(id))).first();
        return docToGameServer(doc);
    }
    
    public GameServer findByGuildId(String guildIdStr) {
        try {
            long guildId = Long.parseLong(guildIdStr);
            Document doc = collection.find(Filters.eq("guildId", guildId)).first();
            return docToGameServer(doc);
        } catch (NumberFormatException e) {
            logger.warning("Invalid guild ID format: " + guildIdStr);
            return null;
        }
    }
    
    public List<GameServer> findAllActive() {
        List<GameServer> results = new ArrayList<>();
        collection.find(Filters.eq("active", true))
                .forEach(doc -> results.add(docToGameServer(doc)));
        return results;
    }
    
    public void save(GameServer server) {
        Document doc = gameServerToDoc(server);
        
        if (server.getId() == null || server.getId().isEmpty()) {
            // Insert new server
            collection.insertOne(doc);
            String id = doc.getObjectId("_id").toString();
            server.setId(id);
        } else {
            // Update existing server
            collection.replaceOne(
                    Filters.eq("_id", new ObjectId(server.getId())),
                    doc
            );
        }
    }
    
    public void delete(String id) {
        collection.deleteOne(Filters.eq("_id", new ObjectId(id)));
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
           .append("active", server.isActive());
        
        return doc;
    }
}