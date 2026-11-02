package xyz.aspectowl.fol.decidability;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.tweetyproject.logics.commons.syntax.Constant;
import org.tweetyproject.logics.commons.syntax.Functor;
import org.tweetyproject.logics.commons.syntax.Variable;
import org.tweetyproject.logics.fol.parser.TPTPParser;
import org.tweetyproject.logics.fol.syntax.*;
import xyz.aspectowl.tptp.decidability.TriguardedFragmentChecker;

/** Regression cases for Definition 1 of <a href="https://arxiv.org/html/2608.02013v1">...</a>. */
class TriguardedFragmentCheckerDefinitionTest {

  private final TriguardedFragmentChecker checker = new TriguardedFragmentChecker();

  @ParameterizedTest(name = "{0}")
  @MethodSource("formulae")
  void checksDefinition(String label, String text, boolean expected) throws IOException {
    assertEquals(expected, checker.isInFragment(parse(text)), label);
  }

  private static Stream<Arguments> formulae() {
    return Stream.of(
        Arguments.of("Unquantified atoms may have any arity", "r(X,Y,Z,W)", true),
        Arguments.of("Constants do not count as variables", "![X,Y]: (r(X,Y,a,b,c))", true),
        Arguments.of("Three-variable universal atom needs a guard", "![X,Y,Z]: (r(X,Y,Z))", false),
        Arguments.of("Existential atom guards itself", "?[X,Y,Z,W]: (r(X,Y,Z,W))", true),
        Arguments.of("Constant equality", "a = b", true),
        Arguments.of("Variable equals constant", "![X]: (X = a)", true),
        Arguments.of("Constant equals variable", "![X]: (a = X)", true),
        Arguments.of("Reflexive equality", "![X]: (X = X)", true),
        Arguments.of("Constant inequality", "a != b", true),
        Arguments.of("Variable differs from constant", "![X]: (X != a)", true),
        Arguments.of("Constant differs from variable", "![X]: (a != X)", true),
        Arguments.of("Negated reflexive equality is syntactically allowed", "![X]: (X != X)", true),
        Arguments.of("Equality of distinct variables is forbidden even in FO2", "![X,Y]: (X = Y)", false),
        Arguments.of("Inequality of distinct variables is forbidden", "?[X,Y]: (X != Y)", false),
        Arguments.of("Restricted equality remains forbidden under a guard",
            "![X,Y,Z]: (g(X,Y,Z) => X = Y)", false),
        Arguments.of("Allowed equality under a guard",
            "![X,Y,Z]: (g(X,Y,Z) => (X = a | Y = Y))", true),
        Arguments.of("Positive-arity function in a ground atom", "r(f(a))", false),
        Arguments.of("Nested function in a guarded matrix",
            "![X,Y,Z]: (g(X,Y,Z) => r(f(X),Y,Z))", false),
        Arguments.of("Functions cannot provide guards",
            "?[X,Y,Z]: (g(f(X),Y,Z) & r(X))", false),
        Arguments.of("Boolean closure with many free variables",
            "(p(X,Y,Z) & q(Y,Z,W)) <=> (~r(X,W) | s(Z))", true),
        Arguments.of("Boolean closure still checks every operand",
            "p(X) | X = Y", false),
        Arguments.of("FO2 permits a conjunctive antecedent",
            "![X,Y]: ((p(X) & q(Y)) => r(X,Y))", true),
        Arguments.of("FO2 variable reuse by renaming",
            "?[X]: (p(X) & ?[Y]: (r(X,Y) & ?[Z]: (r(Y,Z))))", true),
        Arguments.of("Paper Equation 1",
            "![X,Y]: ((r1(X,a) & r2(Y,b)) => ?[Z]: (r3(X,Y,Z,c)))", true),
        Arguments.of("Universal guard covers matrix and binders",
            "![X,Y,Z]: (g(X,Y,Z) => (p(X,Y) & q(Y,Z)))", true),
        Arguments.of("Guard may contain additional free variables",
            "![X]: (g(X,Y,Z,W) => p(X,Y,Z))", true),
        Arguments.of("Guard does not cover the consequent",
            "![X,Y,Z]: (g(X,Y) => p(X,Y,Z))", false),
        Arguments.of("Conjunctive guards are not atomic",
            "![X,Y,Z]: ((g(X,Y) & h(Y,Z)) => p(X,Z))", false),
        Arguments.of("Disjunctive guards are not atomic",
            "![X,Y,Z]: ((g(X,Y,Z) | h(X,Y,Z)) => p(X))", false),
        Arguments.of("Negated guards are not atomic",
            "![X,Y,Z]: (~g(X,Y,Z) => p(X))", false),
        Arguments.of("Outer guards do not guard inner quantifiers",
            "![X,Y,Z]: (g(X,Y,Z) => ![W]: (r(X,Y,Z,W)))", false),
        Arguments.of("Nested guarded quantification",
            "![X,Y]: (p(X,Y) => ?[Z]: (g(X,Y,Z) & q(Z)))", true),
        Arguments.of("Split universal block shares a guard",
            "![X]: (![Y]: (![Z]: (![W]: (g(X,Y,Z,W) => p(X)))))", true),
        Arguments.of("Split existential block shares a guard",
            "?[X]: (?[Y]: (?[Z]: (?[W]: (g(X,Y,Z,W) & p(X)))))", true),
        Arguments.of("Mixed quantifiers cannot share a block",
            "![X,Y]: (?[Z]: (![W]: (g(X,Y,Z,W) => p(X))))", false),
        Arguments.of("Existential guard can be any conjunct",
            "?[X,Y,Z]: (p(X) & g(X,Y,Z) & q(Y,Z))", true),
        Arguments.of("Existential guard can occur in a nested conjunction",
            "?[X,Y,Z]: (p(X) & (q(Y) & g(X,Y,Z)))", true),
        Arguments.of("Duplicate guards remain valid",
            "?[X,Y,Z]: (g(X,Y,Z) & g(X,Y,Z))", true),
        Arguments.of("Binary atoms cannot collectively guard three variables",
            "?[X,Y,Z]: (p(X,Y) & q(Y,Z) & r(X,Z))", false),
        Arguments.of("A guard under disjunction is not an existential guard",
            "?[X,Y,Z]: (g(X,Y,Z) | p(X))", false),
        Arguments.of("A negated existential atom cannot guard itself",
            "?[X,Y,Z]: (~g(X,Y,Z))", false),
        Arguments.of("A guard inside a disjunction cannot guard the conjunction",
            "?[X,Y,Z]: (p(X) & (g(X,Y,Z) | q(Y)))", false),
        Arguments.of("Transitivity is not triguarded",
            "![X,Y,Z]: ((r(X,Y) & r(Y,Z)) => r(X,Z))", false),
        Arguments.of("A low-variable outer matrix still checks inner membership",
            "![X]: (?[Y,Z]: (p(X,Y) & q(Y,Z)))", false));
  }

