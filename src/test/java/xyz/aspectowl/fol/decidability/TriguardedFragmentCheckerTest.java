package xyz.aspectowl.fol.decidability;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.params.Parameter;
import org.junit.jupiter.params.ParameterizedClass;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.openrdf.query.algebra.Str;
import org.tweetyproject.logics.fol.parser.TPTPParser;
import org.tweetyproject.logics.fol.syntax.FolFormula;
import xyz.aspectowl.tptp.decidability.TriguardedFragmentChecker;

/**
 * @author Ralph Schäfermeier
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Execution(ExecutionMode.SAME_THREAD)
@ParameterizedClass
@ValueSource(strings = {"triguarded-positive.json", "triguarded-negative.json"})
public class TriguardedFragmentCheckerTest {

  private final TriguardedFragmentChecker tgfChecker = new TriguardedFragmentChecker();

  private final TPTPParser tptpParser = new TPTPParser();

  @Parameter String filename;
  
  private static class TestData {
    String comment;
    boolean inFragment;
    List<TestDataItem> data;
    
    public void setComment(String comment) {
      this.comment = comment;
    }
    
    public void setInFragment(boolean inFragment) {
      this.inFragment = inFragment;
    }
    
    public void setData(List<TestDataItem> data) {
      this.data = data;
    }
  }

  private static class TestDataItem {
    String label;
    String formula;
    
    public void setLabel(String label) {
      this.label = label;
    }
    
    public void setFormula(String formula) {
      this.formula = formula;
    }
  }

  @ParameterizedTest
  @MethodSource("formulasAndLabels")
  void formula(FolFormula formula, String label, boolean inFragment) throws IOException {
    Assertions.assertTrue(inFragment ? tgfChecker.isInFragment(formula) : !tgfChecker.isInFragment(formula), label);
  }

  private Stream<Arguments> formulasAndLabels() throws IOException {
    String filePath = "/fol/" + filename;
    ObjectMapper objectMapper = new ObjectMapper();
    TestData t =
        objectMapper.readValue(
            TriguardedFragmentCheckerTest.class.getResourceAsStream(filePath), TestData.class);
    return t.data.stream().map(item -> Arguments.of(parseFormula(item.formula), item.label, t.inFragment));
  }

  private FolFormula parseFormula(String formula) {
    try {
      return tptpParser.parseBeliefBase("fof(dummyname,axiom,( " + formula + " )).").stream()
          .findAny()
          .get();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
