package com.deadside.bot.faction;

import com.deadside.bot.db.models.Faction;
import com.deadside.bot.db.models.Player;
import com.deadside.bot.db.repositories.FactionRepository;
import com.deadside.bot.db.repositories.PlayerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Handles synchronization of statistics between player and faction records
 */
public class FactionStatsSync {
    private static final Logger logger = LoggerFactory.getLogger(FactionStatsSync.class);
    private final FactionRepository factionRepository;
    private final PlayerRepository playerRepository;
    
    public FactionStatsSync(FactionRepository factionRepository, PlayerRepository playerRepository) {
        this.factionRepository = factionRepository;
        this.playerRepository = playerRepository;
    }
    
    /**
     * Synchronize faction stats with player data
     * @param factionId ID of the faction to sync
     */
    public void syncFactionStats(String factionId) {
        try {
            Faction faction = factionRepository.findById(factionId);
            if (faction == null) {
                logger.warn("Cannot sync stats for non-existent faction: {}", factionId);
                return;
            }
            
            logger.info("Syncing stats for faction: {}", faction.getName());
            
            // Reset stats
            long totalKills = 0;
            long totalDeaths = 0;
            long totalCoins = 0;
            
            // Aggregate stats from all members
            for (String memberId : faction.getMemberIds()) {
                Player player = playerRepository.findById(memberId);
                if (player != null) {
                    totalKills += player.getKillCount();
                    totalDeaths += player.getDeathCount();
                    totalCoins += player.getCoins();
                }
            }
            
            // Update faction stats
            faction.setTotalKills(totalKills);
            faction.setTotalDeaths(totalDeaths);
            faction.setTotalCoins(totalCoins);
            
            // Save updated faction
            factionRepository.save(faction);
            logger.info("Successfully synced stats for faction: {}", faction.getName());
        } catch (Exception e) {
            logger.error("Error syncing faction stats: {}", factionId, e);
        }
    }
    
    /**
     * Synchronize all factions for a guild
     * @param guildId ID of the guild
     */
    public void syncAllFactionsForGuild(long guildId) {
        try {
            List<Faction> factions = factionRepository.findByGuildId(guildId);
            logger.info("Syncing {} factions for guild {}", factions.size(), guildId);
            
            for (Faction faction : factions) {
                syncFactionStats(faction.getId());
            }
        } catch (Exception e) {
            logger.error("Error syncing all factions for guild: {}", guildId, e);
        }
    }
    
    /**
     * Add player stats to faction totals
     * @param player Player whose stats to add
     * @param faction Faction to update
     */
    public void addPlayerStatsToFaction(Player player, Faction faction) {
        faction.incrementKills(player.getKillCount());
        faction.incrementDeaths(player.getDeathCount());
        faction.addCoins(player.getCoins());
        faction.updateLastActive();
        factionRepository.save(faction);
    }
    
    /**
     * Subtract player stats from faction totals
     * @param player Player whose stats to subtract
     * @param faction Faction to update
     */
    public void removePlayerStatsFromFaction(Player player, Faction faction) {
        faction.setTotalKills(Math.max(0, faction.getTotalKills() - player.getKillCount()));
        faction.setTotalDeaths(Math.max(0, faction.getTotalDeaths() - player.getDeathCount()));
        faction.removeCoins(player.getCoins());
        faction.updateLastActive();
        factionRepository.save(faction);
    }
}