# Javacord Bot 
This is the bot used on the Javacord server.

## I just want to use Javacord :\(
In this case you are wrong here. Just head over to the [Javacord repository](https://github.com/Javacord/Javacord).

## What's the difference to the example bot
The [example bot](https://github.com/Javacord/JavacordExampleBot) is exactly what its name suggests: A bot which
is solely used as an example for Javacord. It tries to cover some basic concepts to give you an idea how a
bot in Javacord may look like. It's kept simple and has very verbose comments.

This bot however is actually used in out Discord Server and thus may contain a lot of specific features which are 
most likely not relevant to you. Simplicity also isn't a goal for this bot, but you can still use this bot
as a reference if you want to.

## How to run it?
You can use Docker to run this bot:
```
git clone https://github.com/Javacord/Javacord-Bot.git
cd Javacord-Bot
docker build -t javacord-bot .
docker run --rm --env-file javacord-bot.env --name javacord-bot javacord-bot
```

Take a look at the `javacord-bot.example.env` file to see how the environment file should look like.