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

# Stage 2: Create the runtime distroless image
# Use the base image defined in pom.xml docker.base.image
FROM gcr.io/distroless/java25-debian13@sha256:4eadd00d3bff73e6a7491dd36653c1d318ac93fb1fb2cd5eef768fd2b4238408

# Define volumes
VOLUME ["/exomiser-data", "/exomiser", "/results"]

# Set the working directory
WORKDIR /app

# Copy the built jar and its dependencies
COPY --from=builder /build/exomiser-cli/target/exomiser-cli-*.jar /app/exomiser-cli.jar
COPY --from=builder /build/exomiser-cli/target/lib /app/lib

# Run the jar
ENTRYPOINT ["java", "-jar", "/app/exomiser-cli.jar"]
