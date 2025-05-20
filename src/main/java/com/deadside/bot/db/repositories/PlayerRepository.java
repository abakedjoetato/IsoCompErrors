package com.deadside.bot.db.repositories;

import com.deadside.bot.db.MongoDBConnection;
import com.deadside.bot.db.models.Player;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;
import com.mongodb.client.model.Updates;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Repository for Player entity
 */
public class PlayerRepository {
    private static final Logger logger = LoggerFactory.getLogger(PlayerRepository.class);
    private static final String COLLECTION_NAME = "players";

    /**
     * Find player by ID
     * @param id Player ID
     * @return Player or null if not found
     */
    public Player findById(String id) {
        try {
            MongoCollection<Document> collection = MongoDBConnection.getCollection(COLLECTION_NAME);
            Document doc = collection.find(Filters.eq("_id", new ObjectId(id))).first();
            
            if (doc == null) {
                return null;
            }
            
            return documentToPlayer(doc);
        } catch (Exception e) {
            logger.error("Error finding player by ID: {}", id, e);
            return null;
        }
    }

    /**
     * Find player by Steam ID
     * @param steamId Steam ID
     * @return Player or null if not found
     */
    public Player findBySteamId(String steamId) {
        try {
            MongoCollection<Document> collection = MongoDBConnection.getCollection(COLLECTION_NAME);
            Document doc = collection.find(Filters.eq("steamId", steamId)).first();
            
            if (doc == null) {
                return null;
            }
            
            return documentToPlayer(doc);
        } catch (Exception e) {
            logger.error("Error finding player by Steam ID: {}", steamId, e);
            return null;
        }
    }

    /**
     * Find player by display name (case-insensitive)
     * @param name Display name
     * @return Player or null if not found
     */
    public Player findByName(String name) {
        try {
            MongoCollection<Document> collection = MongoDBConnection.getCollection(COLLECTION_NAME);
            Document doc = collection.find(
                Filters.regex("displayName", "^" + name + "$", "i")
            ).first();
            
            if (doc == null) {
                return null;
            }
            
            return documentToPlayer(doc);
        } catch (Exception e) {
            logger.error("Error finding player by name: {}", name, e);
            return null;
        }
    }

    /**
     * Find players by server ID
     * @param serverId Server ID
     * @return List of players
     */
    public List<Player> findByServerId(String serverId) {
        List<Player> players = new ArrayList<>();
        
        try {
            MongoCollection<Document> collection = MongoDBConnection.getCollection(COLLECTION_NAME);
            FindIterable<Document> docs = collection.find(
                Filters.and(
                    Filters.in("servers", serverId),
                    Filters.eq("active", true)
                )
            );
            
            for (Document doc : docs) {
                players.add(documentToPlayer(doc));
            }
        } catch (Exception e) {
            logger.error("Error finding players by server ID: {}", serverId, e);
        }
        
        return players;
    }

    /**
     * Find top players by kill count
     * @param limit Maximum number of players to return
     * @return List of top players
     */
    public List<Player> findTopKillers(int limit) {
        List<Player> players = new ArrayList<>();
        
        try {
            MongoCollection<Document> collection = MongoDBConnection.getCollection(COLLECTION_NAME);
            FindIterable<Document> docs = collection.find(
                Filters.eq("active", true)
            ).sort(
                Sorts.descending("killCount")
            ).limit(limit);
            
            for (Document doc : docs) {
                players.add(documentToPlayer(doc));
            }
        } catch (Exception e) {
            logger.error("Error finding top killers", e);
        }
        
        return players;
    }

    /**
     * Find players by partial name match
     * @param namePattern Name pattern to match
     * @param limit Maximum number of players to return
     * @return List of matching players
     */
    public List<Player> findByNamePattern(String namePattern, int limit) {
        List<Player> players = new ArrayList<>();
        
        try {
            MongoCollection<Document> collection = MongoDBConnection.getCollection(COLLECTION_NAME);
            FindIterable<Document> docs = collection.find(
                Filters.and(
                    Filters.regex("displayName", namePattern, "i"),
                    Filters.eq("active", true)
                )
            ).limit(limit);
            
            for (Document doc : docs) {
                players.add(documentToPlayer(doc));
            }
        } catch (Exception e) {
            logger.error("Error finding players by name pattern: {}", namePattern, e);
        }
        
        return players;
    }

