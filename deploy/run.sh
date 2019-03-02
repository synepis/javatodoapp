#!/usr/bin/env bash
echo 'Starting JavaTodoApp'

source 'set-version.sh'
echo "Running ${APPLICATION_JAR_FILENAME}"

cd '/home/ubuntu/javatodoapp'
java -jar ${APPLICATION_JAR_FILENAME}