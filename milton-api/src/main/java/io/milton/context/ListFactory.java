package io.milton.context;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class ListFactory implements Factory<List>{

    final String[] ids;
    final List<BeanLocator> beanLocators;

    public ListFactory(String id, List<BeanLocator> beanLocators) {
        ids = new String[] {id};
        this.beanLocators = beanLocators;
    }

    @Override
    public Class[] keyClasses() {
        return null; // only key by id
    }

    @Override
    public String[] keyIds() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Registration<List> insert(RootContext context, Context requestContext) {
        List list = new ArrayList();
        for( BeanLocator bl : beanLocators ) {
            Object o = bl.locateBean(requestContext);
            list.add(o);
        }
        Registration<List> reg = new Registration<List>(list, null, context);
        return reg;
    }

    @Override
    public void init(RootContext context) {
        
    }

    @Override
    public void destroy() {
        
    }

    @Override
    public void onRemove(List item) {
        
    }

}
