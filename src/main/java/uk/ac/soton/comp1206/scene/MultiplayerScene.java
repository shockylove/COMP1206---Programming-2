package uk.ac.soton.comp1206.scene;

import java.util.ArrayList;
import java.util.HashMap;

import javafx.application.Platform;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.TextField;
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
import javafx.scene.text.TextFlow;
import javafx.util.Pair;
import uk.ac.soton.comp1206.component.GameBoard;
import uk.ac.soton.comp1206.component.Leaderboard;
import uk.ac.soton.comp1206.component.PieceBoard;
import uk.ac.soton.comp1206.game.MultiplayerGame;
import uk.ac.soton.comp1206.network.Communicator;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;
import uk.ac.soton.comp1206.utility.Multimedia;

public class MultiplayerScene extends ChallengeScene {

  private final Communicator communicator;
  private ArrayList<Pair<String, Integer>> onlineScores = new ArrayList<Pair<String, Integer>>();
  private ObservableList<Pair<String, Integer>> onlineScoreList;
  private Leaderboard leaderboard;
  private Text incomingMSG;
  private boolean inchat = false;
  SimpleListProperty<Pair<String, Integer>> scores = new SimpleListProperty();
  private TextField msgSend;

  private HBox boardBox = new HBox();
  private VBox othersBoard = new VBox();
  private HashMap<String, GameBoard> boardList = new HashMap<String, GameBoard>();

  public MultiplayerScene(GameWindow gameWindow) {
    super(gameWindow);
    communicator = gameWindow.getCommunicator();
  }

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

    this.updateName();
    this.updateScores();

    this.game
        .levelProperty()
        .addListener(
            (observable, oldValue, newValue) -> {
              Multimedia.playAudio("/sounds/level.wav");
            });

