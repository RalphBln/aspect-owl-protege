package xyz.aspectowl.rdf.renderer;

import org.eclipse.rdf4j.common.net.ParsedIRI;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFWriter;
import org.eclipse.rdf4j.rio.RDFWriterFactory;

import java.io.OutputStream;
import java.io.Writer;
import java.net.URISyntaxException;

/**
 * @author Ralph Schäfermeier
 */
public class AspectOWLTriGWriterFactory implements RDFWriterFactory {

    public AspectOWLTriGWriterFactory() {
    }

    public RDFFormat getRDFFormat() {
        return RDFFormat.TRIG;
    }

    public RDFWriter getWriter(OutputStream out) {
        return new AspectOWLTriGWriter(out);
    }

    public RDFWriter getWriter(OutputStream out, String baseURI) throws URISyntaxException {
        return new AspectOWLTriGWriter(out, new ParsedIRI(baseURI));
    }

    public RDFWriter getWriter(Writer writer) {
        return new AspectOWLTriGWriter(writer);
    }

    public RDFWriter getWriter(Writer writer, String baseURI) throws URISyntaxException {
        return new AspectOWLTriGWriter(writer, new ParsedIRI(baseURI));
    }
}
