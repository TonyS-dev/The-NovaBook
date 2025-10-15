#!/usr/bin/env bash
set -euo pipefail

# Generate Javadoc and open index.html in default browser
PROJECT_ROOT=$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)
POM_PATH="$PROJECT_ROOT/pom.xml"
JAVADOC_DIR="$PROJECT_ROOT/target/site/apidocs"

if [ ! -f "$POM_PATH" ]; then
  echo "pom.xml not found in project root."
  exit 1
fi

# Generate Javadoc using Maven
mvn javadoc:javadoc

INDEX_HTML="$JAVADOC_DIR/index.html"
if [ ! -f "$INDEX_HTML" ]; then
  echo "Javadoc index.html not found. Generation may have failed."
  exit 1
fi

# Try to open in default browser (Linux: xdg-open, macOS: open, Windows: start)
if command -v xdg-open >/dev/null 2>&1; then
  xdg-open "$INDEX_HTML"
elif command -v open >/dev/null 2>&1; then
  open "$INDEX_HTML"
elif command -v start >/dev/null 2>&1; then
  start "$INDEX_HTML"
else
  echo "Please open $INDEX_HTML manually in your browser."
fi
