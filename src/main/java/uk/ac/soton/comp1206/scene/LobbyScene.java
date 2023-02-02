package uk.ac.soton.comp1206.scene;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import uk.ac.soton.comp1206.component.ChannelContent;
import uk.ac.soton.comp1206.component.ChannelList;
import uk.ac.soton.comp1206.network.Communicator;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;
import uk.ac.soton.comp1206.utility.Multimedia;

public class LobbyScene extends BaseScene {

  public static final Logger logger = LogManager.getLogger(LobbyScene.class);
  public Communicator communicator;
  private BorderPane mainPane;
  private ChannelList channelList;
  private ChannelContent content;
  private Timer timer;
  public SimpleStringProperty currentChannel;
  public SimpleBooleanProperty joined = new SimpleBooleanProperty(false);
  public SimpleBooleanProperty host = new SimpleBooleanProperty(false);
  public SimpleStringProperty name = new SimpleStringProperty();
  public ArrayList<String> players = new ArrayList<String>();

  public LobbyScene(GameWindow gameWindow) {
    super(gameWindow);
    communicator = gameWindow.getCommunicator();
    currentChannel = new SimpleStringProperty();
  }

  @Override
  public void initialise() {
    this.scene.setOnKeyPressed(
        (e) -> {
          if (e.getCode().equals(KeyCode.ESCAPE)) {
            if (this.currentChannel.isNotEmpty().get()) {
              this.communicator.send("PART");
            }
            this.clean();
            this.gameWindow.startMenu();
          }
        });

    communicator.addListener(
        (message) -> {
          Platform.runLater(
              () -> {
                this.getMessage(message.trim());
              });
        });

    TimerTask refresh =
        new TimerTask() {
          public void run() {
            LobbyScene.logger.info("Refreshing lobby channel");
            LobbyScene.this.sendMessage("LIST");
          }
        };

    this.timer = new Timer();
    this.timer.schedule(refresh, 0, 5000);
  }

  public void sendMessage(String message) {
    this.communicator.send(message);
  }

  public void getMessage(String message) {
    logger.info("Received Message: {}", message);
    String[] subMessage = message.split(" ", 2);
    String tab = subMessage[0];
    if (tab.equals("CHANNELS") && subMessage.length != 1) {
      ArrayList<String> channels = new ArrayList<String>();
      String[] temp = subMessage[1].split("\n");
      for (String name : temp) {
        channels.add(name);
      }
      this.channelList.update(channels);
    } else if (tab.equals("JOIN")) {
      join(subMessage[1]);
    } else if (tab.equals("USERS")) {
      receiveUsers(subMessage[1]);
    } else if (tab.equals("MSG")) {
      receiveMSG(subMessage[1]);
    } else if (tab.equals("NICK")) {
      String[] temp = subMessage[1].split(":");
      if (temp.length > 1) receiveNick(temp[1]);
    } else if (tab.equals("PARTED")) {
      this.joined.set(false);
    } else if (tab.equals("START")) {
      logger.info("Received game start");
      this.startGame();
    } else if (tab.equals("ERROR")) {
      Alert alert = new Alert(AlertType.ERROR, subMessage[1], new ButtonType[0]);
      alert.showAndWait();
    }
  }

  private void startGame() {
    this.clean();
    this.gameWindow.startMutiPlayer();
  }

  private void clean() {
    if (this.timer != null) {
      this.timer.purge();
      this.timer.cancel();
      this.timer = null;
    }
  }

  /** Handle receive message and show in chat. */
  public void receiveMSG(String msg) {
    this.content.disPlayMessage(msg);
    Multimedia.playAudio("/sounds/message.wav");
  }

  /**
   * Create a new channel.
   *
   * @param text
   */
  private void createChannel(String text) {
    this.joined.set(true);
  }

  /**
   * Request Join the Channel.
   *
   * @param name
   */
  public void requestJoin(String name) {
    this.communicator.send("JOIN " + name);
  }

  public void join(String channelName) {
    currentChannel.set(channelName);
    this.joined.set(true);
    Multimedia.playAudio("/sounds/message.wav");
  }

