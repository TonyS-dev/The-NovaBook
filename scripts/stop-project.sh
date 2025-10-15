#!/usr/bin/env bash
set -euo pipefail

echo "Stopping Academic application and database..."

# Stop the app if running (find by jar name)
APP_PID=$(pgrep -f "academic-app.jar" || true)
if [ -n "$APP_PID" ]; then
  echo "Stopping application (PID: $APP_PID)..."
  kill "$APP_PID"
else
  echo "Application is not running."
fi

# Stop the database container
docker compose down

echo "Done."