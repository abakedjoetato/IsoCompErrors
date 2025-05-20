package com.deadside.bot.db.models;

/**
 * Represents a bounty on a player
 */
public class Bounty {
    private String id;
    private String targetId;
    private String targetName;
    private String issuerId;
    private String issuerName;
    private long amount;
    private long issuedTimestamp;
    private long completedTimestamp;
    private String completedById;
    private String completedByName;
    private boolean completed;
    private String serverId;
    private String serverName;
    private long guildId;
    private String notes;
    private boolean active;

    public Bounty() {
        this.issuedTimestamp = System.currentTimeMillis();
        this.completed = false;
        this.active = true;
    }

    public Bounty(String targetId, String targetName, String issuerId, String issuerName, long amount) {
        this();
        this.targetId = targetId;
        this.targetName = targetName;
        this.issuerId = issuerId;
        this.issuerName = issuerName;
        this.amount = amount;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTargetId() {
        return targetId;
    }

    public void setTargetId(String targetId) {
        this.targetId = targetId;
    }

    public String getTargetName() {
        return targetName;
    }

    public void setTargetName(String targetName) {
        this.targetName = targetName;
    }

    public String getIssuerId() {
        return issuerId;
    }

    public void setIssuerId(String issuerId) {
        this.issuerId = issuerId;
    }

    public String getIssuerName() {
        return issuerName;
    }

    public void setIssuerName(String issuerName) {
        this.issuerName = issuerName;
    }

    public long getAmount() {
        return amount;
    }

    public void setAmount(long amount) {
        this.amount = amount;
    }

    public long getIssuedTimestamp() {
        return issuedTimestamp;
    }

    public void setIssuedTimestamp(long issuedTimestamp) {
        this.issuedTimestamp = issuedTimestamp;
    }

    public long getCompletedTimestamp() {
        return completedTimestamp;
    }

    public void setCompletedTimestamp(long completedTimestamp) {
        this.completedTimestamp = completedTimestamp;
    }

    public String getCompletedById() {
        return completedById;
    }

    public void setCompletedById(String completedById) {
        this.completedById = completedById;
    }

    public String getCompletedByName() {
        return completedByName;
    }

    public void setCompletedByName(String completedByName) {
        this.completedByName = completedByName;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public void complete(String playerId, String playerName) {
        this.completed = true;
        this.completedById = playerId;
        this.completedByName = playerName;
        this.completedTimestamp = System.currentTimeMillis();
    }

    public String getServerId() {
        return serverId;
    }

    public void setServerId(String serverId) {
        this.serverId = serverId;
    }

    public String getServerName() {
        return serverName;
    }

    public void setServerName(String serverName) {
        this.serverName = serverName;
    }

    public long getGuildId() {
        return guildId;
    }

    public void setGuildId(long guildId) {
        this.guildId = guildId;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}