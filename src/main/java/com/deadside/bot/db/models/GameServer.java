package com.deadside.bot.db.models;

/**
 * Game server model class for storing server data
 */
public class GameServer {
    private String id;
    private String serverName;
    private String serverIp;
    private int gamePort;
    private String serverVersion;
    private long guildId;
    private String ftpHost;
    private int ftpPort;
    private String ftpUsername;
    private String ftpPassword;
    private String logPath;
    private boolean active;
    private boolean readOnly;
    private String name;
    private String host;
    private String sftpHost;
    private int sftpPort;
    private String sftpUsername;
    
    public GameServer() {
        // Default constructor
        this.active = true;
        this.ftpPort = 21; // Default FTP port
        this.readOnly = false;
        this.name = "Default Server";
    }
    
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getServerName() {
        return serverName;
    }
    
    public void setServerName(String serverName) {
        this.serverName = serverName;
        this.name = serverName; // Keep name in sync
    }
    
    public String getServerIp() {
        return serverIp;
    }
    
    public void setServerIp(String serverIp) {
        this.serverIp = serverIp;
        this.host = serverIp; // Keep host in sync
    }
    
    public int getGamePort() {
        return gamePort;
    }
    
    public void setGamePort(int gamePort) {
        this.gamePort = gamePort;
    }
    
    public String getServerVersion() {
        return serverVersion;
    }
    
    public void setServerVersion(String serverVersion) {
        this.serverVersion = serverVersion;
    }
    
    public long getGuildId() {
        return guildId;
    }
    
    public void setGuildId(long guildId) {
        this.guildId = guildId;
    }
    
    public String getFtpHost() {
        return ftpHost;
    }
    
    public void setFtpHost(String ftpHost) {
        this.ftpHost = ftpHost;
        this.sftpHost = ftpHost; // Keep sftpHost in sync
    }
    
    public int getFtpPort() {
        return ftpPort;
    }
    
    public void setFtpPort(int ftpPort) {
        this.ftpPort = ftpPort;
        this.sftpPort = ftpPort; // Keep sftpPort in sync
    }
    
    public String getFtpUsername() {
        return ftpUsername;
    }
    
    public void setFtpUsername(String ftpUsername) {
        this.ftpUsername = ftpUsername;
        this.sftpUsername = ftpUsername; // Keep sftpUsername in sync
    }
    
    public String getFtpPassword() {
        return ftpPassword;
    }
    
    public void setFtpPassword(String ftpPassword) {
        this.ftpPassword = ftpPassword;
    }
    
    public String getLogPath() {
        return logPath;
    }
    
    public void setLogPath(String logPath) {
        this.logPath = logPath;
    }
    
    public boolean isActive() {
        return active;
    }
    
    public void setActive(boolean active) {
        this.active = active;
    }
    
    // Additional methods to support sftp package
    public boolean isReadOnly() {
        return readOnly;
    }
    
    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
    }
    
    public String getName() {
        return name != null ? name : serverName;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getHost() {
        return host != null ? host : serverIp;
    }
    
    public void setHost(String host) {
        this.host = host;
    }
    
    public String getSftpHost() {
        return sftpHost != null ? sftpHost : ftpHost;
    }
    
    public void setSftpHost(String sftpHost) {
        this.sftpHost = sftpHost;
    }
    
    public int getSftpPort() {
        return sftpPort > 0 ? sftpPort : ftpPort;
    }
    
    public void setSftpPort(int sftpPort) {
        this.sftpPort = sftpPort;
    }
    
    public String getSftpUsername() {
        return sftpUsername != null ? sftpUsername : ftpUsername;
    }
    
    public void setSftpUsername(String sftpUsername) {
        this.sftpUsername = sftpUsername;
    }
}