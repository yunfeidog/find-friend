FROM maven:3.5-jdk-8-alpine as builder

# Copy local code to the contaier image
WORKDIR /app
COPY pom.xml .
COPY src ./src
COPY ikunfriend-back-0.0.1.jar ./target/ikunfriend-back-0.0.1.jar

# Build a release artifact
RUN mvn package -DskipTests

# Run the web service on container startup
CMD ["java", "-jar", "/app/target/ikunfriend-back-0.0.1.jar"]
