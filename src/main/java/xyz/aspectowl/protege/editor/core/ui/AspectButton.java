package xyz.aspectowl.protege.editor.core.ui;

import com.google.common.html.HtmlEscapers;
import org.checkerframework.checker.units.qual.A;
import xyz.aspectowl.owlapi.model.OWLAspectManager;
import org.protege.editor.core.ui.list.MListButton;
import org.protege.editor.owl.ui.renderer.OWLRendererPreferences;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLOntology;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.io.IOException;

public class AspectButton extends MListButton {

    private static final Color ROLL_OVER_COLOR = new Color(0, 0, 0);

    private static final String ASPECT_STRING = "⬇";

    private static BufferedImage img = null;
    static {
        try {
            img = ImageIO.read(AspectButton.class.getResourceAsStream("/aspectButton.png"));
        } catch (IOException e) {
        }
    }

    private OWLAxiom axiom;
    private OWLOntology ontology;

    private OWLAspectManager aspectManager;

    public AspectButton(OWLAxiom axiom, OWLOntology ontology, OWLAspectManager aspectManager) {
        super("Aspects", ROLL_OVER_COLOR, null);
        this.axiom = axiom;
        this.ontology = ontology;
        this.aspectManager = aspectManager;
    }

    @Override
    public Color getBackground() {
        if (aspectManager.hasAssertedAspects(ontology, axiom)) {
            return Color.ORANGE;
        }
        else {
            return super.getBackground();
        }
    }
    @Override
    public void paintButtonContent(Graphics2D g) {

        int w = getBounds().width;
        int h = getBounds().height;
        int x = getBounds().x;
        int y = getBounds().y;

        Font font = g.getFont().deriveFont(Font.BOLD, OWLRendererPreferences.getInstance().getFontSize());
        g.setFont(font);
        FontMetrics fontMetrics = g.getFontMetrics(font);
        final Rectangle stringBounds = fontMetrics.getStringBounds(ASPECT_STRING, g).getBounds();
        int baseline = fontMetrics.getLeading() + fontMetrics.getAscent();

        // thought about having different font colors for asserted and inferred axioms, but
        // Protege's built-in buttons do not have that, so I won't add it
//        g.setColor(ontology == null ? Color.RED : g.getColor());

//        g.drawString(ASPECT_STRING, x + w / 2 - stringBounds.width / 2, y + (h - stringBounds.height) / 2 + baseline );
        g.drawImage(img, null, x, y);

    }

    @Override
    public String getName() {
        if (aspectManager.hasAssertedAspects(ontology, axiom)) {
            StringBuilder buf = new StringBuilder("<html><b>View or edit aspects referencing this axiom.</b><ul>");
            aspectManager.getAssertedAspects(ontology, axiom).forEach(aspect -> buf.append("<li>").append(HtmlEscapers.htmlEscaper().escape(aspect.toString())));
            buf.append("</ul></html>");
            return buf.toString();
        }
        return "This axiom is not target of any aspect. Click to add aspects.";
    }
}
