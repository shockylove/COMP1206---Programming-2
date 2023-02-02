package uk.ac.soton.comp1206.component;

import javafx.collections.ListChangeListener;
import javafx.geometry.Pos;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.util.Pair;

public class Leaderboard extends ScoresList {

  public Leaderboard() {
    super();
    scores.addListener(
        (ListChangeListener.Change<? extends Pair<String, Integer>> c) -> {
          this.update();
        });
  }

  public void update() {
    int max = 0;
    scoreBoxes.clear();
    this.getChildren().clear();
    for (Pair<String, Integer> score : scores) {
      System.out.println(scores);
      if (max < 5) {
        HBox box = new HBox();
        Text name = new Text(score.getKey() + ":");

        if (score.getKey().equals(this.name.get())) {
          name.getStyleClass().add("myscore");
        }
        Text num;
        if (score.getValue().equals("-1")) {
          num = new Text("DEAD");
        } else {
          num = new Text(score.getValue().toString());
        }
        box.getChildren().add(name);
        box.getChildren().add(num);
        this.getChildren().add(box);
        this.scoreBoxes.add(box);

        // Styles
        name.setTextAlignment(TextAlignment.CENTER);
        HBox.setHgrow(name, Priority.ALWAYS);
        box.setAlignment(Pos.CENTER);
        box.setSpacing(20);
        box.getStyleClass().add("scoreitem");
        name.getStyleClass().add("scorer");
        num.getStyleClass().add("points");
        num.setTextAlignment(TextAlignment.CENTER);
        HBox.setHgrow(num, Priority.ALWAYS);

        max++;
      }
    }
  }

  public void removePlayer(String name) {
    // this.scoreBoxes.remove(name);
  }
}
