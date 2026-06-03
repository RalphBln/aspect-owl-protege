package xyz.aspectowl.tptp.decidability;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import org.tweetyproject.logics.commons.syntax.interfaces.Atom;
import org.tweetyproject.logics.fol.syntax.*;

/**
 * @author Ralph Schäfermeier
 */
public class TriguardedFragmentChecker implements FolFragmentChecker {

  private final LoadingCache<FolFormula, Boolean> cache =
      CacheBuilder.newBuilder()
          .build(
              new CacheLoader<>() {
                @Nonnull
                @Override
                public Boolean load(@Nonnull FolFormula key) throws Exception {
                  return isInFragment(key);
                }
              });

  public boolean isInFragment(FolFormula formula) {
    
    try {
      // Rule 1: All atoms are in TGF (but equality is forbidden, so we check that here as well).
      if (formula instanceof FolAtom) {
        return !(((FolAtom) formula).getPredicate() instanceof EqualityPredicate);
      }
      
      // Rule 2: TGF is closed under negation.
      if (formula instanceof Negation) {
        return cache.get(((Negation) formula).getFormula());
      }
      
      // Rule 2: TGF is closed under Boolean combinations.
      if (formula instanceof Implication) {
        return cache.get((FolFormula) ((Implication) formula).getFormulas().getFirst())
            && cache.get((FolFormula) ((Implication) formula).getFormulas().getSecond());
      }
      if (formula instanceof Conjunction || formula instanceof Disjunction) {
        AssociativeFolFormula associativeFormula = (AssociativeFolFormula) formula;
        for (var subFormula : associativeFormula.getFormulas()) {
          if (!cache.get((FolFormula) subFormula)) {
            return false;
          }
        }
        return true;
      }
      
      if (formula instanceof ForallQuantifiedFormula || formula instanceof ExistsQuantifiedFormula) {
        var innerFormula = formula.getFormula();
  
        // Rule 3: If A is in TGF, and A has 2 or less free variables, then forall A and
        // exists A are also in TGF.
        if (innerFormula.getUnboundVariables().size() < 3 && cache.get((FolFormula) innerFormula)) {
          return true;
        }
  
        // Rule 4: If G and A are in TGF and G is an atom and free(G) ⊇ free(A), then forall(G → A)
        // and exists(G and A) are in TGF.
        if (formula instanceof ForallQuantifiedFormula && innerFormula instanceof Implication) {
          Implication implication = (Implication) innerFormula;
          var first = implication.getFormulas().getFirst();
          var second = implication.getFormulas().getSecond();
          // first argument of the implication is a guard
          return first instanceof FolAtom
              && cache.get((FolFormula) first)
              && cache.get((FolFormula) second)
              && containsFreeVariables((FolFormula) first, (FolFormula) second);
        }
        if (formula instanceof ExistsQuantifiedFormula) {
          if (innerFormula instanceof Atom) {
            // Special case where the atom can guard itself (i.e., the atom can be used as a guard for
            // ⏉, which only
            // works for existential quantification, as A ∧ ⏉ ≡	A, whereas A → ⏉ ≢ A).
            // We need to check if innerFormula is in TGF because the atom could be an equality.
            return cache.get((FolAtom) innerFormula);
          }
  
          if (innerFormula instanceof Conjunction) {
            // try to find a guard
            Conjunction conjunction = (Conjunction) innerFormula;
            // conjunctions can have size 0 and 1, we must treat these special cases
            switch (conjunction.size()) {
              case 0:
                return true;
              case 1:
                return cache.get((FolFormula) conjunction.getFormulas().get(0).getFormula());
              default:
                for (var potentialGuard : conjunction.getFormulas()) {
                  if (potentialGuard instanceof Atom && cache.get((FolFormula) potentialGuard)) {
                    FolFormula allButPotentialGuard;
                    // If the conjunction has only size 2, we can directly check the other formula.
                    // If size > 2, we need to construct a new conjunction of the rest.
                    if (conjunction.size() == 2) {
                      allButPotentialGuard =
                          (FolFormula)
                              conjunction.getFormulas().stream()
                                  .filter(conjunct -> !(conjunct.equals(potentialGuard)))
                                  .findFirst()
                                  .get();
                    } else {
                      allButPotentialGuard =
                          new Conjunction(
                              conjunction.getFormulas().stream()
                                  .filter(conjunct -> !(conjunct.equals(potentialGuard)))
                                  .collect(Collectors.toList()));
                    }
                    if (cache.get(allButPotentialGuard)
                        && containsFreeVariables((FolFormula) potentialGuard, allButPotentialGuard)) {
                      return true;
                    }
                  }
                }
            }
          }
        }
      }
      return false;
    } catch (ExecutionException e) {
      throw new RuntimeException(e);
    }
  }

  private boolean containsFreeVariables(FolFormula potentialGuard, FolFormula formula) {
    return potentialGuard.getUnboundVariables().containsAll(formula.getUnboundVariables());
  }
}
