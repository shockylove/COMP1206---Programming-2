package uk.ac.soton.comp1206.game;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Random;

import javafx.application.Platform;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.ObservableList;
import javafx.util.Pair;
import uk.ac.soton.comp1206.component.GameBlock;
import uk.ac.soton.comp1206.network.Communicator;

public class MultiplayerGame extends Game {
  private final Communicator communicator;
  private ArrayDeque<GamePiece> feed = new ArrayDeque();
  private Random random = new Random();
  private boolean begun = false;
  public ArrayList<Pair<String, Integer>> scores = new ArrayList();
  public ObservableList<Pair<String, Integer>> wapper;
  public SimpleListProperty<Pair<String, Integer>> ingameScores =
      new SimpleListProperty<Pair<String, Integer>>();

  public MultiplayerGame(Communicator communicator, int cols, int rows) {
    super(cols, rows);
    this.communicator = communicator;
    communicator.addListener(
        (message) -> {
          Platform.runLater(
              () -> {
                this.receiveMessage(message.trim());
              });
        });
  }

  /** game loop of the game, send LIVES to sever when there is a life change. */
  protected void gameLoop() {
    logger.info("Dead once, now has {} lives", lives.get() - 1);

    if (lives.get() > 0) {
      lives.set(lives.get() - 1);
      this.communicator.send("LIVES " + this.getLives());
      multiplier.set(1);
      nextPiece();
      if (this.gameLoopListener != null) {
        this.gameLoopListener.gameLoop(this.getTimerDelay());
      }
      logger.info("Start of a new timed loop, now have {}mm time", this.getTimerDelay());
    } else {
      this.communicator.send("LIVES " + "DEAD");
      gameOver();
    }
  }

  /**
   * Receive message handler.
   *
   * @param message
   */
  private void receiveMessage(String message) {

    // logger.info("Received message: {}", message);
    String[] subMessage = message.split(" ", 2);
    if (subMessage[0].equals("PIECE") && subMessage.length > 1) {
      this.receivePiece(Integer.parseInt(subMessage[1]));
    } else if (subMessage[0].equals("SCORES")) {
      String[] nameScores = subMessage[1].split("\n");
      this.scores.clear();
      for (int i = 0; i < nameScores.length; i++) {
        scores.add(
            new Pair<String, Integer>(
                nameScores[i].split(":")[0], Integer.parseInt(nameScores[i].split(":")[1])));
      }
    }
  }

  /**
   * Calculate the scores of the player get when there is a successful play. Update the new Score to
   * the sever.
   *
   * @param numLines number of lines eliminated.
   * @param numBlocks number of blocks eliminated
   */
  protected void score(int numLines, int numBlocks) {
    super.score(numLines, numBlocks);
    this.communicator.send("SCORE " + this.getScore());
    this.communicator.send("SCORES");
  }

  /**
   * Proecss the incoming piece
   *
   * @param parseInt
   */
  private void receivePiece(int newPiece) {
    GamePiece piece = GamePiece.createPiece(newPiece, this.random.nextInt(3));
    logger.info("Received piece: {}", piece);
    this.feed.add(piece);
    logger.info("Added piece to queue: {}", piece);
    if (!this.begun && this.feed.size() > 2) {
      logger.info("Start the game with pieces in the queue");
      this.followingPiece = this.spawnPiece();
      this.nextPiece();
      this.begun = true;
    }
  }

  public GamePiece spawnPiece() {
    logger.info("SpawnPiece from queue: {}", feed);
    this.communicator.send("PIECE");
    return this.feed.pop();
  }

  /** initialise game, and populate the queue */
  public void initialiseGame() {
    logger.info("Starting game");
    this.score.set(0);
    this.level.set(0);
    this.lives.set(3);
    this.multiplier.set(1);
    communicator.send("SCORES");
    this.feedUp();
  }

  /** Handle blockClicked Event, send the board info to sever. */
  public void blockClicked(GameBlock gameBlock) {
    if (this.getLives() >= 0) {
      super.blockClicked(gameBlock);
      StringBuilder info = new StringBuilder();
      for (int y = 0; y < this.cols; y++) {
        for (int x = 0; x < this.rows; x++) {
          int value = this.grid.get(x, y);
          info.append(value + " ");
        }
      }
      this.communicator.send("BOARD " + info.toString());
    }
  }
  /** Populate the queue to start game */
  public void feedUp() {
    for (int i = 0; i < 10; i++) {
      this.communicator.send("PIECE");
    }
  }

  public SimpleListProperty scoresProperty() {
    return this.ingameScores;
  }
}
