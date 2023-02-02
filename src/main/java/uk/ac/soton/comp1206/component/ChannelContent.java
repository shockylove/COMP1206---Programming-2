package uk.ac.soton.comp1206.component;

import java.util.ArrayList;

import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import uk.ac.soton.comp1206.scene.LobbyScene;

public class ChannelContent extends VBox {
  private LobbyScene lobby;
  public ArrayList<String> players;
  public TextFlow playerList;
  public ScrollPane scroller;
  public VBox messages;
  private boolean scrollToBottom = false;

  public ChannelContent(LobbyScene lobby) {


    this.lobby = lobby;
    this.players = lobby.players;
    playerList = new TextFlow();
    playerList.getStyleClass().add("playerBox");

    this.getStyleClass().add("gameBox");
    this.setSpacing(10);
    this.setPadding(new Insets(5, 5, 5, 5));
    this.getChildren().add(playerList);

    scroller = new ScrollPane();
    scroller.setPrefHeight(lobby.gameWindow.getHeight() / 2);
    scroller.getStyleClass().add("scroller");
    scroller.setFitToWidth(true);
    scroller.setVvalue(1.0);
    messages = new VBox();
    messages.getStyleClass().add("messages");
    scroller.setContent(messages);
    disPlayMessage("Type /nick NewName to change your name in the lobby");
    this.getChildren().add(scroller);

    TextField messageSent = new TextField();
    messageSent.setPromptText("Send a message");
    messageSent.getStyleClass().add("messageBox");
    messageSent.setOnKeyPressed(
        (e) -> {
          if (e.getCode().equals(KeyCode.ENTER)) {
            lobby.sendMsg(messageSent.getText());
            messageSent.clear();
          }
        });
    this.getChildren().add(messageSent);

    AnchorPane buttons = new AnchorPane();
    Button leaveButton = new Button("Leave Channel");
    leaveButton.setOnAction(
        (e) -> {
          lobby.sendMessage("PART");
          lobby.currentChannel = null;
          lobby.sendMessage("LIST");
        });
    buttons.getChildren().add(leaveButton);
    AnchorPane.setRightAnchor(leaveButton, (double) 0);
    Button newGameButton = new Button("Start Game");
    newGameButton.visibleProperty().bind(lobby.host);
    newGameButton.setOnAction(
        (e) -> {
          lobby.communicator.send("START");
        });
    buttons.getChildren().add(newGameButton);
    AnchorPane.setLeftAnchor(newGameButton, (double) 0);
    this.getChildren().add(buttons);
  }

  /**
   * Update the player list.
   *
   * @param newPlayers
   */
  public void updatePlayer(ArrayList<String> newPlayers) {

    playerList.getChildren().clear();

    for (String name : newPlayers) {
      Text temp = new Text(name + "   ");
      if (name.equals(lobby.name.get())) {
        temp.getStyleClass().add("myname");
      }
      playerList.getChildren().add(temp);
    }
  }

  /**
   * Show one message on the pane.
   *
   * @param message
   */
  public void disPlayMessage(String message) {
    Text temp = new Text(message);
    messages.getChildren().add(temp);
    scroller.setVvalue(1.0);
  }
}
