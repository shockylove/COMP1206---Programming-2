package uk.ac.soton.comp1206.utility;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javafx.scene.image.Image;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

public class Multimedia {

  private static final Logger logger = LogManager.getLogger(Multimedia.class);

  protected static MediaPlayer audio;
  protected static MediaPlayer music;

  /**
   * Play the audio of certain file
   *
   * @param file file name
   */
  public static void playAudio(String file) {
    String toPlay = Multimedia.class.getResource(file).toExternalForm();
    logger.info("Playing audio: " + toPlay);

    Media play = new Media(toPlay);
    audio = new MediaPlayer(play);
    audio.play();
  }

  /**
   * Play the music of certain file;
   *
   * @param file file name
   */
  public static void playMusic(String file) {
    if (music != null) {
      music.stop();
    }
    try {
      String toPlay = Multimedia.class.getResource(file).toExternalForm();
      logger.info("Playing music: " + toPlay);
      Media play = new Media(toPlay);
      music = new MediaPlayer(play);
      music.setVolume(0.50);
      music.setCycleCount(-1);
      music.play();
      logger.info("Playing music!!!: " + toPlay);

    } catch (Exception e) {
      System.out.println(e.getMessage());
    }
  }

  /**
   * Get the file name image.
   *
   * @param file file name
   * @return
   */
  public static Image getImage(String file) {
    Image result = new Image(Multimedia.class.getResource(file).toExternalForm());
    return result;
  }

  public static void stop() {}
}
