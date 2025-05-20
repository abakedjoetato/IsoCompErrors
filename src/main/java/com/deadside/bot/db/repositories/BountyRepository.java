package com.deadside.bot.db.repositories;

import com.deadside.bot.db.MongoDBConnection;
import com.deadside.bot.db.models.Bounty;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Repository for Bounty entity
 */
public class BountyRepository {
    private static final Logger logger = LoggerFactory.getLogger(BountyRepository.class);
    private static final String COLLECTION_NAME = "bounties";

    /**
     * Find bounty by ID
     * @param id Bounty ID
     * @return Bounty or null if not found
     */
    public Bounty findById(String id) {
        try {
            MongoCollection<Document> collection = MongoDBConnection.getCollection(COLLECTION_NAME);
            Document doc = collection.find(Filters.eq("_id", new ObjectId(id))).first();
            
            if (doc == null) {
                return null;
            }
            
            return documentToBounty(doc);
        } catch (Exception e) {
            logger.error("Error finding bounty by ID: {}", id, e);
            return null;
        }
    }

    /**
     * Find all active bounties for a guild
     * @param guildId Guild ID
     * @return List of active bounties
     */
    public List<Bounty> findActiveByGuildId(long guildId) {
        List<Bounty> bounties = new ArrayList<>();
        
        try {
            MongoCollection<Document> collection = MongoDBConnection.getCollection(COLLECTION_NAME);
            FindIterable<Document> docs = collection.find(
                Filters.and(
                    Filters.eq("guildId", guildId),
                    Filters.eq("completed", false),
                    Filters.eq("active", true)
                )
            ).sort(Sorts.descending("amount"));
            
            for (Document doc : docs) {
                bounties.add(documentToBounty(doc));
            }
        } catch (Exception e) {
            logger.error("Error finding active bounties by guild ID: {}", guildId, e);
        }
        
        return bounties;
    }

    /**
     * Find all active bounties for a target player
     * @param targetId Target player ID
     * @return List of active bounties on this player
     */
    public List<Bounty> findActiveByTargetId(String targetId) {
        List<Bounty> bounties = new ArrayList<>();
        
        try {
            MongoCollection<Document> collection = MongoDBConnection.getCollection(COLLECTION_NAME);
            FindIterable<Document> docs = collection.find(
                Filters.and(
                    Filters.eq("targetId", targetId),
                    Filters.eq("completed", false),
                    Filters.eq("active", true)
                )
            ).sort(Sorts.descending("amount"));
            
            for (Document doc : docs) {
                bounties.add(documentToBounty(doc));
            }
        } catch (Exception e) {
            logger.error("Error finding active bounties by target ID: {}", targetId, e);
        }
        
        return bounties;
    }

    /**
     * Find all bounties issued by a player
     * @param issuerId Issuer player ID
     * @return List of bounties issued by this player
     */
    public List<Bounty> findByIssuerId(String issuerId) {
        List<Bounty> bounties = new ArrayList<>();
        
        try {
            MongoCollection<Document> collection = MongoDBConnection.getCollection(COLLECTION_NAME);
            FindIterable<Document> docs = collection.find(
                Filters.and(
                    Filters.eq("issuerId", issuerId),
                    Filters.eq("active", true)
                )
            ).sort(Sorts.descending("issuedTimestamp"));
            
            for (Document doc : docs) {
                bounties.add(documentToBounty(doc));
            }
        } catch (Exception e) {
            logger.error("Error finding bounties by issuer ID: {}", issuerId, e);
        }
        
        return bounties;
    }

    /**
     * Save a bounty (insert or update)
     * @param bounty Bounty to save
     * @return Saved bounty
     */
    public Bounty save(Bounty bounty) {
        try {
            MongoCollection<Document> collection = MongoDBConnection.getCollection(COLLECTION_NAME);
            Document doc = bountyToDocument(bounty);
            
            if (bounty.getId() == null) {
                // Insert new bounty
                collection.insertOne(doc);
                
                // Get the ID
                bounty.setId(doc.getObjectId("_id").toString());
            } else {
                // Update existing bounty
                UpdateResult result = collection.replaceOne(
                    Filters.eq("_id", new ObjectId(bounty.getId())),
                    doc
                );
                
                if (result.getMatchedCount() == 0) {
                    logger.warn("No bounty found with ID: {}", bounty.getId());
                    return null;
                }
            }
            
            return bounty;
        } catch (Exception e) {
            logger.error("Error saving bounty for target: {}", bounty.getTargetName(), e);
            return null;
        }
    }

