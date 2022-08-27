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
 * Invokes SPASS (<a href="http://www.mpi-inf.mpg.de/departments/automation-of-logic/software/spass-workbench/"
 * >http://www.mpi-inf.mpg.de/departments/automation-of-logic/software/spass-workbench/</a>),
 * an automated theorem prover for first-order logic, modal logic and description logics.
 *
 * @author Anna Gessler
 *
 */
public class SpassTptpFolReasoner extends FolReasoner {
    /**
     * String representation of the SPASS path.
     */
    private String binaryLocation;

    /**
     * Shell to run SPASS.
     */
    private Shell bash;

    /**
     * Command line options that will be used by SPASS when executing the query.
     * The default value disables most outputs except for the result and enables
     * TPTP input format instead of SPASS input format.
     * */
    private String cmdOptions = "-PGiven=0 -PProblem=0 -TPTP";

    /**
     * Constructs a new instance pointing to a specific SPASS Prover.
     *
     * @param binaryLocation
     *            of the SPASS executable on the hard drive
     * @param bash
     *            shell to run commands
     */
    public SpassTptpFolReasoner(String binaryLocation, Shell bash) {
        this.binaryLocation = binaryLocation;
        this.bash = bash;
    }

    /**
     * Constructs a new instance pointing to a specific SPASS
     *
     * @param binaryLocation
     *            of the SPASS executable on the hard drive
     */
    public SpassTptpFolReasoner(String binaryLocation) {
        this(binaryLocation, Shell.getNativeShell());
    }

    /**
     * Sets the command line options that will be used by SPASS when executing the query.
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

            String cmd = binaryLocation + " " + cmdOptions + " " + file.getAbsolutePath().replaceAll("\\\\", "/");
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
     * May decrease SPASS's performance, use {@link xyz.aspectowl.tptp.reasoner.SpassTptpFolReasoner#query(FolBeliefSet,FolFormula)}
     * if only a yes/no result is needed.
     * @param kb the knowledge base
     *
     * @param query a formula
     * @return a string containing proof documentation
     */
    public String queryProof(FolBeliefSet kb, FolFormula query) {
        String output = null;
        try {
            File file = File.createTempFile("tmp", ".p");
            UnsortedTPTPWriter writer = new UnsortedTPTPWriter(new PrintWriter(file));
            writer.printBase(kb);
            writer.printQuery(query);
            writer.close();

            //Run query with option to document proofs
            String cmd = binaryLocation + " " + cmdOptions + " -DocProof" + " " + file.getAbsolutePath().replaceAll("\\\\", "/");
            output = bash.run(cmd);
        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
        }
        int i = output.indexOf("Here is a proof with");
        if (i==-1)
            return "No proof found.";
        return output.substring(i);
    }

    /**
     * Evaluates SPASS results.
     *
     * @param output
     *            of a SPASS query
     * @return true if a proof was found, false otherwise
     */
    private boolean evaluateResult(String output) {
        if (Pattern.compile("SPASS beiseite: Proof found").matcher(output).find())
            return true;
        if (Pattern.compile("SPASS beiseite: Completion found").matcher(output).find())
            return false;
        if (Pattern.compile("SPASS beiseite: Ran out of time").matcher(output).find())
            throw new RuntimeException("Failure: SPASS timeout.");
        throw new RuntimeException("Failure: SPASS returned no result which can be interpreted. The message was: " + output);
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
