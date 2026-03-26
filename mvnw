#!/bin/sh
# Maven Wrapper Script

set -e

# Determine working directory
MAVEN_PROJECTBASEDIR="${MAVEN_BASEDIR:-$(pwd)}"

# Locate the maven-wrapper.jar
WRAPPER_JAR="$MAVEN_PROJECTBASEDIR/.mvn/wrapper/maven-wrapper.jar"
WRAPPER_PROPERTIES="$MAVEN_PROJECTBASEDIR/.mvn/wrapper/maven-wrapper.properties"

# Read the distributionUrl
DISTRIBUTION_URL=$(grep "^distributionUrl=" "$WRAPPER_PROPERTIES" | sed 's/distributionUrl=//')

# Resolve the distribution directory name
DIST_NAME=$(basename "$DISTRIBUTION_URL" .zip)
MAVEN_USER_HOME="${MAVEN_USER_HOME:-$HOME/.m2}"
MAVEN_HOME="$MAVEN_USER_HOME/wrapper/dists/$DIST_NAME"

if [ ! -d "$MAVEN_HOME" ]; then
    mkdir -p "$MAVEN_HOME"
    echo "Downloading Maven from $DISTRIBUTION_URL..."
    if command -v curl > /dev/null 2>&1; then
        curl -fsSL -o "$MAVEN_HOME/maven.zip" "$DISTRIBUTION_URL"
    elif command -v wget > /dev/null 2>&1; then
        wget -q -O "$MAVEN_HOME/maven.zip" "$DISTRIBUTION_URL"
    else
        echo "ERROR: Neither curl nor wget is available."
        exit 1
    fi
    unzip -q "$MAVEN_HOME/maven.zip" -d "$MAVEN_HOME"
    rm -f "$MAVEN_HOME/maven.zip"
fi

# Find the mvn executable
MAVEN_BIN=$(find "$MAVEN_HOME" -name "mvn" -type f | head -1)

if [ -z "$MAVEN_BIN" ]; then
    echo "ERROR: Could not find mvn in $MAVEN_HOME"
    exit 1
fi

exec "$MAVEN_BIN" "$@"
