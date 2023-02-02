package uk.ac.soton.comp1206.scene;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.util.Pair;
import uk.ac.soton.comp1206.component.ScoresList;
import uk.ac.soton.comp1206.game.Game;
import uk.ac.soton.comp1206.game.MultiplayerGame;
import uk.ac.soton.comp1206.network.Communicator;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;
import uk.ac.soton.comp1206.utility.Multimedia;
import uk.ac.soton.comp1206.utility.Save;

public class ScoresScene extends BaseScene {
  private Game game;

  private BorderPane mainPane;
  private VBox scoreBox;
  private Text highScoreTitle;
  private static final Logger logger = LogManager.getLogger(ScoresScene.class);
  private SimpleListProperty<Pair<String, Integer>> localScores =
      new SimpleListProperty<Pair<String, Integer>>();
  private ArrayList<Pair<String, Integer>> scoreList;
  private ObservableList<Pair<String, Integer>> localScoreList;
  private ObservableList<Pair<String, Integer>> onlineScoreList;
  private SimpleStringProperty name = new SimpleStringProperty();
  private Pair<String, Integer> newScore;
  private ScoresList localBox;
  private ScoresList remoteBox;
  private Timer timer;
  private SimpleBooleanProperty showScore = new SimpleBooleanProperty();
  public static Save save;
  private boolean localHighScore = false;

  private SimpleListProperty<Pair<String, Integer>> remoteScores =
      new SimpleListProperty<Pair<String, Integer>>();
  private ArrayList<Pair<String, Integer>> remoteList;
  private Communicator communicator;
  private boolean received = false;
  private boolean remoteHighScore;

  private boolean updated = false;

  public ScoresScene(GameWindow gameWindow, Game game) {
    super(gameWindow);
    this.game = game;
    showScore.set(true);
    communicator = gameWindow.getCommunicator();
    this.remoteList = new ArrayList<Pair<String, Integer>>();
    this.onlineScoreList = FXCollections.observableArrayList(this.remoteList);
    if (game instanceof MultiplayerGame) {
      this.scoreList = ((MultiplayerGame) game).scores;
    } else {
      this.scoreList = Save.loadScores();
    }
    System.out.println(scoreList);
  }

  @Override
  public void initialise() {
    Multimedia.playAudio("/sounds/explode.wav");
    Multimedia.playMusic("/music/end.wav");
    this.communicator.addListener(
        (message) -> {
          Platform.runLater(
              () -> {
                this.getMessage(message.trim());
              });
        });
    communicator.send("HISCORES");
    save = new Save();
    scoreList = save.loadScores();
  }

  private void getMessage(String message) {
    logger.info("Received message: {}", message);
    String[] subMessage = message.split(" ", 2);
    String tag = subMessage[0];
    if (tag.equals("HISCORES")) {
      if (subMessage.length > 1) {
        this.loadOnlineScore(subMessage[1]);

      } else {
        this.loadOnlineScore("");
      }
    }
  }

  private void loadOnlineScore(String data) {
    logger.info("Updating the online score: {}", data);
    remoteList.clear();
    String[] sets = data.split("\n");
    for (String set : sets) {
      String[] pairs = set.split(":", 2);
      this.remoteList.add(new Pair<String, Integer>(pairs[0], Integer.parseInt(pairs[1])));
    }
    this.onlineScoreList.clear();
    this.onlineScoreList.addAll(this.remoteList);
    if (!this.received && !(this.game instanceof MultiplayerGame)) {
      compare();
      received = true;
    } else {
      this.reveal();
    }
  }

