# Build stage
FROM gradle:jdk17-alpine AS build
WORKDIR /home/gradle/src
# COPY . .
COPY build.gradle .
COPY settings.gradle .
COPY src src

RUN gradle build

# Package stage
FROM alpine:3.18
WORKDIR /app

# Install OpenJDK 17
RUN apk add dumb-init
RUN apk add --no-cache curl
RUN apk add --no-cache --repository=https://dl-cdn.alpinelinux.org/alpine/edge/community/x86_64/ openjdk17-jre-headless
RUN apk update && apk upgrade --no-cache

RUN addgroup --system javauser && adduser -S -s /bin/false -G javauser javauser
COPY --from=build /home/gradle/src/build/libs/*.jar app.jar
RUN chown -R javauser:javauser /app

USER javauser
CMD ["dumb-init", "java", "-jar", "app.jar", "-s"]