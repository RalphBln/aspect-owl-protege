package xyz.aspectowl.fol.decidability;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import java.io.IOException;
import java.util.Arrays;
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
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.tweetyproject.logics.fol.parser.TPTPParser;
import org.tweetyproject.logics.fol.syntax.FolFormula;
import xyz.aspectowl.tptp.decidability.TriguardedFragmentChecker;

/**
 * @author Ralph Schäfermeier
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Execution(ExecutionMode.SAME_THREAD)
@ParameterizedClass
@MethodSource("fileNames")
public class TriguardedFragmentCheckerTest {

  private final TriguardedFragmentChecker tgfChecker = new TriguardedFragmentChecker();
  private final ObjectMapper objectMapper = new ObjectMapper(new YAMLFactory());
  
  @Parameter String filename;
  
  private static Stream<Arguments> fileNames() throws IOException {
    PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
    Resource[] resources = resolver.getResources("classpath*:/fol/*.yaml");
    return Arrays.stream(resources).map(Resource::getFilename).map(Arguments::of);
  }
  
  private static class TestData {
    String title;
    String comment;
    boolean inFragment;
    List<TestDataItem> data;
    
    public void setTitle(String title) {
      this.title = title;
    }
    
    public void setComment(String comment) {
      this.comment = comment;
    }
    
    public void setInFragment(boolean inFragment) {
      this.inFragment = inFragment;
    }
    
    public void setData(List<TestDataItem> data) {
      this.data = data;
    }
    
    @Override
    public String toString() {
      return title;
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
    
    @Override
    public String toString() {
      return label + ": " + formula;
    }
  }

  @ParameterizedTest
  @MethodSource("formulasAndLabels")
  void formula(FolFormula formula, String label, boolean inFragment) throws IOException {
    Assertions.assertTrue(inFragment ? tgfChecker.isInFragment(formula) : !tgfChecker.isInFragment(formula), label);
  }

  private Stream<Arguments> formulasAndLabels() throws IOException {
    String filePath = "/fol/" + filename;
    TestData t =
        objectMapper.readValue(
            TriguardedFragmentCheckerTest.class.getResourceAsStream(filePath), TestData.class);
    return t.data.stream().map(item -> Arguments.of(parseFormula(item.formula), item.label, t.inFragment));
  }

  private FolFormula parseFormula(String formula) {
    try {
      return new TPTPParser().parseBeliefBase("fof(dummyname,axiom,( " + formula + " )).").stream()
          .findAny()
          .get();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
