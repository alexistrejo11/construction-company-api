FROM ghcr.io/graalvm/native-image-community:25 AS build

WORKDIR /workspace

COPY gradlew build.gradle settings.gradle ./
COPY gradle gradle

RUN chmod +x gradlew
RUN ./gradlew --no-daemon dependencies

COPY src src

# Include production-only Flyway configuration during native AOT processing.
ENV SPRING_PROFILES_ACTIVE=prod
RUN ./gradlew --no-daemon nativeCompile

FROM debian:bookworm-slim

WORKDIR /app

RUN groupadd --system spring && useradd --system --gid spring spring
RUN mkdir uploads && chown spring:spring uploads

COPY --from=build /workspace/build/native/nativeCompile/construction-company-api app

USER spring

EXPOSE 8017

ENTRYPOINT ["./app"]
