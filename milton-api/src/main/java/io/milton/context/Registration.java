/*
 *
 * Copyright 2014 McEvoy Software Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
