package xyz.aspectowl.protege.editor.core.ui;

import org.protege.editor.owl.ui.renderer.OWLClassIcon;

import java.awt.*;

public class OWLAspectIcon extends OWLClassIcon {

    public static final Color COLOR = new Color(31, 182, 170);

    public static final BasicStroke HOLLOW_STROKE = new BasicStroke(2);

    private Type type;

    public OWLAspectIcon() {
        this(Type.PRIMITIVE, FillType.FILLED);
    }

    public OWLAspectIcon(Type type) {
        this(type, FillType.FILLED);
    }

    public OWLAspectIcon(Type type, FillType fillType) {
        super(type, fillType);
        this.type = type;
    }

    @Override
    public Color getEntityColor() {
        return COLOR;
    }
}
