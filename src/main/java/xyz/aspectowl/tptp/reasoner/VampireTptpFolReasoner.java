package xyz.aspectowl.tptp.reasoner;

import net.sf.tweety.commons.util.Shell;
import net.sf.tweety.logics.fol.reasoner.FolReasoner;
import net.sf.tweety.logics.fol.syntax.FolBeliefSet;
import net.sf.tweety.logics.fol.syntax.FolFormula;
import xyz.aspectowl.tptp.reasoner.util.UnsortedTPTPWriter;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.regex.Pattern;

/**
 *
 * This class implements a wrapper around [Vampire][link], an automated first-order logic theorem prover.
 *
 * [link]: https://github.com/vprover/vampire
 *
 * @author Ralph Schäfermeier
 *
 */
public class VampireTptpFolReasoner extends FolReasoner {

    /**
     * String representation of the Vampire path.
     */
    private String binaryLocation;

    /**
     * Shell to run VAMPIRE.
     */
    private Shell bash;

    /**
     * Command line options that will be used by VAMPIRE when executing the query.
     * The default value disables most outputs except for the result and enables
     * TPTP input format instead of VAMPIRE input format.
     * */
    private String cmdOptions = "--mode casc";

    /**
     * Constructs a new instance pointing to a specific VAMPIRE Prover.
     *
     * @param binaryLocation
     *            of the VAMPIRE executable on the hard drive
     * @param bash
     *            shell to run commands
     */
    public VampireTptpFolReasoner(String binaryLocation, Shell bash) {
        this.binaryLocation = binaryLocation;
        this.bash = bash;
    }

    /**
     * Constructs a new instance pointing to a specific VAMPIRE
     *
     * @param binaryLocation
     *            of the VAMPIRE executable on the hard drive
     */
    public VampireTptpFolReasoner(String binaryLocation) {
        this(binaryLocation, Shell.getNativeShell());
    }

    /**
     * Sets the command line options that will be used by VAMPIRE when executing the query.
     * @param s a string containing the command line arguments
     */
    public void setCmdOptions(String s){
        this.cmdOptions = s;
    }

    /* (non-Javadoc)
     * @see net.sf.tweety.logics.fol.reasoner.FolReasoner#query(net.sf.tweety.logics.fol.syntax.FolBeliefSet, net.sf.tweety.logics.fol.syntax.FolFormula)
     */
    @Override
    public Boolean query(FolBeliefSet kb, FolFormula query) {
        String output = null;
        try {
            File file = File.createTempFile("tmp", ".txt");
            file.deleteOnExit();
            UnsortedTPTPWriter writer = new UnsortedTPTPWriter(new PrintWriter(file));
            writer.printBase(kb);
            writer.printQuery(query);
            writer.close();

            // todo
            String cmd = binaryLocation + " " + cmdOptions + " -t " + 300 + " " + file.getAbsolutePath().replaceAll("\\\\", "/");
            output = bash.run(cmd);
            if (evaluateResult(output))
                return true;
            else
                return false;
        } catch (InterruptedException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Determines the answer wrt. to the given query and returns the proof (if applicable).
     * May decrease VAMPIRE's performance, use {@link VampireTptpFolReasoner#query(FolBeliefSet,FolFormula)}
     * if only a yes/no result is needed.
     * @param kb the knowledge base
     *
     * @param query a formula
     * @return a string containing proof documentation
     */
    public String queryProof(FolBeliefSet kb, FolFormula query) {
        String output = null;
        String problemName = null;
        try {
            File file = File.createTempFile("tmp", ".txt");
            problemName = file.getName().replaceAll("\\.txt$", "");
            UnsortedTPTPWriter writer = new UnsortedTPTPWriter(new PrintWriter(file));
            writer.printBase(kb);
            writer.printQuery(query);
            writer.close();

            //Run query with option to document proofs
            String cmd = binaryLocation + " " + cmdOptions + " -t " + 300 + " " + file.getAbsolutePath().replaceAll("\\\\", "/");
            output = bash.run(cmd);
        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
        }
        String proofStartText = "% SZS output start Proof for " + problemName;
        String proofEndText = "% SZS output end Proof for " + problemName;
        int i = output.indexOf(proofStartText);
        if (i==-1)
            return "No proof found.";
        return output.substring(i + proofStartText.length(), output.indexOf(proofEndText));
    }

    /**
     * Evaluates VAMPIRE results.
     *
     * @param output
     *            of a VAMPIRE query
     * @return true if a proof was found, false otherwise
     */
    private boolean evaluateResult(String output) {
        // Vampire uses proof by refutation (find a refutation for the negated conjecture/counter hypothesis).
        // Confusingly, it outputs the result of the refutation of the negated conjecture.
        // So "Termination reason: Refutation" means that Vampire was able to refute the counter hypothesis and that
        // the actual conjecture is true.
        // "Termination reason: Satisfiable" means that the counter hypothesis is satisfiable, so the actual conjecture
        // is false.
        // Moreover, Vampire uses multiple strategies. Whenever one strategy fails to find a proof or refutation,
        // Vampire outputs "Termination reason: Refutation not found, incomplete strategy". There might be multiple
        // instances of this text in the output, one for each failed strategy. However, if Vampire finds a proof or
        // refutation, it will eventually output one of the "Termination reason: Refutation/Satisfiable" lines. This
        // will be unique for each run. So we just have to search for these two.
        // Timeouts will also be output for each strategy. If all strategies time out Vampire will output a line
        // "SZS status Timeout for ..." in the end.
        if (Pattern.compile("% SZS status ContradictoryAxioms for").matcher(output).find())
            throw new InconsistentOntologyException();
        if (Pattern.compile("% Termination reason: Refutation").matcher(output).find())
            return true;
        if (Pattern.compile("% Termination reason: Satisfiable").matcher(output).find())
            return false;
        if (Pattern.compile("SZS status Timeout for").matcher(output).find())
            throw new RuntimeException("Failure: Vampire timeout.");
        throw new RuntimeException("Failure: Vampire returned no result which can be interpreted. The message was: " + output);
    }

    @Override
    public boolean equivalent(FolBeliefSet kb, FolFormula a, FolFormula b) {
        String output = null;
        try {
            File file = File.createTempFile("tmp", ".txt");
            UnsortedTPTPWriter writer = new UnsortedTPTPWriter(new PrintWriter(file));
            writer.printBase(kb);
            writer.printEquivalence(a,b);
            writer.close();

            String cmd = binaryLocation + " " + cmdOptions + " " + file.getAbsolutePath().replaceAll("\\\\", "/");
            output = bash.run(cmd);
            if (evaluateResult(output))
                return true;
        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
        }
        return false;
    }

}
