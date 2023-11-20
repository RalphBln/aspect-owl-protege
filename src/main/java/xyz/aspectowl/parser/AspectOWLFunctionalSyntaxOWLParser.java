/**
 * 
 */
package xyz.aspectowl.parser;

import xyz.aspectowl.owlapi.model.OWLAspectManager;
import org.semanticweb.owlapi.io.AbstractOWLParser;
import org.semanticweb.owlapi.io.OWLOntologyDocumentSource;
import org.semanticweb.owlapi.io.OWLParserException;
import org.semanticweb.owlapi.model.OWLDocumentFormat;
import org.semanticweb.owlapi.model.OWLDocumentFormatFactory;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyLoaderConfiguration;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

/**
 * @author ralph
 */
public class AspectOWLFunctionalSyntaxOWLParser extends AbstractOWLParser {

	private static final long serialVersionUID = -3478225770820865746L;

	private OWLAspectManager am;

	public AspectOWLFunctionalSyntaxOWLParser(OWLAspectManager am) {
		this.am = am;
	}

	@Nonnull
	@Override
	public String getName() {
		return "AspectOWLFunctionalSyntaxOWLParser";
	}

	@Override
	public OWLDocumentFormatFactory getSupportedFormat() {
		return new AspectOWLFunctionalSyntaxDocumentFormatFactory();
	}

	@Nonnull
	@Override
	public OWLDocumentFormat parse(
			@Nonnull OWLOntologyDocumentSource documentSource,
			@Nonnull OWLOntology ontology,
			OWLOntologyLoaderConfiguration configuration) throws IOException {
		Reader reader = null;
		InputStream is = null;
		try {
			AspectOWLFunctionalSyntaxParser parser;
			if (documentSource.isReaderAvailable()) {
				reader = documentSource.getReader();
				parser = new AspectOWLFunctionalSyntaxParser(reader);
			} else if (documentSource.isInputStreamAvailable()) {
				is = documentSource.getInputStream();
				parser = new AspectOWLFunctionalSyntaxParser(new InputStreamReader(is, "UTF-8"));
			} else {
				is = getInputStream(documentSource.getDocumentIRI(),
						configuration, documentSource.getAcceptHeaders().orElse("*/*"));
				parser = new AspectOWLFunctionalSyntaxParser(new InputStreamReader(is, "UTF-8"));
			}
			parser.setUp(ontology, configuration, am);
			return parser.parse();
		} catch (ParseException e) {
			throw new OWLParserException(e.getMessage(), e, 0, 0);
		} catch (TokenMgrError e) {
			throw new OWLParserException(e);
		} catch (Throwable t) {
			throw new OWLParserException(new RuntimeException(t));
		} finally {
			if (is != null) {
				is.close();
			}
			if (reader != null) {
				reader.close();
			}
		}
	}
}
