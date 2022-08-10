package xyz.aspectowl.reasoner.test;

import xyz.aspectowl.protege.editorkit.AspectOWLEditorKitFactory;
import io.github.cdimascio.dotenv.Dotenv;
import net.sf.tweety.logics.fol.parser.FolParser;
import net.sf.tweety.logics.fol.reasoner.FolReasoner;
import net.sf.tweety.logics.fol.syntax.FolBeliefSet;
import net.sf.tweety.logics.fol.syntax.FolFormula;
import net.sf.tweety.logics.fol.writer.TPTPWriter;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.launch.Framework;
import org.osgi.framework.launch.FrameworkFactory;
import org.protege.editor.core.ProtegeApplication;
import org.protege.editor.core.editorkit.Initializers;
import org.protege.editor.owl.OWLEditorKit;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.aspectowl.tptp.reasoner.SpassTptpFolReasoner;
import xyz.aspectowl.tptp.renderer.AspectAnnotationOWL2TPTPObjectRenderer;

import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.stream.Stream;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author ralph
 */
public class AspectOwlFolReasonerTest {

    private static final Logger log = LoggerFactory.getLogger(AspectOwlFolReasonerTest.class);

    private static final IRI conjectureProp = IRI.create("http://ontology.aspectowl.xyz/reasoner/aspectowl2tptp/test/base#conjecture");
    private static final IRI nonConjectureProp = IRI.create("http://ontology.aspectowl.xyz/reasoner/aspectowl2tptp/test/base#nonconjecture");

    private static FolReasoner prover;

    @BeforeAll
    public static void setup() {

        ServiceLoader<FrameworkFactory> ffs = ServiceLoader.load(FrameworkFactory.class);
        FrameworkFactory ff = ffs.findFirst().get();
        HashMap<String, Object> config = new HashMap<>();
        Framework fwk = ff.newFramework(config);
        try {
            fwk.start();
        } catch (BundleException e) {
            throw new RuntimeException(e);
        }

        BundleContext context = fwk.getBundleContext();

        Dotenv dotenv = Dotenv.load();
        String protegeHome = dotenv.get("PROTEGE_HOME");
        File protegeHomeDir = new File(protegeHome);
        assert(protegeHomeDir.isDirectory());


//        PluginUtilities.getInstance().initialise(context);

        ProtegeApplication app = new ProtegeApplication();
        app.start(context);

        File bundleDir = new File(protegeHomeDir, "bundles");
        File pluginDir = new File(protegeHomeDir, "plugins");
        Stream.concat(Arrays.stream(bundleDir.listFiles()), Arrays.stream(pluginDir.listFiles())).filter(file -> file.isFile() && file.getName().endsWith(".jar")).forEach(bundleFile -> {
            try {
                context.installBundle(bundleFile.toURI().toString());
            } catch (BundleException e) {
                log.warn("Could not install bundle {}. Cause: {}", bundleFile.getAbsolutePath(), e.getMessage());
            }
        });

        OWLEditorKit editorKit = new OWLEditorKit(new AspectOWLEditorKitFactory());
        Initializers.loadEditorKitHooks(editorKit);

        String spassLocation = Optional.of(dotenv.get("SPASS_HOME")).orElseThrow(() -> new IllegalStateException("SPASS_HOME environment variable must point to SPASS installation directory"));
        FolReasoner.setDefaultReasoner(new SpassTptpFolReasoner(spassLocation)); //Set default prover, options are NaiveProver, EProver, Prover9
        prover = FolReasoner.getDefaultReasoner();
    }

    @ParameterizedTest
    @MethodSource("provideOntologySourceLocations")
    public void reasonOnOntologies(File ontologyFile, FolBeliefSet bs, Conjecture conjecture)  {
        System.out.printf("Processing ontology file: %s\n\n", ontologyFile.getName());
        try {
            PrintWriter out = new PrintWriter(System.out);
            TPTPWriter w = new TPTPWriter(out);
            w.printBase(bs);
            w.printQuery(conjecture.getConjecture());
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.printf(((SpassTptpFolReasoner)prover).queryProof(bs, conjecture.getConjecture()));
        if (conjecture instanceof NonConjecture) {
            assertFalse(prover.query(bs, conjecture.getConjecture()));
        } else {
            assertTrue(prover.query(bs, conjecture.getConjecture()));
        }
    }

    private static Stream<Arguments> provideOntologySourceLocations() throws URISyntaxException {

        OWLOntologyManager man = OWLManager.createOWLOntologyManager();
        man.getIRIMappers().add(ontologyIRI -> IRI.create(AspectOwlFolReasonerTest.class.getResource("/reasoner/ontologies/test-base.ofn")));

        URL ontologiesBaseURL = AspectOwlFolReasonerTest.class.getResource("/reasoner/ontologies");
        File ontologiesBaseDir = new File(ontologiesBaseURL.toURI());

        OWLAnnotationProperty conjectureAnnotationProperty = man.getOWLDataFactory().getOWLAnnotationProperty(conjectureProp);
        OWLAnnotationProperty nonConjectureAnnotationProperty = man.getOWLDataFactory().getOWLAnnotationProperty(nonConjectureProp);

        return Arrays.stream(ontologiesBaseDir.listFiles(file -> !file.getName().equals("test-base.ofn") && file.getName().endsWith(".ofn"))).flatMap(file -> {
            try {
                OWLOntology onto = man.loadOntologyFromOntologyDocument(AspectOwlFolReasonerTest.class.getResourceAsStream("/reasoner/ontologies/" + file.getName()));
                AspectAnnotationOWL2TPTPObjectRenderer renderer = new AspectAnnotationOWL2TPTPObjectRenderer(onto, new PrintWriter(new PrintStream(OutputStream.nullOutputStream())));
                onto.accept(renderer);
                return onto.getAnnotations().stream().filter(annotation -> annotation.getProperty().equals(conjectureAnnotationProperty) || annotation.getProperty().equals(nonConjectureAnnotationProperty)).map(annotation ->
                        Arguments.of(file, renderer.getBeliefSet(), getConjecture(parseFormula(annotation.getValue().asLiteral().get().getLiteral(), renderer.getFolParser()).orElseThrow(RuntimeException::new), annotation.getProperty().equals(nonConjectureAnnotationProperty)))
                );
            } catch (OWLOntologyCreationException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private static Optional<FolFormula> parseFormula(String formula, FolParser parser) {
        FolFormula result = null;
        try {
            result = parser.parseFormula(formula);
        } catch (IOException e) {
            return Optional.empty();
        }
        return Optional.of(result);
    }

    private static Conjecture getConjecture(FolFormula conjecture, boolean negative) {
        return (negative ? new NonConjecture(conjecture) : new Conjecture(conjecture));
    }

    private static class Conjecture {
        private FolFormula conjecture;

        Conjecture(FolFormula conjecture) {this.conjecture = conjecture;}

        public FolFormula getConjecture() {return conjecture;}
    }

    private static class NonConjecture extends Conjecture {
        NonConjecture(FolFormula conjecture) {super(conjecture);}
    }
}
