FROM eclipse-temurin:26-jdk AS build

WORKDIR /workspace

COPY gradlew build.gradle settings.gradle ./
COPY gradle gradle

RUN chmod +x gradlew
RUN ./gradlew --no-daemon dependencies

COPY src src

RUN ./gradlew --no-daemon bootJar

FROM eclipse-temurin:26-jre

WORKDIR /app

RUN groupadd --system spring && useradd --system --gid spring spring
RUN mkdir uploads && chown spring:spring uploads

COPY --from=build /workspace/build/libs/*.jar app.jar

USER spring

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
