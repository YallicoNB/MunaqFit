# Etapa 1: Compilar con Maven
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
# Descargar dependencias (cacheado)
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn clean package -DskipTests -B

# Etapa 2: Imagen runtime ligera
FROM eclipse-temurin:17-jre
WORKDIR /app
ENV JAVA_OPTS=""
# Render inyecta PORT
ENV PORT=8080
COPY --from=build /app/target/*.jar app.jar
EXPOSE $PORT
# Ajuste de memoria para Render free tier
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -Dserver.port=$PORT -jar app.jar"]
