package uk.ac.soton.comp1206.component;

import java.util.ArrayList;
import java.util.List;

import javafx.beans.property.SimpleStringProperty;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import uk.ac.soton.comp1206.scene.LobbyScene;

/**
 * Class for the lobbyscene to show the channel lists.
 *
 * @author Shock
 */
public class ChannelList extends VBox {
  public final ArrayList<String> channelList;
  private LobbyScene lobby;
  public SimpleStringProperty currentChannel;

  public ChannelList(LobbyScene lobby) {
    this.lobby = lobby;
    this.channelList = new ArrayList<String>();
    currentChannel = new SimpleStringProperty();
  }

  /** Update the list and boxes when there is a change. */
  public void update(List<String> input) {
    this.getChildren().clear();
    lobby.logger.info("updating list");

    for (String name : input) {
      lobby.logger.info("Adding {} ", name);
      channelList.add(name);
      Text temp = new Text(name);
      temp.getStyleClass().add("channelItem");
      System.out.println(currentChannel.get());
      if (currentChannel != null) {
        if (name.equals(currentChannel.get())) {
          temp.getStyleClass().add("selected");
        }
      }

      this.getChildren().add(temp);

      if (!this.lobby.joined.get()) {
        temp.setOnMouseClicked(
            (e) -> {
              this.lobby.requestJoin(name);
              this.lobby.joined.set(true);
            });
      }
    }
  }

  public SimpleStringProperty currentChannelProperty() {
    return this.currentChannel;
  }
}
