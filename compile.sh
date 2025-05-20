#!/bin/bash

# Create target directories
mkdir -p target/classes

# Build classpath string with all libraries in the lib directory
CLASSPATH="target/classes"
for jar in lib/*.jar; do
  CLASSPATH="$CLASSPATH:$jar"
done

# Compile all Java files
javac -d target/classes -cp "$CLASSPATH" $(find src/main/java -name "*.java")

# Check compilation status
if [ $? -eq 0 ]; then
  echo "Compilation successful!"
else
  echo "Compilation failed."
  exit 1
fi