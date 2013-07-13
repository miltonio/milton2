package io.milton.context;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

    public class Registration<T> {
        public final T item;
        public final RemovalCallback callBack;
        public List<Registration> dependents;
        private List<String> keyIds;
        private List<Class> keyClasses;
        private boolean removing;
        private WeakReference<Context> refParent;
        
        public Registration(T o,RemovalCallback callBack, Context parent ) {
            item = o;
            this.callBack = callBack;
            refParent = new WeakReference<Context>(parent);
        }
        
        private Context context() {
            return refParent.get();
        }
        
        public void addDependent(Registration rm) {
            if( dependents == null ) dependents = new Stack<Registration>();
            dependents.add( 0,rm );
        }
        
        public void addKey(String id) {
            if( keyIds == null ) keyIds = new ArrayList<String>();
            keyIds.add(id);
        }
        
        public void addKey(Class c ) {
            if( keyClasses == null ) keyClasses = new ArrayList<Class>();
            keyClasses.add(c);            
        }
        
        public boolean contains(Class c) {
            if( keyClasses == null ) return false;
            return keyClasses.contains(c);
        }
        
        public void remove() {
            if( removing ) return;
            removing = true;

            // Do dependents (ie child objects first)
            if( dependents != null ) {
                for( Registration rm : dependents) {
                    rm.remove();
                }
                dependents = null;
            }
            
            
            if( callBack != null ) {
                callBack.onRemove(item);
            }
            if( keyIds != null ) {
                for( String id : keyIds ) {
                    context().itemByName.remove(id);
                }
                keyIds = null;
            }
            if( keyClasses != null ) {
                for( Class c : keyClasses ) {
                    context().itemByClass.remove(c);
                }
                keyClasses = null;
            }
        }    
    }            
