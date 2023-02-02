package uk.ac.soton.comp1206.scene;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ObservableValue;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.util.Duration;
import uk.ac.soton.comp1206.component.GameBlock;
import uk.ac.soton.comp1206.component.GameBlockCoordinate;
import uk.ac.soton.comp1206.component.GameBoard;
import uk.ac.soton.comp1206.component.PieceBoard;
import uk.ac.soton.comp1206.game.Game;
import uk.ac.soton.comp1206.game.GamePiece;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;
import uk.ac.soton.comp1206.utility.Multimedia;
import uk.ac.soton.comp1206.utility.Save;

/**
 * The Single Player challenge scene. Holds the UI for the single player challenge mode in the game.
 */
public class ChallengeScene extends BaseScene {

  protected static final Logger logger = LogManager.getLogger(MenuScene.class);
  protected Game game;

  protected IntegerProperty score = new SimpleIntegerProperty(0);
  protected IntegerProperty level = new SimpleIntegerProperty(0);
  protected IntegerProperty lives = new SimpleIntegerProperty(0);
  protected IntegerProperty multiplier = new SimpleIntegerProperty(0);

  protected GameBoard board;
  protected PieceBoard nextBoard;
  protected PieceBoard nextBoard2;

  protected int keyX;
  protected int keyY;

  protected Rectangle timer;
  protected StackPane timerBox;

  protected IntegerProperty highScore = new SimpleIntegerProperty(0);
  private boolean load;

  /**
   * Create a new Single Player challenge scene
   *
   * @param gameWindow the Game Window
   */
  public ChallengeScene(GameWindow gameWindow) {
    super(gameWindow);
    this.load = false;
    highScore.set(Save.loadScores().get(0).getValue());
    logger.info("Creating Menu Scene");
  }

  public ChallengeScene(GameWindow gameWindow, boolean load) {
    super(gameWindow);
    this.load = load;
    highScore.set(Save.loadScores().get(0).getValue());
  }

  /** Build the Challenge window */
  @Override
  public void build() {
    logger.info("Building " + this.getClass().getName());

    setupGame();

    root = new GamePane(gameWindow.getWidth(), gameWindow.getHeight());

    var challengePane = new StackPane();
    challengePane.setMaxWidth(gameWindow.getWidth());
    challengePane.setMaxHeight(gameWindow.getHeight());
    challengePane.getStyleClass().add("menu-background");
    root.getChildren().add(challengePane);

    var mainPane = new BorderPane();
    challengePane.getChildren().add(mainPane);

    this.board =
        new GameBoard(game.getGrid(), gameWindow.getWidth() / 2, gameWindow.getWidth() / 2);
    mainPane.setCenter(board);

    this.nextBoard = new PieceBoard(3, 3, gameWindow.getWidth() / 6, gameWindow.getWidth() / 6);

    this.nextBoard2 = new PieceBoard(3, 3, gameWindow.getWidth() / 10, gameWindow.getWidth() / 10);

    timer = new Rectangle();
    timer.setHeight(20);
    timer.setFill(Color.BLUE);

    VBox side = new VBox();
    side.setAlignment(Pos.CENTER);
    side.setPadding(new Insets(5, 5, 5, 5));
    mainPane.setRight(side);
    GridPane topBar = new GridPane();
    topBar.setPadding(new Insets(10, 10, 10, 10));
    mainPane.setTop(topBar);

    VBox scoreBox = new VBox();
    scoreBox.setAlignment(Pos.CENTER);
    Text scoreLable = new Text("Score");
    scoreLable.getStyleClass().add("heading");
    scoreBox.getChildren().add(scoreLable);
    Text score = new Text("0");
    score.getStyleClass().add("score");
    score.textProperty().bind(game.scoreProperty().asString());
    scoreBox.getChildren().add(score);

    VBox lifeBox = new VBox();
    lifeBox.setAlignment(Pos.CENTER);
    Text lifeLable = new Text("Life");
    lifeLable.getStyleClass().add("heading");
    lifeBox.getChildren().add(lifeLable);
    Text life = new Text("3");
    life.getStyleClass().add("lives");
    life.textProperty().bind(game.livesProperty().asString());
    lifeBox.getChildren().add(life);

    Text title = new Text("Challenge Mode");
    HBox.setHgrow(title, Priority.ALWAYS);
    title.getStyleClass().add("title");
    title.setTextAlignment(TextAlignment.CENTER);

    topBar.add(scoreBox, 0, 0);
    topBar.add(lifeBox, 2, 0);
    topBar.add(title, 1, 0);
    GridPane.setFillWidth(title, true);
    GridPane.setHgrow(title, Priority.ALWAYS);
    GridPane.setHalignment(title, HPos.CENTER);

    Text highScoreLable = new Text("High Score");
    highScoreLable.getStyleClass().add("heading");
    Text highScore = new Text(this.highScore + "");
    highScore.getStyleClass().add("hiscore");
    highScore.textProperty().bind(this.highScore.asString());
    side.getChildren().add(highScoreLable);
    side.getChildren().add(highScore);

    Text lvLable = new Text("Level");
    lvLable.getStyleClass().add("heading");
    Text lv = new Text("0");
    lv.getStyleClass().add("level");
    lv.textProperty().bind(game.levelProperty().asString());
    side.getChildren().add(lvLable);
    side.getChildren().add(lv);

    Text strikeTitle = new Text("Strike");
    strikeTitle.getStyleClass().add("heading");
    Text strike = new Text("0");
    strike.textProperty().bind(game.multiplierProperty().asString());
    strike.getStyleClass().add("strike");
    side.getChildren().add(strikeTitle);
    side.getChildren().add(strike);

    side.getChildren().add(nextBoard);
    side.getChildren().add(nextBoard2);
    nextBoard2.setPadding(new Insets(20, 0, 0, 0));

    // Handle block on gameboard grid being clicked
    board.setOnBlockClick(this::blockClicked);
    board.setOnRightClicked(this::rotate);
    nextBoard.setOnBlockClick(this::rotate);
    nextBoard2.setOnBlockClick(this::swapCurrentPiece);

    // Timer bar
    this.timerBox = new StackPane();
    mainPane.setBottom(timerBox);
    this.timerBox.getChildren().add(timer);
    StackPane.setAlignment(timer, Pos.CENTER_LEFT);
    BorderPane.setMargin(this.timerBox, new Insets(10, 10, 10, 10));
  }

