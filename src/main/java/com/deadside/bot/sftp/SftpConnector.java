package com.deadside.bot.sftp;

import com.deadside.bot.db.models.GameServer;
import com.jcraft.jsch.*;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Handles SFTP connections to game servers for log file retrieval
 */
public class SftpConnector {
    private static final Logger logger = LoggerFactory.getLogger(SftpConnector.class);
    private static final int DEFAULT_SFTP_PORT = 22;
    private static final int DEFAULT_CONNECTION_TIMEOUT = 10000; // 10 seconds
    private static final String LOCAL_TEMP_DIRECTORY = "temp";
    private Map<String, ChannelSftp> activeChannels;
    private Map<String, Session> activeSessions;
    private boolean readOnly = false;
    
    public SftpConnector() {
        this(false);
    }
    
    public SftpConnector(boolean readOnly) {
        this.readOnly = readOnly;
        this.activeChannels = new HashMap<>();
        this.activeSessions = new HashMap<>();
        
        // Create local temp directory if it doesn't exist
        File tempDir = new File(LOCAL_TEMP_DIRECTORY);
        if (!tempDir.exists()) {
            tempDir.mkdirs();
        }
    }
    
    /**
     * Connect to an SFTP server
     * @param host SFTP host
     * @param port SFTP port
     * @param username SFTP username
     * @param password SFTP password
     * @return An SFTP channel
     * @throws JSchException if connection fails
     */
    public ChannelSftp connect(String host, int port, String username, String password) throws JSchException {
        String connectionId = host + ":" + port + ":" + username;
        
        // Return existing connection if available
        if (activeChannels.containsKey(connectionId) && activeChannels.get(connectionId).isConnected()) {
            return activeChannels.get(connectionId);
        }
        
        // Create new connection
        JSch jsch = new JSch();
        Session session = jsch.getSession(username, host, port > 0 ? port : DEFAULT_SFTP_PORT);
        session.setPassword(password);
        
        // Skip host key checking (not recommended for production)
        java.util.Properties config = new java.util.Properties();
        config.put("StrictHostKeyChecking", "no");
        session.setConfig(config);
        session.setTimeout(DEFAULT_CONNECTION_TIMEOUT);
        
        session.connect();
        
        Channel channel = session.openChannel("sftp");
        channel.connect();
        
        ChannelSftp sftpChannel = (ChannelSftp) channel;
        
        // Store the connection
        activeChannels.put(connectionId, sftpChannel);
        activeSessions.put(connectionId, session);
        
        return sftpChannel;
    }
    
    /**
     * Connect to an SFTP server using a GameServer configuration
     * @param server The GameServer configuration
     * @return An SFTP channel
     * @throws JSchException if connection fails
     */
    public ChannelSftp connect(GameServer server) throws JSchException {
        return connect(
                server.getFtpHost(),
                server.getFtpPort(),
                server.getFtpUsername(),
                server.getFtpPassword()
        );
    }
    
    /**
     * Download a file from the SFTP server
     * @param channel The SFTP channel
     * @param remotePath The remote file path
     * @param localFilename The local filename to save to (in the temp directory)
     * @return The local file path
     * @throws SftpException if download fails
     */
    public String downloadFile(ChannelSftp channel, String remotePath, String localFilename) throws SftpException {
        String localPath = LOCAL_TEMP_DIRECTORY + File.separator + localFilename;
        channel.get(remotePath, localPath);
        return localPath;
    }
    
    /**
     * Upload a file to the SFTP server
     * @param channel The SFTP channel
     * @param localPath The local file path
     * @param remotePath The remote file path
     * @throws SftpException if upload fails
     */
    public void uploadFile(ChannelSftp channel, String localPath, String remotePath) throws SftpException {
        if (readOnly) {
            logger.warn("Cannot upload file in read-only mode: {}", remotePath);
            return;
        }
        
        channel.put(localPath, remotePath);
    }
    
