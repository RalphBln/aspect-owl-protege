package xyz.aspectowl.rdf;

import org.semanticweb.owlapi.io.RDFResource;
import org.semanticweb.owlapi.io.RDFResourceIRI;
import org.semanticweb.owlapi.io.RDFTriple;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.rdf.model.RDFGraph;

import javax.annotation.Nonnull;
import java.util.*;

/**
 * @author Ralph Schäfermeier
 */
public class RDFQuadGraph extends RDFGraph {
  
  private Map<RDFResource, Set<RDFQuad>> quadsByContext = new HashMap<>();
  
  @Override
  public void addTriple(@Nonnull RDFTriple triple) {
    if (!(triple instanceof RDFQuad)) {
      throw new IllegalArgumentException(String.format("Expected quad but received triple: %s", triple));
    }
    RDFQuad quad = (RDFQuad) triple;
    
    RDFResource context = quad.getContext();
    
    var quadsInThisContext = quadsByContext.get(context);
    if (quadsInThisContext == null) {
      quadsInThisContext = new HashSet<>();
      quadsByContext.put(context, quadsInThisContext);
    }
    quadsInThisContext.add(quad);
    super.addTriple(quad);
  }
}
