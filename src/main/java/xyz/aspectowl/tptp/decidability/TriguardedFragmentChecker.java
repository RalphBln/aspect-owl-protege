package xyz.aspectowl.tptp.decidability;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.stream.Collectors;
import net.sf.tweety.logics.commons.syntax.Predicate;
import net.sf.tweety.logics.commons.syntax.interfaces.Atom;
import net.sf.tweety.logics.fol.parser.TPTPParser;
import net.sf.tweety.logics.fol.syntax.*;

/**
 * @author Ralph Schäfermeier
 */
public class TriguardedFragmentChecker implements FolFragmentChecker {

  Predicate spuriousGuardPredicate;

  private final HashMap<FolFormula, Boolean> cache = new HashMap<>();

  public boolean isInFragment(FolFormula formula) {

    System.out.println(formula);

    if (cache.containsKey(formula)) return cache.get(formula);

    // Rule 1: All atoms are in TGF (but equality is forbidden, so we check that here as well).
    if (formula instanceof FolAtom) {
      return cache(formula, !(((FolAtom) formula).getPredicate() instanceof EqualityPredicate));
    }
    
    // Rule 2: TGF is closed under negation.
    if (formula instanceof Negation) {
      return cache(formula, isInFragment(((Negation) formula).getFormula()));
    }

    // Rule 2: TGF is closed under Boolean combinations.
    if (formula instanceof Implication) {
      return cache(
          formula,
          isInFragment((FolFormula) ((Implication) formula).getFormulas().getFirst())
              && isInFragment((FolFormula) ((Implication) formula).getFormulas().getSecond()));
    }
    if (formula instanceof Conjunction || formula instanceof Disjunction) {
      AssociativeFolFormula associativeFormula = (AssociativeFolFormula) formula;
      for (var subFormula : associativeFormula.getFormulas()) {
        if (!isInFragment((FolFormula) subFormula)) return cache(formula, false);
      }
      return cache(formula, true);
    }

    if (formula instanceof ForallQuantifiedFormula || formula instanceof ExistsQuantifiedFormula) {
      var innerFormula = formula.getFormula();

      // Rule 3: If A is atom, A is in TGF, and A has 2 or less free variables, then forall A and
      // exists A are
      // also in TGF.
      if (innerFormula instanceof Atom
          && innerFormula.getUnboundVariables().size() < 3
          && isInFragment((FolFormula) innerFormula)) return cache(formula, true);

      // Structural composition (Kazakov 2006): If A is an atom and Q is an existential
      // quantification of A and the set of variables Q quantifies over

      // Rule 4: If G and A are in TGF and G is an atom and free(G) ⊇ free(A), then forall(G → A)
      // and exists(G and A) are in TGF.
      if (formula instanceof ForallQuantifiedFormula && innerFormula instanceof Implication) {
        Implication implication = (Implication) innerFormula;
        var first = implication.getFormulas().getFirst();
        var second = implication.getFormulas().getSecond();
        if (first instanceof FolAtom
            && isInFragment((FolFormula) first)
            && isInFragment((FolFormula) second)
            && containsFreeVariables((FolFormula) first, (FolFormula) second)) {
          // first argument of the implication is a guard
          return cache(formula, true);
        }
      }
      if (formula instanceof ExistsQuantifiedFormula) {
        if (innerFormula instanceof Atom) {
          // existential quantification with 2 or less free variables (not of the inner formula but
          // of the quantification) can be converted into a new binary atom,
          // e.g. exists z (sum(x,y,z)) can be treated as summable(x,y).
          if (formula.getUnboundVariables().size() - innerFormula.getUnboundVariables().size()
              < 3) {
            return cache(formula, true);
          }
        }
        if (innerFormula instanceof Conjunction) {
          // try to find a guard
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
        }
      }
      // The formula itself is not in TGF.
      // Try adding a spurious dummy guard to the inner formula of the quantification.
      if (!innerFormula.getUnboundVariables().isEmpty()) {
        var unboundVariables =
                new ArrayList<>(innerFormula.getUnboundVariables());
        FolAtom spuriousGuardAtom =
            innerFormula.getUnboundVariables().size() == 1
                ? new FolAtom(
                    getSpuriousGuardPredicate((FolFormula) innerFormula),
                    unboundVariables.get(0),
                    unboundVariables.get(0))
                : new FolAtom(
                    getSpuriousGuardPredicate((FolFormula) innerFormula),
                    unboundVariables.get(0),
                    unboundVariables.get(1));
        Implication dummyGuardedImplication = new Implication(spuriousGuardAtom, innerFormula);
        FolFormula newFormula =
            formula instanceof ForallQuantifiedFormula
                ? new ForallQuantifiedFormula(
                    dummyGuardedImplication, formula.getQuantifierVariables())
                : new ExistsQuantifiedFormula(
                    dummyGuardedImplication, formula.getQuantifierVariables());
        return isInFragment(newFormula);
      }
    }

    return cache(formula, false);
  }

  private boolean containsFreeVariables(FolFormula potentialGuard, FolFormula formula) {
    return potentialGuard.getUnboundVariables().containsAll(formula.getUnboundVariables());
  }

  private Predicate getSpuriousGuardPredicate(FolFormula formula) {
    if (spuriousGuardPredicate == null) {
      spuriousGuardPredicate = new Predicate("u", 2);
      while (formula.getSignature().contains(spuriousGuardPredicate)) {
        spuriousGuardPredicate = new Predicate(spuriousGuardPredicate.getName() + "_", 2);
      }
      System.out.printf("Spurious guard predicate is %s\n", spuriousGuardPredicate);
    }
    return spuriousGuardPredicate;
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
                  //                  + "fof(formula1,axiom,(![X,Y] : (a(X,Y) => b(X)))).\n"
                  //                  + "fof(formula2,axiom,(![X,Y] : ((b(X) & c(Y)) => (X ==
                  // Y)))).\n"
                  + "fof(formula3,axiom,(![X,Y] : ((r1(X,a) & r2(Y,b)) => ?[Z] : (r3(X,Y,Z,c)))))."
              // ∀x∀y.((R1(x, a) ∧ R2(y, b)) → ∃z.R3(x, y, z, c)).
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
        System.out.printf("%n=> %s: %b%n%n", f, checker.isInFragment(f));
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
