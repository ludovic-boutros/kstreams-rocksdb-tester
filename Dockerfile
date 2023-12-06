# Start with a base image containing Java runtime
FROM arm64v8/openjdk:17

# Add Maintainer Info
LABEL maintainer="lboutros@confluent.io"

# Add a volume pointing to /tmp
VOLUME /tmp

# Make port 8080 available to the world outside this container
EXPOSE 8080

# The application's jar file
ARG JAR_FILE=target/kstreams-rocksdb-tester-1.0-SNAPSHOT-jar-with-dependencies.jar

# Add the application's jar to the container
ADD ${JAR_FILE} my-app.jar

# Run the jar file
ENTRYPOINT ["java","--add-opens=java.base/java.lang.reflect=ALL-UNNAMED","--add-opens=java.base/java.util=ALL-UNNAMED","-jar","/my-app.jar"]