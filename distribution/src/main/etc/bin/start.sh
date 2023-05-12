#!/bin/bash
#==========================
# GMP start script
#==========================
#
# Usage:
#   ./start.sh

# Var setup
# Don't allow non initialized vars
set -o nounset

# Exit on failure
set -e

# Verify dependencies
#====================
# Check that java is available
which java > /dev/null || { echo "Need java in PATH to run"; exit 1; }

# Check no other instance is running
#===================================

# Check if gmp is already running
RUNNING=`ps ax | grep "org.apache.felix.main.Main" | grep -v "grep" | wc -l`

if ! [ $RUNNING = "0" ]; then
    echo "GMP is already running"
    exit 1
fi

# Find path to script
#==========================
ABSPATH=$(cd ${0%/*} && echo $PWD/${0##*/})

# to get the path only - not the script name - add
SCRIPT_PATH=`dirname "$ABSPATH"`

echo "Starting GMP version: ${gmp.version}"
# Will start pax-runner reading the configuration from the file bin/runner.args
exec java -jar $SCRIPT_PATH/pax-runner-${pax-runner.version}.jar
