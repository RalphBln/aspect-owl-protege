package xyz.aspectowl.tptp.util;

import java.io.IOException;
import org.tweetyproject.logics.commons.syntax.Variable;
import org.tweetyproject.logics.commons.syntax.interfaces.Atom;
import org.tweetyproject.logics.fol.parser.TPTPParser;
import org.tweetyproject.logics.fol.syntax.*;

// Thought I might need it (for a potential Gödel class membership checker) but probably don't.
// Keeping it around just in case.
/**
 * @author Ralph Schäfermeier
 */
public class ToPrenexNormalFormConverter {
  public FolFormula toPrenexNormalForm(Atom atom) {
    return (FolFormula) atom;
  }

  public FolFormula toPrenexNormalForm(Negation negation) {
    if (negation.getFormula() instanceof ForallQuantifiedFormula) {
      var quantifiedFormula = (ForallQuantifiedFormula) negation.getFormula();
      var matrix = quantifiedFormula.getFormula();
      return toPrenexNormalForm(
          new ExistsQuantifiedFormula(
              new Negation(matrix), quantifiedFormula.getQuantifierVariables()));
    }
    if (negation.getFormula() instanceof ExistsQuantifiedFormula) {
      var quantifiedFormula = (ExistsQuantifiedFormula) negation.getFormula();
      var matrix = quantifiedFormula.getFormula();
      return toPrenexNormalForm(
          new ForallQuantifiedFormula(
              new Negation(matrix), quantifiedFormula.getQuantifierVariables()));
    }

    return new Negation(toPrenexNormalForm(negation.getFormula()));
  }

  public FolFormula toPrenexNormalForm(Conjunction conjunction) {
    throw new UnsupportedOperationException("Not implemented yet.");
  }

  public FolFormula toPrenexNormalForm(Disjunction formula) {
    throw new UnsupportedOperationException("Not implemented yet.");
  }

  public FolFormula toPrenexNormalForm(Implication formula) {
    throw new UnsupportedOperationException("Not implemented yet.");
  }

  public FolFormula toPrenexNormalForm(ForallQuantifiedFormula formula) {
    throw new UnsupportedOperationException("Not implemented yet.");
  }

  public FolFormula toPrenexNormalForm(ExistsQuantifiedFormula formula) {
    throw new UnsupportedOperationException("Not implemented yet.");
  }

  public FolFormula toPrenexNormalForm(FolFormula formula) {
    if (formula instanceof Atom) {
      return toPrenexNormalForm((Atom) formula);
    } else if (formula instanceof Negation) {
      return toPrenexNormalForm((Negation) formula);
    } else if (formula instanceof Conjunction) {
      return toPrenexNormalForm((Conjunction) formula);
    } else if (formula instanceof Disjunction) {
      return toPrenexNormalForm((Disjunction) formula);
    } else if (formula instanceof Implication) {
      return toPrenexNormalForm((Implication) formula);
    } else if (formula instanceof ForallQuantifiedFormula) {
      return toPrenexNormalForm((ForallQuantifiedFormula) formula);
    } else if (formula instanceof ExistsQuantifiedFormula) {
      return toPrenexNormalForm((ExistsQuantifiedFormula) formula);
    } else {
      throw new IllegalArgumentException(
          String.format(
              "Formula of type '%s' is not supported.", formula.getClass().getSimpleName()));
    }
  }

  private void renameVariable(Variable v, FolFormula f) {
    int counter = 0;
    Variable newVariable;
    do {
      newVariable = new Variable(v.get() + counter++);
    } while (f.getQuantifierVariables().contains(newVariable));
    f.substitute(v, newVariable);
  }

  public static void main(String[] args) {
    ToPrenexNormalFormConverter converter = new ToPrenexNormalFormConverter();
    try {
      TPTPParser parser = new TPTPParser();
      FolBeliefSet bs =
          parser.parseBeliefBase(
              "fof(formula3,axiom,(![X,Y] : ((r1(X,a) & r2(Y,b)) => ?[Z] : (r3(X,Y,Z,c))))).");
      for (FolFormula f : bs) {
        System.out.printf("%n=> %s: %b%n%n", f, converter.toPrenexNormalForm(f));
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
