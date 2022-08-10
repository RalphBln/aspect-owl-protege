package xyz.aspectowl.util;

import xyz.aspectowl.owlapi.model.*;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class AspectOWLPointcutComparator implements Comparator<AspectOWLAxiomPointcut> {

    private List<Class> pointcutTypes = Stream.of(
            AspectOWLModulePointcut.class,
            DLQueryPointcutAspect.class,
            AspectOWLSPARQLPointcut.class,
            AspectOWLJoinPointAxiomPointcut.class).collect(Collectors.toList());

    @Override
    public int compare(AspectOWLAxiomPointcut o1, AspectOWLAxiomPointcut o2) {
        return 0;
    }
}