  /** Request the user list in the channel. */
  public void requestUsers() {
    this.communicator.send("USERS");
  }

  /**
   * Update the userList when receive users.
   *
   * @param users
   */
  public void receiveUsers(String users) {
    logger.info("Received channel user list: {}", users);
    this.players.clear();
    String[] user = users.split("\n");
    for (int i = 0; i < user.length; i++) {
      this.players.add(user[i]);
    }
    this.content.updatePlayer(players);
    Multimedia.playAudio("/sounds/message.wav");
  }

  public void receiveNick(String newName) {
    this.name.set(newName);
    this.requestUsers();
  }

  /**
   * Send a chat message to the sever.
   *
   * @param text
   */
  public void sendMsg(String text) {
    if (text.startsWith("/nick")) {
      String[] subText = text.split(" ", 2);
      this.sendMessage("NICK " + subText[1]);

    } else {
      this.sendMessage("MSG " + text);
    }
  }

  @Override
  public void build() {
    logger.info("Building " + this.getClass().getName());
    this.root = new GamePane(this.gameWindow.getWidth(), this.gameWindow.getHeight());
    StackPane lobbyPane = new StackPane();
    lobbyPane.setMaxWidth(this.gameWindow.getWidth());
    lobbyPane.setMaxHeight(this.gameWindow.getHeight());
    lobbyPane.getStyleClass().add("menu-background");
    this.root.getChildren().add(lobbyPane);
    this.mainPane = new BorderPane();
    lobbyPane.getChildren().add(mainPane);
    Text multiplayerText = new Text("Multiplayer");
    BorderPane.setAlignment(multiplayerText, Pos.CENTER);
    multiplayerText.setTextAlignment(TextAlignment.CENTER);
    multiplayerText.getStyleClass().add("title");
    this.mainPane.setTop(multiplayerText);
    GridPane gridPane = new GridPane();
    gridPane.setHgap(10);
    gridPane.setVgap(10);
    gridPane.setPadding(new Insets(5, 5, 5, 5));

    Text channelText = new Text("Current Games");
    channelText.setTextAlignment(TextAlignment.CENTER);
    channelText.getStyleClass().add("heading");
    gridPane.add(channelText, 0, 0);
    VBox button = new VBox();

    Text hostButton = new Text("Host Game");
    hostButton.visibleProperty().bind(joined.not());
    hostButton.getStyleClass().add("channelItem");
    button.getChildren().add(hostButton);
    TextField newChannel = new TextField();
    newChannel.setVisible(false);
    this.channelList = new ChannelList(this);
    this.channelList.currentChannelProperty().bind(this.currentChannel);

    gridPane.add(button, 0, 2);
    hostButton.setOnMouseClicked(
        (e) -> {
          Multimedia.playAudio("/sounds/rotate.wav");
          newChannel.setVisible(true);
        });
    newChannel.setOnKeyPressed(
        (e) -> {
          if (e.getCode().equals(KeyCode.ENTER)) {
            Multimedia.playAudio("/sounds/rotate.wav");
            this.sendMessage("CREATE " + newChannel.getText().trim());

            this.sendMessage("LIST");
            this.sendMessage("USERS");
            this.host.set(true);
            newChannel.setVisible(false);
            this.currentChannel.set(newChannel.getText());
            createChannel(newChannel.getText());

            newChannel.clear();
          }
        });
    button.getChildren().add(newChannel);

    Text lobbyText = new Text();
    lobbyText.textProperty().bind(this.channelList.currentChannelProperty());
    lobbyText.visibleProperty().bind(joined);
    lobbyText.setTextAlignment(TextAlignment.CENTER);
    lobbyText.getStyleClass().add("heading");
    gridPane.add(lobbyText, 1, 1);

    this.content = new ChannelContent(this);
    gridPane.add(content, 1, 2);
    GridPane.setHgrow(this.content, Priority.ALWAYS);
    content.visibleProperty().bind(joined);

    mainPane.setCenter(gridPane);
    gridPane.add(this.channelList, 0, 1);
  }
}