    /**
     * Save a player (insert or update)
     * @param player Player to save
     * @return Saved player
     */
    public Player save(Player player) {
        try {
            MongoCollection<Document> collection = MongoDBConnection.getCollection(COLLECTION_NAME);
            Document doc = playerToDocument(player);
            
            if (player.getId() == null) {
                // Insert new player
                collection.insertOne(doc);
                
                // Get the ID
                player.setId(doc.getObjectId("_id").toString());
            } else {
                // Update existing player
                UpdateResult result = collection.replaceOne(
                    Filters.eq("_id", new ObjectId(player.getId())),
                    doc
                );
                
                if (result.getMatchedCount() == 0) {
                    logger.warn("No player found with ID: {}", player.getId());
                    return null;
                }
            }
            
            return player;
        } catch (Exception e) {
            logger.error("Error saving player: {}", player.getDisplayName(), e);
            return null;
        }
    }

    /**
     * Get or create a player
     * @param steamId Steam ID
     * @param playerName Player name
     * @return Player
     */
    public Player getOrCreate(String steamId, String playerName) {
        Player player = findBySteamId(steamId);
        
        if (player == null) {
            player = new Player(steamId, playerName);
            save(player);
        } else if (!playerName.equals(player.getPlayerName())) {
            // Update player name if it has changed
            player.setPlayerName(playerName);
            save(player);
        }
        
        return player;
    }

    /**
     * Update player last seen timestamp
     * @param playerId Player ID
     * @param timestamp Last seen timestamp
     * @return true if updated, false otherwise
     */
    public boolean updateLastSeen(String playerId, long timestamp) {
        try {
            MongoCollection<Document> collection = MongoDBConnection.getCollection(COLLECTION_NAME);
            UpdateResult result = collection.updateOne(
                Filters.eq("_id", new ObjectId(playerId)),
                Updates.set("lastSeen", timestamp)
            );
            
            return result.getModifiedCount() > 0;
        } catch (Exception e) {
            logger.error("Error updating last seen for player: {}", playerId, e);
            return false;
        }
    }

    /**
     * Delete a player
     * @param id Player ID
     * @return true if deleted, false otherwise
     */
    public boolean delete(String id) {
        try {
            MongoCollection<Document> collection = MongoDBConnection.getCollection(COLLECTION_NAME);
            DeleteResult result = collection.deleteOne(Filters.eq("_id", new ObjectId(id)));
            
            return result.getDeletedCount() > 0;
        } catch (Exception e) {
            logger.error("Error deleting player with ID: {}", id, e);
            return false;
        }
    }

    /**
     * Convert document to player
     * @param doc MongoDB document
     * @return Player object
     */
    @SuppressWarnings("unchecked")
    private Player documentToPlayer(Document doc) {
        Player player = new Player();
        
        player.setId(doc.getObjectId("_id").toString());
        player.setSteamId(doc.getString("steamId"));
        player.setDisplayName(doc.getString("displayName"));
        player.setLastSeen(doc.getLong("lastSeen", 0));
        player.setKillCount(doc.getInteger("killCount", 0));
        player.setDeathCount(doc.getInteger("deathCount", 0));
        player.setTotalPlaytime(doc.getInteger("totalPlaytime", 0));
        player.setTotalDamageDealt(doc.getLong("totalDamageDealt", 0));
        player.setTotalDamageTaken(doc.getLong("totalDamageTaken", 0));
        player.setAccuracy(doc.getDouble("accuracy", 0.0));
        player.setKdr(doc.getDouble("kdr", 0.0));
        
        List<String> servers = (List<String>) doc.get("servers");
        if (servers != null) {
            player.setServers(servers);
        }
        
        Document statsDoc = (Document) doc.get("stats");
        if (statsDoc != null) {
            for (String key : statsDoc.keySet()) {
                player.addStat(key, statsDoc.get(key));
            }
        }
        
        Document weaponStatsDoc = (Document) doc.get("weaponStats");
        if (weaponStatsDoc != null) {
            for (String weapon : weaponStatsDoc.keySet()) {
                player.getWeaponStats().put(weapon, weaponStatsDoc.getInteger(weapon));
            }
        }
        
        player.setCoins(doc.getLong("coins", 0));
        player.setLastServer(doc.getString("lastServer"));
        player.setActive(doc.getBoolean("active", true));
        
        return player;
    }