  @Test
  void handlesTruthConstantsAndEmptyConnectives() {
    assertTrue(checker.isInFragment(new Tautology()));
    assertTrue(checker.isInFragment(new Contradiction()));
    assertTrue(checker.isInFragment(new Conjunction()));
    assertTrue(checker.isInFragment(new Disjunction()));
  }

  @Test
  void handlesSingletonAndNestedConjunctions() throws IOException {
    FolFormula guard = parse("g(X,Y,Z)");
    var variables = Set.of(new Variable("X"), new Variable("Y"), new Variable("Z"));
    assertTrue(checker.isInFragment(
        new ExistsQuantifiedFormula(new Conjunction(List.of(guard)), variables)));
    assertTrue(checker.isInFragment(new ExistsQuantifiedFormula(
        new Conjunction(parse("p(X)"), new Conjunction(parse("q(Y)"), guard)), variables)));
    assertTrue(checker.isInFragment(new ExistsQuantifiedFormula(
        new Conjunction(new Disjunction(List.of(guard)), parse("p(X)")), variables)));
    assertFalse(checker.isInFragment(new ExistsQuantifiedFormula(
        new Conjunction(List.of(new Negation(guard))), variables)));
    assertTrue(checker.isInFragment(new ForallQuantifiedFormula(
        new Conjunction(List.of(new Implication(new Conjunction(List.of(guard)), parse("p(X)")))), variables)));
  }

  @Test
  void countsVacuousBindersWhenCheckingBlocksAndGuards() throws IOException {
    var variables = Set.of(new Variable("X"), new Variable("Y"), new Variable("Z"));
    // The TPTP parser drops vacuous binders, so construct these formulae directly.
    assertFalse(checker.isInFragment(new ForallQuantifiedFormula(parse("p(X)"), variables)));
    assertFalse(checker.isInFragment(
        new ForallQuantifiedFormula(parse("g(X,Y) => p(X)"), variables)));
    assertFalse(checker.isInFragment(new ExistsQuantifiedFormula(parse("g(X,Y)"), variables)));
  }

  @Test
  void keepsIndividuallyLegalVacuousQuantifiers() throws IOException {
    FolFormula inner = new ForallQuantifiedFormula(
        parse("p(Y)"), Set.of(new Variable("Y"), new Variable("Z")));
    assertTrue(checker.isInFragment(
        new ForallQuantifiedFormula(inner, new Variable("X"))));
  }

  @Test
  void doesNotReuseResultsAfterFormulaMutation() throws IOException {
    Conjunction matrix = new Conjunction(List.of(parse("g(X,Y,Z)")));
    FolFormula formula = new ExistsQuantifiedFormula(
        matrix, Set.of(new Variable("X"), new Variable("Y"), new Variable("Z")));
    assertTrue(checker.isInFragment(formula));
    matrix.add(parse("p(W)"));
    assertFalse(checker.isInFragment(formula));

    matrix.add(parse("g4(X,Y,Z,W)"));
    assertTrue(checker.isInFragment(formula));
  }

  private static FolFormula parse(String formula) throws IOException {
    TPTPParser parser = new TPTPParser();
    // Signature inference misses constants used only in equality or inside functions.
    parser.getSignature().add(new Constant("a"));
    parser.getSignature().add(new Constant("b"));
    parser.getSignature().add(new Constant("c"));
    parser.getSignature().add(new Functor("f", 1));
    return parser.parseBeliefBase("fof(test,axiom,(" + formula + ")).")
        .iterator().next();
  }
}
