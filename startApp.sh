#!/bin/bash

echo "🚧 Stopping all running Docker containers..."
docker stop $(docker ps -q) 2>/dev/null

echo "🗑️ Removing all Docker containers..."
docker rm $(docker ps -aq) 2>/dev/null

echo "🔥 Removing only student-management-backend images..."
docker rmi -f $(docker images -q --filter=reference='*student-management-backend*') 2>/dev/null

echo "⚡ Running Maven build..."
mvn clean package -DskipTests

echo "🐳 Starting Docker Compose..."
docker compose up --build -d
