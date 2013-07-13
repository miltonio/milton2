package io.milton.context;

/**
 *
 */
public class SimpleBeanLocator implements BeanLocator{

    final Object bean;

    public SimpleBeanLocator(Object bean) {
        this.bean = bean;
    }


    public Object locateBean(Context context) {
        return bean;
    }

}
