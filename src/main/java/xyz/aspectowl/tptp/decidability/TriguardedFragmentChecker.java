package xyz.aspectowl.tptp.decidability;

import java.util.HashMap;
import java.util.stream.Collectors;
import net.sf.tweety.logics.commons.syntax.interfaces.Atom;
import net.sf.tweety.logics.fol.syntax.*;

/**
 * @author Ralph Schäfermeier
 */
public class TriguardedFragmentChecker implements FolFragmentChecker {

  private final HashMap<FolFormula, Boolean> cache = new HashMap<>();

  public boolean isInFragment(FolFormula formula) {
    cache.clear();
    return isInFragmentInternal(formula);
  }

  public boolean isInFragmentInternal(FolFormula formula) {

    if (cache.containsKey(formula)) return cache.get(formula);

    // Rule 1: All atoms are in TGF (but equality is forbidden, so we check that here as well).
    if (formula instanceof FolAtom) {
      return cache(formula, !(((FolAtom) formula).getPredicate() instanceof EqualityPredicate));
    }

    // Rule 2: TGF is closed under negation.
    if (formula instanceof Negation) {
      return cache(formula, isInFragmentInternal(((Negation) formula).getFormula()));
    }

    // Rule 2: TGF is closed under Boolean combinations.
    if (formula instanceof Implication) {
      return cache(
          formula,
          isInFragmentInternal((FolFormula) ((Implication) formula).getFormulas().getFirst())
              && isInFragmentInternal(
                  (FolFormula) ((Implication) formula).getFormulas().getSecond()));
    }
    if (formula instanceof Conjunction || formula instanceof Disjunction) {
      AssociativeFolFormula associativeFormula = (AssociativeFolFormula) formula;
      for (var subFormula : associativeFormula.getFormulas()) {
        if (!isInFragmentInternal((FolFormula) subFormula)) {
          return cache(formula, false);
        }
      }
      return cache(formula, true);
    }

    if (formula instanceof ForallQuantifiedFormula || formula instanceof ExistsQuantifiedFormula) {
      var innerFormula = formula.getFormula();

      // Rule 3: If A is in TGF, and A has 2 or less free variables, then forall A and
      // exists A are also in TGF.
      if (innerFormula.getUnboundVariables().size() < 3
          && isInFragmentInternal((FolFormula) innerFormula)) {
        return cache(formula, true);
      }

      // Rule 4: If G and A are in TGF and G is an atom and free(G) ⊇ free(A), then forall(G → A)
      // and exists(G and A) are in TGF.
      if (formula instanceof ForallQuantifiedFormula && innerFormula instanceof Implication) {
        Implication implication = (Implication) innerFormula;
        var first = implication.getFormulas().getFirst();
        var second = implication.getFormulas().getSecond();
        if (first instanceof FolAtom
            && isInFragmentInternal((FolFormula) first)
            && isInFragmentInternal((FolFormula) second)
            && containsFreeVariables((FolFormula) first, (FolFormula) second)) {
          // first argument of the implication is a guard
          return cache(formula, true);
        }
        return cache(formula, false);
      }
      if (formula instanceof ExistsQuantifiedFormula) {
        if (innerFormula instanceof Atom) {
          // Special case where the atom can guard itself (i.e., the atom can be used as a guard for
          // ⏉, which only
          // works for existential quantification, as A ∧ ⏉ ≡	A, whereas A → ⏉ ≢ A).
          return cache(formula, true);
        }

        if (innerFormula instanceof Conjunction) {
          // try to find a guard
          Conjunction conjunction = (Conjunction) innerFormula;
          for (var potentialGuard : conjunction.getFormulas()) {
            if (potentialGuard instanceof Atom
                && isInFragmentInternal((FolFormula) potentialGuard)) {
              Conjunction allButPotentialGuard =
                  new Conjunction(
                      conjunction.getFormulas().stream()
                          .filter(conjunct -> !(conjunct.equals(potentialGuard)))
                          .collect(Collectors.toList()));
              if (isInFragmentInternal(allButPotentialGuard)
                  && containsFreeVariables((FolFormula) potentialGuard, allButPotentialGuard)) {
                return cache(formula, true);
              }
            }
          }
        }
      }
    }
    return cache(formula, false);
  }

  private boolean containsFreeVariables(FolFormula potentialGuard, FolFormula formula) {
    return potentialGuard.getUnboundVariables().containsAll(formula.getUnboundVariables());
  }

  private boolean cache(FolFormula formula, boolean value) {
    cache.put(formula, value);
    return value;
  }
}
