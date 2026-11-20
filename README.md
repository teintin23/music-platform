# Skeleton spring board

## Prerequisites

- **Java 21 (LTS)** — Download from https://adoptium.net/ — Verify: `java -version` should show `21.x.x`
- **Maven (3.8+)** — Download from https://maven.apache.org/download.cgi, or use package manager:
  - macOS: `brew install maven`
  - Linux: `sudo apt install maven` (Ubuntu/Debian) or `sudo dnf install maven` (Fedora)
  - Windows: Download and add to PATH
- **PostgreSQL (14 or newer)** — Download from https://www.postgresql.org/download/ — During installation, remember the password you set for the `postgres` user

## Create Database

```
psql -U postgres
CREATE DATABASE music_streaming;
```

Navigate to the project root and run:

```
psql -U postgres -d music_streaming -f database/schema.sql
```

## Configure Database Credentials

### Method A: Use Default Credentials

If your PostgreSQL password is `postgres`, just run the app. No configuration needed.

### Method B: Set Environment Variables

**Linux/macOS:**
```
export DB_PASSWORD=your_actual_password
```

**Windows (PowerShell):**
```
$env:DB_PASSWORD="your_actual_password"
```

**Windows (Command Prompt):**
```
set DB_PASSWORD=your_actual_password
```

## Start the Application

```
cd backend
mvn spring-boot:run
```

## Test the API

Open browser and go to:

```
http://localhost:8080/swagger-ui.html
```