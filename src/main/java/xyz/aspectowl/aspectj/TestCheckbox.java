package xyz.aspectowl.aspectj;


/*
 * A swing checkbox example using different constructors
 */

import xyz.aspectowl.protege.editor.core.ui.ComponentWithAspectButton;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;

import javax.swing.*;

public class TestCheckbox {

    public static void main(String[] args) {
        // Create and set up a frame window
        JFrame.setDefaultLookAndFeelDecorated(true);
        JFrame frame = new JFrame("Simple checkbox demo");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Define the panel to hold the checkbox
        JPanel panel = new JPanel();

        // Create checkbox with different constructors
//        JCheckBox checkbox1 = new JCheckBox("Apple", true);

        OWLOntologyManager man = OWLManager.createOWLOntologyManager();
        OWLDataFactory df = man.getOWLDataFactory();

        try {
            OWLOntology ontology = man.createOntology(IRI.create("https://aspectowl.xyz/example#"));
            OWLAxiom axiom = man.getOWLDataFactory().getOWLDeclarationAxiom(df.getOWLClass(IRI.create("https://aspectowl.xyz/example#A")));

            JCheckBox checkbox1 = new ComponentWithAspectButton(axiom, ontology,"A", e -> System.out.println(e.getActionCommand()), null);


            JCheckBox checkbox2 = new JCheckBox("B");
            JCheckBox checkbox3 = new JCheckBox("C", true);
            JCheckBox checkbox4 = new JCheckBox("D");
            JCheckBox checkbox5 = new JCheckBox("E", true);

            // Set up the title for the panel
            panel.setBorder(BorderFactory.createTitledBorder("Checkboxes"));

            // Add the checkbox into the panels
            panel.add(checkbox1);
            panel.add(checkbox2);
            panel.add(checkbox3);
            panel.add(checkbox4);
            panel.add(checkbox5);

            // Add the panel into the frame
            frame.add(panel);

            // Set the window to be visible as the default to be false
            frame.pack();
            frame.setVisible(true);
        } catch (OWLOntologyCreationException ex) {
            ex.printStackTrace();
        }

    }

}
