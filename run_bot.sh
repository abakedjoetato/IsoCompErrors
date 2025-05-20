#!/bin/bash

# Check if MongoDB URI is set
if [ -z "$MONGO_URI" ]; then
    echo "MONGO_URI environment variable is not set. Using default local URI."
    export MONGO_URI="mongodb://localhost:27017/deadside"
fi

# Check if Discord token is set
if [ -z "$DISCORD_TOKEN" ]; then
    echo "ERROR: DISCORD_TOKEN environment variable is not set. Cannot start bot."
    exit 1
fi

# Run the Deadside Discord bot using compiled JAR
java -jar target/deadside-bot-1.0-SNAPSHOT-jar-with-dependencies.jar