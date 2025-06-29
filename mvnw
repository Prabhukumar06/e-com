#!/bin/sh
# Maven Wrapper Script

# Download Maven if not present
if [ ! -f .mvn/wrapper/maven-wrapper.jar ]; then
    mkdir -p .mvn/wrapper
    curl -L -o .mvn/wrapper/maven-wrapper.jar https://repo.maven.apache.org/maven2/org/apache/maven/wrapper/maven-wrapper/3.2.0/maven-wrapper-3.2.0.jar
fi

MAVEN_OPTS="$MAVEN_OPTS"
exec java $MAVEN_OPTS -jar .mvn/wrapper/maven-wrapper.jar "$@"