    this.game
        .scoreProperty()
        .addListener(
            (observable, oldValue, newValue) -> {
              MultiplayerScene.this.sendMessage("SCORE " + newValue);
            });
    communicator.addListener(
        (message) -> {
          Platform.runLater(
              () -> {
                this.receiveMessage(message.trim());
              });
        });
    this.game.setOnGameOver(
        () -> {
          this.close();
          this.gameWindow.startScores(game);
        });
  }

  /** Update the names from the sever. */
  private void updateName() {
    this.communicator.send("NICK");
  }

  /** Update the scores of the players from the sever. */
  private void updateScores() {
    this.communicator.send("SCORES");
  }

  /**
   * Send message to the sever.
   *
   * @param message s
   */
  protected void sendMessage(String message) {
    this.communicator.send(message);
  }

  /** Receive message from the sever. */
  private void receiveMessage(String message) {

    System.out.println("line*****");
    logger.info("Received message: {}", message);
    String[] subMessage = message.split(" ", 2);
    if (subMessage[0].equals("SCORES") && subMessage.length > 1) {

      this.receiveScores(subMessage[1]);
    } else if (subMessage[0].equals("NICK") && subMessage.length > 1) {
      if (!subMessage[1].contains(":")) {
        this.setName(subMessage[1]);
      }
    } else if (subMessage[0].equals("MSG")) {
      this.receiveMsg(subMessage[1]);
    } else if (subMessage[0].equals("BOARD")) {
      this.receiveBoard(subMessage[1]);
    }
  }

  /**
   * Handle receiving others' board info.
   *
   * @param message
   */
  private void receiveBoard(String message) {
    this.othersBoard.getChildren().clear();
    String[] boardInfo = message.split(":");
    String player = boardInfo[0];
    // if (!player.equals(this.game.nameProperty().get())) {

    String[] playerBoard = boardInfo[1].split(" ");
    int x = 0;
    int y = 0;
    for (int i = 0; i < 25; i++) {
      int j = Integer.parseInt(playerBoard[i]);
      boardList.get(player).grid.set(x, y, j);

      x++;
      if (x == 5) {
        x = 0;
        y++;
      }
    }
    this.othersBoard.getChildren().add(boardList.get(player));
    HBox textBox = new HBox();
    Text playerBox = new Text(player);
    playerBox.getStyleClass().add("scorer");
    playerBox.setTextAlignment(TextAlignment.CENTER);
    textBox.setPadding(new Insets(0, 20, 20, 50));
    textBox.getChildren().add(playerBox);
    othersBoard.getChildren().add(textBox);
    // }
  }

  /**
   * Pop up received msg from sever.
   *
   * @param
   *
   */
  private void receiveMsg(String msg) {
    this.incomingMSG.setText(msg);
    Multimedia.playAudio("/sounds/message.wav");
  }

  /**
   * Set the new name of the game.
   *
   * @param name
   */
  private void setName(String name) {
    this.game.nameProperty().set(name);
  }

  /**
   * Update the scores, as well as update the board list has others boards.
   *
   * @param scores
   */
  private void receiveScores(String scores) {
    System.out.println("receive scores" + scores);
    this.onlineScores.clear();
    this.boardList.clear();
    String[] scorePairs = scores.split("\n");
    for (int i = 0; i < scorePairs.length; i++) {
      String[] nameScore = scorePairs[i].split(":");

      if (nameScore[2].equals("DEAD")) {
        nameScore[2] = "-1";
      }

      this.onlineScores.add(
          new Pair<String, Integer>(nameScore[0], Integer.parseInt(nameScore[1])));
      GameBoard tempGrid =
          new GameBoard(5, 5, this.gameWindow.getWidth() / 10, this.gameWindow.getWidth() / 10);
      tempGrid.setPadding(new Insets(20, 20, 0, 50));
      tempGrid.setReadOnly(true);
      tempGrid.name = nameScore[0];

      boardList.put(tempGrid.name, tempGrid);
    }

    this.onlineScores.sort(
        (a, b) -> {
          return ((Integer) b.getValue()).compareTo((Integer) a.getValue());
        });
    onlineScoreList = FXCollections.observableArrayList(this.onlineScores);
    this.scores.set(onlineScoreList);
  }

  /**
   * Event handler for all the keys, added keyevents to handle chat mode.
   *
   * @param keyEvent
   */
  private void keyListener(KeyEvent keyEvent) {
    if (keyEvent.getCode().equals(KeyCode.ESCAPE)) {
      if (!inchat) {
        this.gameWindow.startMenu();
        close();
      } else {
        msgSend.setEditable(false);
        msgSend.setVisible(false);
        msgSend.clear();
        inchat = false;
      }
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
    } else if (keyEvent.getCode().equals(KeyCode.T)) {
      inchat = true;
      this.msgSend.setVisible(true);
      this.msgSend.setEditable(true);
    }

    board.hover(board.getBlock(keyX, keyY));
  }

  public void setupGame() {
    logger.info("Starting a new MultiplayerGame");
    game = new MultiplayerGame(communicator, 5, 5);
  }

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

    VBox display = new VBox();
    display.setAlignment(Pos.CENTER);
    VBox mainBoard = new VBox();
    mainBoard.setAlignment(Pos.CENTER);
    boardBox.setAlignment(Pos.CENTER);
    this.board =
        new GameBoard(game.getGrid(), gameWindow.getWidth() / 3, gameWindow.getWidth() / 3);
    boardBox.getChildren().add(board);
    boardBox.getChildren().add(this.othersBoard);
    mainBoard.getChildren().add(boardBox);

    display.getChildren().add(mainBoard);
    mainPane.setCenter(display);
    this.incomingMSG = new Text("In-Game Chat: Press T to send a chat message");
    TextFlow incomingFlow = new TextFlow();
    incomingFlow.setTextAlignment(TextAlignment.CENTER);
    incomingFlow.getChildren().add(this.incomingMSG);
    incomingFlow.getStyleClass().add("messages");
    display.getChildren().add(incomingFlow);

    this.msgSend = new TextField();
    msgSend.setVisible(false);
    msgSend.setEditable(false);
    msgSend.getStyleClass().add("messageBox");
    msgSend.setOnKeyPressed(
        (e) -> {
          if (e.getCode().equals(KeyCode.ENTER)) {
            this.sendMSG(msgSend.getText());
            msgSend.clear();
            inchat = false;
          }
        });
    display.getChildren().add(msgSend);

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

    Text title = new Text("Multiplayer Mode");
    HBox.setHgrow(title, Priority.ALWAYS);
    title.getStyleClass().add("title");
    title.setTextAlignment(TextAlignment.CENTER);

    topBar.add(scoreBox, 0, 0);
    topBar.add(lifeBox, 2, 0);
    topBar.add(title, 1, 0);
    GridPane.setFillWidth(title, true);
    GridPane.setHgrow(title, Priority.ALWAYS);
    GridPane.setHalignment(title, HPos.CENTER);

    Leaderboard leaderboard = new Leaderboard();
    onlineScoreList = FXCollections.observableArrayList(this.onlineScores);
    this.scores.set(onlineScoreList);
    leaderboard.getScoreProperty().bind(this.scores);
    Text leaderboardTitle = new Text("Scores");
    leaderboardTitle.getStyleClass().add("heading");
    side.getChildren().add(leaderboardTitle);
    side.getChildren().add(leaderboard);

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
   * Send chat message to the sever.
   *
   * @param msg
   */
  private void sendMSG(String msg) {
    msgSend.setEditable(false);
    msgSend.setVisible(false);
    this.communicator.send("MSG " + msg);
  }
}
