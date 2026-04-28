#!/usr/bin/env bash
# =============================================================================
# startApp.sh — Local development launcher
#
# Builds the frontend and backend INDEPENDENTLY, then starts the full stack
# via Docker Compose.
#
# Usage:
#   ./startApp.sh              — build everything + start stack
#   ./startApp.sh --skip-fe   — skip frontend build (already built)
#   ./startApp.sh --skip-be   — skip backend build  (already built)
#   ./startApp.sh --skip-fe --skip-be  — only restart docker compose
# =============================================================================
set -euo pipefail

# ── Colour helpers ─────────────────────────────────────────────────────────────
RED='\033[0;31m'; GREEN='\033[0;32m'; YELLOW='\033[1;33m'
CYAN='\033[0;36m'; BOLD='\033[1m'; NC='\033[0m'

log()  { echo -e "${CYAN}${BOLD}[startApp]${NC} $*"; }
ok()   { echo -e "${GREEN}✔${NC}  $*"; }
warn() { echo -e "${YELLOW}⚠${NC}  $*"; }
err()  { echo -e "${RED}✘${NC}  $*" >&2; exit 1; }

# ── Argument parsing ───────────────────────────────────────────────────────────
SKIP_FRONTEND=false
SKIP_BACKEND=false

for arg in "$@"; do
  case $arg in
    --skip-fe) SKIP_FRONTEND=true ;;
    --skip-be) SKIP_BACKEND=true  ;;
    *)         warn "Unknown flag: $arg (ignored)" ;;
  esac
done

# ── Prerequisite checks ────────────────────────────────────────────────────────
command -v node   &>/dev/null || err "node is not installed."
command -v npm    &>/dev/null || err "npm is not installed."
command -v mvn    &>/dev/null || err "mvn is not installed."
command -v docker &>/dev/null || err "docker is not installed."

echo ""
echo -e "${BOLD}╔══════════════════════════════════════════════════╗${NC}"
echo -e "${BOLD}║      IJA Student Portal — Local Dev Launcher     ║${NC}"
echo -e "${BOLD}╚══════════════════════════════════════════════════╝${NC}"
echo ""

# ── Step 1: Build Frontend ─────────────────────────────────────────────────────
# Vite outputs to src/main/resources/static (per vite.config.js) so Spring Boot
# can serve the SPA from localhost:8080 in local dev.
if [ "$SKIP_FRONTEND" = true ]; then
  warn "Skipping frontend build (--skip-fe)."
else
  log "Building frontend (Vite → src/main/resources/static)..."
  pushd frontend > /dev/null
    npm install --prefer-offline --silent
    npm run build        # uses default outDir = ../src/main/resources/static
  popd > /dev/null
  ok "Frontend built → src/main/resources/static"
fi

echo ""

# ── Step 2: Build Backend JAR ──────────────────────────────────────────────────
# -DskipFrontend=true skips the frontend-maven-plugin because we already
# built the frontend above (avoids a redundant double-build).
# -DskipTests speeds up the local loop; run `mvn test` separately when needed.
if [ "$SKIP_BACKEND" = true ]; then
  warn "Skipping backend build (--skip-be)."
else
  log "Building backend JAR (Maven, frontend plugin skipped)..."
  mvn clean package \
    -DskipTests \
    -DskipFrontend=true \
    --batch-mode --no-transfer-progress
  ok "Backend JAR built → target/"
fi

echo ""

# ── Step 3: Start Docker Compose stack ────────────────────────────────────────
# --build rebuilds the app image with the freshly created JAR.
log "Starting Docker Compose stack (app + postgres)..."
docker compose up --build -d

echo ""
ok "Stack is up!"
echo -e "   ${BOLD}App  :${NC} http://localhost:8080"
echo -e "   ${BOLD}DB   :${NC} localhost:5432  (feesDb / postgres / root)"
echo ""
echo -e "   ${CYAN}Tip:${NC} tail logs with  ${BOLD}docker compose logs -f ija-admin-app${NC}"
echo ""
