package io.milton.context;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class BeanFactory implements Factory {

    public static final String SCOPE_APP = "app";
    public static final String SCOPE_REQUEST = "request";

    private Class beanClass;
    private Class[] keyClasses;
    private String scope = SCOPE_APP;

    public Class[] keyClasses() {
        return keyClasses;
    }

    public String[] keyIds() {
        return null;
    }

    public Registration insert(RootContext context, Context requestContext) {
        Object o = instantiateBean();
        if( isRequestScope() ) {
            return requestContext.put(o);
        } else {
            return context.put(o);
        }
    }

    public void init(RootContext context) {
        
    }

    public void destroy() {
        
    }

    public void onRemove(Object item) {
        
    }

    public String getBeanClass() {
        return beanClass.getCanonicalName();
    }

    public void setBeanClass(String beanClass) {
        try {
            this.beanClass = Class.forName(beanClass);
        } catch (ClassNotFoundException ex) {
            throw new RuntimeException(beanClass, ex);
        }
        if( this.keyClasses == null ) this.keyClasses = this.beanClass.getInterfaces();
    }

    public String getKeyClasses() {
        String s = "";
        for( Class c : keyClasses ) {
            s = s + c.getCanonicalName() + ",";
        }
        return s;
    }

    public void setKeyClasses(String keyClasses) {
        String[] arr = keyClasses.split(",");
        List<Class> list = new ArrayList<Class>();
        for( String s : arr) {
            try {
                Class i = Class.forName(s);
                list.add(i);
            } catch (ClassNotFoundException ex) {
                throw new RuntimeException(s, ex);
            }
        }
        this.keyClasses = new Class[list.size()];
        list.toArray(this.keyClasses);
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        if( scope.equals(SCOPE_APP) || scope.equals(SCOPE_REQUEST)) {
            this.scope = scope;
        } else {
            throw new RuntimeException("Scope attribute on BeanFactory must be " + SCOPE_APP + " or " + SCOPE_REQUEST + "  not: " + scope);
        }
    }

    private Object instantiateBean() {
        try {
            return beanClass.newInstance();
        } catch (InstantiationException ex) {
            throw new RuntimeException(ex);
        } catch (IllegalAccessException ex) {
            throw new RuntimeException(ex);
        }
    }

    private boolean isRequestScope() {
        return scope.equals(SCOPE_REQUEST);
    }


}
