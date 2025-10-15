#!/usr/bin/env bash
set -euo pipefail

# Quick script to bring up postgres with docker-compose and wait until it's ready.
# Usage: ./scripts/init-project.sh

SCRIPT_DIR=$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)
ROOT_DIR=$(cd "$SCRIPT_DIR/.." && pwd)

if [ ! -f "$ROOT_DIR/.env" ]; then
  echo ".env not found in project root. Copy .env.example to .env and edit credentials."
  exit 1
fi

echo "Starting postgres via docker-compose..."

docker compose up -d db

# Wait for postgres to accept connections
echo "Waiting for Postgres to be ready..."

MAX_RETRIES=90
COUNT=0
# Load .env values (export them) so we can support POSTGRES_* or DB_* naming
if [ -f "$ROOT_DIR/.env" ]; then
  # shellcheck disable=SC1090
  set -o allexport
  # shellcheck source=/dev/null
  source "$ROOT_DIR/.env"
  set +o allexport
fi

# Support both POSTGRES_* and DB_* variable names with sensible defaults
PGPASSWORD="${POSTGRES_PASSWORD:-${DB_PASSWORD:-}}"
export PGPASSWORD
DB_HOST="${POSTGRES_HOST:-${DB_HOST:-localhost}}"
DB_PORT="${POSTGRES_PORT:-${DB_PORT:-5432}}"
DB_NAME="${POSTGRES_DB:-${DB_NAME:-}}"
DB_USER="${POSTGRES_USER:-${DB_USER:-}}"

until psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -d "$DB_NAME" -c '\l' > /dev/null 2>&1; do
  COUNT=$((COUNT+1))
  if [ "$COUNT" -ge "$MAX_RETRIES" ]; then
    echo "Postgres did not become ready in time"
    exit 1
  fi
  sleep 1
done

echo "Postgres is ready and migrations in /db/migrations were applied by the container on init (if first run)."

echo "Building the project with Maven..."

# Change to project root and build
if ! command -v mvn >/dev/null 2>&1; then
  echo "Maven (mvn) not found in PATH. Please install Maven or run the build manually."
  echo "Done. To stop the DB: docker compose down"
  exit 0
fi

pushd "$ROOT_DIR" >/dev/null
if mvn package -DskipTests; then
  echo "Maven build succeeded."
else
  echo "Maven build failed. Check the output above."
  popd >/dev/null
  echo "Done. To stop the DB: docker compose down"
  exit 1
fi
popd >/dev/null

# Locate the generated jar. Prefer target/novabook-app.jar (fat jar), else pick the first jar in target
JAR_PATH="$ROOT_DIR/target/novabook-app.jar"
if [ ! -f "$JAR_PATH" ]; then
  # try to find a jar in target
  found=$(find "$ROOT_DIR/target" -maxdepth 1 -type f -name "*.jar" | head -n 1 || true)
  if [ -n "$found" ]; then
    JAR_PATH="$found"
  fi
fi

if [ ! -f "$JAR_PATH" ]; then
  echo "No jar found in target/. Cannot start the application."
  echo "Done. To stop the DB: docker compose down"
  exit 1
fi

echo "Starting JavaFX application using Maven..."
mkdir -p "$ROOT_DIR/logs"

# Start the app in background using Maven JavaFX plugin and redirect logs
nohup mvn javafx:run 2>&1 | awk '{ print strftime("[%Y-%m-%d %H:%M:%S]"), $0; fflush(); }' > "$ROOT_DIR/logs/app.log" &
APP_PID=$!
sleep 2
echo "Application started (PID: $APP_PID). Logs: $ROOT_DIR/logs/app.log"

echo "Done. To stop the DB: docker compose down"
