package xyz.aspectowl.tptp.decidability;

import net.sf.tweety.logics.fol.syntax.*;

/**
 * @author Ralph Schäfermeier
 */
public interface FolFragmentChecker {
  public boolean isInFragment(FolFormula formula);
}