    /**
     * Convert player to document
     * @param player Player object
     * @return MongoDB document
     */
    private Document playerToDocument(Player player) {
        Document doc = new Document();
        
        if (player.getId() != null) {
            doc.append("_id", new ObjectId(player.getId()));
        }
        
        doc.append("steamId", player.getSteamId())
           .append("displayName", player.getDisplayName())
           .append("lastSeen", player.getLastSeen())
           .append("killCount", player.getKillCount())
           .append("deathCount", player.getDeathCount())
           .append("totalPlaytime", player.getTotalPlaytime())
           .append("totalDamageDealt", player.getTotalDamageDealt())
           .append("totalDamageTaken", player.getTotalDamageTaken())
           .append("accuracy", player.getAccuracy())
           .append("kdr", player.getKdr())
           .append("servers", player.getServers())
           .append("coins", player.getCoins())
           .append("lastServer", player.getLastServer())
           .append("active", player.isActive());
        
        Document statsDoc = new Document();
        for (String key : player.getStats().keySet()) {
            statsDoc.append(key, player.getStats().get(key));
        }
        doc.append("stats", statsDoc);
        
        Document weaponStatsDoc = new Document();
        for (String weapon : player.getWeaponStats().keySet()) {
            weaponStatsDoc.append(weapon, player.getWeaponStats().get(weapon));
        }
        doc.append("weaponStats", weaponStatsDoc);
        
        return doc;
    }
    
    /**
     * Count all players in the database
     * @return Number of players
     */
    public long countAll() {
        try {
            MongoCollection<Document> collection = MongoDBConnection.getCollection(COLLECTION_NAME);
            return collection.countDocuments();
        } catch (Exception e) {
            logger.error("Error counting all players", e);
            return 0;
        }
    }
    
    /**
     * Count active players in the database
     * @return Number of active players
     */
    public long countActive() {
        try {
            MongoCollection<Document> collection = MongoDBConnection.getCollection(COLLECTION_NAME);
            return collection.countDocuments(Filters.eq("active", true));
        } catch (Exception e) {
            logger.error("Error counting active players", e);
            return 0;
        }
    }
    
    /**
     * Add coins to player
     * @param playerId Player ID
     * @param amount Coin amount to add
     * @return true if successful, false otherwise
     */
    public boolean addCoins(String playerId, long amount) {
        try {
            MongoCollection<Document> collection = MongoDBConnection.getCollection(COLLECTION_NAME);
            Bson filter = Filters.eq("_id", new ObjectId(playerId));
            UpdateResult result = collection.updateOne(filter, Updates.inc("coins", amount));
            return result.getModifiedCount() > 0;
        } catch (Exception e) {
            logger.error("Error adding coins to player {}: {}", playerId, e.getMessage());
            return false;
        }
    }
    
    /**
     * Remove coins from player
     * @param playerId Player ID
     * @param amount Coin amount to remove
     * @return true if successful, false otherwise
     */
    public boolean removeCoins(String playerId, long amount) {
        try {
            Player player = findById(playerId);
            if (player == null) {
                return false;
            }
            
            if (player.getCoins() < amount) {
                return false;
            }
            
            MongoCollection<Document> collection = MongoDBConnection.getCollection(COLLECTION_NAME);
            Bson filter = Filters.eq("_id", new ObjectId(playerId));
            UpdateResult result = collection.updateOne(filter, Updates.inc("coins", -amount));
            return result.getModifiedCount() > 0;
        } catch (Exception e) {
            logger.error("Error removing coins from player {}: {}", playerId, e.getMessage());
            return false;
        }
    }
}