import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.formats.TrigDocumentFormat;
import org.semanticweb.owlapi.io.FileDocumentTarget;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;

import java.io.File;

/**
 * @author Ralph Schäfermeier
 */
public class Test {
    public static void main(String[] args) {
        var man = OWLManager.createOWLOntologyManager();
        try {
            var onto = man.loadOntologyFromOntologyDocument(new File("/tmp/test.owl"));
            man.saveOntology(onto, new TrigDocumentFormat(), new FileDocumentTarget(new File("/tmp/test.trig")));
        } catch (OWLOntologyCreationException | OWLOntologyStorageException e) {
            throw new RuntimeException(e);
        }
    }
}
