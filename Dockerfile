# Build Stage
FROM eclipse-temurin:17-jdk-alpine AS build

WORKDIR /app
COPY backend/.mvn/ .mvn/
COPY backend/mvnw backend/pom.xml ./
# Resolve dependencies
RUN ./mvnw dependency:go-offline

# Copy the backend source
COPY backend/src ./src

# Copy the frontend so maven-resources-plugin can find it in ../frontend
COPY frontend /frontend

# Build the application (which will also copy frontend into the static directory)
RUN ./mvnw package -DskipTests

# Run Stage
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Expose port 8080 
EXPOSE 8080

# Copy the jar from build stage
COPY --from=build /app/target/smartpark-1.0.0.jar app.jar

# Setup default placeholder values for environment variables used in Railway/Render
ENV DB_URL="jdbc:h2:file:./data/smartpark;DB_CLOSE_DELAY=-1;AUTO_SERVER=TRUE"
ENV DB_USER="sa"
ENV DB_PASSWORD=""
ENV JWT_SECRET="SmartPark2026SuperSecretKeyForJWTTokenGenerationMustBeAtLeast256BitsLong!!"
ENV STRIPE_API_KEY="sk_test_placeholder"
ENV STRIPE_WEBHOOK_SECRET="whsec_placeholder"

# Execute
ENTRYPOINT ["java", "-jar", "app.jar", "--spring.datasource.url=${DB_URL}", "--spring.datasource.username=${DB_USER}", "--spring.datasource.password=${DB_PASSWORD}", "--app.jwt.secret=${JWT_SECRET}", "--stripe.api.key=${STRIPE_API_KEY}", "--stripe.webhook.secret=${STRIPE_WEBHOOK_SECRET}"]
