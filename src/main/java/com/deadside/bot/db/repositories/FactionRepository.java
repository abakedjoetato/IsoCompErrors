package com.deadside.bot.db.repositories;

import com.deadside.bot.db.MongoDBConnection;
import com.deadside.bot.db.models.Faction;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
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
 * Repository for Faction entity
 */
public class FactionRepository {
    private static final Logger logger = LoggerFactory.getLogger(FactionRepository.class);
    private static final String COLLECTION_NAME = "factions";

    /**
     * Find faction by ID
     * @param id Faction ID
     * @return Faction or null if not found
     */
    public Faction findById(String id) {
        try {
            MongoCollection<Document> collection = MongoDBConnection.getCollection(COLLECTION_NAME);
            Document doc = collection.find(Filters.eq("_id", new ObjectId(id))).first();
            
            if (doc == null) {
                return null;
            }
            
            return documentToFaction(doc);
        } catch (Exception e) {
            logger.error("Error finding faction by ID: {}", id, e);
            return null;
        }
    }
    
    /**
     * Find faction by name and guild ID
     * @param name Faction name
     * @param guildId Guild ID
     * @return Faction or null if not found
     */
    public Faction findByNameAndGuildId(String name, long guildId) {
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
            
            return documentToFaction(doc);
        } catch (Exception e) {
            logger.error("Error finding faction by name and guild ID: {}, {}", name, guildId, e);
            return null;
        }
    }
    
    /**
     * Find all factions for a guild
     * @param guildId Guild ID
     * @return List of factions
     */
    public List<Faction> findByGuildId(long guildId) {
        List<Faction> factions = new ArrayList<>();
        
        try {
            MongoCollection<Document> collection = MongoDBConnection.getCollection(COLLECTION_NAME);
            FindIterable<Document> docs = collection.find(Filters.eq("guildId", guildId));
            
            for (Document doc : docs) {
                factions.add(documentToFaction(doc));
            }
        } catch (Exception e) {
            logger.error("Error finding factions by guild ID: {}", guildId, e);
        }
        
        return factions;
    }
    
    /**
     * Find faction by member ID
     * @param playerId Player ID
     * @return Faction or null if not found
     */
    public Faction findByMemberId(String playerId) {
        try {
            MongoCollection<Document> collection = MongoDBConnection.getCollection(COLLECTION_NAME);
            Document doc = collection.find(Filters.in("memberIds", playerId)).first();
            
            if (doc == null) {
                return null;
            }
            
            return documentToFaction(doc);
        } catch (Exception e) {
            logger.error("Error finding faction by member ID: {}", playerId, e);
            return null;
        }
    }
    
    /**
     * Save a faction (insert or update)
     * @param faction Faction to save
     * @return Saved faction
     */
    public Faction save(Faction faction) {
        try {
            MongoCollection<Document> collection = MongoDBConnection.getCollection(COLLECTION_NAME);
            Document doc = factionToDocument(faction);
            
            if (faction.getId() == null) {
                // Insert new faction
                collection.insertOne(doc);
                
                // Get the ID
                faction.setId(doc.getObjectId("_id").toString());
            } else {
                // Update existing faction
                UpdateResult result = collection.replaceOne(
                    Filters.eq("_id", new ObjectId(faction.getId())),
                    doc
                );
                
                if (result.getMatchedCount() == 0) {
                    logger.warn("No faction found with ID: {}", faction.getId());
                    return null;
                }
            }
            
            return faction;
        } catch (Exception e) {
            logger.error("Error saving faction: {}", faction.getName(), e);
            return null;
        }
    }
    
    /**
     * Delete a faction
     * @param id Faction ID
     * @return true if deleted, false otherwise
     */
    public boolean delete(String id) {
        try {
            MongoCollection<Document> collection = MongoDBConnection.getCollection(COLLECTION_NAME);
            DeleteResult result = collection.deleteOne(Filters.eq("_id", new ObjectId(id)));
            
            return result.getDeletedCount() > 0;
        } catch (Exception e) {
            logger.error("Error deleting faction with ID: {}", id, e);
            return false;
        }
    }
    
    /**
     * Add member to faction
     * @param factionId Faction ID
     * @param playerId Player ID
     * @return true if added, false otherwise
     */
    public boolean addMember(String factionId, String playerId) {
        try {
            MongoCollection<Document> collection = MongoDBConnection.getCollection(COLLECTION_NAME);
            
            // First check if player is already in a faction
            if (findByMemberId(playerId) != null) {
                logger.warn("Player {} is already in a faction", playerId);
                return false;
            }
            
            UpdateResult result = collection.updateOne(
                Filters.eq("_id", new ObjectId(factionId)),
                Updates.addToSet("memberIds", playerId)
            );
            
            return result.getModifiedCount() > 0;
        } catch (Exception e) {
            logger.error("Error adding member {} to faction {}", playerId, factionId, e);
            return false;
        }
    }
    
    /**
     * Remove member from faction
     * @param factionId Faction ID
     * @param playerId Player ID
     * @return true if removed, false otherwise
     */
    public boolean removeMember(String factionId, String playerId) {
        try {
            MongoCollection<Document> collection = MongoDBConnection.getCollection(COLLECTION_NAME);
            
            // First check if this member is the leader
            Faction faction = findById(factionId);
            if (faction != null && faction.getLeaderId().equals(playerId)) {
                logger.warn("Cannot remove leader {} from faction {}", playerId, factionId);
                return false;
            }
            
            // Remove from members and officers
            List<Bson> updates = new ArrayList<>();
            updates.add(Updates.pull("memberIds", playerId));
            updates.add(Updates.pull("officerIds", playerId));
            
            UpdateResult result = collection.updateOne(
                Filters.eq("_id", new ObjectId(factionId)),
                Updates.combine(updates)
            );
            
            return result.getModifiedCount() > 0;
        } catch (Exception e) {
            logger.error("Error removing member {} from faction {}", playerId, factionId, e);
            return false;
        }
    }
    
    /**
     * Convert document to faction
     * @param doc MongoDB document
     * @return Faction object
     */
    @SuppressWarnings("unchecked")
    private Faction documentToFaction(Document doc) {
        Faction faction = new Faction();
        
        faction.setId(doc.getObjectId("_id").toString());
        faction.setName(doc.getString("name"));
        faction.setDescription(doc.getString("description"));
        faction.setLeaderId(doc.getString("leaderId"));
        
        List<String> officerIds = (List<String>) doc.get("officerIds");
        if (officerIds != null) {
            faction.setOfficerIds(officerIds);
        }
        
        List<String> memberIds = (List<String>) doc.get("memberIds");
        if (memberIds != null) {
            faction.setMemberIds(memberIds);
        }
        
        Document statsDoc = (Document) doc.get("stats");
        if (statsDoc != null) {
            for (String key : statsDoc.keySet()) {
                faction.setStat(key, statsDoc.get(key));
            }
        }
        
        faction.setCreationTimestamp(doc.getLong("creationTimestamp"));
        faction.setTotalKills(doc.getLong("totalKills"));
        faction.setTotalDeaths(doc.getLong("totalDeaths"));
        faction.setTotalCoins(doc.getLong("totalCoins"));
        faction.setActive(doc.getBoolean("active", true));
        faction.setBannerUrl(doc.getString("bannerUrl"));
        faction.setColorHex(doc.getString("colorHex"));
        faction.setLastActiveTimestamp(doc.getLong("lastActiveTimestamp"));
        faction.setGuildId(doc.getLong("guildId"));
        faction.setServerId(doc.getString("serverId"));
        
        return faction;
    }
    
    /**
     * Convert faction to document
     * @param faction Faction object
     * @return MongoDB document
     */
    private Document factionToDocument(Faction faction) {
        Document doc = new Document();
        
        if (faction.getId() != null) {
            doc.append("_id", new ObjectId(faction.getId()));
        }
        
        doc.append("name", faction.getName())
           .append("description", faction.getDescription())
           .append("leaderId", faction.getLeaderId())
           .append("officerIds", faction.getOfficerIds())
           .append("memberIds", faction.getMemberIds())
           .append("creationTimestamp", faction.getCreationTimestamp())
           .append("totalKills", faction.getTotalKills())
           .append("totalDeaths", faction.getTotalDeaths())
           .append("totalCoins", faction.getTotalCoins())
           .append("active", faction.isActive())
           .append("bannerUrl", faction.getBannerUrl())
           .append("colorHex", faction.getColorHex())
           .append("lastActiveTimestamp", faction.getLastActiveTimestamp())
           .append("guildId", faction.getGuildId())
           .append("serverId", faction.getServerId());
        
        Document statsDoc = new Document();
        for (String key : faction.getStats().keySet()) {
            statsDoc.append(key, faction.getStats().get(key));
        }
        doc.append("stats", statsDoc);
        
        return doc;
    }
}