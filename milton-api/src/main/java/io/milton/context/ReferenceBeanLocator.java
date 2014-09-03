package io.milton.context;

/**
 *
 */
public class ReferenceBeanLocator implements BeanLocator {
    final String id;

    public ReferenceBeanLocator(String id) {
        this.id = id;
    }

    public Object locateBean(Context context) {
        return context.get(id);
    }


}

