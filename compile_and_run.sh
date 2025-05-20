#!/bin/bash

echo "Starting Phase 0 compilation fix for Deadside Discord Bot..."

# Create required directories
mkdir -p target/classes
mkdir -p logs
mkdir -p temp
mkdir -p data/db
mkdir -p data/logs
mkdir -p data/deathlogs

# Make sure test data exists
if [ ! -f data/deathlogs/2025.05.15-00.00.00.csv ]; then
  echo "Creating sample test data..."
  cat > data/deathlogs/2025.05.15-00.00.00.csv << 'EOF'
2025-05-15 00:00:01,kill,Player1,Player2,AK-47,137.5
2025-05-15 00:01:12,kill,Player3,Player4,MP5,42.8
2025-05-15 00:02:33,kill,Player2,Player3,M4A1,88.2
2025-05-15 00:03:44,kill,Player1,Player4,SVD,242.1
2025-05-15 00:04:55,kill,Player4,Player1,Knife,5.3
EOF
fi

# Check if lib directory exists and has jar files
if [ ! -d "lib" ] || [ "$(find lib -name "*.jar" 2>/dev/null | wc -l)" -eq 0 ]; then
  echo "Creating lib directory and copying dependencies from Maven..."
  mkdir -p lib
  mvn dependency:copy-dependencies -DoutputDirectory=lib
fi

# Build classpath
CLASSPATH="target/classes"
for jar in lib/*.jar; do
  if [ -f "$jar" ]; then
    CLASSPATH="$CLASSPATH:$jar"
  fi
done

# Compile
echo "Compiling Java files..."
javac -d target/classes -cp "$CLASSPATH" $(find src/main/java -name "*.java")

# Check if compilation succeeded
if [ $? -eq 0 ]; then
  echo "Compilation successful! Starting DeadsideBot..."
  java -cp "$CLASSPATH" com.deadside.bot.Main
else
  echo "Compilation failed. Please check the error messages above."
  exit 1
fi