package xyz.aspectowl.tptp.renderer;

/**
 * @author ralph
 */
public class OWL2TPTPRendererError extends Error {
    public OWL2TPTPRendererError(String message) {
        super(message);
    }

    public OWL2TPTPRendererError(String message, Throwable cause) {
        super(message, cause);
    }
}