  private void compare() {

    // checking if the new score beats the remote scores
    for (Pair<String, Integer> scorePair : this.remoteList) {
      if (scorePair.getValue() < game.getScore()) {
        this.remoteHighScore = true;
        break;
      }
    }

    for (Pair<String, Integer> scorePair : this.scoreList) {
      if (scorePair.getValue() < game.getScore()) {
        this.localHighScore = true;
        break;
      }
    }

    // to check if the loaded list is not full;
    if (scoreList.size() < 10) {
      this.localHighScore = true;
    }
    if (this.remoteList.size() < 10) {
      this.remoteHighScore = true;
    }

    // if there is a new high score, ask the player name
    if (remoteHighScore || localHighScore) {
      TextField name = new TextField();
      name.setPromptText("Enter your name");
      name.setPrefWidth((double) (this.gameWindow.getWidth() / 2));
      name.requestFocus();
      this.scoreBox.getChildren().add(2, name);
      Button button = new Button("Submit");
      button.setDefaultButton(true);
      this.scoreBox.getChildren().add(3, button);
      this.updated = false;
      button.setOnAction(
          (e) -> {
            this.name.set(name.getText());
            if (localHighScore = true) {
              scoreList.add(new Pair<String, Integer>(this.name.get(), this.game.getScore()));
              scoreList.sort(
                  (a, b) -> {
                    return b.getValue().compareTo(a.getValue());
                  });
              scoreList.remove(scoreList.size() - 1);
              localHighScore = false;
            }

            if (remoteHighScore = true) {
              this.remoteList.add(new Pair<String, Integer>(this.name.get(), this.game.getScore()));
              remoteList.sort(
                  (a, b) -> {
                    return b.getValue().compareTo(a.getValue());
                  });
              remoteList.remove(remoteList.size() - 1);
              this.communicator.send("HISCORES " + this.name.get() + ":" + game.getScore());
              this.communicator.send("HISCORES");
              remoteHighScore = false;
            }

            Save.writeScores(scoreList);

            localScoreList = FXCollections.observableArrayList(scoreList);
            localScores.set(localScoreList);
            localBox.getScoreProperty().bind(localScores);

            this.onlineScoreList = FXCollections.observableArrayList(this.remoteList);
            this.remoteScores.set(onlineScoreList);
            remoteBox.getScoreProperty().bind(remoteScores);

            this.reveal();
            Multimedia.playAudio("/sounds/pling.wav");
            Save.writeScores(scoreList);
            updated = true;
          });
    }

    this.reveal();
  }

  /** Show the score box and add animation to it */
  private void reveal() {
    if (this.timer != null) {
      this.timer.cancel();
      this.timer.purge();
    }

    TimerTask task =
        new TimerTask() {
          public void run() {
            Platform.runLater(
                () -> {
                  ScoresScene.this.returnToMenu();
                });
          }
        };
    this.timer = new Timer();
    this.timer.schedule(task, 15000);
    this.scene.setOnKeyPressed(
        (e) -> {
          this.returnToMenu();
        });
    this.localBox.reveal();
    this.remoteBox.reveal();
  }

  /** Return to the menu */
  private void returnToMenu() {
    if (this.timer != null) {
      timer.cancel();
    }
    this.gameWindow.startMenu();
  }

  @Override
  public void build() {
    logger.info("Building " + this.getClass().getName());

    root = new GamePane(gameWindow.getWidth(), gameWindow.getHeight());

    var scorePane = new StackPane();
    scorePane.setMaxWidth(gameWindow.getWidth());
    scorePane.setMaxHeight(gameWindow.getHeight());
    scorePane.getStyleClass().add("menu-background");
    root.getChildren().add(scorePane);
    mainPane = new BorderPane();
    scorePane.getChildren().add(mainPane);

    scoreBox = new VBox();
    scoreBox.setAlignment(Pos.TOP_CENTER);
    mainPane.setCenter(scoreBox);

    ImageView image = new ImageView(Multimedia.getImage("/images/TetrECS.png"));
    image.setPreserveRatio(true);
    image.setFitWidth((double) this.gameWindow.getWidth() * 0.5);

    scoreBox.getChildren().add(image);

    highScoreTitle = new Text("High Scores");
    highScoreTitle.setTextAlignment(TextAlignment.CENTER);
    highScoreTitle.getStyleClass().add("bigtitle");
    scoreBox.getChildren().add(highScoreTitle);

    Text yourScoreTitle = new Text("Your Score");
    yourScoreTitle.setTextAlignment(TextAlignment.CENTER);
    yourScoreTitle.getStyleClass().add("title");
    scoreBox.getChildren().add(yourScoreTitle);

    Text yourScore = new Text();
    yourScore.setTextAlignment(TextAlignment.CENTER);
    yourScore.getStyleClass().add("myscore");
    yourScore.textProperty().bind(game.scoreProperty().asString());
    scoreBox.getChildren().add(yourScore);

    localBox = new ScoresList();
    remoteBox = new ScoresList();
    GridPane scores = new GridPane();
    scores.visibleProperty().bind(this.showScore);
    scores.setAlignment(Pos.CENTER);
    scores.setHgap(100.0D);
    scoreBox.getChildren().add(scores);
    System.out.println(save.loadScores());
    localScoreList = FXCollections.observableArrayList(scoreList);
    localScores.set(localScoreList);
    localBox.getScoreProperty().bind(localScores);
    this.onlineScoreList = FXCollections.observableArrayList(this.remoteList);
    this.remoteScores.set(onlineScoreList);
    remoteBox.getScoreProperty().bind(remoteScores);
    scores.add(localBox, 0, 1);
    scores.add(remoteBox, 1, 1);
    GridPane.setHalignment(localBox, HPos.CENTER);
    GridPane.setHalignment(remoteBox, HPos.CENTER);
  }
}