  /**
   * Handle when a block is clicked
   *
   * @param gameBlock the Game Block that was clocked
   */
  protected void blockClicked(GameBlock gameBlock) {
    game.blockClicked(gameBlock);
  }

  /** Setup the game object and model */
  public void setupGame() {
    logger.info("Starting a new challenge");
    // Start new game
    if (!load) {
      game = new Game(5, 5);
    } else {
      Game tempGame = null;
      try {
        FileInputStream fileIn = new FileInputStream("save.txt");
        game = new Game(5, 5, true);
        game.read(fileIn);
        System.out.println(game.getLives() + "lives");
      } catch (IOException ioe) {
        ioe.printStackTrace();
      }
    }
  }

  /** Initialise the scene and start the game */
  @Override
  public void initialise() {
    logger.info("Initialising Challenge");
    Multimedia.playMusic("/music/game.wav");
    game.setNextPieceListener(this::setNextPiece);
    keyX = 0;
    keyY = 0;
    this.scene.setOnKeyPressed(this::keyListener);
    this.game.setOnLineCleared(this::lineCleared);
    this.game.setOnGameLoop(this::gameLoop);
    this.game.scoreProperty().addListener(this::getHighScore);
    game.start();
    this.game.setOnGameOver(
        () -> {
          this.close();
          this.gameWindow.startScores(game);
        });
  }
  /**
   * Event handler for all the keys.
   *
   * @param keyEvent
   */
  private void keyListener(KeyEvent keyEvent) {
    if (keyEvent.getCode().equals(KeyCode.ESCAPE)) {
      this.gameWindow.startMenu();
      close();
    } else if (keyEvent.getCode().equals(KeyCode.ENTER) || keyEvent.getCode().equals(KeyCode.X)) {
      this.blockClicked(this.board.getBlock(this.keyX, this.keyY));
    } else if (keyEvent.getCode().equals(KeyCode.SPACE) || keyEvent.getCode().equals(KeyCode.R)) {
      this.swapCurrentPiece();
    } else if (keyEvent.getCode().equals(KeyCode.Q)
        || keyEvent.getCode().equals(KeyCode.Z)
        || keyEvent.getCode().equals(KeyCode.OPEN_BRACKET)) {
      this.rotate(3);
    } else if (keyEvent.getCode().equals(KeyCode.E)
        || keyEvent.getCode().equals(KeyCode.C)
        || keyEvent.getCode().equals(KeyCode.CLOSE_BRACKET)) {
      this.rotate(1);
    } else if (keyEvent.getCode().equals(KeyCode.A) || keyEvent.getCode().equals(KeyCode.LEFT)) {
      if (this.keyX > 0) {
        keyX = keyX - 1;
      }
    } else if (keyEvent.getCode().equals(KeyCode.D) || keyEvent.getCode().equals(KeyCode.RIGHT)) {
      if (this.keyX < game.getCols() - 1) {
        keyX++;
      }
    } else if (keyEvent.getCode().equals(KeyCode.W) || keyEvent.getCode().equals(KeyCode.UP)) {
      if (this.keyY > 0) {
        keyY = keyY - 1;
      }
    } else if (keyEvent.getCode().equals(KeyCode.S) || keyEvent.getCode().equals(KeyCode.DOWN)) {
      if (this.keyY < game.getRows() - 1) {
        keyY++;
      }
    }

    board.hover(board.getBlock(keyX, keyY));
  }

