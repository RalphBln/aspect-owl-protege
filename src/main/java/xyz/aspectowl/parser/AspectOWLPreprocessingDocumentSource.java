/**
 * 
 */
package xyz.aspectowl.parser;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.FileDocumentSource;
import org.semanticweb.owlapi.io.OWLOntologyDocumentSource;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLDocumentFormat;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLRuntimeException;

import xyz.aspectowl.util.AspectOWLUtils;

/**
 * @author ralph
 */
@Deprecated
public class AspectOWLPreprocessingDocumentSource implements OWLOntologyDocumentSource {
	
	private final OWLOntologyDocumentSource originalSource;
	private String processedContent;
	
	/**
	 * 
	 */
	public AspectOWLPreprocessingDocumentSource(OWLOntologyDocumentSource originalSource) {
		this.originalSource = originalSource;
	}
	
	private String escapeSpecialRegexChars(String stringWithUnescapedChars)
	{
		Pattern SPECIAL_REGEX_CHARS = Pattern.compile("[\\{\\}\\(\\)\\[\\]\\.\\+\\*\\?\\^\\$\\\\\\|\\/]");
		return SPECIAL_REGEX_CHARS.matcher(stringWithUnescapedChars).replaceAll("\\\\$0");
	}
	
	/*
	 * Lazily called on first invocation of getReader or getInputStream
	 */
	private void init()
	{
		OWLOntologyManager om = OWLManager.createOWLOntologyManager();
		OWLOntology activeOntology = null;
		try {
			activeOntology = om.createOntology();
		}
		catch (OWLOntologyCreationException e) {
			e.printStackTrace();
		}
		
		Set<OWLAnnotationProperty> activeOntologyProperties = AspectOWLUtils.getAllAspectAnnotationProperties(activeOntology);
		
		ArrayList<OWLAnnotationProperty> ontologyProperties = new ArrayList<OWLAnnotationProperty>(activeOntologyProperties);
		
		String aspectIRI = "https://ontology.aspectowl.xyz/aspect_owl";
		
		if (ontologyProperties.size() > 0)
		{
			aspectIRI = ontologyProperties.get(0).toStringID();
		}
		
		String hasAspectIRI = "<" + aspectIRI + "#hasAspect>";
		
		String aspectIriAsRegex = escapeSpecialRegexChars(aspectIRI);
		
		System.out.println(aspectIRI);
		
		final String aspectRegex = "Aspect(\\s*?)\\(";
		final Pattern aspectPattern = Pattern.compile(aspectRegex);
		
		try {
			// Read the whole thing into memory. We need to go through the lines several times anyway.
			String buf = IOUtils.toString(originalSource.getReader());
			
			Pattern aspectIriPattern = Pattern.compile(aspectIriAsRegex);
			Matcher aspectIriMatcher = aspectIriPattern.matcher(buf);
			
			if (! aspectIriMatcher.find())
			{
				processedContent = buf;
				return;
			}
			
			Matcher aspectMatcher = aspectPattern.matcher(buf);
			
			if (aspectMatcher.find())
			{
				String annotationProperty = "Annotation ( " + hasAspectIRI + " ";
				buf = buf.replaceAll(aspectRegex, annotationProperty);
			}
			
			processedContent = buf;
		}
		catch (IOException e) {
			throw new OWLRuntimeException(e);
		}
	}
	
	/* (non-Javadoc)
	 * @see org.semanticweb.owlapi.io.OWLOntologyDocumentSource#isReaderAvailable()
	 */
	@Override
	public boolean isReaderAvailable() {
		return true;
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owlapi.io.OWLOntologyDocumentSource#getReader()
	 */
	@Override
	public Reader getReader() {
		synchronized (this) {
			if (processedContent == null)
				init();
		}

		return new StringReader(processedContent);
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owlapi.io.OWLOntologyDocumentSource#isInputStreamAvailable()
	 */
	@Override
	public boolean isInputStreamAvailable() {
		return true;
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owlapi.io.OWLOntologyDocumentSource#getInputStream()
	 */
	@Override
	public InputStream getInputStream() {
		synchronized (this) {
			if (processedContent == null)
				init();
		}

		return new ByteArrayInputStream(processedContent.getBytes(StandardCharsets.UTF_8));
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owlapi.io.OWLOntologyDocumentSource#getDocumentIRI()
	 */
	@Override
	public IRI getDocumentIRI() {
		return originalSource.getDocumentIRI();
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owlapi.io.OWLOntologyDocumentSource#getFormat()
	 */
	@Override
	public OWLDocumentFormat getFormat() {
		return originalSource.getFormat();
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owlapi.io.OWLOntologyDocumentSource#isFormatKnown()
	 */
	@Override
	public boolean isFormatKnown() {
		return originalSource.isFormatKnown();
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owlapi.io.OWLOntologyDocumentSource#getMIMEType()
	 */
	@Override
	public String getMIMEType() {
		return originalSource.getMIMEType();
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owlapi.io.OWLOntologyDocumentSource#isMIMETypeKnown()
	 */
	@Override
	public boolean isMIMETypeKnown() {
		return originalSource.isMIMETypeKnown();
	}
	
	public static void main(String[] args) {
		FileDocumentSource orig = new FileDocumentSource(new File("/Users/ralph/Documents/Diss/Ontologien/AspectOWL/owlapi_parser/minimal_example.aofn"));
		AspectOWLPreprocessingDocumentSource ds = new AspectOWLPreprocessingDocumentSource(orig);
		
		ds.getReader();
	}

}
