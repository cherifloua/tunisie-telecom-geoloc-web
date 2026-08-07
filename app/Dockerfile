# --- Etape 1 : compilation ---
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn -q clean package -DskipTests

# --- Etape 2 : image finale, plus legere ---
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
# La base SQLite vit ici (./data/sites.db). Sur un hebergeur avec disque
# persistant (ex: "Disks" sur Render), monter le disque sur /app/data pour
# que les donnees survivent aux redeploiements.
VOLUME ["/app/data"]
ENTRYPOINT ["java", "-jar", "app.jar"]
