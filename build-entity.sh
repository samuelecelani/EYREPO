#!/usr/bin/env bash
# Script: build-entity.sh
# Equivalente bash di build-entity.ps1
# Builda il modulo Common/Entity e copia il jar (e il pom) nelle local-repo di BE e BFF.

set -euo pipefail

REPO_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ENTITY_DIR="$REPO_ROOT/Common/Entity"

if ! command -v mvn >/dev/null 2>&1; then
    echo "ERRORE: Maven non trovato nel PATH. Installa Maven o aggiungilo al PATH." >&2
    exit 1
fi

VERSION="1.0.0"
JAR_NAME="Entity-$VERSION.jar"
POM_NAME="Entity-$VERSION.pom"
SOURCE_JAR="$ENTITY_DIR/target/$JAR_NAME"
SOURCE_POM="$ENTITY_DIR/pom.xml"

COMMON_POM="$REPO_ROOT/Common/pom.xml"
ROOT_POM="$REPO_ROOT/pom.xml"

LOCAL_REPOS=(
    "$REPO_ROOT/dfp-piao-be/local-repo"
    "$REPO_ROOT/dfp-piao-bff/local-repo"
)

echo "==> Build di Common/Entity..."
(
    cd "$ENTITY_DIR"
    mvn -B clean package -DskipTests
)

if [ ! -f "$SOURCE_JAR" ]; then
    echo "ERRORE: Jar non trovato: $SOURCE_JAR" >&2
    exit 1
fi

for repo in "${LOCAL_REPOS[@]}"; do
    ENTITY_DEST="$repo/it/ey/Entity/$VERSION"
    COMMON_DEST="$repo/it/ey/common/$VERSION"
    PIAO_DEST="$repo/it/ey/piao/$VERSION"

    mkdir -p "$ENTITY_DEST" "$COMMON_DEST" "$PIAO_DEST"

    echo "==> Copio Entity in $ENTITY_DEST"
    cp -f "$SOURCE_JAR" "$ENTITY_DEST/$JAR_NAME"
    cp -f "$SOURCE_POM" "$ENTITY_DEST/$POM_NAME"

    echo "==> Copio parent common in $COMMON_DEST"
    cp -f "$COMMON_POM" "$COMMON_DEST/common-$VERSION.pom"

    echo "==> Copio parent piao in $PIAO_DEST"
    cp -f "$ROOT_POM" "$PIAO_DEST/piao-$VERSION.pom"
done

echo "==> Fatto."
