FROM eclipse-temurin:25-jdk AS build

WORKDIR /workspace

COPY gradlew build.gradle settings.gradle ./
COPY gradle gradle

RUN chmod +x gradlew
RUN ./gradlew --no-daemon dependencies

COPY src src
RUN ./gradlew --no-daemon bootJar

FROM eclipse-temurin:25-jre

WORKDIR /app

RUN groupadd --system spring && useradd --system --gid spring spring
RUN mkdir uploads data && chown spring:spring uploads data

COPY --from=build /workspace/build/libs/construction-company-api-2.0.0.jar app.jar

USER spring

ENV SPRING_PROFILES_ACTIVE=prod

EXPOSE 8017

ENTRYPOINT ["java", "-jar", "app.jar"]
