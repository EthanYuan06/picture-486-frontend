FROM eclipse-temurin:17-jre
WORKDIR /app
COPY *.jar app.jar
EXPOSE 8124
ENTRYPOINT ["java", "-jar", "app.jar", "--spring.profiles.active=prod"]
