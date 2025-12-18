package xyz.aspectowl.tptp.decidability;

import java.io.IOException;
import java.util.HashMap;
import java.util.stream.Collectors;
import net.sf.tweety.logics.commons.syntax.interfaces.Atom;
import net.sf.tweety.logics.fol.parser.TPTPParser;
import net.sf.tweety.logics.fol.syntax.*;

/**
 * @author Ralph Schäfermeier
 */
public class TriguardedFragmentChecker implements FolFragmentChecker {

  private HashMap<FolFormula, Boolean> cache = new HashMap<>();

  public boolean isInFragment(FolFormula formula) {
    
    if (cache.containsKey(formula)) return cache.get(formula);
    
    // Rule 1: All atoms are in TGF (but equality is forbidden, so we check that here as well).
    if (formula instanceof FolAtom) {
      return cache(formula, !(((FolAtom) formula).getPredicate() instanceof EqualityPredicate));
    }
    ;
    
    // Rule 2: TGF is closed under negation.
    if (formula instanceof Negation) {
      return cache(formula, isInFragment((Negation) formula));
    }
    
    // Rule 2: TGF is closes under Boolean combinations.
    if (formula instanceof Implication) {
      return cache(
              formula,
              isInFragment((FolFormula) ((Implication) formula).getFormulas().getFirst())
                      && isInFragment((FolFormula) ((Implication) formula).getFormulas().getSecond()));
    }
    if (formula instanceof Conjunction || formula instanceof Disjunction) {
      AssociativeFolFormula associativeFormula = (AssociativeFolFormula) formula;
      for (FolFormula subFormula : associativeFormula.getFormulas(FolFormula.class)) {
        if (!isInFragment(subFormula)) return cache(formula, false);
      }
      return cache(formula, true);
    }
    
    if (formula instanceof ForallQuantifiedFormula || formula instanceof ExistsQuantifiedFormula) {
      var innerFormula = formula.getFormula();
      
      // Rule 3: If A is in TGF, and A has 2 or less free variables, then forall A and exists A are
      // also
      // in TGF.
      if (innerFormula.getUnboundVariables().size() < 3 && isInFragment((FolFormula) innerFormula))
        return cache(formula, true);
      
      // Rule 4: If G and A are in TGF and G is an atom and free(G) ⊇ free(A), then forall(G → A)
      // and exists(G and A) are in TGF.
      if (formula instanceof ForallQuantifiedFormula && innerFormula instanceof Implication) {
        Implication implication = (Implication) innerFormula;
        var first = implication.getFormulas().getFirst();
        var second = implication.getFormulas().getSecond();
        return cache(
                formula,
                first instanceof FolAtom
                        && isInFragment((FolFormula) first)
                        && isInFragment((FolFormula) second)
                        && containsFreeVariables((FolFormula) first, (FolFormula) second));
      }
      if (formula instanceof ExistsQuantifiedFormula && innerFormula instanceof Conjunction) {
        Conjunction conjunction = (Conjunction) innerFormula;
        for (var potentialGuard : conjunction.getFormulas()) {
          if (potentialGuard instanceof Atom && isInFragment((FolFormula) potentialGuard)) {
            Conjunction allButPotentialGuard =
                    new Conjunction(
                            conjunction.getFormulas().stream()
                                    .filter(conjunct -> !(conjunct.equals(potentialGuard)))
                                    .collect(Collectors.toList()));
            if (isInFragment(allButPotentialGuard)
                    && containsFreeVariables((FolFormula) potentialGuard, allButPotentialGuard)) {
              return cache(formula, true);
            }
          }
        }
        return cache(formula, false);
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

  public static void main(String[] args) {
    try {
      TPTPParser parser = new TPTPParser();
      FolBeliefSet bs =
          parser.parseBeliefBase(
              "%---some comments \n"
                  + "%----more comments \n"
                  + "fof(formula1,axiom,(![X,Y] : (a(X,Y) => b(X)))).\n"
                  + "fof(formula2,axiom,(![X,Y] : ((b(X) & c(Y)) => (X == Y))))."
              // Include formulas named formula2 and formula3 from another file
              //              +
              // "include('src/main/resources/tptpexample.fologic',[formula2,formula3]).\n"
              //              + "fof(formula1,axiom,(p(functor(b)) & r)).\n"
              //              + "fof(formula2,axiom,(~'PredicateInSingleQuotes'(a,b) & r |
              // ~(r))).\n"
              //              + "fof(formula3,axiom,(~p(a) & r | r)).\n"
              //              + "fof(formula4,axiom,(r <=> ~q(a))).\n"
              //              + "% random comment \n"
              //              + "fof(formula5,axiom,(predicate_of_arity3(a,b,a) | r)).\n"
              //              + "fof(formula6,axiom,((~p(a) => r) & (~p(b) <= r))).\n"
              //              + "fof(formula7,axiom,(? [X] : (q(X)) & r => p(b))).\n"
              //              + "fof(formula8,axiom,(r | ! [Y] : (p(Y)))).\n");
              );
      TriguardedFragmentChecker checker = new TriguardedFragmentChecker();
      for (FolFormula f : bs) {
        System.out.printf("%s: %b%n%n", f, checker.isInFragment(f));
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
