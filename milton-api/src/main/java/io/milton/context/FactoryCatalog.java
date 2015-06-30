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

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class FactoryCatalog {

    private static final Logger log = LoggerFactory.getLogger(FactoryCatalog.class);

    public Map<Class,Factory> factoriesByClass = new HashMap<Class,Factory>();
    public Map<String,Factory> factoriesById = new HashMap<String,Factory>();
    public List<Factory> factories = new ArrayList<Factory>();
    public Map<String,String> keys = new HashMap<String,String>();
    public File configFile;

    public void setKeys(Map<String,String> mapOfKeys) {
        for( Map.Entry<String,String> entry : mapOfKeys.entrySet()) {
            addKey( entry.getKey(), entry.getValue());
        }
    }

    public Map<String,String> getKeys() {
        return keys;
    }

    public List<Factory> getFactories() {
        return factories;
    }

    public void setFactories(List<Factory> list) {
        for( Factory f : list ) {
            addFactory( f );
        }
    }

    public void setSingletons(List<Object> list) {
        for( Object single : list ) {
            SingletonFactory f = new SingletonFactory();
            f.setBean( single );
            addFactory( f );
        }
    }


    public void addFactory(Factory factory) {
        log.debug("addFactory: " + factory.getClass());
        if( factory.keyClasses() != null ) {
            for( Class c: factory.keyClasses() ) {
                if( !factoriesByClass.containsKey(c) ) {
                    log.debug("    " + c.getCanonicalName());
                    factoriesByClass.put(c, factory);
                }
            }
        }
        if( factory.keyIds() != null ) {
            for( String id: factory.keyIds() ) {
                if( !factoriesById.containsKey(id) ) {
                    factoriesById.put(id, factory);
                }
            }
        }
        factories.add( factory );
    }


    public Factory get(Class c) throws IllegalArgumentException {
        Factory factory = factoriesByClass.get(c);
        if( factory == null ) {
            log.warn("No factory found for: " + c.getCanonicalName());
            for( Class cc : factoriesByClass.keySet() ) {
                log.warn("  key: " + cc.getCanonicalName());
            }
            return null;
        }
        return factory;
    }

    public Factory get(String id) {
        Factory factory = factoriesById.get(id);
        if( factory == null ) return null;
        return factory;
    }

    
    
    public void destroy() {
        log.debug("destroy FactoryCatalog");
        for( Factory f : factories ) {
            log.debug("destroying " + f.getClass().getName() );
            f.destroy();
        }
    }

    public void addKey(String key, String value) {
        keys.put(key,value);
    }

    public File getConfigFile() {
        return configFile;
    }

    public void setConfigFile(File configFile) {
        this.configFile = configFile;
    }
    

}
