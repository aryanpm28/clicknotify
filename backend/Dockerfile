FROM eclipse-temurin:21-jdk

WORKDIR /app

COPY . .

RUN apt-get update \
    && apt-get install -y maven \
    && mvn clean package -DskipTests \
    && rm -rf /var/lib/apt/lists/*

EXPOSE 8080

CMD ["java", "-jar", "target/clicknotify-1.0.0.jar"]
