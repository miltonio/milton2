package io.milton.context;

public interface Factory<T> extends RemovalCallback<T> {

    Class[] keyClasses();

    String[] keyIds();

    Registration<T> insert(RootContext context, Context requestContext);

    void init(RootContext context);

    void destroy();
}
