package xyz.aspectowl.rdf;

import org.semanticweb.owlapi.io.*;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLLiteral;

import javax.annotation.Nonnull;
import java.io.Serializable;

/**
 * @author Ralph Schäfermeier
 */
public class RDFQuad extends RDFTriple {
  
  private final static RDFResourceBlankNode DEFAULT_CONTEXT = new RDFResourceBlankNode(false, false, false) {
    @Nonnull
    @Override
    public String toString() {
      return "";
    }
    
    @Override
    public boolean equals(Object obj) {
      return obj == this;
    }
    
    @Override
    public int compareTo(RDFNode o) {
      return o == this ? 0 : 1;
    }
  };
  
  private RDFResource context;
  
  public RDFQuad(@Nonnull RDFResource subject, @Nonnull RDFResourceIRI predicate, @Nonnull RDFNode object) {
    super(subject, predicate, object);
  }
  
  public RDFQuad(@Nonnull IRI subject, boolean subjectAnon, boolean subjectAxiom, @Nonnull IRI predicate, @Nonnull IRI object, boolean objectAnon, boolean objectAxiom) {
    super(subject, subjectAnon, subjectAxiom, predicate, object, objectAnon, objectAxiom);
  }
  
  public RDFQuad(@Nonnull IRI subject, boolean subjectAnon, boolean axiom, @Nonnull IRI predicate, @Nonnull OWLLiteral object) {
    super(subject, subjectAnon, axiom, predicate, object);
  }
  
  public RDFResource getContext() {
    return context;
  }
  
  public void setContext(@Nonnull RDFResource context) {
    this.context = context;
  }
  
  @Override
  public int hashCode() {
    return super.hashCode() + context.hashCode() * 47;
  }
  
  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (!(obj instanceof RDFQuad)) {
      return false;
    }
    RDFQuad other = (RDFQuad) obj;
    return super.equals(other) && context.equals(other.context);
  }
  
  @Override
  public String toString() {
    return String.format("%s %s %s %s.\n", getSubject(), getPredicate(), getObject(), context);
  }
  
  @Override
  public int compareTo(RDFTriple o) {
    // sort by context, then triple sort order
    RDFResource otherContext = o instanceof RDFQuad ? ((RDFQuad) o).getContext() : DEFAULT_CONTEXT;
    int c = context.compareTo(otherContext);
    return c != 0 ? c : compareTo(o);
  }
}
