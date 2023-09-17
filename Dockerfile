# Build stage
FROM gradle:jdk18-alpine AS build
WORKDIR /home/gradle/src
COPY . .
RUN gradle build

# Package stage
FROM alpine:3.18
WORKDIR /app

# Install OpenJDK 18
RUN apk add --no-cache curl
RUN curl -LJO "https://github.com/adoptium/temurin18-binaries/releases/download/jdk-18.0.2.1%2B1/OpenJDK18U-jdk_x64_alpine-linux_hotspot_18.0.2.1_1.tar.gz"
RUN tar -xzf OpenJDK18U-jdk_x64_alpine-linux_hotspot_18.0.2.1_1.tar.gz -C /opt
RUN rm OpenJDK18U-jdk_x64_alpine-linux_hotspot_18.0.2.1_1.tar.gz
ENV JAVA_HOME="/opt/jdk-18.0.2.1+1" PATH="${JAVA_HOME}/bin:${PATH}"

COPY --from=build /home/gradle/src/build/libs/*.jar app.jar

CMD ["java", "-jar", "app.jar", "-s"]