  /** Event handler for close of the game; */
  protected void close() {
    try {
      FileOutputStream fileOut = new FileOutputStream("save.txt");
      game.write(fileOut);
      System.out.println("saved to save.txt");

    } catch (IOException e) {
      e.printStackTrace();
    }
    Multimedia.stop();
    game.stop();
    logger.info("closed a game");
  }

  /**
   * Event handler for next piece;
   *
   * @param nextPiece
   */
  public void setNextPiece(GamePiece nextPiece) {
    this.nextBoard.showPiece(nextPiece);
    this.nextBoard2.showPiece(this.game.getNextPiece());
  }

  /**
   * Event handler for swap the current piece
   *
   * @param gameblock1
   */
  protected void swapCurrentPiece(GameBlock gameblock1) {
    swapCurrentPiece();
  }

  protected void swapCurrentPiece() {
    Multimedia.playAudio("/sounds/rotate.wav");
    game.swapCurrentPiece();
    nextBoard.showPiece(game.getCurrentPiece());
    nextBoard2.showPiece(game.getNextPiece());
  }

  /** Rotate methods for blocks */
  protected void rotate() {
    rotate(1);
  }

  protected void rotate(GameBlock gameblock1) {
    rotate();
  }

  protected void rotate(int rotations) {
    Multimedia.playAudio("/sounds/rotate.wav");
    game.rotateCurrentPiece(rotations);
    nextBoard.showPiece(game.getCurrentPiece());
  }

  /**
   * Handler method for line cleared event.
   *
   * @param blocks
   */
  protected void lineCleared(Set<GameBlockCoordinate> blocks) {
    board.fadeOut(blocks);
    Multimedia.playAudio("/sounds/clear.wav");
  }

  /**
   * Listener method to display the time bar
   *
   * @param delay of time left
   */
  protected void gameLoop(int delay) {
    Timeline timeline =
        new Timeline(
            new KeyFrame[] {
              new KeyFrame(
                  Duration.ZERO,
                  new KeyValue[] {new KeyValue(this.timer.fillProperty(), Color.BLUE)}),
              new KeyFrame(
                  Duration.ZERO,
                  new KeyValue[] {
                    new KeyValue(this.timer.widthProperty(), this.timerBox.getWidth())
                  }),
              new KeyFrame(
                  new Duration((double) delay * 0.25),
                  new KeyValue[] {new KeyValue(this.timer.fillProperty(), Color.GREEN)}),
              new KeyFrame(
                  new Duration((double) delay * 0.5),
                  new KeyValue[] {new KeyValue(this.timer.fillProperty(), Color.YELLOW)}),
              new KeyFrame(
                  new Duration((double) delay * 0.75),
                  new KeyValue[] {new KeyValue(this.timer.fillProperty(), Color.RED)}),
              new KeyFrame(
                  new Duration((double) delay),
                  new KeyValue[] {new KeyValue(this.timer.widthProperty(), 0)})
            });
    timeline.play();
  }

  protected void getHighScore(
      ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
    if (newValue.intValue() > this.highScore.get()) {
      this.highScore.set(newValue.intValue());
    }
  }
}
