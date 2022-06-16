FROM openjdk:8-alpine
COPY ./target/scala-*/*.jar /*.jar
CMD ["java", "-cp", "*.jar:scala-library-*.jar", "Main"]