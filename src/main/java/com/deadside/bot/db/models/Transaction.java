package com.deadside.bot.db.models;

/**
 * Represents an economy transaction between players or a system operation
 */
public class Transaction {
    private String id;
    private String senderId;
    private String senderName;
    private String receiverId;
    private String receiverName;
    private long amount;
    private String reason;
    private long timestamp;
    private boolean system;
    private String serverId;
    private String serverName;
    private long guildId;
    private String transactionType;
    private boolean success;
    
    public Transaction() {
        this.timestamp = System.currentTimeMillis();
        this.success = true;
        this.system = false;
    }
    
    public Transaction(String senderId, String senderName, String receiverId, String receiverName, long amount, String reason) {
        this();
        this.senderId = senderId;
        this.senderName = senderName;
        this.receiverId = receiverId;
        this.receiverName = receiverName;
        this.amount = amount;
        this.reason = reason;
        this.transactionType = "transfer";
    }
    
    public static Transaction systemTransaction(String receiverId, String receiverName, long amount, String reason) {
        Transaction transaction = new Transaction();
        transaction.setSenderId("system");
        transaction.setSenderName("System");
        transaction.setReceiverId(receiverId);
        transaction.setReceiverName(receiverName);
        transaction.setAmount(amount);
        transaction.setReason(reason);
        transaction.setSystem(true);
        transaction.setTransactionType("system");
        return transaction;
    }
    
    public static Transaction bountyTransaction(String senderId, String senderName, String receiverId, String receiverName, long amount, String targetName) {
        Transaction transaction = new Transaction();
        transaction.setSenderId(senderId);
        transaction.setSenderName(senderName);
        transaction.setReceiverId(receiverId);
        transaction.setReceiverName(receiverName);
        transaction.setAmount(amount);
        transaction.setReason("Bounty completed on " + targetName);
        transaction.setTransactionType("bounty");
        return transaction;
    }
    
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getSenderId() {
        return senderId;
    }
    
    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }
    
    public String getSenderName() {
        return senderName;
    }
    
    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }
    
    public String getReceiverId() {
        return receiverId;
    }
    
    public void setReceiverId(String receiverId) {
        this.receiverId = receiverId;
    }
    
    public String getReceiverName() {
        return receiverName;
    }
    
    public void setReceiverName(String receiverName) {
        this.receiverName = receiverName;
    }
    
    public long getAmount() {
        return amount;
    }
    
    public void setAmount(long amount) {
        this.amount = amount;
    }
    
    public String getReason() {
        return reason;
    }
    
    public void setReason(String reason) {
        this.reason = reason;
    }
    
    public long getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
    
    public boolean isSystem() {
        return system;
    }
    
    public void setSystem(boolean system) {
        this.system = system;
    }
    
    public String getServerId() {
        return serverId;
    }
    
    public void setServerId(String serverId) {
        this.serverId = serverId;
    }
    
    public String getServerName() {
        return serverName;
    }
    
    public void setServerName(String serverName) {
        this.serverName = serverName;
    }
    
    public long getGuildId() {
        return guildId;
    }
    
    public void setGuildId(long guildId) {
        this.guildId = guildId;
    }
    
    public String getTransactionType() {
        return transactionType;
    }
    
    public void setTransactionType(String transactionType) {
        this.transactionType = transactionType;
    }
    
    public boolean isSuccess() {
        return success;
    }
    
    public void setSuccess(boolean success) {
        this.success = success;
    }
}