import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.regex.*;

public class FixCompilationErrors {
    public static void main(String[] args) throws Exception {
        System.out.println("Fixing compilation errors in Deadside Discord Bot...");
        
        // Add isReadOnly() method to GameServer class if it's missing
        fixGameServerClass();
        
        // Fix CommandManager constructor parameters
        fixCommandManagerClass();
        
        // Fix CommandListener class if needed
        fixCommandListenerClass();
        
        System.out.println("Compilation fixes applied successfully!");
    }
    
    private static void fixGameServerClass() throws Exception {
        Path path = Paths.get("src/main/java/com/deadside/bot/db/models/GameServer.java");
        if (!Files.exists(path)) {
            System.out.println("GameServer class not found at expected location.");
            return;
        }
        
        String content = new String(Files.readAllBytes(path));
        
        // Check if isReadOnly method is missing
        if (!content.contains("isReadOnly()")) {
            System.out.println("Adding isReadOnly() method to GameServer class...");
            
            // Find the last closing brace
            int lastBracePos = content.lastIndexOf("}");
            if (lastBracePos == -1) {
                System.out.println("Could not find closing brace in GameServer class.");
                return;
            }
            
            // Add missing fields and methods before the last brace
            String newMethods = "    private boolean readOnly = false;\n" +
                    "    private String name;\n" +
                    "    private String host;\n" +
                    "    private String sftpHost;\n" +
                    "    private int sftpPort;\n" +
                    "    private String sftpUsername;\n\n" +
                    "    public boolean isReadOnly() {\n" +
                    "        return readOnly;\n" +
                    "    }\n\n" +
                    "    public void setReadOnly(boolean readOnly) {\n" +
                    "        this.readOnly = readOnly;\n" +
                    "    }\n\n" +
                    "    public String getName() {\n" +
                    "        return name != null ? name : serverName;\n" +
                    "    }\n\n" +
                    "    public void setName(String name) {\n" +
                    "        this.name = name;\n" +
                    "    }\n\n" +
                    "    public String getHost() {\n" +
                    "        return host != null ? host : serverIp;\n" +
                    "    }\n\n" +
                    "    public void setHost(String host) {\n" +
                    "        this.host = host;\n" +
                    "    }\n\n" +
                    "    public String getSftpHost() {\n" +
                    "        return sftpHost != null ? sftpHost : ftpHost;\n" +
                    "    }\n\n" +
                    "    public void setSftpHost(String sftpHost) {\n" +
                    "        this.sftpHost = sftpHost;\n" +
                    "    }\n\n" +
                    "    public int getSftpPort() {\n" +
                    "        return sftpPort > 0 ? sftpPort : ftpPort;\n" +
                    "    }\n\n" +
                    "    public void setSftpPort(int sftpPort) {\n" +
                    "        this.sftpPort = sftpPort;\n" +
                    "    }\n\n" +
                    "    public String getSftpUsername() {\n" +
                    "        return sftpUsername != null ? sftpUsername : ftpUsername;\n" +
                    "    }\n\n" +
                    "    public void setSftpUsername(String sftpUsername) {\n" +
                    "        this.sftpUsername = sftpUsername;\n" +
                    "    }\n";
            
            // Insert the methods before the last brace
            content = content.substring(0, lastBracePos) + newMethods + content.substring(lastBracePos);
            
            // Update constructor to initialize new fields
            Pattern constructorPattern = Pattern.compile("public GameServer\(\) \{([^}]+)\}");
            Matcher constructorMatcher = constructorPattern.matcher(content);
            
            if (constructorMatcher.find()) {
                String existingConstructorBody = constructorMatcher.group(1);
                String newConstructorBody = existingConstructorBody + 
                        "        this.readOnly = false;\n" +
                        "        this.name = \"Default Server\";\n";
                content = content.replace(constructorMatcher.group(0), 
                        "public GameServer() {" + newConstructorBody + "    }");
            }
            
            // Save changes
            Files.write(path, content.getBytes());
            System.out.println("GameServer class updated successfully.");
        } else {
            System.out.println("GameServer class already has isReadOnly() method, no changes needed.");
        }
    }
    
    private static void fixCommandManagerClass() throws Exception {
        Path path = Paths.get("src/main/java/com/deadside/bot/commands/CommandManager.java");
        if (!Files.exists(path)) {
            System.out.println("CommandManager class not found at expected location.");
            return;
        }
        
        String content = new String(Files.readAllBytes(path));
        boolean modified = false;
        
        // Check and fix constructor parameters if needed
        Pattern constructorPattern = Pattern.compile("public CommandManager\([^)]+\)");
        Matcher constructorMatcher = constructorPattern.matcher(content);
        
        if (constructorMatcher.find()) {
            String existingConstructor = constructorMatcher.group(0);
            
            // If constructor isn't already fixed
            if (!existingConstructor.contains("JDA jda, Config config")) {
                System.out.println("Fixing CommandManager constructor parameters...");
                String newConstructor = "public CommandManager(JDA jda, Config config)";
                content = content.replace(existingConstructor, newConstructor);
                modified = true;
            }
        }
        
        // Save changes if modified
        if (modified) {
            Files.write(path, content.getBytes());
            System.out.println("CommandManager class updated successfully.");
        } else {
            System.out.println("CommandManager constructor already correct, no changes needed.");
        }
    }
    
    private static void fixCommandListenerClass() throws Exception {
        Path path = Paths.get("src/main/java/com/deadside/bot/listeners/CommandListener.java");
        if (!Files.exists(path)) {
            System.out.println("CommandListener class not found at expected location.");
            return;
        }
        
        String content = new String(Files.readAllBytes(path));
        boolean modified = false;
        
        // Ensure getCommandByName method is properly used
        if (content.contains("commandManager.get") && !content.contains("commandManager.getCommandByName")) {
            System.out.println("Fixing CommandListener to use getCommandByName method...");
            content = content.replace("commandManager.get", "commandManager.getCommandByName");
            modified = true;
        }
        
        // Save changes if modified
        if (modified) {
            Files.write(path, content.getBytes());
            System.out.println("CommandListener class updated successfully.");
        } else {
            System.out.println("CommandListener class already correct, no changes needed.");
        }
    }
}
