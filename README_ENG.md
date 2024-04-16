# FINAL SOFTWARE ENGINEERING PROJECT ~ 2020 - 2021

Welcome to the digital version of the board game "Masters of the Renaissance"!

![alt text](src/main/resources/it/polimi/ingsw/view/GUI/images/newLogo.jpg)

The version we propose here uses the Model-View-Controller pattern to allow a single server, hosted on a machine, to handle multiple games simultaneously, each composed of a maximum of 4 players.
Everything related to communication between players and the central server, connections, disconnections, and reconnections is entirely managed using TCP Sockets as the communication channel.

Moreover, it is possible to play "Masters of the Renaissance" in two different modes: via command line interface (CLI) or through a graphical user interface (GUI).

Before starting to play, please consult the [Masters Of Renaissance rulebook](https://github.com/citteriomatteo/ingswAM2021-Barbiero-Citterio-Caspani/tree/main/deliveries/documents/Masters_of_Renaissance_Rules.pdf) to discover all the game's features.

## Documentation

### UML
The following diagrams represent the schematic evolution of the product, from initial design to the final product diagram.
Both versions can be consulted at the following links:
- [UML Iniziale](https://github.com/citteriomatteo/ingswAM2021-Barbiero-Citterio-Caspani/tree/main/deliveries/uml/Old_Version_Uml.png)
- [UML Finali](https://github.com/citteriomatteo/ingswAM2021-Barbiero-Citterio-Caspani/tree/main/deliveries/uml/finals)

### JavaDoc
The entire project documentation, including classes and functions, is available at the following link: [Javadoc](https://github.com/citteriomatteo/ingswAM2021-Barbiero-Citterio-Caspani/tree/main/deliveries/javadoc)

### Libraries and Plugins
|Library/Plugin|Description|
|---------------|-----------|
|__Maven__|Automation tool for compiling mainly used for Java projects.|
|__JavaFx__|Java library used for creating the graphical user interface.|
|__JUnit 5 Jupiter__|Framework for unit testing.|
|__Gson / Gson-extras__|External libraries for parsing text in Json format.|

## Features
### Developed Features
- Complete Rulebook
- CLI Version
- GUI Version
- Usage of TCP Sockets
- 3 Additional Features:
    - __Resilience to Disconnections:__ A player is free to disconnect and reconnect later in the game, rejoining with the same nickname.
      The player's state is also maintained even in the middle of a semi-completed turn, giving them the opportunity, once their turn returns, to resume from the exact point they left off.
    - __Multiple Games:__ The server is capable of handling multiple games simultaneously, and the match creation occurs in series. 
      The waiting and creation procedure for a game is repeated for players who connect later, as soon as the game the server is busy building has started.
    - __Local Game:__ By starting the jar with the additional command --local, it is possible to start the game without the need for a listening server. 
      The device will become stand-alone and will take care of keeping track of the game status and managing it entirely internally and independently.
      However, this mode does not replace the standard single-player game: it is still possible to play against Lorenzo by starting the game according to its normal setup and take advantage of the match resumption capabilities following a disconnection.
- Extra Feature:
    - __Rematch:__ At the end of a game, the final ranking is presented, and players can ask for a rematch from the others present. 
    The fastest player to request a rematch will be displayed on the others' screens, who can accept or decline the proposal. 
    If all players accept, the game restarts from the initial phase with the same configuration as the previous game. 
    This feature is excellent and convenient, especially when combined with the additional parameter customization feature: the rematch would allow skipping the creation process of a configuration and going straight to action.

## Packaging and Jar
The project Jars have been created using a specific plugin offered by Maven (Shade Plugin). 
The precompiled Jars of the project (two independent files for the Client and Server components) can be obtained by downloading them from the following link:
- [Masters of Renaissance's Client and Server Jars](https://github.com/citteriomatteo/ingswAM2021-Barbiero-Citterio-Caspani/tree/main/deliveries/jar).

## Execution
This project requires Java 11 (or one of the later versions) to function correctly.

### MASTERS OF RENAISSANCE'S CLIENT
The game is available in two versions: CLI and GUI.

To enjoy the full gaming experience, a minimum screen resolution of 1024x768 is required to play with advanced graphics and to start the CLI version on any Unix terminal (preferably with Line Wrapping disabled).

Below are the instructions on how to launch the game in both versions.

#### CLI
To play Masters of Renaissance in CLI version, type the following command from the terminal:
```
java -jar mastersOfRenaissance-client.jar --cli
```
after navigating to the directory where the jars are located.

#### GUI
The GUI mode is chosen as the default version of the game. 
Consequently, it is possible to play in GUI mode in two different ways:
- double-click the executable  ```clientApp.jar```
- type the following command from the terminal:
```
java -jar mastersOfRenaissance-client.jar
```
or
```
java -jar mastersOfRenaissance-client.jar --gui
```
after navigating to the directory where the jars are located.

#### Client Launch Parameters
Once launched, the Client will try to connect to a server running on the local machine (127.0.0.1) at port 2500. 
To change the connection parameters, add the following optional settings to the end of the terminal command:
- `--ip` `<ip>`,
  `--address` `<ip>`: to change the server's IP address;
- `--port` `<port number>` : to specify a preferred port;
- `--local` : to start the game in "offline single-match" mode;

### MASTERS OF RENAISSANCE'S SERVER
To launch Masters of Renaissance Server, open the terminal, navigate to the root of the Jar file, and type the command:
```
java -jar mastersOfRenaissance-server.jar
```
The server will be launched on the local machine, accepting connections on port 2500.
To change this value, add a number to the end of the command, which will be used as the new port.

## Team Members
- [__Federico Caspani__](https://github.com/FedericoCaspani)
- [__Alessandro Barbiero__](https://github.com/AlessandroBarbiero)
- [__Matteo Citterio__](https://github.com/citteriomatteo)
