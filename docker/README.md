# Docker Configuration Guide

This directory contains containerization guidelines and supporting assets for the **Skill- and Labor Market Demand–Based Career Recommendation System**.

## Services Overview

The system is configured in `docker-compose.yml` with the following services:

1. **`postgres`**: PostgreSQL database service for persistent data storage.
2. **`backend`**: Spring Boot REST API service built using Java 17 and Maven.
3. **`frontend`**: React + TypeScript SPA served via Nginx.

## Usage

From the project root:

```bash
# 1. Copy environment variables template
cp .env.example .env

# 2. Build and run all services
docker compose up --build -d

# 3. View service logs
docker compose logs -f

# 4. Stop all services
docker compose down
```
