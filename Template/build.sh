#!/bin/bash
set -e

echo "Building extension frontend..."
cd frontend
npm run build:all

echo "Building extension backend..."
cd ..
mvn clean package

echo "Build completed, check \"target\" folder."