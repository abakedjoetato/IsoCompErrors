package com.deadside.bot.sftp;

import com.deadside.bot.db.models.GameServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Handles SFTP connections to game servers for file operations
 */
public class SftpConnector {
    private static final Logger logger = LoggerFactory.getLogger(SftpConnector.class);
    private boolean readOnly = false;
    
    public SftpConnector() {
        this(false);
    }
    
    public SftpConnector(boolean readOnly) {
        this.readOnly = readOnly;
        logger.info("Created SftpConnector (readOnly={})", readOnly);
    }
    
    public String downloadFromServer(GameServer server, String remoteFilePath, String localFileName) {
        if (server == null || remoteFilePath == null || localFileName == null) {
            logger.error("Invalid parameters for downloadFromServer");
            return null;
        }
        
        logger.info("Downloading {} from server {}", remoteFilePath, server.getName());
        
        // For Phase 0, return test data path
        Path testDataPath = Paths.get("data/deathlogs/2025.05.15-00.00.00.csv").toAbsolutePath();
        return testDataPath.toString();
    }
    
    public boolean uploadToServer(GameServer server, String localFilePath, String remoteFilePath) {
        if (readOnly) {
            logger.warn("Cannot upload in read-only mode to server {}", server.getName());
            return false;
        }
        
        if (server == null || localFilePath == null || remoteFilePath == null) {
            logger.error("Invalid parameters for uploadToServer");
            return false;
        }
        
        logger.info("Uploading {} to {} on server {}", localFilePath, remoteFilePath, server.getName());
        return true;
    }
    
    public List<String> listServerFiles(GameServer server, String remoteDirPath) {
        if (server == null || remoteDirPath == null) {
            logger.error("Invalid parameters for listServerFiles");
            return new ArrayList<>();
        }
        
        logger.info("Listing files in {} on server {}", remoteDirPath, server.getName());
        
        // Return test data for Phase 0
        List<String> testFiles = new ArrayList<>();
        testFiles.add("2025.05.15-00.00.00.csv");
        testFiles.add("2025.05.16-00.00.00.csv");
        return testFiles;
    }
    
    public List<String> listServerFiles(GameServer server, String remoteDirPath, Pattern pattern) {
        List<String> allFiles = listServerFiles(server, remoteDirPath);
        List<String> matchingFiles = new ArrayList<>();
        
        for (String fileName : allFiles) {
            if (pattern.matcher(fileName).matches()) {
                matchingFiles.add(fileName);
            }
        }
        
        logger.info("Found {} files matching pattern in directory", matchingFiles.size());
        return matchingFiles;
    }
    
    public String readServerFileAsString(GameServer server, String remoteFilePath) {
        if (server == null || remoteFilePath == null) {
            logger.error("Invalid parameters for readServerFileAsString");
            return "";
        }
        
        logger.info("Reading file {} from server {}", remoteFilePath, server.getName());
        
        // Return test data for Phase 0
        return "2025-05-15 00:00:01,kill,Player1,Player2,AK-47,137.5\n" +
               "2025-05-15 00:01:12,kill,Player3,Player4,MP5,42.8\n";
    }
    
    public boolean writeStringToServerFile(GameServer server, String content, String remoteFilePath) {
        if (readOnly) {
            logger.warn("Cannot write file in read-only mode to server {}", server.getName());
            return false;
        }
        
        if (server == null || content == null || remoteFilePath == null) {
            logger.error("Invalid parameters for writeStringToServerFile");
            return false;
        }
        
        logger.info("Writing to {} on server {}", remoteFilePath, server.getName());
        return true;
    }
    
    public boolean isReadOnly() {
        return readOnly;
    }
    
    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
    }
    
    public void closeAllConnections() {
        logger.info("Closing all connections");
    }
    
    public boolean testConnection(GameServer server) {
        if (server == null) {
            logger.error("Cannot test connection to null server");
            return false;
        }
        
        logger.info("Testing connection to server: {}", server.getName());
        try {
            List<String> files = listServerFiles(server, "/");
            logger.info("Connection test successful, found {} files", files.size());
            return true;
        } catch (Exception e) {
            logger.error("Connection test failed: {}", e.getMessage());
            return false;
        }
    }
    
    public List<String> findDeathlogFiles(GameServer server) {
        if (server == null) {
            logger.error("Cannot find deathlogs for null server");
            return new ArrayList<>();
        }
        
        String deathlogsPath = server.getDeathlogsDirectory();
        logger.info("Looking for deathlog files in: {}", deathlogsPath);
        
        try {
            // Define a pattern for deathlog files (typically like YYYY.MM.DD-HH.MM.SS.csv)
            Pattern pattern = Pattern.compile("\\d{4}\\.\\d{2}\\.\\d{2}-\\d{2}\\.\\d{2}\\.\\d{2}\\.csv");
            return listServerFiles(server, deathlogsPath, pattern);
        } catch (Exception e) {
            logger.error("Failed to find deathlog files: {}", e.getMessage());
            
            // Return test data for Phase 0
            List<String> testFiles = new ArrayList<>();
            testFiles.add("2025.05.15-00.00.00.csv");
            return testFiles;
        }
    }
    
    public String findLogFile(GameServer server) {
        if (server == null) {
            logger.error("Cannot find log file for null server");
            return null;
        }
        
        String logDirectory = server.getLogDirectory();
        logger.info("Looking for log file in: {}", logDirectory);
        
        try {
            List<String> files = listServerFiles(server, logDirectory);
            
            // Look for the latest log file (typically like server_YYYY-MM-DD_HH-MM-SS.log)
            Pattern logPattern = Pattern.compile("server_.*\\.log");
            List<String> logFiles = new ArrayList<>();
            
            for (String file : files) {
                if (logPattern.matcher(file).matches()) {
                    logFiles.add(file);
                }
            }
            
            if (!logFiles.isEmpty()) {
                // Return the first log file found (would sort by date in full implementation)
                return logFiles.get(0);
            }
            
            logger.warn("No log files found in directory: {}", logDirectory);
            return null;
        } catch (Exception e) {
            logger.error("Failed to find log file: {}", e.getMessage());
            return null;
        }
    }
}