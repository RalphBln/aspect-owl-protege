package xyz.aspectowl.tptp.reasoner;

import org.tweetyproject.logics.commons.syntax.Constant;
import org.tweetyproject.logics.commons.syntax.Predicate;
import org.tweetyproject.logics.fol.parser.FolParser;
import org.tweetyproject.logics.fol.syntax.FolBeliefSet;
import org.tweetyproject.logics.fol.syntax.FolFormula;
import org.tweetyproject.logics.fol.syntax.FolSignature;

import java.io.IOException;

/**
 * @author ralph
 */
public class Test {
    public static void main(String[] args) throws IOException {
        FolParser p = new FolParser();
        FolSignature sig = new FolSignature();
        sig.add(new Predicate("A", 1));
        sig.add(new Predicate("r", 2));
        sig.add(new Constant("I1"));
        sig.add(new Constant("I2"));
        p.setSignature(sig);

        FolBeliefSet bs = new FolBeliefSet();
        bs.setSignature(sig);

        FolFormula f = p.parseFormula("forall X: (forall Y: (forall Z: (r(X,Y) && r(Y,Z) => r(X,Z))))");
        System.out.println(f);
        System.out.println(f.getQuantifierVariables());
        System.out.println(f.getUniformProbability());
        System.out.println(f.isDnf());
        System.out.println(f.toNnf());
        System.out.println(f.collapseAssociativeFormulas());
    }
}
