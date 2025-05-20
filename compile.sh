#!/bin/bash

# Clean target directory but preserve its structure
mkdir -p target/classes

# Build classpath from all JARs in lib directory
CLASSPATH=$(find lib -name "*.jar" | tr '\n' ':')

# Compile all Java sources
echo "Compiling all Java sources..."
javac -d target/classes -cp "$CLASSPATH:target/classes" $(find src/main/java -name "*.java")

if [ $? -eq 0 ]; then
    echo "Compilation successful. All classes compiled to target/classes."
    echo "Running java syntax check..."
    
    # Verify compilation by trying to load the Main class
    java -cp "$CLASSPATH:target/classes" -verify com.deadside.bot.Main
    
    if [ $? -eq 0 ]; then
        echo "Java syntax check passed. Bot is ready to run."
    else
        echo "Java syntax check failed. There might be runtime issues."
    fi
else
    echo "Compilation failed. Please fix the errors and try again."
fi