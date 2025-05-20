package com.deadside.bot.isolation;

/**
 * Bootstrap class for isolation functionality
 */
public class IsolationBootstrap {
    private static IsolationBootstrap instance;
    private final DataCleanupTool dataCleanupTool;
    
    private IsolationBootstrap() {
        this.dataCleanupTool = new DataCleanupTool();
    }
    
    public static IsolationBootstrap getInstance() {
        if (instance == null) {
            instance = new IsolationBootstrap();
        }
        return instance;
    }
    
    public DataCleanupTool getDataCleanupTool() {
        return dataCleanupTool;
    }
}