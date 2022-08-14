package xyz.aspectowl.protege;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;

import org.protege.editor.core.ui.action.ProtegeAction;
import org.protege.editor.owl.OWLEditorKit;
import org.semanticweb.owlapi.functional.renderer.OWLFunctionalSyntaxRenderer;
import org.semanticweb.owlapi.io.OWLRendererException;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLOntology;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import xyz.aspectowl.util.AspectOWLUtils;

public class SaveAspectOWLOntologyAction extends ProtegeAction {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4833133243808118467L;

	private static final Logger log = LoggerFactory.getLogger(AspectOWLEditorKitHook.class);
	
	private String fileName  = "ontology.aofn";
	private String directory = "";
	
	
	@Override
	public void initialise() throws Exception {
		
	}

	@Override
	public void dispose() throws Exception {
		
	}

	@Override
	public void actionPerformed(ActionEvent event) {
		
		JFrame parentFrame = new JFrame();
		
		JFileChooser fc = new JFileChooser();
		
		if (! this.directory.equals(""))
		{
			fc.setCurrentDirectory( new File(this.directory) );
		}
		
		fc.setFileFilter( new FileFilter() {
			
			@Override
			public boolean accept(File f) {
				if (f.isDirectory()) {
					return true;
				}
				else {
					String filename = f.getName().toLowerCase();
					return filename.endsWith(".aofn");
				}
			}

			@Override
			public String getDescription() {
				return "Aspect-Oriented Functional Notation (*.aofn)";
			}
			
		});
		
		fc.setSelectedFile( new File(this.fileName) );
		fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
		
		fc.setFileHidingEnabled(true);
		fc.setMultiSelectionEnabled(false);
		fc.setControlButtonsAreShown(true);
		fc.setDragEnabled(true);
		
		int userSelection = fc.showSaveDialog(parentFrame);
		
		if (userSelection == JFileChooser.APPROVE_OPTION)
		{
			
			File fileToSave = fc.getSelectedFile();
			
			this.fileName  = fileToSave.getName();
			this.directory = fileToSave.getPath();
			
			if (! fileToSave.exists())
			{
				saveActiveAspectOntology(fileToSave);
			}
			else {
				// if the file already exists, ask to overwrite this file
				
				int dialogResult = JOptionPane.showConfirmDialog( null,
						"Do you want to overwrite the existing file '" + this.fileName + "'?",
						"File already exists",
						JOptionPane.YES_NO_OPTION,
						JOptionPane.QUESTION_MESSAGE);
				
				if (dialogResult == JOptionPane.YES_OPTION) {
					saveActiveAspectOntology(fileToSave);
				}
				else {
					actionPerformed(event);
				}
			}
			
		}
		
	}
	
	/**
	 * 
	 * @param fileToSave
	 */
	private void saveActiveAspectOntology(File fileToSave)
	{
		Writer stringWriter = new StringWriter();
		
		OWLEditorKit editorKit = (OWLEditorKit) getEditorKit();
		OWLOntology activeOntology = editorKit.getModelManager().getActiveOntology();
		
		OWLFunctionalSyntaxRenderer renderer = new OWLFunctionalSyntaxRenderer();
		try {
			renderer.render(activeOntology, stringWriter);
		}
		catch (OWLRendererException e) {
			e.printStackTrace();
		}
		
		String ontologyInFunctionalSyntax = stringWriter.toString();
		
		Set<OWLAnnotationProperty> activeOntologyProperties = AspectOWLUtils.getAllAspectAnnotationProperties(activeOntology);
		
		//perform regex stuff on the ontology string
		ontologyInFunctionalSyntax =
				changeToAspectOrientedFunctionalSyntax(ontologyInFunctionalSyntax, new ArrayList<OWLAnnotationProperty>(activeOntologyProperties));
		
		log.info("About to save ontology " + activeOntology.getOntologyID());
		
		try {
			FileWriter fw = new FileWriter(fileToSave);
			fw.write(ontologyInFunctionalSyntax);
			fw.close();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}

	private String escapeSpecialRegexChars(String stringWithUnescapedChars)
	{
		Pattern SPECIAL_REGEX_CHARS = Pattern.compile("[\\{\\}\\(\\)\\[\\]\\.\\+\\*\\?\\^\\$\\\\\\|\\/]");
		return SPECIAL_REGEX_CHARS.matcher(stringWithUnescapedChars).replaceAll("\\\\$0");
	}
	
	/**
	 * 
	 * @param buf
	 * @return
	 */
	private String changeToAspectOrientedFunctionalSyntax(String buf, List<OWLAnnotationProperty> properties)
	{
		String aspectIRI = properties.size() == 0
							? "http://www.corporate-semantic-web.de/ontologies/aspect_owl"
							: properties.get(0).toStringID();
		
		String aspectIriAsRegex = escapeSpecialRegexChars(aspectIRI);
		
		Pattern prefixPattern = Pattern.compile("(?m)^Prefix(\\s*?)\\((\\s*)?(.*?:)=\\<" + aspectIriAsRegex + "#\\>(\\s*)?\\)$");
		
		Pattern importPattern = Pattern.compile("(?m)^Import(\\s*?)\\((\\s*?)\\<" + aspectIriAsRegex + "\\>(\\s*?)\\)$");
		
		Matcher importMatcher = importPattern.matcher(buf);
		
		// add the import statement if not already available
		if (! importMatcher.find())
		{
			String ontologyStartRegex = "Ontology(\\s*)?\\((\\s*)?(\\<.*\\>?)(\\s\\<.*?\\>)?";
			String importSyntax = "Import ( <" + aspectIRI + "> )";
			
			Pattern ontologyStartPattern = Pattern.compile(ontologyStartRegex);
			Matcher ontologyStartMatcher = ontologyStartPattern.matcher(buf);
			
			if (ontologyStartMatcher.find())
			{
				buf = buf.replaceAll(ontologyStartRegex, ontologyStartMatcher.group(0) + " " + importSyntax);
			}
		}
		
		Matcher prefixMatcher = prefixPattern.matcher(buf);
		
		if (prefixMatcher.find())
		{
			Matcher aspectPrefixMatcher = prefixPattern.matcher(prefixMatcher.group(0));
			
			if (aspectPrefixMatcher.matches())
			{
				String aspectPrefix = aspectPrefixMatcher.group(3);
				buf = buf.replaceAll(aspectPrefix + "hasAspect", "<" + aspectIRI + "#hasAspect>");
			}
		}
		
		buf = buf.replaceAll("Annotation\\(<" + aspectIRI + "#hasAspect>", "Aspect(");
		
		return buf;
	}
	
}
