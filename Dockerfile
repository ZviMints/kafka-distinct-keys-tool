FROM openjdk:8-alpine

# Setting up environment variables
ENV ERVICE_JAR "kafka-distinct-keys-tool.jar"

# Copying app
COPY ./target/scala-*/$ERVICE_JAR /app/$ERVICE_JAR

WORKDIR /app/

# Entry Point
ENTRYPOINT ["sh", "-c"]
CMD ["java -Xmx10000m -jar $ERVICE_JAR"]