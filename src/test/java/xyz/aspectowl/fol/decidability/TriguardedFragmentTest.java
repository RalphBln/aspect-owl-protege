package xyz.aspectowl.fol.decidability;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.stream.Stream;
import net.sf.tweety.logics.fol.parser.TPTPParser;
import net.sf.tweety.logics.fol.syntax.FolFormula;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import xyz.aspectowl.tptp.decidability.TriguardedFragmentChecker;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * @author Ralph Schäfermeier
 */
public class TriguardedFragmentTest {
  
  private final TriguardedFragmentChecker tgfChecker = new TriguardedFragmentChecker();

  @ParameterizedTest
  @MethodSource("dataPositive")
  void inTGF(FolFormula folFormula) {
    assertTrue(tgfChecker.isInFragment(folFormula));
  }

  @ParameterizedTest
  @MethodSource("dataNegative")
  public void notInTGF(FolFormula folFormula) {
    assertFalse(tgfChecker.isInFragment(folFormula));
  }

  public static Stream<Arguments> dataPositive() throws IOException {
    return data("/fol/tri-guarded_positive.tptp");
  }

  public static Stream<Arguments> dataNegative() throws IOException {
    return data("/fol/tri-guarded_negative.tptp");
  }

  public static Stream<Arguments> data(String beliefSetFilePath) throws IOException {
    return new TPTPParser().parseBeliefBase(
            new BufferedReader(
                new InputStreamReader(
                    TriguardedFragmentTest.class.getResourceAsStream(beliefSetFilePath)))).stream().map(Arguments::of);
  }
}
