/**
 * 
 */
package xyz.aspectowl.protege.preferences;

import java.awt.BorderLayout;

import javax.swing.JPanel;
import javax.swing.JTextField;

import org.protege.editor.core.prefs.Preferences;
import org.protege.editor.core.prefs.PreferencesManager;
import org.protege.editor.core.ui.preferences.PreferencesPanelLayoutManager;
import org.protege.editor.owl.ui.preferences.OWLPreferencesPanel;

/**
 * @author ralph
 */
public class AspectOWLPreferencesPanel extends OWLPreferencesPanel {

	private static final long serialVersionUID = 8998405290609582694L;
	
	// TODO: make an extra class for that
	
	public static final String PREFERENCES_SET_ID = "xyz.aspectowl";
	public static final String PREFERENCES_ID_DEFAULT = "default";
	
	public static final String PREFERENCES_ID_ASPECT_PROPERTY_IRI = "aspect ns";
	public static final String PREFERENCES_ID_ASPECT_PROPERTY_IRI_DEFAULT_VALUE = "https://ontology.aspectowl.xyz/aspect/owl";
	
	
	
	
	private Preferences prefs;
	
	private JTextField annotationPropertyIRITextField;
	

	/* (non-Javadoc)
	 * @see org.protege.editor.core.plugin.ProtegePluginInstance#initialise()
	 */
	@Override
	public void initialise() throws Exception {
		prefs = PreferencesManager.getInstance().getPreferencesForSet(PREFERENCES_SET_ID, PREFERENCES_ID_DEFAULT);

		JPanel aspectOWLGeneralPrefsPanel = new JPanel();
		PreferencesPanelLayoutManager layout = new PreferencesPanelLayoutManager(aspectOWLGeneralPrefsPanel);
		aspectOWLGeneralPrefsPanel.setLayout(layout);
		
		annotationPropertyIRITextField = new JTextField(prefs.getString(PREFERENCES_ID_ASPECT_PROPERTY_IRI, PREFERENCES_ID_ASPECT_PROPERTY_IRI_DEFAULT_VALUE));
		aspectOWLGeneralPrefsPanel.add("Aspect annotation property IRI", annotationPropertyIRITextField);
		
        setLayout(new BorderLayout());
        add(aspectOWLGeneralPrefsPanel, BorderLayout.CENTER);

	}

	/* (non-Javadoc)
	 * @see org.protege.editor.core.Disposable#dispose()
	 */
	@Override
	public void dispose() throws Exception {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.protege.editor.core.ui.preferences.PreferencesPanel#applyChanges()
	 */
	@Override
	public void applyChanges() {
		prefs.putString(PREFERENCES_ID_ASPECT_PROPERTY_IRI, annotationPropertyIRITextField.getText());
	}


}