    /**
     * List files in a directory on the SFTP server
     * @param channel The SFTP channel
     * @param remotePath The remote directory path
     * @return A list of filenames
     * @throws SftpException if listing fails
     */
    public List<String> listFiles(ChannelSftp channel, String remotePath) throws SftpException {
        List<String> fileList = new ArrayList<>();
        
        java.util.Vector<ChannelSftp.LsEntry> list = channel.ls(remotePath);
        for (ChannelSftp.LsEntry entry : list) {
            if (!entry.getAttrs().isDir()) {
                fileList.add(entry.getFilename());
            }
        }
        
        return fileList;
    }
    
    /**
     * List files in a directory on the SFTP server matching a pattern
     * @param channel The SFTP channel
     * @param remotePath The remote directory path
     * @param pattern A regex pattern to match filenames against
     * @return A list of matching filenames
     * @throws SftpException if listing fails
     */
    public List<String> listFiles(ChannelSftp channel, String remotePath, Pattern pattern) throws SftpException {
        List<String> allFiles = listFiles(channel, remotePath);
        return allFiles.stream()
                .filter(filename -> pattern.matcher(filename).matches())
                .collect(Collectors.toList());
    }
    
    /**
     * Read a file from the SFTP server as a string
     * @param channel The SFTP channel
     * @param remotePath The remote file path
     * @return The file contents as a string
     * @throws SftpException if reading fails
     * @throws IOException if reading fails
     */
    public String readFileAsString(ChannelSftp channel, String remotePath) throws SftpException, IOException {
        // Download to temp file
        String uniqueFilename = "temp_" + System.currentTimeMillis() + "_" + FilenameUtils.getName(remotePath);
        String localPath = downloadFile(channel, remotePath, uniqueFilename);
        
        // Read file content
        String content = new String(Files.readAllBytes(Paths.get(localPath)), StandardCharsets.UTF_8);
        
        // Clean up temp file
        new File(localPath).delete();
        
        return content;
    }
    
    /**
     * Write a string to a file on the SFTP server
     * @param channel The SFTP channel
     * @param content The content to write
     * @param remotePath The remote file path
     * @throws IOException if writing fails
     * @throws SftpException if writing fails
     */
    public void writeStringToFile(ChannelSftp channel, String content, String remotePath) throws IOException, SftpException {
        if (readOnly) {
            logger.warn("Cannot write file in read-only mode: {}", remotePath);
            return;
        }
        
        // Write to temp file
        String uniqueFilename = "temp_" + System.currentTimeMillis() + "_" + FilenameUtils.getName(remotePath);
        String localPath = LOCAL_TEMP_DIRECTORY + File.separator + uniqueFilename;
        
        Path path = Paths.get(localPath);
        Files.write(path, content.getBytes(StandardCharsets.UTF_8));
        
        // Upload to server
        uploadFile(channel, localPath, remotePath);
        
        // Clean up temp file
        new File(localPath).delete();
    }
    
    /**
     * Close all active connections
     */
    public void closeAllConnections() {
        for (ChannelSftp channel : activeChannels.values()) {
            if (channel.isConnected()) {
                channel.disconnect();
            }
        }
        
        for (Session session : activeSessions.values()) {
            if (session.isConnected()) {
                session.disconnect();
            }
        }
        
        activeChannels.clear();
        activeSessions.clear();
    }
    
    /**
     * Get a named server connection string
     * @param host Server host
     * @param port Server port
     * @param username Server username
     * @return Connection string identifier
     */
    private String getConnectionString(String host, int port, String username) {
        return host + ":" + port + ":" + username;
    }
    
    /**
     * Download a file from a configured game server
     * @param server The GameServer to connect to
     * @param remoteFilePath The remote file path on the server
     * @param localFileName The local file name to save as (in the temp directory)
     * @return Path to the downloaded file, or null if failed
     */
    public String downloadFromServer(GameServer server, String remoteFilePath, String localFileName) {
        try {
            logger.info("Downloading file {} from {} (mode: {})",
                    remoteFilePath,
                    server.isReadOnly() ? "read-only" : "disabled isolation", server.getServerName());
        } catch (Exception e) {
            logger.warn("Error downloading from server: {}", e.getMessage());
        }
        
        try {
            ChannelSftp channel = connect(server);
            return downloadFile(channel, remoteFilePath, localFileName);
        } catch (JSchException | SftpException e) {
            logger.error("Failed to download file {} from server: {}", 
                    remoteFilePath, e.getMessage());
            return null;
        }
    }
    
