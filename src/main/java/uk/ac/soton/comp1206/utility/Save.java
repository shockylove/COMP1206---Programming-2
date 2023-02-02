package uk.ac.soton.comp1206.utility;

import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javafx.util.Pair;

public class Save {

  private static final Logger logger = LogManager.getLogger(Save.class);

  /**
   * Load the score pairs from the saving file. All these methods should be static in ordert to
   * ensure that there is only one saved file in the syste.
   *
   * @return the score pairs that loaded.
   */
  public static ArrayList<Pair<String, Integer>> loadScores() {

    ArrayList<Pair<String, Integer>> result = new ArrayList<Pair<String, Integer>>();

    try {
      Path path = Paths.get("scores.txt");
      // if there is no svae file, create a default one;
      if (Files.notExists(path, new LinkOption[0])) {
        defaultScores();
      }
      List<String> scores = Files.readAllLines(path);
      for (String score : scores) {
        String[] values = score.split(":");
        result.add(new Pair(values[0], Integer.parseInt(values[1])));
      }
    } catch (Exception e) {
      logger.error("Error while reading the file" + e.getMessage());
    }

    return result;
  }

  /**
   * Save the new score list into the save file
   *
   * @param scores new score list.
   */
  public static void writeScores(ArrayList<Pair<String, Integer>> scores) {
    System.out.println("Saving");
    scores.sort(
        (a, b) -> {
          return b.getValue().compareTo(a.getValue());
        });

    try {
      int maxNum = 0;
      String data = "";
      Path path = Paths.get("scores.txt");
      for (Pair<String, Integer> score : scores) {
        String name = score.getKey().toString();
        String points = score.getValue().toString();
        data = data + name + ":" + points + "\n";
        if (maxNum > 10) {
          break;
        }
      }
      Files.writeString(path, data);
    } catch (Exception e) {
      logger.error("Svae error" + e.getMessage());
    }
  }

  /** Create a default score using same format as demo */
  public static void defaultScores() {

    ArrayList<Pair<String, Integer>> list = new ArrayList();
    for (int i = 0; i < 10; i++) {
      list.add(new Pair("Oli", 1000 * (10 - i)));
    }
    writeScores(list);
  }
}
