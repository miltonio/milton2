package io.milton.context;

public interface RemovalCallback<T> {
    public void onRemove( T item );
}
