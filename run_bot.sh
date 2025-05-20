#!/bin/bash

# Build classpath string with all libraries in the lib directory
CLASSPATH="target/classes"
for jar in lib/*.jar; do
  CLASSPATH="$CLASSPATH:$jar"
done

# Run the main class
java -cp "$CLASSPATH" com.deadside.bot.Main