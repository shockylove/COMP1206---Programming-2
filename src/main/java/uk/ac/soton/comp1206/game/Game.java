package uk.ac.soton.comp1206.game;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import uk.ac.soton.comp1206.component.GameBlock;
import uk.ac.soton.comp1206.component.GameBlockCoordinate;
import uk.ac.soton.comp1206.event.GameLoopListener;
import uk.ac.soton.comp1206.event.GameOverListener;
import uk.ac.soton.comp1206.event.LineClearedListener;
import uk.ac.soton.comp1206.event.NextPieceListener;
import uk.ac.soton.comp1206.utility.Multimedia;

/**
 * The Game class handles the main logic, state and properties of the TetrECS game. Methods to
 * manipulate the game state and to handle actions made by the player should take place inside this
 * class.
 */
public class Game implements Serializable {

  /** */
  private static final long serialVersionUID = 1L;

  protected static final Logger logger = LogManager.getLogger(Game.class);

  /** Number of rows */
  protected final int rows;

  /** Number of columns */
  protected final int cols;

  /** The grid model linked to the game */
  protected Grid grid;

  protected IntegerProperty score = new SimpleIntegerProperty(0);
  protected IntegerProperty level = new SimpleIntegerProperty(0);
  protected IntegerProperty lives = new SimpleIntegerProperty(3);
  protected IntegerProperty multiplier = new SimpleIntegerProperty(0);
  protected BooleanProperty clear = new SimpleBooleanProperty(false);
  protected StringProperty name = new SimpleStringProperty(" ");
  protected Timer gameTimer = new Timer();
  protected boolean played;

  /** Current piece of the game */
  protected GamePiece currentPiece;

  protected GamePiece followingPiece;

  // private Grid nextGrid;
  // private Grid nextGrid2;

  private NextPieceListener nextPieceListener = null;

  protected final ScheduledExecutorService executor;

  private LineClearedListener lineClearedListener;

  protected GameLoopListener gameLoopListener;

  private GameOverListener gameOverListener;

  private boolean load = false;

  /**
   * Create a new game with the specified rows and columns. Creates a corresponding grid model.
   *
   * @param cols number of columns
   * @param rows number of rows
   */
  public Game(int cols, int rows) {
    this.cols = cols;
    this.rows = rows;
    this.executor = Executors.newSingleThreadScheduledExecutor();
    // Create a new grid model to represent the game state
    this.grid = new Grid(cols, rows);
  }

  public Game(int cols, int rows, boolean load) {
    this.cols = cols;
    this.rows = rows;
    this.executor = Executors.newSingleThreadScheduledExecutor();
    // Create a new grid model to represent the game state
    this.grid = new Grid(cols, rows);
    this.load = load;
  }

  /** Start the game */
  public void start() {
    logger.info("Starting game");
    initialiseGame();
    if (this.gameLoopListener != null) {
      this.gameLoopListener.gameLoop(this.getTimerDelay());
    }
    TimerTask task =
        new TimerTask() {

          public void run() {
            if (Game.this.getLives() < 0) {
              Game.this.gameTimer.cancel();
            }
            gameLoop();
          }
        };

    this.gameTimer.schedule(task, this.getTimerDelay(), this.getTimerDelay());
  }

