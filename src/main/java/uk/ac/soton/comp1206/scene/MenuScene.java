package uk.ac.soton.comp1206.scene;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javafx.animation.RotateTransition;
import javafx.event.ActionEvent;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;
import uk.ac.soton.comp1206.App;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;
import uk.ac.soton.comp1206.utility.Multimedia;
/** The main menu of the game. Provides a gateway to the rest of the game. */
public class MenuScene extends BaseScene {

  private Menu menu;

  private static final Logger logger = LogManager.getLogger(MenuScene.class);

  /**
   * Create a new menu scene
   *
   * @param gameWindow the Game Window this will be displayed in
   */
  public MenuScene(GameWindow gameWindow) {
    super(gameWindow);
    logger.info("Creating Menu Scene");
  }

  /** Build the menu layout */
  @Override
  public void build() {
    logger.info("Building " + this.getClass().getName());

    root = new GamePane(gameWindow.getWidth(), gameWindow.getHeight());

    var menuPane = new StackPane();
    menuPane.setMaxWidth(gameWindow.getWidth());
    menuPane.setMaxHeight(gameWindow.getHeight());
    menuPane.getStyleClass().add("menu-background");
    root.getChildren().add(menuPane);

    var mainPane = new BorderPane();
    menuPane.getChildren().add(mainPane);

    ImageView image = new ImageView(Multimedia.getImage("/images/TetrECS.png"));
    image.setFitWidth((double) this.gameWindow.getHeight());
    image.setPreserveRatio(true);
    mainPane.setCenter(image);
    RotateTransition rotater = new RotateTransition(new Duration(5000), image);
    rotater.setCycleCount(-1);
    rotater.setFromAngle(-3);
    rotater.setToAngle(3);
    rotater.setAutoReverse(true);
    rotater.play();

    // press alt to use short cut navigating between menu
    menu = new Menu("_Start");
    MenuItem single = new MenuItem("_Single Player");
    single.setOnAction(e -> gameWindow.startChallenge());
    menu.getItems().add(single);
    MenuItem load = new MenuItem("_Load Single Player");
    load.setOnAction(e -> gameWindow.startChallengLoad());
    menu.getItems().add(load);
    MenuItem multi = new MenuItem("_Multi Player");
    multi.setOnAction(e -> gameWindow.startMulti());
    menu.getItems().add(multi);
    MenuItem how = new MenuItem("_How to Play");
    how.setOnAction(e -> gameWindow.startIntro());
    menu.getItems().add(how);
    MenuItem exit = new MenuItem("_EXIT");
    exit.setOnAction(e -> App.getInstance().shutdown());
    menu.getItems().add(exit);

    MenuBar menuBar = new MenuBar();
    menuBar.getMenus().addAll(menu);
    mainPane.setTop(menuBar);
  }

  /** Initialise the menu */
  @Override
  public void initialise() {
    Multimedia.playMusic("/music/menu.mp3");
    this.scene.setOnKeyPressed(this::handleKey);
  }

  /**
   * Handle when the Start Game button is pressed
   *
   * @param event event
   */
  private void startGame(ActionEvent event) {
    gameWindow.startChallenge();
  }

  /**
   * Event handler for esc pressed.
   *
   * @param keyEvent
   */
  private void handleKey(KeyEvent keyEvent) {
    if (keyEvent.getCode().equals(KeyCode.ESCAPE)) {
      App.getInstance().shutdown();
    }
  }
}
