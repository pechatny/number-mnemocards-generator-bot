# Стадия сборки
FROM maven:3.8.4-openjdk-11 AS builder
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn package -DskipTests

# Стадия запуска (оптимизированная)
FROM openjdk:11.0.16-jre-slim

# Устанавливаем временную зону и очищаем кэш
RUN apt-get update && \
    apt-get install -y --no-install-recommends tzdata && \
    apt-get install -y --no-install-recommends iputils-ping && \
    rm -rf /var/lib/apt/lists/* && \
    apt-get clean

WORKDIR /app

# Копируем только JAR файл
COPY --from=builder /app/target/*.jar app.jar

# Создаем пользователя
RUN groupadd -r spring && useradd --no-log-init -r -g spring spring

# Меняем владельца ВСЕХ файлов в рабочей директории
RUN chown -R spring:spring /app

USER spring

EXPOSE 8080

# Оптимизированные параметры Java
ENTRYPOINT ["java", \
    "-XX:+UseContainerSupport", \
    "-XX:MaxRAMPercentage=75.0", \
    "-Djava.security.egd=file:/dev/./urandom", \
    "-Dfile.encoding=UTF-8", \
    "-jar", \
    "app.jar"]