    /**
     * Mark a bounty as completed
     * @param bountyId Bounty ID
     * @param completedById Player ID who completed the bounty
     * @param completedByName Player name who completed the bounty
     * @return true if completed, false otherwise
     */
    public boolean completeBounty(String bountyId, String completedById, String completedByName) {
        try {
            Bounty bounty = findById(bountyId);
            
            if (bounty == null) {
                logger.warn("No bounty found with ID: {}", bountyId);
                return false;
            }
            
            if (bounty.isCompleted()) {
                logger.warn("Bounty already completed: {}", bountyId);
                return false;
            }
            
            bounty.complete(completedById, completedByName);
            return save(bounty) != null;
        } catch (Exception e) {
            logger.error("Error completing bounty: {}", bountyId, e);
            return false;
        }
    }

    /**
     * Delete a bounty
     * @param id Bounty ID
     * @return true if deleted, false otherwise
     */
    public boolean delete(String id) {
        try {
            MongoCollection<Document> collection = MongoDBConnection.getCollection(COLLECTION_NAME);
            DeleteResult result = collection.deleteOne(Filters.eq("_id", new ObjectId(id)));
            
            return result.getDeletedCount() > 0;
        } catch (Exception e) {
            logger.error("Error deleting bounty with ID: {}", id, e);
            return false;
        }
    }

    /**
     * Convert document to bounty
     * @param doc MongoDB document
     * @return Bounty object
     */
    private Bounty documentToBounty(Document doc) {
        Bounty bounty = new Bounty();
        
        bounty.setId(doc.getObjectId("_id").toString());
        bounty.setTargetId(doc.getString("targetId"));
        bounty.setTargetName(doc.getString("targetName"));
        bounty.setIssuerId(doc.getString("issuerId"));
        bounty.setIssuerName(doc.getString("issuerName"));
        bounty.setAmount(doc.getLong("amount"));
        bounty.setIssuedTimestamp(doc.getLong("issuedTimestamp"));
        bounty.setCompletedTimestamp(doc.getLong("completedTimestamp", 0));
        bounty.setCompletedById(doc.getString("completedById"));
        bounty.setCompletedByName(doc.getString("completedByName"));
        bounty.setCompleted(doc.getBoolean("completed", false));
        bounty.setServerId(doc.getString("serverId"));
        bounty.setServerName(doc.getString("serverName"));
        bounty.setGuildId(doc.getLong("guildId"));
        bounty.setNotes(doc.getString("notes"));
        bounty.setActive(doc.getBoolean("active", true));
        
        return bounty;
    }

    /**
     * Convert bounty to document
     * @param bounty Bounty object
     * @return MongoDB document
     */
    private Document bountyToDocument(Bounty bounty) {
        Document doc = new Document();
        
        if (bounty.getId() != null) {
            doc.append("_id", new ObjectId(bounty.getId()));
        }
        
        doc.append("targetId", bounty.getTargetId())
           .append("targetName", bounty.getTargetName())
           .append("issuerId", bounty.getIssuerId())
           .append("issuerName", bounty.getIssuerName())
           .append("amount", bounty.getAmount())
           .append("issuedTimestamp", bounty.getIssuedTimestamp())
           .append("completedTimestamp", bounty.getCompletedTimestamp())
           .append("completedById", bounty.getCompletedById())
           .append("completedByName", bounty.getCompletedByName())
           .append("completed", bounty.isCompleted())
           .append("serverId", bounty.getServerId())
           .append("serverName", bounty.getServerName())
           .append("guildId", bounty.getGuildId())
           .append("notes", bounty.getNotes())
           .append("active", bounty.isActive());
        
        return doc;
    }
}