  /** Initialise a new game and set up anything that needs to be done at the start */
  public void initialiseGame() {
    if (!load) {
      currentPiece = spawnPiece();
      followingPiece = spawnPiece();
      load = false;

      logger.info("Initialising game");
      score.set(0);
      level.set(0);
      lives.set(3);
      multiplier.set(1);

      // spawn a new piece to start the game.
      nextPiece();
    } else {
      logger.info("Initialising game with load");
      gameTimer = new Timer("Timer");

      try {
        this.read(new FileInputStream("save.txt"));
        if (this.nextPieceListener != null) {
          this.nextPieceListener.nextPiece(this.currentPiece);
        }
      } catch (FileNotFoundException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      } catch (IOException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }
  }

  /**
   * Handle what should happen when a particular block is clicked
   *
   * @param gameBlock the block that was clicked
   */
  public void blockClicked(GameBlock gameBlock) {
    // Get the position of this block
    int x = gameBlock.getX();
    int y = gameBlock.getY();

    if (grid.canPlayPiece(currentPiece, x - 1, y - 1)) {
      Multimedia.playAudio("/sounds/place.wav");
      grid.playPiece(currentPiece, x, y);
      if (gameTimer != null) {
        gameTimer.cancel();
      }
      gameTimer = new Timer("timer");
      logger.info("new timer setted");
      nextPiece();
      afterPiece();
      TimerTask task =
          new TimerTask() {
            public void run() {
              gameLoop();
            }
          };
      if (this.gameLoopListener != null) {
        this.gameLoopListener.gameLoop(this.getTimerDelay());
      }
      gameTimer.schedule(task, this.getTimerDelay(), this.getTimerDelay());

    } else {
      Multimedia.playAudio("/sounds/fail.wav");
      setmultiplier(1);
      setClear(false);
    }
  }

  /**
   * Get the grid model inside this game representing the game state of the board
   *
   * @return game grid model
   */
  public Grid getGrid() {
    return grid;
  }

  /**
   * Get the number of columns in this game
   *
   * @return number of columns
   */
  public int getCols() {
    return cols;
  }

  /**
   * Get the number of rows in this game
   *
   * @return number of rows
   */
  public int getRows() {
    return rows;
  }

  /**
   * Create a new random GamePiece.
   *
   * @return GamePiece a new random piece for player.
   */
  public GamePiece spawnPiece() {
    Random random = new Random();
    int seed = random.nextInt(15);
    return GamePiece.createPiece(seed);
  }

  /** Replace the current piece with a new piece. */
  public void nextPiece() {
    currentPiece = followingPiece;
    followingPiece = spawnPiece();

    if (this.nextPieceListener != null) {
      this.nextPieceListener.nextPiece(this.currentPiece);
    }

    logger.info("spawned the next piece {}", currentPiece.toString());
  }

  /** Check the player's move, update the scores, grid board, and others. */
  public void afterPiece() {
    setClear(false);
    HashSet<GameBlockCoordinate> filledBlock = new HashSet<GameBlockCoordinate>();
    HashSet<Integer> filledRow = new HashSet<Integer>();
    HashSet<Integer> filledCol = new HashSet<Integer>();
    for (int x = 0; x < grid.getRows(); x++) {
      if (isFulledRow(x)) {
        filledRow.add(x);
        for (int y = 0; y < grid.getCols(); y++) {
          filledBlock.add(new GameBlockCoordinate(x, y));
        }
      }
    }

    for (int y = 0; y < grid.getCols(); y++) {
      if (isFulledCol(y)) {
        filledCol.add(y);
        for (int x = 0; x < grid.getCols(); x++) {
          filledBlock.add(new GameBlockCoordinate(x, y));
        }
      }
    }

    if (filledRow.size() != 0 || filledCol.size() != 0) {
      for (Integer x : filledRow) {
        for (int y = 0; y < grid.getCols(); y++) {
          grid.set(x, y, 0);
        }
      }

      for (Integer y : filledCol) {
        for (int x = 0; x < grid.getRows(); x++) {
          grid.set(x, y, 0);
        }
      }

      // trigger the fade out event when blocks are cleared
      if (this.lineClearedListener != null) {
        this.lineClearedListener.lineCleared(filledBlock);
      }
    }

    score(filledRow.size() + filledCol.size(), filledBlock.size());

    // implementation of multiplier
    if (getClear()) {
      this.setmultiplier(this.getmultiplier() + 1);
    } else {
      setClear(false);
    }

    // implementation of level
    int barrier = 1000;
    setLevel(getScore() / barrier);
  }

  /**
   * Check if the row is fully colored
   *
   * @param c
   * @return true if the line is fully colored
   */
  private boolean isFulledRow(int row) {
    for (int i = 0; i < grid.getCols(); i++) {
      if (grid.get(row, i) == 0) {
        return false;
      }
    }
    clear.set(true);
    return true;
  }

  /**
   * Check if the col is fully colored
   *
   * @param c
   * @return true if the line is fully colored
   */
  private boolean isFulledCol(int col) {
    for (int i = 0; i < grid.getCols(); i++) {
      if (grid.get(i, col) == 0) {
        return false;
      }
    }
    clear.set(true);
    return true;
  }

  /**
   * Calculate the scores of the player get when there is a successful play.
   *
   * @param numLines number of lines eliminated.
   * @param numBlocks number of blocks eliminated
   */
  protected void score(int numLines, int numBlocks) {
    int scoreGet = numLines * numBlocks * 10 * this.getmultiplier();
    this.setScore(scoreGet + this.getScore());
  }

  /*
   *
   * get/set propertys of the game.
   *
   */
  public IntegerProperty scoreProperty() {
    return this.score;
  }

  public int getScore() {
    return score.get();
  }

  public void setScore(int input) {
    score.set(input);
  }

  public IntegerProperty levelProperty() {
    return this.level;
  }

  public int getlevel() {
    return level.get();
  }

  public void setLevel(int input) {
    Multimedia.playAudio("/sounds/level.wav");
    level.set(input);
  }

  public IntegerProperty livesProperty() {
    return this.lives;
  }

  public int getLives() {
    return lives.get();
  }

  public void setLives(int input) {
    lives.set(input);
  }

  public IntegerProperty multiplierProperty() {
    return this.multiplier;
  }

  public StringProperty nameProperty() {
    return this.name;
  }

  public int getmultiplier() {
    return multiplier.get();
  }

  public void setmultiplier(int input) {
    multiplier.set(input);
  }

  public BooleanProperty clearProperty() {
    return this.clear;
  }

  public boolean getClear() {
    return clear.get();
  }

  public void setClear(boolean input) {
    clear.set(input);
  }

  //  public Grid getNextGrid() {
  //    return this.nextGrid;
  //  }

  public GamePiece getNextPiece() {
    return this.followingPiece;
  }

  public GamePiece getCurrentPiece() {
    return this.currentPiece;
  }

  //  public Grid getNextGrid2() {
  //    return this.nextGrid2;
  //  }

  /** Complete stop the current game session. */
  public void stop() {
    gameTimer.cancel();
    this.executor.shutdownNow();
  }

  /**
   * Listener handler of updating next piece for the client.
   *
   * @param listener
   */
  public void setNextPieceListener(NextPieceListener listener) {
    this.nextPieceListener = listener;
  }

  public void setOnLineCleared(LineClearedListener listener) {
    this.lineClearedListener = listener;
  }

  /**
   * Game loop listener for game loop.
   *
   * @param listener
   */
  public void setOnGameLoop(GameLoopListener listener) {
    this.gameLoopListener = listener;
  }

  /**
   * Rotate the current piece accordingly.
   *
   * @param num num of rotate
   */
  public void rotateCurrentPiece(int num) {
    for (int i = 0; i < num; i++) {
      currentPiece.rotate();
    }
    if (this.nextPieceListener != null) {
      this.nextPieceListener.nextPiece(this.currentPiece);
    }
  }

  /** Swap the incoming two pieces. */
  public void swapCurrentPiece() {
    GamePiece temp = currentPiece;
    currentPiece = followingPiece;
    followingPiece = temp;
  }

  /**
   * Calculate timer delay following the function.
   *
   * @return int delay of the time;
   */
  public int getTimerDelay() {
    int result = 12000 - 500 * this.level.get();
    if (result < 2500) {
      result = 2500;
    }
    return result;
  }

  /** game loop of the game */
  protected void gameLoop() {

    logger.info("Dead once, now has {} lives", lives.get() - 1);

    if (lives.get() > 0) {
      Multimedia.playAudio("/sounds/lifelose.wav");
      lives.set(lives.get() - 1);
      multiplier.set(1);
      nextPiece();
      if (this.gameLoopListener != null) {

        this.gameLoopListener.gameLoop(this.getTimerDelay());
      }
      logger.info("Start of a new timed loop, now have {}mm time", this.getTimerDelay());
    } else {
      gameOver();
    }
  }

  protected void gameOver() {
    if (gameOverListener != null) {
      Platform.runLater(
          () -> {
            this.gameOverListener.gameOver();
          });
    }

    this.stop();
    logger.info("Game over");
  }

  public void setOnGameOver(GameOverListener listener) {
    this.gameOverListener = listener;
  }

  /**
   * Save info of the game to save.txt when exiting the game.
   *
   * @param file
   * @throws IOException
   */
  public void write(FileOutputStream file) throws IOException {
    StringBuilder info = new StringBuilder();
    info.append(score.get());
    info.append(" ");
    info.append(level.get());
    info.append(" ");
    info.append(lives.get());
    info.append(" ");
    info.append(multiplier.get());
    info.append(":");
    info.append(currentPiece.getValue() - 1);
    System.out.println(currentPiece.getValue() - 1);
    info.append(" ");
    info.append(followingPiece.getValue());
    info.append(":");
    for (int y = 0; y < this.cols; y++) {
      for (int x = 0; x < this.rows; x++) {
        int value = this.grid.get(x, y);
        info.append(value + " ");
      }
    }
    byte[] out = info.toString().getBytes();
    file.write(out);
    file.close();
  }

  /**
   * Read infomation from the saved file.
   *
   * @param file
   * @throws IOException
   */
  public void read(FileInputStream file) throws IOException {
    String info = "";
    int i = 0;
    while ((i = file.read()) != -1) {
      info = info + (char) i;
    }
    System.out.println(info);
    String[] propertys = info.split(":");
    String[] intPropertys = propertys[0].split(" ");
    String[] pieceinfo = propertys[1].split(" ");
    String[] boardinfo = propertys[2].split(" ");

    this.score.set(Integer.parseInt(intPropertys[0]));
    this.level.set(Integer.parseInt(intPropertys[1]));
    this.lives.set(Integer.parseInt(intPropertys[2]));
    this.multiplier.set(Integer.parseInt(intPropertys[3]));

    this.currentPiece = GamePiece.createPiece(Integer.parseInt(pieceinfo[0]));
    this.followingPiece = GamePiece.createPiece(Integer.parseInt(pieceinfo[1]));

    int x = 0;
    int y = 0;
    for (int k = 0; k < 25; k++) {
      int j = Integer.parseInt(boardinfo[k]);
      this.grid.set(x, y, j);

      x++;
      if (x == 5) {
        x = 0;
        y++;
      }
    }
  }
}
