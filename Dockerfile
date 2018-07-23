FROM java:8

COPY . /usr/src/myapp
WORKDIR /usr/src/myapp
RUN chmod +x gradlew
RUN ./gradlew jar

CMD java -jar build/libs/bot.jar $TOKEN