package io.milton.context;

public interface Executable<T> {
    public T execute( Context context );    
}
