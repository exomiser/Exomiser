# Stage 1: Build the exomiser-cli application
FROM eclipse-temurin:21-jdk AS builder

# Set the working directory
WORKDIR /build

# Copy the maven wrapper and pom files
COPY mvnw .
COPY . .

# Build and install phenix-repository to local maven repo first
RUN ./mvnw install -DskipTests -pl phenix-repository -am

# Build only the CLI and its dependencies
RUN ./mvnw clean package -DskipTests -pl exomiser-cli -am

# Stage 2: Create the runtime bash image
# Use eclipse-temurin:25.0.2_10-jre as requested
FROM eclipse-temurin:25.0.2_10-jre

# Define volumes
VOLUME ["/exomiser-data", "/exomiser", "/results"]

# Set the working directory
WORKDIR /app

# Copy the built jar and its dependencies
COPY --from=builder /build/exomiser-cli/target/exomiser-cli-*.jar /app/exomiser-cli.jar
COPY --from=builder /build/exomiser-cli/target/lib /app/lib

# Copy executable exomiser script and make it available on the PATH
COPY exomiser-cli/exomiser /usr/local/bin/exomiser
RUN chmod +x /usr/local/bin/exomiser
