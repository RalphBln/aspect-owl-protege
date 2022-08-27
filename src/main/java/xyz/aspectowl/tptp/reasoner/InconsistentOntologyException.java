package xyz.aspectowl.tptp.reasoner;

/**
 * @author ralph
 */
public class InconsistentOntologyException extends RuntimeException {
//    private String proof;
//
//    public InconsistentOntologyException(String proof) {
//        super("Inconsistent ontology");
//        this.proof = proof;
//    }

    public InconsistentOntologyException() {
        super("Inconsistent ontology");
    }

}
