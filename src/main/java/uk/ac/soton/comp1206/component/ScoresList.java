package uk.ac.soton.comp1206.component;

import java.util.ArrayList;

import javafx.animation.Animation;
import javafx.animation.FadeTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.Transition;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ListChangeListener;
import javafx.geometry.Pos;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.util.Duration;
import javafx.util.Pair;

public class ScoresList extends VBox {

  public final SimpleListProperty<Pair<String, Integer>> scores =
      new SimpleListProperty<Pair<String, Integer>>();
  protected SimpleStringProperty name = new SimpleStringProperty();
  protected ArrayList<HBox> scoreBoxes;
  private int scoreNum;

  public ScoresList() {
    this.getStyleClass().add("scorelist");
    this.setSpacing(1);
    scoreNum = 10;
    scoreBoxes = new ArrayList<HBox>();
    this.name.addListener(
        (e) -> {
          this.update();
        });

    // This thing kills me......
    // add listener to the list change c
    scores.addListener(
        (ListChangeListener.Change<? extends Pair<String, Integer>> c) -> {
          this.update();
        });
  }

  /** Update the list of high scores. */
  private void update() {
    scoreBoxes.clear();
    getChildren().clear();
    int count = 0;
    for (Pair<String, Integer> score : scores) {
      if (count < this.scoreNum) {
        HBox box = new HBox();
        Text name = new Text(score.getKey() + ":");
        // if is played by self, highlight it.
        if (score.getKey().equals(this.name.get())) {
          name.getStyleClass().add("myscore");
        }

        Text num = new Text(score.getValue().toString());
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
      }
    }
    reveal();
  }

  /** Animation for revealing the scores in the list. */
  public void reveal() {
    ArrayList<Transition> anim = new ArrayList<Transition>();
    for (HBox box : scoreBoxes) {
      FadeTransition fade = new FadeTransition(new Duration(300), box);
      fade.setFromValue(0);
      fade.setToValue(1);
      anim.add(fade);
    }
    System.out.println("REVIED");
    SequentialTransition movie =
        new SequentialTransition(
            (Animation[])
                anim.toArray(
                    (x$0) -> {
                      return new Animation[x$0];
                    }));
    movie.play();
  }

  /**
   * Get method for score preperty
   *
   * @return score property
   */
  public SimpleListProperty<Pair<String, Integer>> getScoreProperty() {
    return this.scores;
  }

  /**
   * Get method for names property
   *
   * @return name of player
   */
  public SimpleStringProperty getNameProperty() {
    return this.name;
  }
}
