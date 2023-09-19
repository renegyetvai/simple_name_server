# Build stage
FROM gradle:jdk18-alpine AS build
WORKDIR /home/gradle/src
COPY . .
RUN gradle build

# Package stage
FROM alpine:3.18
WORKDIR /app

# Install OpenJDK 18
RUN apk add dumb-init
RUN apk add --no-cache curl
RUN apk add --no-cache --repository=http://dl-cdn.alpinelinux.org/alpine/edge/community openjdk17-jre-headless-17.0.8_p7-r2.apk

RUN addgroup --system javauser && adduser -S -s /bin/false -G javauser javauser
COPY --from=build /home/gradle/src/build/libs/*.jar app.jar
RUN chown -R javauser:javauser /app

USER javauser
CMD ["dumb-init", "java", "-jar", "app.jar", "-s"]