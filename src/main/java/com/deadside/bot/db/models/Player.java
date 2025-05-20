package com.deadside.bot.db.models;

import java.time.Instant;

/**
 * Player model class for storing player data
 */
public class Player {
    private String id;
    private String name;
    private String discordId;
    private long guildId;
    private int kills;
    private int deaths;
    private int suicides;
    private long playtimeMinutes;
    private long currency;
    private Instant lastDailyReward;
    private Instant lastWorkTime;
    
    public Player() {
        // Default constructor
        this.lastDailyReward = Instant.EPOCH;
        this.lastWorkTime = Instant.EPOCH;
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
    
    public String getDiscordId() {
        return discordId;
    }
    
    public void setDiscordId(String discordId) {
        this.discordId = discordId;
    }
    
    public long getGuildId() {
        return guildId;
    }
    
    public void setGuildId(long guildId) {
        this.guildId = guildId;
    }
    
    public int getKills() {
        return kills;
    }
    
    public void setKills(int kills) {
        this.kills = kills;
    }
    
    public int getDeaths() {
        return deaths;
    }
    
    public void setDeaths(int deaths) {
        this.deaths = deaths;
    }
    
    public int getSuicides() {
        return suicides;
    }
    
    public void setSuicides(int suicides) {
        this.suicides = suicides;
    }
    
    public long getPlaytimeMinutes() {
        return playtimeMinutes;
    }
    
    public void setPlaytimeMinutes(long playtimeMinutes) {
        this.playtimeMinutes = playtimeMinutes;
    }
    
    public long getCurrency() {
        return currency;
    }
    
    public void setCurrency(long currency) {
        this.currency = currency;
    }
    
    public Instant getLastDailyReward() {
        return lastDailyReward;
    }
    
    public void setLastDailyReward(Instant lastDailyReward) {
        this.lastDailyReward = lastDailyReward;
    }
    
    public Instant getLastWorkTime() {
        return lastWorkTime;
    }
    
    public void setLastWorkTime(Instant lastWorkTime) {
        this.lastWorkTime = lastWorkTime;
    }
    
    public double getKdRatio() {
        if (deaths == 0) {
            return kills;
        }
        return (double) kills / deaths;
    }
}