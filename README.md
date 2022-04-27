# Javacord Bot 

This is the bot used on the Javacord server.

## I just want to use Javacord :-(

In this case you are wrong here. Just head over to the [Javacord repository](https://github.com/Javacord/Javacord).

## What's the difference to the example bot

The [example bot](https://github.com/Javacord/Example-Bot) is exactly what its name suggests: A bot which
is solely used as an example for Javacord. It tries to cover some basic concepts to give you an idea how a
bot using Javacord may look like. It is kept simple and has very verbose comments.

This bot however is actually used on our Discord Server and thus may contain a lot of specific features which are 
most likely not relevant to you. Simplicity also is not a goal for this bot, but you can still use this bot
as a reference if you want to.

## How to build it

To build a distribution archive execute `./gradlew distTar` or `./gradlew distZip`.

## How to run it ...

### ... locally

- execute `./gradlew installDist`
- (optional) store your discord api token in a text file
- execute `build/install/javacord-bot/bin/javacord-bot <token>`
  or `build/install/javacord-bot/bin/javacord-bot <path to the file with the token>`

### ... manually

- copy the built distribution archive from `build/distributions/` to the system where you want to run it
- unpack the archive somewhere
- (optional) store your discord api token in a text file
- execute `javacord-bot/bin/javacord-bot <token>`
  or `javacord-bot/bin/javacord-bot <path to the file with the token>`

### ... with Docker

- build the Dockerfile with `docker build .`
- run the generated image. The token has to be provided with the `DISCORD_TOKEN` environment variable