[![Build Status](https://travis-ci.org/PlanetTeamSpeakk/Impulse.svg?branch=master)](https://travis-ci.org/PlanetTeamSpeakk/Impulse)

# Impulse
Source code of the Impulse Discord bot.

## Dependencies
### Windows/Mac
Install the latest version of Java 8 which can be found [here](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html).
### Linux
On Linux you gotta open a terminal (ctrl+alt+t) and type the following command: `sudo apt-get install openjdk-8-jdk`, once you've installed that make sure to also install JavaFX by running `sudo apt-get install openjfx`.

## Sharding
**If you are not planning on using shards, e.g. your bot is in less than 2500 servers, then you can ignore this.**
Since version 2.0 sharding has been totally revamped, instead of every shard just running in 1 process and totally defying the meaning of sharding, shards now run all in their own process and are connected to a server using Netty.

Now I hear you ask, how do I start the server and the shards? Well, simple:
### Starting the server
0. If you have not yet started the bot at least once, there is no config, do that first so a config is generated and fill it out.
1. Set the variable 'shards' in the config to at least 2.
2. Start the server by just double clicking the Impulse jar file, it should say starting server and give a success message a few seconds later.
3. There you go, you just started the server! Yes, it was that easy.

### Starting the clients (read shards)
0. No config is needed here, the clients get the config from the server.
1. On windows, make a start.bat file that is somewhat like the following: 
```batch
@echo off
java -jar <jar file>.jar -shard <shardId>
```
  Just make sure that the Impulse jar file, which is indeed the same as the server, contains with the -shard <shardId> parameters and make sure to change <jar file> and <shardId> to the corresponding variables. **Note:** shard ids are index-based meaning they start at 0 and end at shardcount-1.
2. Now do repeat that for each shard.
3. As soon as every shard is online, the server should start sending packets to each of them to tell them to log in, once they're all logged in, they will start listening to commands.
4. That's it, you're done!
  
### Starting the server and clients on different hosts
If you're planning on starting the server on let's say VPS 1 and you want to start the clients on a different one, or want the clients distributed over multiple hosts (read computers) then you're gonna have to follow these steps:
1. Make sure port 62192 on the host of the server is port forwarded, you can find how to do so online, but if it does not help, search on how to do it with UPnP if your router supports that.
2. In the starting file of each of the clients, you're gonna have to add -hostname <ip of server host> to the end of the line that starts with java -jar. You can use a custom domain and its DNS to change the ip to a domain name instead of an ip but that's unnecessary, as long as port 62192 is forwarded on the host.
