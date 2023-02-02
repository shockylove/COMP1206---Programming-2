package uk.ac.soton.comp1206.event;

import java.util.Set;

import uk.ac.soton.comp1206.component.GameBlockCoordinate;

/**
 * Line clear listener for line clear animations.
 *
 * @author stmodst
 */
public interface LineClearedListener {
  public void lineCleared(Set<GameBlockCoordinate> cors);
}
