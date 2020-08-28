# Acquire Banker

A command line banker for the board game [Acquire](https://boardgamegeek.com/boardgame/5/acquire).

![Screenshot](acquire-screenshot.png)

## About

[Acquire](https://boardgamegeek.com/boardgame/5/acquire) is a turn based stock-market simulation board game, for 2-6 players.  
Throughout the game, players must keep track of their own resources, as well as public game resources. Without a banking software this can easily become a tedious task that slows down the playing and fun.  
This repository hosts the sources of an interactive software that takes care of all counting, so the players can entirely enjoy their game.  

The following sections provide brief instructions on how to compile and use the software.

## Build & Run

### Requirements

 * While the banker-software is a Java program and as such compatible to any platform that supports a JDK, the output uses terminal color codes that are only correctly displayed on UNIX like systems.  
The client was specifically tested for **Mac OS**  and **Linux**.
 * JDK 8 or higher is required.

### Instructions

 * Direct launch:  
```bash
mvn exec:java
```

 * Build a self contained jar:  
```bash
mvn clean package
```

 * Start from self-contained jar:  

```bash
java -jar target/AcquireBanker.jar
```

Optionally you can pass a ```SAVEGAME``` as first runtime argument, to restore a previously saved game.

 > Note: ```SAVEGAME``` is the absolute file path of a previously automatically saved game in your ```TMPDIR```. Use ```echo $TMPDIR``` to locate the savegames.

## Usage

The entire banker runs interaction / dialogue based.

 * When initializing a new game, provide the player amount as a number, e.g. ```3```. Then type the player names, separated by ```return```.
 * When asked for a company name / names, abbreviate them with the first letter.  
Examples:
   * Buying two *Sackson* and a *Festival* share: ```ssf```
   * Funding the *Continental* chain: ```c```
 * When asked for a position, provide a single word that encodes the coordinates.  
Example: 4-B is  provided as ```4b```

## Contact / Pull Requests

 * Author: Maximilian Schiedermeier ![email](email.png)
 * First release: June 2011
 * Github: Kartoffelquadrat
 * Webpage: https://www.cs.mcgill.ca/~mschie3
 * License: [MIT](https://opensource.org/licenses/MIT)
