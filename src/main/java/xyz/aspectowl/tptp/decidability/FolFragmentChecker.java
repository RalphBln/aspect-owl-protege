package xyz.aspectowl.tptp.decidability;

import org.tweetyproject.logics.fol.syntax.*;

/**
 * @author Ralph Schäfermeier
 */
public interface FolFragmentChecker {
  public boolean isInFragment(FolFormula formula);
}
