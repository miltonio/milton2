package io.milton.context;

/**
 * Represents a missing class
 *
 * @author brad
 */
public class ClassNotInContextException extends RuntimeException{
    private static final long serialVersionUID = 1L;
    private final Class missing;

    public ClassNotInContextException( Class missing ) {
        super("The requested class is not in context: " + missing.getCanonicalName());
        this.missing = missing;
    }

    public Class getMissing() {
        return missing;
    }

}
