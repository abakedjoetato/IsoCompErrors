package com.deadside.bot.db.repositories;

import com.deadside.bot.db.MongoDBConnection;
import com.deadside.bot.db.models.Player;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Repository class for Player objects
 */
public class PlayerRepository {
    private static final Logger logger = Logger.getLogger(PlayerRepository.class.getName());
    private final MongoCollection<Document> collection;
    
    public PlayerRepository() {
        MongoDatabase database = MongoDBConnection.getDatabase();
        this.collection = database.getCollection("players");
    }
    
    public Player findById(String id) {
        Document doc = collection.find(Filters.eq("_id", new ObjectId(id))).first();
        return docToPlayer(doc);
    }
    
    public Player findByNameAndGuildId(String name, String guildIdStr) {
        try {
            long guildId = Long.parseLong(guildIdStr);
            Document doc = collection.find(
                    Filters.and(
                            Filters.eq("name", name),
                            Filters.eq("guildId", guildId)
                    )
            ).first();
            return docToPlayer(doc);
        } catch (NumberFormatException e) {
            logger.warning("Invalid guild ID format: " + guildIdStr);
            return null;
        }
    }
    
    public List<Player> searchByPartialName(String partialName, String guildIdStr) {
        return searchByPartialName(partialName, guildIdStr, 10);
    }
    
    public List<Player> searchByPartialName(String partialName, String guildIdStr, int limit) {
        List<Player> results = new ArrayList<>();
        try {
            long guildId = Long.parseLong(guildIdStr);
            
            // Create a regex pattern for case-insensitive partial matching
            String regexPattern = ".*" + partialName + ".*";
            Bson filter = Filters.and(
                    Filters.regex("name", regexPattern, "i"), // 'i' for case-insensitive
                    Filters.eq("guildId", guildId)
            );
            
            collection.find(filter)
                    .limit(limit)
                    .forEach(doc -> results.add(docToPlayer(doc)));
            
            return results;
        } catch (NumberFormatException e) {
            logger.warning("Invalid guild ID format: " + guildIdStr);
            return results;
        }
    }
    
    public Player findByDiscordId(String discordId, long guildId) {
        Document doc = collection.find(
                Filters.and(
                        Filters.eq("discordId", discordId),
                        Filters.eq("guildId", guildId)
                )
        ).first();
        return docToPlayer(doc);
    }
    
    public List<Player> findAllByGuildId(String guildIdStr) {
        List<Player> results = new ArrayList<>();
        try {
            long guildId = Long.parseLong(guildIdStr);
            collection.find(Filters.eq("guildId", guildId))
                    .forEach(doc -> results.add(docToPlayer(doc)));
            return results;
        } catch (NumberFormatException e) {
            logger.warning("Invalid guild ID format: " + guildIdStr);
            return results;
        }
    }
    
    public List<Player> findTopPlayersByKills(long guildId, int limit) {
        List<Player> results = new ArrayList<>();
        collection.find(Filters.eq("guildId", guildId))
                .sort(Sorts.descending("kills"))
                .limit(limit)
                .forEach(doc -> results.add(docToPlayer(doc)));
        return results;
    }
    
    public List<Player> findTopPlayersByDeaths(long guildId, int limit) {
        List<Player> results = new ArrayList<>();
        collection.find(Filters.eq("guildId", guildId))
                .sort(Sorts.descending("deaths"))
                .limit(limit)
                .forEach(doc -> results.add(docToPlayer(doc)));
        return results;
    }
    
    public void save(Player player) {
        Document doc = playerToDoc(player);
        
        if (player.getId() == null || player.getId().isEmpty()) {
            // Insert new player
            collection.insertOne(doc);
            String id = doc.getObjectId("_id").toString();
            player.setId(id);
        } else {
            // Update existing player
            collection.replaceOne(
                    Filters.eq("_id", new ObjectId(player.getId())),
                    doc
            );
        }
    }
    
    public void delete(String id) {
        collection.deleteOne(Filters.eq("_id", new ObjectId(id)));
    }
    
    private Player docToPlayer(Document doc) {
        if (doc == null) {
            return null;
        }
        
        Player player = new Player();
        player.setId(doc.getObjectId("_id").toString());
        player.setName(doc.getString("name"));
        player.setDiscordId(doc.getString("discordId"));
        player.setGuildId(doc.getLong("guildId"));
        player.setKills(doc.getInteger("kills", 0));
        player.setDeaths(doc.getInteger("deaths", 0));
        player.setSuicides(doc.getInteger("suicides", 0));
        player.setPlaytimeMinutes(doc.getLong("playtimeMinutes", 0L));
        player.setCurrency(doc.getLong("currency", 0L));
        
        // Handle timestamps
        Long lastDailyTimestamp = doc.getLong("lastDailyReward");
        if (lastDailyTimestamp != null) {
            player.setLastDailyReward(Instant.ofEpochMilli(lastDailyTimestamp));
        } else {
            player.setLastDailyReward(Instant.EPOCH);
        }
        
        Long lastWorkTimestamp = doc.getLong("lastWorkTime");
        if (lastWorkTimestamp != null) {
            player.setLastWorkTime(Instant.ofEpochMilli(lastWorkTimestamp));
        } else {
            player.setLastWorkTime(Instant.EPOCH);
        }
        
        return player;
    }
    
    private Document playerToDoc(Player player) {
        Document doc = new Document();
        
        if (player.getId() != null && !player.getId().isEmpty()) {
            doc.append("_id", new ObjectId(player.getId()));
        }
        
        doc.append("name", player.getName())
           .append("discordId", player.getDiscordId())
           .append("guildId", player.getGuildId())
           .append("kills", player.getKills())
           .append("deaths", player.getDeaths())
           .append("suicides", player.getSuicides())
           .append("playtimeMinutes", player.getPlaytimeMinutes())
           .append("currency", player.getCurrency());
        
        // Handle timestamps
        if (player.getLastDailyReward() != null) {
            doc.append("lastDailyReward", player.getLastDailyReward().toEpochMilli());
        }
        
        if (player.getLastWorkTime() != null) {
            doc.append("lastWorkTime", player.getLastWorkTime().toEpochMilli());
        }
        
        return doc;
    }
}