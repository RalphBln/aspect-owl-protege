package xyz.aspectowl.parser;

import java.util.HashSet;
import java.util.Set;

import xyz.aspectowl.owlapi.model.OWLAspect;

/**
 * @author Ralph Schäfermeier
 */
public class OWLAspectNestingContainer {
  private OWLAspect aspect;
  private HashSet<OWLAspectNestingContainer> nestedAspects = new HashSet<>();
  
  public void setAspect(OWLAspect aspect) {
    this.aspect = aspect;
  }
  
  public OWLAspect getAspect() {
    return aspect;
  }
  
  public void addNestedAspect(OWLAspectNestingContainer nestedAspect) {
    nestedAspects.add(nestedAspect);
  }
  
  public Set<OWLAspectNestingContainer> getNestedAspects() {
    return nestedAspects;
  }
}
