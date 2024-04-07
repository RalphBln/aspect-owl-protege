package xyz.aspectowl.rdf.renderer;

import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFHandler;
import org.eclipse.rdf4j.rio.Rio;
import org.semanticweb.owlapi.model.OWLDocumentFormat;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semanticweb.owlapi.rio.RioStorer;
import xyz.aspectowl.owlapi.model.OWLAspectManager;
import xyz.aspectowl.rdf.AspectOWLTrigDocumentFormat;
import xyz.aspectowl.rdf.AspectOWLTrigDocumentFormatFactory;
import xyz.aspectowl.renderer.AspectOWLStorer;

import javax.annotation.Nonnull;
import java.io.Writer;
import java.net.URISyntaxException;

/**
 * @author ralph
 */
public class AspectOWLTrigStorer extends RioStorer implements AspectOWLStorer {
  
  private OWLAspectManager aspectManager;
  
  public AspectOWLTrigStorer(OWLAspectManager aspectManager) {
    super(new AspectOWLTrigDocumentFormatFactory());
    this.aspectManager = aspectManager;
  }
  
  @Override
    protected void storeOntology(@Nonnull OWLOntology ontology, @Nonnull Writer writer, @Nonnull OWLDocumentFormat format) throws OWLOntologyStorageException {
      try {
        RDFHandler handler = Rio.createWriter(RDFFormat.TRIG, writer, ontology.getOntologyID().getOntologyIRI().get().toString());
        super.storeOntology(ontology, writer, format);
      } catch (URISyntaxException e) {
        throw new OWLOntologyStorageException(e);
      }
    }

    @Override
    public boolean canStoreOntology(@Nonnull OWLDocumentFormat ontologyFormat) {
        return ontologyFormat instanceof AspectOWLTrigDocumentFormat;
    }
  
  @Override
  public OWLAspectManager getAspectManager() {
    return aspectManager;
  }
}
