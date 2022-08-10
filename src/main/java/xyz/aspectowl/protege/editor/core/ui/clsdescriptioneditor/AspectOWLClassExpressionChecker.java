package xyz.aspectowl.protege.editor.core.ui.clsdescriptioneditor;

import org.protege.editor.owl.model.OWLModelManager;
import org.protege.editor.owl.model.classexpression.OWLExpressionParserException;
import org.protege.editor.owl.model.parser.ParserUtil;
import org.protege.editor.owl.model.parser.ProtegeOWLEntityChecker;
import org.protege.editor.owl.ui.clsdescriptioneditor.OWLExpressionChecker;
import org.semanticweb.owlapi.manchestersyntax.parser.ManchesterOWLSyntaxParserImpl;
import org.semanticweb.owlapi.manchestersyntax.renderer.ParserException;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLOntologyLoaderConfiguration;
import org.semanticweb.owlapi.util.mansyntax.ManchesterOWLSyntaxParser;

public class AspectOWLClassExpressionChecker implements OWLExpressionChecker<OWLClassExpression> {

    private OWLModelManager mngr;


    public AspectOWLClassExpressionChecker(OWLModelManager mngr) {
        this.mngr = mngr;
    }

    @Override
    public void check(String text) throws OWLExpressionParserException {
        createObject(text);
    }

    @Override
    public OWLClassExpression createObject(String text) throws OWLExpressionParserException {
        if(text.isEmpty()) {
            return null;
        }
        ManchesterOWLSyntaxParser parser = new ManchesterOWLSyntaxParserImpl(OWLOntologyLoaderConfiguration::new, mngr.getOWLDataFactory());
        parser.setOWLEntityChecker(new ProtegeOWLEntityChecker(mngr.getOWLEntityFinder()));
        try {
            return parser.parseClassExpression(text);
        }
        catch (ParserException e) {
            throw ParserUtil.convertException(e);
        }
    }
}
