package com.deadside.bot.db.models;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Player model class for storing player data
 */
public class Player {
    private String id;
    private String steamId;
    private String displayName;
    private long lastSeen;
    private int killCount;
    private int deathCount;
    private int totalPlaytime;
    private long totalDamageDealt;
    private long totalDamageTaken;
    private double accuracy;
    private double kdr;
    private List<String> servers;
    private Map<String, Object> stats;
    private Map<String, Integer> weaponStats;
    private long coins;
    private String lastServer;
    private boolean active;

    public Player() {
        this.servers = new ArrayList<>();
        this.stats = new HashMap<>();
        this.weaponStats = new HashMap<>();
        this.active = true;
        this.killCount = 0;
        this.deathCount = 0;
        this.totalPlaytime = 0;
        this.totalDamageDealt = 0;
        this.totalDamageTaken = 0;
        this.accuracy = 0.0;
        this.kdr = 0.0;
        this.coins = 0;
    }

    public Player(String steamId) {
        this();
        this.steamId = steamId;
    }

    public Player(String steamId, String displayName) {
        this(steamId);
        this.displayName = displayName;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSteamId() {
        return steamId;
    }

    public void setSteamId(String steamId) {
        this.steamId = steamId;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public long getLastSeen() {
        return lastSeen;
    }

    public void setLastSeen(long lastSeen) {
        this.lastSeen = lastSeen;
    }

    public int getKillCount() {
        return killCount;
    }

    public void setKillCount(int killCount) {
        this.killCount = killCount;
    }

    public void incrementKills() {
        this.killCount++;
        updateKDR();
    }

    public int getDeathCount() {
        return deathCount;
    }

    public void setDeathCount(int deathCount) {
        this.deathCount = deathCount;
    }

    public void incrementDeaths() {
        this.deathCount++;
        updateKDR();
    }

    public int getTotalPlaytime() {
        return totalPlaytime;
    }

    public void setTotalPlaytime(int totalPlaytime) {
        this.totalPlaytime = totalPlaytime;
    }

    public void incrementPlaytime(int seconds) {
        this.totalPlaytime += seconds;
    }

    public long getTotalDamageDealt() {
        return totalDamageDealt;
    }

    public void setTotalDamageDealt(long totalDamageDealt) {
        this.totalDamageDealt = totalDamageDealt;
    }

    public void incrementDamageDealt(double damage) {
        this.totalDamageDealt += Math.round(damage);
    }

    public long getTotalDamageTaken() {
        return totalDamageTaken;
    }

    public void setTotalDamageTaken(long totalDamageTaken) {
        this.totalDamageTaken = totalDamageTaken;
    }

    public void incrementDamageTaken(double damage) {
        this.totalDamageTaken += Math.round(damage);
    }

    public double getAccuracy() {
        return accuracy;
    }

    public void setAccuracy(double accuracy) {
        this.accuracy = accuracy;
    }

    public double getKdr() {
        return kdr;
    }

    public void setKdr(double kdr) {
        this.kdr = kdr;
    }

    private void updateKDR() {
        if (deathCount == 0) {
            this.kdr = killCount;
        } else {
            this.kdr = (double) killCount / deathCount;
        }
    }

    public List<String> getServers() {
        return servers;
    }

    public void setServers(List<String> servers) {
        this.servers = servers;
    }

    public void addServer(String serverId) {
        if (!this.servers.contains(serverId)) {
            this.servers.add(serverId);
        }
    }

    public Map<String, Object> getStats() {
        return stats;
    }

    public void setStats(Map<String, Object> stats) {
        this.stats = stats;
    }

    public void addStat(String key, Object value) {
        this.stats.put(key, value);
    }

    public Object getStat(String key) {
        return this.stats.get(key);
    }

    public Map<String, Integer> getWeaponStats() {
        return weaponStats;
    }

    public void setWeaponStats(Map<String, Integer> weaponStats) {
        this.weaponStats = weaponStats;
    }

    public int getWeaponKills(String weapon) {
        return weaponStats.getOrDefault(weapon, 0);
    }

    public void incrementWeaponKills(String weapon) {
        int current = weaponStats.getOrDefault(weapon, 0);
        weaponStats.put(weapon, current + 1);
    }

    public long getCoins() {
        return coins;
    }

    public void setCoins(long coins) {
        this.coins = coins;
    }

    public void addCoins(long amount) {
        this.coins += amount;
    }

    public boolean removeCoins(long amount) {
        if (this.coins >= amount) {
            this.coins -= amount;
            return true;
        }
        return false;
    }

    public String getLastServer() {
        return lastServer;
    }

    public void setLastServer(String lastServer) {
        this.lastServer = lastServer;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    // Method required by PlayerRepository getOrCreate
    public String getPlayerName() {
        return displayName;
    }
    
    // Method required by PlayerRepository getOrCreate
    public void setPlayerName(String name) {
        this.displayName = name;
    }
}