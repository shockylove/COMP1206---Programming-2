package uk.ac.soton.comp1206.scene;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javafx.geometry.Pos;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.TextFlow;
import uk.ac.soton.comp1206.component.PieceBoard;
import uk.ac.soton.comp1206.game.GamePiece;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;
import uk.ac.soton.comp1206.utility.Multimedia;

public class InstructionsScene extends BaseScene {
  private static final Logger logger = LogManager.getLogger(InstructionsScene.class);

  public InstructionsScene(GameWindow gameWindow) {
    super(gameWindow);
    logger.info("Creating Instruction Scene");
  }
  /** Initialise the instruction scene. */
  @Override
  public void initialise() {
    this.scene.setOnKeyPressed(
        (e) -> {
          this.gameWindow.startMenu();
        });
  }

  /** Build the scene. */
  @Override
  public void build() {
    this.root = new GamePane(this.gameWindow.getWidth(), this.gameWindow.getHeight());
    StackPane introPane = new StackPane();
    introPane.setMaxWidth(this.gameWindow.getWidth());
    introPane.setMaxHeight(this.gameWindow.getHeight());
    introPane.getStyleClass().add("menu-background");
    this.root.getChildren().add(introPane);

    BorderPane mainPane = new BorderPane();
    introPane.getChildren().add(mainPane);

    VBox introBox = new VBox();
    introBox.setAlignment(Pos.CENTER);
    mainPane.setCenter(introBox);
    Text introTitle = new Text("Instructions");
    introTitle.getStyleClass().add("heading");
    introBox.getChildren().add(introTitle);

    TextFlow introText =
        new TextFlow(
            new Text(
                "TetrECS is a fast-paced gravity-free block placement game, "
                    + "where you must survive by clearing rows through careful placement of"
                    + "the upcoming blocks before the time runs out."
                    + "Lose all 3 lives and you're destroyed!"));
    introBox.getChildren().add(introText);
    introText.getStyleClass().add("instructions");
    introText.setTextAlignment(TextAlignment.CENTER);

    ImageView introImage = new ImageView(Multimedia.getImage("/images/instructions.png"));
    introImage.setFitWidth(this.gameWindow.getWidth() / 1.5);
    introImage.setPreserveRatio(true);
    introBox.getChildren().add(introImage);

    GridPane gridPane = new GridPane();
    gridPane.setAlignment(Pos.CENTER);
    introBox.getChildren().add(gridPane);
    gridPane.setVgap(10);
    gridPane.setHgap(10);

    int x = 0;
    int y = 0;
    for (int i = 0; i < 15; i++) {
      GamePiece piece = GamePiece.createPiece(i);
      PieceBoard gameBoard =
          new PieceBoard(3, 3, this.gameWindow.getWidth() / 15, this.gameWindow.getWidth() / 15);
      gameBoard.showPiece(piece);
      gridPane.add(gameBoard, x, y);
      x++;
      if (x == 5) {
        x = 0;
        y++;
      }
    }
  }
}
