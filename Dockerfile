FROM java:8

COPY . /usr/src/myapp
WORKDIR /usr/src/myapp
RUN chmod +x gradlew
RUN ./gradlew installDist

CMD build/install/javacord-bot/bin/javacord-bot $TOKEN
