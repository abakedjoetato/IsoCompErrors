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
    private String sftpPassword;
    private int playerCount;
    private int maxPlayers;
    private boolean online;
    private long logChannelId;
    private boolean useSftpForLogs;
    private long lastProcessedTimestamp;
    private String username;
    private String password;
    
    public GameServer() {
        // Default constructor
        this.active = true;
        this.lastProcessedTimestamp = 0L;
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
    
    public String getSftpPassword() {
        return sftpPassword != null ? sftpPassword : ftpPassword;
    }
    
    public void setSftpPassword(String sftpPassword) {
        this.sftpPassword = sftpPassword;
    }
    
    public int getPlayerCount() {
        return playerCount;
    }
    
    public void setPlayerCount(int playerCount) {
        this.playerCount = playerCount;
    }
    
    public int getMaxPlayers() {
        return maxPlayers;
    }
    
    public void setMaxPlayers(int maxPlayers) {
        this.maxPlayers = maxPlayers;
    }
    
    public boolean isOnline() {
        return online;
    }
    
    public void setOnline(boolean online) {
        this.online = online;
    }
    
    public long getLogChannelId() {
        return logChannelId;
    }
    
    public void setLogChannelId(long logChannelId) {
        this.logChannelId = logChannelId;
    }
    
    public boolean isUseSftpForLogs() {
        return useSftpForLogs;
    }
    
    public void setUseSftpForLogs(boolean useSftpForLogs) {
        this.useSftpForLogs = useSftpForLogs;
    }
    
    // Alias methods for compatibility
    public String getServerId() {
        return id;
    }
    
    public String getDeathlogsDirectory() {
        return logPath != null ? logPath + "/deathlogs" : "/deathlogs";
    }
    
    public void setDeathlogsDirectory(String path) {
        // Extract base log path from deathlogs path
        if (path != null) {
            if (path.endsWith("/deathlogs")) {
                this.logPath = path.substring(0, path.length() - 10);
            } else {
                this.logPath = path;
            }
        }
    }
    
    public String getLogDirectory() {
        return logPath;
    }
    
    public void setLogDirectory(String path) {
        this.logPath = path;
    }
    
    public String getUsername() {
        return username != null ? username : ftpUsername;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getPassword() {
        return password != null ? password : ftpPassword;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    public int getPort() {
        return ftpPort;
    }
    
    public void setPort(int port) {
        this.ftpPort = port;
        this.sftpPort = port;
    }
    
    public long getLastProcessedTimestamp() {
        return lastProcessedTimestamp;
    }
    
    public void setLastProcessedTimestamp(long lastProcessedTimestamp) {
        this.lastProcessedTimestamp = lastProcessedTimestamp;
    }
    
    public void synchronizeCredentials() {
        // Ensure all credentials are synchronized
        if (this.sftpHost == null) this.sftpHost = this.ftpHost;
        if (this.sftpUsername == null) this.sftpUsername = this.ftpUsername;
        if (this.sftpPassword == null) this.sftpPassword = this.ftpPassword;
        if (this.sftpPort <= 0) this.sftpPort = this.ftpPort;
        
        if (this.host == null) this.host = this.serverIp;
        if (this.username == null) this.username = this.ftpUsername;
        if (this.password == null) this.password = this.ftpPassword;
    }
    
    // Constructor with all parameters for backwards compatibility
    public GameServer(String serverName, String serverIp, int gamePort, String ftpHost, 
                     String ftpUsername, String ftpPassword, long guildId) {
        this();
        this.serverName = serverName;
        this.serverIp = serverIp;
        this.gamePort = gamePort;
        this.ftpHost = ftpHost;
        this.ftpUsername = ftpUsername;
        this.ftpPassword = ftpPassword;
        this.guildId = guildId;
        this.name = serverName;
        this.host = serverIp;
        this.sftpHost = ftpHost;
        this.sftpUsername = ftpUsername;
        this.sftpPassword = ftpPassword;
    }
    
    // Constructor with server ID for use in ServerCommand
    public GameServer(String serverId, String serverName, int gamePort, String ftpHost,
                     String ftpUsername, String ftpPassword, long guildId) {
        this();
        this.id = serverId;
        this.serverName = serverName;
        this.gamePort = gamePort;
        this.ftpHost = ftpHost;
        this.ftpUsername = ftpUsername;
        this.ftpPassword = ftpPassword;
        this.guildId = guildId;
        this.name = serverName;
        this.sftpHost = ftpHost;
        this.sftpUsername = ftpUsername;
        this.sftpPassword = ftpPassword;
        this.serverIp = "127.0.0.1"; // Default value
        this.host = this.serverIp;
        this.username = ftpUsername;
        this.password = ftpPassword;
    }
}