package com.deadside.bot.db.models;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents a player faction in the game
 */
public class Faction {
    private String id;
    private String name;
    private String description;
    private String leaderId;
    private List<String> officerIds;
    private List<String> memberIds;
    private Map<String, Object> stats;
    private long creationTimestamp;
    private long totalKills;
    private long totalDeaths;
    private long totalCoins;
    private boolean active;
    private String bannerUrl;
    private String colorHex;
    private long lastActiveTimestamp;
    private long guildId;
    private String serverId;

    public Faction() {
        this.officerIds = new ArrayList<>();
        this.memberIds = new ArrayList<>();
        this.stats = new HashMap<>();
        this.creationTimestamp = System.currentTimeMillis();
        this.totalKills = 0;
        this.totalDeaths = 0;
        this.totalCoins = 0;
        this.active = true;
        this.lastActiveTimestamp = System.currentTimeMillis();
        this.colorHex = "#E34234"; // Default Deadside red
    }

    public Faction(String name, String leaderId) {
        this();
        this.name = name;
        this.leaderId = leaderId;
        this.memberIds.add(leaderId); // Leader is also a member
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLeaderId() {
        return leaderId;
    }

    public void setLeaderId(String leaderId) {
        this.leaderId = leaderId;
        if (!memberIds.contains(leaderId)) {
            memberIds.add(leaderId);
        }
    }

    public List<String> getOfficerIds() {
        return officerIds;
    }

    public void setOfficerIds(List<String> officerIds) {
        this.officerIds = officerIds;
    }

    public void addOfficer(String playerId) {
        if (!officerIds.contains(playerId)) {
            officerIds.add(playerId);
        }
        if (!memberIds.contains(playerId)) {
            memberIds.add(playerId);
        }
    }

    public void removeOfficer(String playerId) {
        officerIds.remove(playerId);
    }

    public boolean isOfficer(String playerId) {
        return officerIds.contains(playerId);
    }

    public List<String> getMemberIds() {
        return memberIds;
    }

    public void setMemberIds(List<String> memberIds) {
        this.memberIds = memberIds;
    }

    public void addMember(String playerId) {
        if (!memberIds.contains(playerId)) {
            memberIds.add(playerId);
        }
    }

    public void removeMember(String playerId) {
        memberIds.remove(playerId);
        officerIds.remove(playerId);
        if (leaderId != null && leaderId.equals(playerId)) {
            leaderId = null;
        }
    }

    public boolean isMember(String playerId) {
        return memberIds.contains(playerId);
    }

    public boolean isLeader(String playerId) {
        return playerId != null && playerId.equals(leaderId);
    }

    public int getMemberCount() {
        return memberIds.size();
    }

    public Map<String, Object> getStats() {
        return stats;
    }

    public void setStats(Map<String, Object> stats) {
        this.stats = stats;
    }

    public Object getStat(String key) {
        return stats.get(key);
    }

    public void setStat(String key, Object value) {
        stats.put(key, value);
    }

    public long getCreationTimestamp() {
        return creationTimestamp;
    }

    public void setCreationTimestamp(long creationTimestamp) {
        this.creationTimestamp = creationTimestamp;
    }

    public long getTotalKills() {
        return totalKills;
    }

    public void setTotalKills(long totalKills) {
        this.totalKills = totalKills;
    }

    public void incrementKills() {
        this.totalKills++;
    }

    public void incrementKills(long amount) {
        this.totalKills += amount;
    }

    public long getTotalDeaths() {
        return totalDeaths;
    }

    public void setTotalDeaths(long totalDeaths) {
        this.totalDeaths = totalDeaths;
    }

    public void incrementDeaths() {
        this.totalDeaths++;
    }

    public void incrementDeaths(long amount) {
        this.totalDeaths += amount;
    }

    public double getKDR() {
        if (totalDeaths == 0) {
            return totalKills;
        }
        return (double) totalKills / totalDeaths;
    }

    public long getTotalCoins() {
        return totalCoins;
    }

    public void setTotalCoins(long totalCoins) {
        this.totalCoins = totalCoins;
    }

    public void addCoins(long amount) {
        this.totalCoins += amount;
    }

    public boolean removeCoins(long amount) {
        if (this.totalCoins >= amount) {
            this.totalCoins -= amount;
            return true;
        }
        return false;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getBannerUrl() {
        return bannerUrl;
    }

    public void setBannerUrl(String bannerUrl) {
        this.bannerUrl = bannerUrl;
    }

    public String getColorHex() {
        return colorHex;
    }

    public void setColorHex(String colorHex) {
        this.colorHex = colorHex;
    }

    public long getLastActiveTimestamp() {
        return lastActiveTimestamp;
    }

    public void setLastActiveTimestamp(long lastActiveTimestamp) {
        this.lastActiveTimestamp = lastActiveTimestamp;
    }

    public void updateLastActive() {
        this.lastActiveTimestamp = System.currentTimeMillis();
    }

    public long getGuildId() {
        return guildId;
    }

    public void setGuildId(long guildId) {
        this.guildId = guildId;
    }

    public String getServerId() {
        return serverId;
    }

    public void setServerId(String serverId) {
        this.serverId = serverId;
    }
}