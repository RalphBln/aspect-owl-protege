package xyz.aspectowl.tptp.decidability;

import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.tweetyproject.logics.commons.syntax.Constant;
import org.tweetyproject.logics.commons.syntax.Variable;
import org.tweetyproject.logics.fol.syntax.*;

/**
 * Checks syntactic membership in TGF as defined in Definition 1 of
 * <a href="https://arxiv.org/html/2608.02013v1#S3.SS1">The Triguarded Fragment</a>.
 *
 * <p>The signature is relational: terms must be variables or constants. Equality
 * (and its negation) is allowed except between distinct variables. Quantification
 * needs an atomic guard covering the matrix's free variables and the variables
 * being quantified, unless their union has at most two members.
 *
 * <p>Conjunction association, singleton Boolean connectives and adjacent
 * quantifiers of the same kind are treated as notational variants. As usual,
 * an existentially quantified atom can guard itself (its other conjunct is true).
 * No general equivalence-preserving rewriting or satisfiability checking is done.
 *
 * @author Ralph Schäfermeier
 */
public class TriguardedFragmentChecker implements FolFragmentChecker {

  @Override
  public boolean isInFragment(FolFormula formula) {
    Objects.requireNonNull(formula, "formula");
    // Tweety formulae are mutable. Retain neither formulae nor results between calls.
    return check(formula, new IdentityHashMap<>());
  }

  private boolean check(FolFormula formula, Map<FolFormula, Boolean> cache) {
    Boolean cached = cache.get(formula);
    if (cached != null) {
      return cached;
    }
    boolean result = checkFormula(formula, cache);
    cache.put(formula, result);
    return result;
  }

  private boolean checkFormula(FolFormula formula, Map<FolFormula, Boolean> cache) {
    // Definition 1(1), with the function-free signature from Section 2.
    if (formula instanceof FolAtom atom) {
      return isAdmissibleAtom(atom);
    }
    if (formula instanceof Tautology || formula instanceof Contradiction) {
      return true;
    }

    // Definition 1(2): Boolean closure.
    if (formula instanceof Negation negation) {
      return check(negation.getFormula(), cache);
    }
    if (formula instanceof Implication implication) {
      return check((FolFormula) implication.getFormulas().getFirst(), cache)
          && check((FolFormula) implication.getFormulas().getSecond(), cache);
    }
    if (formula instanceof Equivalence equivalence) {
      return check((FolFormula) equivalence.getFormulas().getFirst(), cache)
          && check((FolFormula) equivalence.getFormulas().getSecond(), cache);
    }
    if (formula instanceof Conjunction || formula instanceof Disjunction) {
      for (var operand : ((AssociativeFolFormula) formula).getFormulas()) {
        if (!check((FolFormula) operand, cache)) {
          return false;
        }
      }
      return true;
    }
    if (formula instanceof ForallQuantifiedFormula || formula instanceof ExistsQuantifiedFormula) {
      return checkQuantified(formula, cache);
    }
    return false;
  }

  private boolean isAdmissibleAtom(FolAtom atom) {
    var arguments = atom.getArguments();
    if (arguments.size() != atom.getPredicate().getArity()) {
      return false;
    }
    for (var argument : arguments) {
      if (!(argument instanceof Variable || argument instanceof Constant)) {
        return false;
      }
    }
    if (atom.getPredicate() instanceof EqualityPredicate
        || atom.getPredicate() instanceof InequalityPredicate) {
      // Allowed: c=c', x=c, c=x, x=x, and their negations.
      return arguments.size() == 2
          && (!(arguments.get(0) instanceof Variable)
              || !(arguments.get(1) instanceof Variable)
              || arguments.get(0).equals(arguments.get(1)));
    }
    return true;
  }

  private boolean checkQuantified(FolFormula formula, Map<FolFormula, Boolean> cache) {
    boolean universal = formula instanceof ForallQuantifiedFormula;
    Set<Variable> quantified = new HashSet<>(formula.getQuantifierVariables());
    FolFormula matrix = unwrapSingleton((FolFormula) formula.getFormula());

    if (canQuantify(matrix, quantified, universal, cache)) {
      return true;
    }

    // A guard may belong to a whole block, e.g. forall X forall Y forall Z forall W.
    // First try the original nesting above: individually legal quantifiers need
    // not share a guard, especially when some quantified variables are vacuous.
    while (universal ? matrix instanceof ForallQuantifiedFormula
        : matrix instanceof ExistsQuantifiedFormula) {
      quantified.addAll(matrix.getQuantifierVariables());
      matrix = unwrapSingleton((FolFormula) matrix.getFormula());
    }
    return canQuantify(matrix, quantified, universal, cache);
  }

  private boolean canQuantify(
      FolFormula matrix,
      Set<Variable> quantified,
      boolean universal,
      Map<FolFormula, Boolean> cache) {
    if (!check(matrix, cache)) {
      return false;
    }
    Set<Variable> required = new HashSet<>(matrix.getUnboundVariables());
    required.addAll(quantified);

    // Definition 1(4): count variables before binding, including vacuous binders.
    if (required.size() <= 2) {
      return true;
    }

    // Definition 1(3): the guard must be a single positive atom.
    if (universal) {
      if (matrix instanceof Implication implication) {
        return isGuard(
            unwrapSingleton((FolFormula) implication.getFormulas().getFirst()), required);
      }
      return false;
    }
    return hasExistentialGuard(matrix, required);
  }

  private boolean hasExistentialGuard(FolFormula formula, Set<Variable> required) {
    formula = unwrapSingleton(formula);
    if (isGuard(formula, required)) {
      return true;
    }
    if (formula instanceof Conjunction conjunction) {
      for (var conjunct : conjunction.getFormulas()) {
        if (hasExistentialGuard((FolFormula) conjunct, required)) {
          return true;
        }
      }
    }
    return false;
  }

  private boolean isGuard(FolFormula formula, Set<Variable> required) {
    return formula instanceof FolAtom atom
        // Tweety represents inequality as a FolAtom, but it is negated equality.
        && !(atom.getPredicate() instanceof InequalityPredicate)
        && isAdmissibleAtom(atom)
        && atom.getUnboundVariables().containsAll(required);
  }

  private FolFormula unwrapSingleton(FolFormula formula) {
    while (formula instanceof Conjunction || formula instanceof Disjunction) {
      var operands = ((AssociativeFolFormula) formula).getFormulas();
      if (operands.size() != 1) {
        break;
      }
      formula = (FolFormula) operands.getFirst();
    }
    return formula;
  }
}
