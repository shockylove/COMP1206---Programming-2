# COMP1206-Programming-2


## TetrECS

Uos - COMP1206 - Programming 2

[CW Specification.pdf](https://github.com/shockylove/COMP1206-Programming-2/files/10574517/CW.Specification.pdf)

The game is developed and managed using JavaFX and Maven.

* [Game Introduction](#game-introduction)
* [Setup](#setup)
* [Technologies](#technologies)
* [Video Presentation](*video-presentation])



## Game Introduction

### Single mode

	A fast-paced block placement game
	You have a 5x5 grid
	You must place pieces in that grid
	You score by clearing lines, horizontally or vertically
	You can rotate pieces
	You can store a single piece to come back to later
	The more lines you clear in one go, the more points you get
	Every piece that you play that clears at least one line increases your score multiplier
	As your score goes up, so does your level, and you get less time to think
	If you fail to place a block, you lose a life
	Lose 3 lives, and you’re out of the game
	The score is stored locally and the top ten list is refreshed if there is a new record on the server
	

### Multiplayer mode
	Lobby
		Find all games
		Create a new Game
		Join a game that exists already
	Chat
	Multiplayer Gameplay
		Create a leaderboard against the people you’re playing with
		Send and receive blocks from the server
		Send and receive game updates


## SetUp
	mvn clean javafx:run
	mvn javafx:ru​​n@debug
	
	The server that is connected to is ofb-labs.ecs.soton.ac.uk:9700 (WARNING: Keys may have changed)
		This is different to the ECS Chat Server
		This is a dedicated server for TetrECS
		You need to be on the VPN to reach this server

	
## Technologies
	This coursework builds on everything you have learnt and practiced so far:
		JavaFX 
		MVN
		Custom Components
		Graphics and Animation
		Listeners, Properties and Binding
		Communications
		Media
		Files
		

## Video Presentation

https://user-images.githubusercontent.com/93628439/216355323-87316a79-ea93-4602-81e4-a56d2b6bb579.mp4



