FROM debian:stable-slim

RUN export DEBIAN_FRONTEND=noninteractive && \
    apt-get update && \
    mkdir /usr/share/man/man1 && \
    apt-get install -y --no-install-recommends openjdk-8-jre-headless && \
    apt-get autoremove --purge -y && \
    rm -rv /var/lib/apt/lists/* && \
    useradd --no-log-init --shell /bin/false --no-create-home javacord-bot

ADD javacord-bot.tar /opt/
RUN chown -R javacord-bot:javacord-bot /opt/javacord-bot/

USER javacord-bot
WORKDIR /opt/javacord-bot/

RUN mkdir log && \
    sed -i \
    -e 's|level="DEBUG"|level="INFO"|' \
    -e 's|<!--<AppenderRef ref="File Appender" level="INFO"/>-->|<AppenderRef ref="File Appender" level="INFO"/>|' \
    -e 's|<!--<AppenderRef ref="ALL File Appender"/>-->|<AppenderRef ref="ALL File Appender"/>|' \
    -e 's|<!--<RollingRandomAccessFile|<RollingRandomAccessFile|' \
    -e 's|</RollingRandomAccessFile>-->|</RollingRandomAccessFile>|' \
    config/log4j2.xml

VOLUME /opt/javacord-bot/log/
ENTRYPOINT ["bin/javacord-bot"]
CMD ["/run/secrets/discord_api_token"]
