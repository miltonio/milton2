package io.milton.context;

/** This interface represents those classes which contain context
 */
public interface Contextual {
    public <T> T get(String id);
    
    public <T> T get(Class<T> c);
}
