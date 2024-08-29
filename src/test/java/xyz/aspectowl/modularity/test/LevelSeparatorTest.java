package xyz.aspectowl.modularity.test;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.util.OWLOntologyMerger;
import uk.ac.manchester.cs.owlapi.modularity.ModuleType;
import uk.ac.manchester.cs.owlapi.modularity.SyntacticLocalityModuleExtractor;
import xyz.aspectowl.reasoner.util.AspectOWLConsistentModuleSeparator;

import java.util.Collections;

/**
 * @author ralph
 */
class LevelSeparatorTest {

    private OWLOntologyManager man;

    private OWLOntology objectLevelOnto;
    private OWLOntology contextLevelOnto;
    private OWLOntology mergedOnto;


    @BeforeEach
    public void setup() throws OWLOntologyCreationException {
        man = OWLManager.createOWLOntologyManager();
        objectLevelOnto = man.loadOntologyFromOntologyDocument(LevelSeparatorTest.class.getResourceAsStream("/modularity/object.owl"));
        contextLevelOnto = man.loadOntologyFromOntologyDocument(LevelSeparatorTest.class.getResourceAsStream("/modularity/temporal.owl"));
        OWLOntologyMerger merger = new OWLOntologyMerger(man);
        mergedOnto = merger.createMergedOntology(man, IRI.create("http://aspectowl.xyz/test/modularity/merged"));
    }

    @Test
    public void separate() throws OWLOntologyCreationException {
        SyntacticLocalityModuleExtractor extractor = new SyntacticLocalityModuleExtractor(man, mergedOnto, ModuleType.STAR);
        OWLOntology module1 = extractor.extractAsOntology(Collections.singleton(man.getOWLDataFactory().getOWLClass(IRI.create("http://aspectowl.xyz/test/modularity/object#Country"))), IRI.create("http://aspectowl.xyz/test/modularity/module/country"));
        System.out.println("\n\n *** Object Ontology *** ");
        module1.getAxioms().forEach(axiom -> System.out.println(axiom));

        OWLOntology module2 = extractor.extractAsOntology(Collections.singleton(man.getOWLDataFactory().getOWLClass(IRI.create("http://aspectowl.xyz/test/modularity/temporal#Interval1"))), IRI.create("http://aspectowl.xyz/test/modularity/module/timeinstance"));
        System.out.println("\n\n *** Context Ontology *** ");
        module2.getAxioms().forEach(axiom -> System.out.println(axiom));
    }

    @Test
    public void graphPartition() throws OWLOntologyCreationException {
        AspectOWLConsistentModuleSeparator sep = new AspectOWLConsistentModuleSeparator();
        sep.getDisconnectedModules(mergedOnto);
    }
}