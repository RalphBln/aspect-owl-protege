package xyz.aspectowl.rdf.renderer;

import org.eclipse.rdf4j.common.net.ParsedIRI;
import org.eclipse.rdf4j.rio.trig.TriGWriter;

import java.io.OutputStream;
import java.io.Writer;

/**
 * @author Ralph Schäfermeier
 */
public class AspectOWLTriGWriter extends TriGWriter {
    public AspectOWLTriGWriter(OutputStream out) {
        super(out);
    }

    public AspectOWLTriGWriter(OutputStream out, ParsedIRI baseIRI) {
        super(out, baseIRI);
    }

    public AspectOWLTriGWriter(Writer writer) {
        super(writer);
    }

    public AspectOWLTriGWriter(Writer writer, ParsedIRI baseIRI) {
        super(writer, baseIRI);
    }
}
