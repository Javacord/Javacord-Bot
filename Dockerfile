# syntax=docker/dockerfile:1
FROM openjdk:11 as builder
WORKDIR /app
COPY . .
RUN ./gradlew installDist

FROM openjdk:11 
WORKDIR /app
COPY --from=builder /app/build ./
CMD ["sh", "-c", "./install/javacord-bot/bin/javacord-bot $DISCORD_TOKEN"]