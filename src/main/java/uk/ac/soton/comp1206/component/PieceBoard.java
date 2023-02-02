package uk.ac.soton.comp1206.component;

import uk.ac.soton.comp1206.game.GamePiece;
import uk.ac.soton.comp1206.game.Grid;

public class PieceBoard extends GameBoard {

  public PieceBoard(Grid grid, double width, double height) {
    super(grid, width, height);
  }

  public PieceBoard(int i, int j, double width, double height) {
    super(i, j, width, height);
  }

  /**
   * Show the next piece on the board.
   *
   * @param piece
   */
  public void showPiece(GamePiece piece) {
    for (int i = 0; i < piece.getBlocks().length; i++) {
      for (int j = 0; j < piece.getBlocks()[i].length; j++) {
        if (i == 1 && j == 1) {
          this.blocks[1][1].setCenter(true);
        }
        grid.set(i, j, piece.getBlocks()[i][j]);
      }
    }
  }
}
