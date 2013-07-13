package io.milton.context;

import java.util.ArrayList;

public class RequestContext extends Context implements RemovalCallback {

    private static final ThreadLocal<RequestContext> tlContext = new ThreadLocal<RequestContext>();
    private RootContext parent;

    /**
     * For convenience, equivalent to RequestContext.getCurrent().get(c)
     *
     * This method assumes that the requested class is required and will throw
     * an exception if it is not found
     *
     * @return - an object of the given type
     */
    public static <T> T _(Class<T> c) throws ClassNotInContextException {
        T t = getCurrent().get(c);
        if (t == null) {
            throw new ClassNotInContextException(c);
        }
        return t;
    }

    /**
     * For convenience, equivalent to RequestContext.getCurrent().get(c)
     *
     * Returns the object in context of the given type or null if not found and
     * required is false.
     *
     * @param required - if true will throw an exception if the requested class
     * is not found. Otherwise will return null.
     * @return - an object of the given type
     */
    public static <T> T _(Class<T> c, boolean required) throws ClassNotInContextException {
        T t = getCurrent().get(c);
        if (t == null && required) {
            throw new ClassNotInContextException(c);
        }
        return t;
    }

    public static RequestContext getCurrent() {
        RequestContext c = tlContext.get();
        return c;
    }

    static void setCurrent(RequestContext rc) {
        if (rc == null) {
            tlContext.remove();
        } else {
            tlContext.set(rc);
        }
    }

    public static RequestContext getInstance(RootContext parent) {
        RequestContext c = getCurrent();
        if (c == null) {
            c = new RequestContext(parent);
            setCurrent(c);
        }
        return c;
    }

    public static RequestContext peekInstance() {
        return getCurrent();
    }

    private RequestContext(RootContext parent) {
        if (parent == null) {
            throw new IllegalArgumentException("parent cannot be null");
        }
        this.parent = parent;
    }

    public RootContext getRootContext() {
        return parent;
    }

    @Override
    protected Registration getRegistration(Class c) {
        return getOrCreateRegistration(c, this);
    }

    @Override
    protected Registration getRegistration(String id) {
        return getOrCreateRegistration(id, this);
    }

    @Override
    protected Registration getOrCreateRegistration(Class c, Context context) {
        Registration reg = super.getRegistration(c);
        if (reg != null) {
            return reg;
        }
        return parent.getOrCreateRegistration(c, this);
    }

    @Override
    Registration getOrCreateRegistration(String id, Context context) {
        Registration reg = super.getRegistration(id);
        if (reg != null) {
            return reg;
        }
        return parent.getRegistration(id);
    }

    /**
     * Called when this request context goes out of context. Should shutdown all
     * of its contents
     */
    @Override
    public void onRemove(Object item) {
        tlContext.set(null);
        ArrayList<Registration> items = new ArrayList<Registration>(this.itemByClass.values());
        for (Registration reg : items) {
            reg.remove();
        }
    }
}