    /**
     * Upload a file to a configured game server
     * @param server The GameServer to connect to
     * @param localFilePath The local file path
     * @param remoteFilePath The remote file path on the server
     * @return true if successful, false otherwise
     */
    public boolean uploadToServer(GameServer server, String localFilePath, String remoteFilePath) {
        if (readOnly) {
            logger.warn("Cannot upload to server in read-only mode: {}", server.getName());
            return false;
        }
        
        try {
            ChannelSftp channel = connect(server);
            uploadFile(channel, localFilePath, remoteFilePath);
            return true;
        } catch (JSchException | SftpException e) {
            logger.error("Failed to upload file {} to server {}: {}", 
                    localFilePath, server.getName(), e.getMessage());
            return false;
        }
    }
    
    /**
     * Get a list of files from a configured game server
     * @param server The GameServer to connect to
     * @param remoteDirPath The remote directory path
     * @return List of files, or null if failed
     */
    public List<String> listServerFiles(GameServer server, String remoteDirPath) {
        try {
            ChannelSftp channel = connect(server);
            return listFiles(channel, remoteDirPath);
        } catch (JSchException | SftpException e) {
            logger.error("Failed to list files in {} on server {}: {}", 
                    remoteDirPath, server.getName(), e.getMessage());
            return new ArrayList<>();
        }
    }
    
    /**
     * Get a list of files matching a pattern from a configured game server
     * @param server The GameServer to connect to
     * @param remoteDirPath The remote directory path
     * @param pattern A regex pattern to match filenames against
     * @return List of matching files, or null if failed
     */
    public List<String> listServerFiles(GameServer server, String remoteDirPath, Pattern pattern) {
        try {
            ChannelSftp channel = connect(server);
            return listFiles(channel, remoteDirPath, pattern);
        } catch (JSchException | SftpException e) {
            logger.error("Failed to list filtered files in {} on server {}: {}", 
                    remoteDirPath, server.getName(), e.getMessage());
            return new ArrayList<>();
        }
    }
    
    /**
     * Read a file from a configured game server as a string
     * @param server The GameServer to connect to
     * @param remoteFilePath The remote file path
     * @return File content as string, or null if failed
     */
    public String readServerFileAsString(GameServer server, String remoteFilePath) {
        try {
            ChannelSftp channel = connect(server);
            return readFileAsString(channel, remoteFilePath);
        } catch (JSchException | SftpException | IOException e) {
            logger.error("Failed to read file {} from server {}: {}", 
                    remoteFilePath, server.getName(), e.getMessage());
            return null;
        }
    }
    
    /**
     * Write a string to a file on a configured game server
     * @param server The GameServer to connect to
     * @param content The content to write
     * @param remoteFilePath The remote file path
     * @return true if successful, false otherwise
     */
    public boolean writeStringToServerFile(GameServer server, String content, String remoteFilePath) {
        if (readOnly) {
            logger.warn("Cannot write to server in read-only mode: {}", server.getName());
            return false;
        }
        
        try {
            ChannelSftp channel = connect(server);
            writeStringToFile(channel, content, remoteFilePath);
            return true;
        } catch (JSchException | IOException | SftpException e) {
            logger.error("Failed to write to file {} on server {}: {}", 
                    remoteFilePath, server.getName(), e.getMessage());
            return false;
        }
    }
    
    /**
     * Check if this connector is in read-only mode
     * @return true if in read-only mode, false otherwise
     */
    public boolean isReadOnly() {
        return readOnly;
    }
    
    /**
     * Set whether this connector is in read-only mode
     * @param readOnly Whether to use read-only mode
     */
    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
    }
}