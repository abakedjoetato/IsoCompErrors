package com.deadside.bot.economy;

import com.deadside.bot.db.models.Player;
import java.time.Instant;

/**
 * Represents an economic transaction between players
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
    private boolean success;
    private String transactionType;
    private boolean systemTransaction;
    
    public Transaction() {
        this.timestamp = Instant.now().toEpochMilli();
        this.success = false;
        this.systemTransaction = false;
        this.transactionType = "transfer";
    }
    
    public Transaction(Player sender, Player receiver, long amount, String reason) {
        this();
        this.senderId = sender.getId();
        this.senderName = sender.getDisplayName();
        this.receiverId = receiver.getId();
        this.receiverName = receiver.getDisplayName();
        this.amount = amount;
        this.reason = reason;
    }
    
    public Transaction(String senderId, String senderName, String receiverId, String receiverName, long amount, String reason) {
        this();
        this.senderId = senderId;
        this.senderName = senderName;
        this.receiverId = receiverId;
        this.receiverName = receiverName;
        this.amount = amount;
        this.reason = reason;
    }
    
    public static Transaction createSystemTransaction(String receiverId, String receiverName, long amount, String reason) {
        Transaction transaction = new Transaction();
        transaction.setSenderId("system");
        transaction.setSenderName("System");
        transaction.setReceiverId(receiverId);
        transaction.setReceiverName(receiverName);
        transaction.setAmount(amount);
        transaction.setReason(reason);
        transaction.setSystemTransaction(true);
        transaction.setTransactionType("system");
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
    
    public boolean isSuccess() {
        return success;
    }
    
    public void setSuccess(boolean success) {
        this.success = success;
    }
    
    public String getTransactionType() {
        return transactionType;
    }
    
    public void setTransactionType(String transactionType) {
        this.transactionType = transactionType;
    }
    
    public boolean isSystemTransaction() {
        return systemTransaction;
    }
    
    public void setSystemTransaction(boolean systemTransaction) {
        this.systemTransaction = systemTransaction;
    }
}