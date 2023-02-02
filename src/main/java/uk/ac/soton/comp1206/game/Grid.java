package uk.ac.soton.comp1206.game;

import java.io.Serializable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;

/**
 * The Grid is a model which holds the state of a game board. It is made up of a set of Integer
 * values arranged in a 2D arrow, with rows and columns.
 *
 * <p>Each value inside the Grid is an IntegerProperty can be bound to enable modification and
 * display of the contents of the grid.
 *
 * <p>The Grid contains functions related to modifying the model, for example, placing a piece
 * inside the grid.
 *
 * <p>The Grid should be linked to a GameBoard for it's display.
 */
public class Grid implements Serializable {

  /** */
  private static final long serialVersionUID = 1L;

  /** The number of columns in this grid */
  private final int cols;

  /** The number of rows in this grid */
  private final int rows;

  /** The grid is a 2D arrow with rows and columns of SimpleIntegerProperties. */
  private final SimpleIntegerProperty[][] grid;

  private static final Logger logger = LogManager.getLogger(Grid.class);

  /**
   * Create a new Grid with the specified number of columns and rows and initialise them
   *
   * @param cols number of columns
   * @param rows number of rows
   */
  public Grid(int cols, int rows) {

    this.cols = cols;
    this.rows = rows;

    // Create the grid itself
    grid = new SimpleIntegerProperty[cols][rows];

    // Add a SimpleIntegerProperty to every block in the grid
    for (var y = 0; y < rows; y++) {
      for (var x = 0; x < cols; x++) {
        grid[x][y] = new SimpleIntegerProperty(0);
      }
    }
  }

  /**
   * Get the Integer property contained inside the grid at a given row and column index. Can be used
   * for binding.
   *
   * @param x column
   * @param y row
   * @return the IntegerProperty at the given x and y in this grid
   */
  public IntegerProperty getGridProperty(int x, int y) {
    return grid[x][y];
  }

  /**
   * Update the value at the given x and y index within the grid
   *
   * @param x column
   * @param y row
   * @param value the new value
   */
  public void set(int x, int y, int value) {
    grid[x][y].set(value);
  }

  /**
   * Get the value represented at the given x and y index within the grid
   *
   * @param x column
   * @param y row
   * @return the value
   */
  public int get(int x, int y) {
    try {
      // Get the value held in the property at the x and y index provided
      return grid[x][y].get();
    } catch (ArrayIndexOutOfBoundsException e) {
      // No such index
      return -1;
    }
  }

  /**
   * Get the number of columns in this game
   *
   * @return number of columns
   */
  public int getCols() {
    return cols;
  }

  /**
   * Get the number of rows in this game
   *
   * @return number of rows
   */
  public int getRows() {
    return rows;
  }

  /**
   * Tell whether a grid can be play or not
   *
   * @param GamePiece piece,
   * @param int x coordinate of placing
   * @param int y coordinate of placing
   * @return true if that piece can be played
   */
  public boolean canPlayPiece(GamePiece piece, int x, int y) {
    logger.info("testing {} piece at: {} {}", piece.toString(), x, y);
    // check each block of the piece
    for (int i = 0; i < piece.getBlocks().length; i++) {
      for (int j = 0; j < piece.getBlocks()[i].length; j++) {
        if (piece.getBlocks()[i][j] != 0) {
          // if the block is already occupied or is out of the bound, return false .
          if (this.get(i + x, j + y) != 0
              || i + x >= this.getCols()
              || j + y >= this.getRows()
              || i + x < 0
              || y + j < 0) {
            return false;
          }
        }
      }
    }
    return true;
  }

  /**
   * Place a piece on the grid
   *
   * @param GamePiece piece
   * @param int x coordinate of placing.
   * @param int y coordinate of placing.
   */
  public void playPiece(GamePiece piece, int x, int y) {

    x = x - 1;
    y = y - 1;
    if (canPlayPiece(piece, x, y)) {
      logger.info("placing {} piece at: {} {}", piece.toString(), x, y);
      for (int i = 0; i < piece.getBlocks().length; i++) {
        for (int j = 0; j < piece.getBlocks()[i].length; j++) {
          if (piece.getBlocks()[i][j] != 0) {
            // set the according block to the value same as the piece
            this.set(i + x, j + y, piece.getBlocks()[i][j]);
          }
        }
      }
    }
  }

  /**
   * Fill the grid with certain piece.
   *
   * @param piece
   */
  public void showPiece(GamePiece piece) {
    for (int i = 0; i < piece.getBlocks().length; i++) {
      for (int j = 0; j < piece.getBlocks()[i].length; j++) {
        this.set(i, j, piece.getBlocks()[i][j]);
      }
    }
  }
}
