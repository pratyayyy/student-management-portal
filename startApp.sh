#!/bin/bash

echo "⚡ Running Maven build..."
mvn clean package -DskipTests

echo "🐳 Starting Docker Compose..."
docker compose up --build -d
