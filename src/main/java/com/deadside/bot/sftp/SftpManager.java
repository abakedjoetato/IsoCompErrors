package com.deadside.bot.sftp;

import com.deadside.bot.db.models.GameServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Manager class for SFTP operations
 */
public class SftpManager {
    private static final Logger logger = LoggerFactory.getLogger(SftpManager.class);
    private final SftpConnector sftpConnector;
    
    public SftpManager() {
        this.sftpConnector = new SftpConnector();
    }
    
    public boolean testConnection(GameServer server) {
        return sftpConnector.testConnection(server);
    }
    
    public List<String> getKillfeedFiles(GameServer server) {
        return sftpConnector.findDeathlogFiles(server);
    }
    
    public SftpConnector getSftpConnector() {
        return sftpConnector;
    }
}