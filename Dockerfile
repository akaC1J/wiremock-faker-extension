# ---- build extension ----
FROM harbor.wildberries.ru/docker-hub-proxy/library/eclipse-temurin:17.0.10_7-jdk AS build
WORKDIR /src

# Копируем wrapper и файлы сборки первыми для кэша
COPY gradlew ./
COPY gradle ./gradle
COPY build.gradle settings.gradle gradle.properties ./

# Права на gradlew
RUN chmod +x ./gradlew

# Прогрев зависимостей (зафиксирует слои)
RUN ./gradlew --no-daemon help > /dev/null

# Исходники
COPY src ./src
RUN ./gradlew --no-daemon shadowJar

# ---- wiremock runtime ----
FROM harbor.wildberries.ru/docker-hub-proxy/wiremock/wiremock:3.13.2
LABEL description="WireMock image with customized faker-extension"

COPY --from=build /src/build/libs/*standalone*.jar /var/wiremock/extensions/wiremock-faker-extension.jar

EXPOSE 8080
