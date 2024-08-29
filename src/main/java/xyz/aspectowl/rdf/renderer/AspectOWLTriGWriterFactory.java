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
    
    @Override
    public RDFFormat getRDFFormat() {
        return RDFFormat.TRIG;
    }
    
    @Override
    public RDFWriter getWriter(OutputStream out) {
        return new AspectOWLTriGWriter(out);
    }
    
    @Override
    public RDFWriter getWriter(OutputStream out, String baseURI) throws URISyntaxException {
        return new AspectOWLTriGWriter(out, ParsedIRI.create(baseURI));
    }
    
    @Override
    public RDFWriter getWriter(Writer writer) {
        return new AspectOWLTriGWriter(writer);
    }
    
    @Override
    public RDFWriter getWriter(Writer writer, String baseURI) throws URISyntaxException {
        return new AspectOWLTriGWriter(writer, ParsedIRI.create(baseURI));
    }
}
