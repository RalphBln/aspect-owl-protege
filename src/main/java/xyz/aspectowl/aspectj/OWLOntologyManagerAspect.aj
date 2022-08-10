package xyz.aspectowl.aspectj;

import org.protege.editor.core.ui.list.MListButton;
import org.protege.editor.owl.ui.framelist.ExplainButton;

import java.util.ArrayList;
import java.util.List;

public aspectAssertionAxiom AspectOrientedOWLOntologyManagerAspect {

    pointcut testPointcut():
            execution(java.util.List getAdditionalButtons());

    List<MListButton> around(): testPointcut() {
        List<MListButton> additionalButtons = new ArrayList<>(proceed()); // proceed() may return an immutable list, so we need to create a new mutable one
        additionalButtons.add(new ExplainButton(e -> System.out.println("I do nothing.")));

        return additionalButtons;
    }


}
