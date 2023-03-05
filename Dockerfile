FROM sbtscala/scala-sbt:eclipse-temurin-jammy-11.0.17_8_1.8.2_2.13.10

RUN mkdir bot-log

ADD . /discord-music-bot

WORKDIR /discord-music-bot

ENV BOT_LOG_DIR "/bot-log"

ENV DISCORD_BOT_TOKEN ""

RUN sbt compile

CMD sbt run

# docker build . -t discord-music-bot
# docker run -d \
#  --restart=unless-stopped \
#  -v <path to log dir>:/bot-log \
#  -e DISCORD_BOT_TOKEN=<discord bot token> \
#  --name discord-music-bot \
#  discord-music-bot
