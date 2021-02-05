FROM gradle:6.7-jdk15 AS build
COPY . /home/gradle/src
WORKDIR /home/gradle/src

RUN gradle shadowJar

FROM openjdk:15-slim

EXPOSE 8080

RUN mkdir /app
RUN mkdir /persistence/
WORKDIR /app
ENV DISCORD_TOKEN=Nzg3MzQ4MjgxMTQyNTQyMzY2.X9TpOg.1PuKldq-LKOJjZ38USgoR54wbwo
ENV PERSISTENCE_ROOT=/persistence
COPY --from=build /home/gradle/src/build/libs/sample-gradle-3.1.2-SNAPSHOT-all.jar /app/angel-bot.jar

ENTRYPOINT ["java", "-jar","angel-bot.jar"]
