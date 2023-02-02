package uk.ac.soton.comp1206.event;

import uk.ac.soton.comp1206.game.GamePiece;

public interface NextPieceListener {

  /**
   * Handle Next piece
   *
   * @param next piece in the game
   */
  public void nextPiece(GamePiece piece);
}
