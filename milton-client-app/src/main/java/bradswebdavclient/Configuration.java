/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package bradswebdavclient;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Map;
import java.util.Properties;

public class Configuration {
    private final File configFile;
    private final Properties props;
    
    public final ToolbarElements toolbars = new ToolbarElements();
    public final Property[] allProperties = new Property[]{};
    
    

    public Configuration() {
        String s = System.getProperty("user.home");
        s = s + "/.webdav.properties";
        configFile = new File(s);
        props = new Properties();
        try {
            if (configFile.exists()) {
                FileInputStream in = new FileInputStream(configFile);
                props.load(in);
            } else {
                InputStream in = this.getClass().getResourceAsStream("default.properties");
                props.load(in);
                save();
            }
        } catch (FileNotFoundException ex) {
            throw new RuntimeException(ex);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    public void save() {
        try {
            FileOutputStream out = new FileOutputStream(configFile);
            props.store(out, null);
        } catch (FileNotFoundException ex) {
            throw new RuntimeException(ex);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    public abstract class ConfigElement<T> {

        public final String name;

        public abstract T get();

        ConfigElement(String name) {
            this.name = name;
        }

        public String name() {
            return name;
        }

        @Override
        public final String toString() {
            Object o = get();
            if (o == null) {
                return "";
            }
            return o.toString();
        }
    }

    public class ToolbarElements extends ConfigElement<Map<String, Runnable>> {

        ToolbarElements() {
            super("toolbars");
        }

        public Map<String, Runnable> get() {
            try {
                String s = props.getProperty(name);
                if (s == null || s.length() == 0) {
                    return null;
                }
                byte[] arr = StringEncrypter.decodeBase64(s);
                ByteArrayInputStream bin = new ByteArrayInputStream(arr);
                ObjectInputStream oin = new ObjectInputStream(bin);
                Object o = oin.readObject();
                Map<String, Runnable> map = (Map<String, Runnable>) o;
                return map;
            } catch (ClassNotFoundException ex) {
                throw new RuntimeException(ex);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }

        public void set(Map<String, Runnable> map) {
            try {
                ByteArrayOutputStream bout = new ByteArrayOutputStream();
                ObjectOutputStream oos = new ObjectOutputStream(bout);
                oos.writeObject(map);
                byte[] arr = bout.toByteArray();
                String s = StringEncrypter.encodeBase64(arr);
                props.setProperty(name, s);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    public abstract class Property extends ConfigElement<String> {

        Property(String name) {
            super(name);
        }
    }

    public class EditableProperty extends Property {

        private final String defValue;
        
        EditableProperty(String name, String defValue) {
            super(name);
            this.defValue = defValue;
        }
        
        EditableProperty(String name) {
            super(name);
            defValue = null;                    
        }

        public String get() {
            String s = props.getProperty(name);
            if( s == null ) s = defValue;
            return s;
        }

        public void set(String value) {
            props.setProperty(name, value);
        }
    }

    public class SecretProperty extends EditableProperty {

        SecretProperty(String name) {
            super(name);
        }

        @Override
        public String get() {
            String s = props.getProperty(name);
            if (s == null || s.length() == 0) {
                return null;
            }
            return decrypt(s);
        }

        @Override
        public void set(String value) {
            if( value != null && value.length() > 0 ) {
              String s = encrypt(value);
              props.setProperty(name, s);
            } else {
              props.setProperty(name, "");
            }
        }

        String encrypt(String x) {
            try {
                return StringEncrypter.getInstance().encrypt(x);
            } catch (StringEncrypter.EncryptionException ex) {
                throw new RuntimeException(ex);
            }
        }

        String decrypt(String s) {
            try {
                return StringEncrypter.getInstance().decrypt(s);
            } catch (StringEncrypter.EncryptionException ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    public class SuperSecretProperty extends Property {

        SuperSecretProperty(String name) {
            super(name);
        }

        public String get() {
            String s = props.getProperty(name);
            if (s == null || s.length() == 0) {
                return null;
            }
            return decrypt(s);
        }

        public void set(String value) {
            String s = encrypt(value);
            props.setProperty(name, s);
        }

        String encrypt(String x) {
            try {
                return StringEncrypter.getInstance().encrypt(x);
            } catch (StringEncrypter.EncryptionException ex) {
                throw new RuntimeException(ex);
            }
        }

        String decrypt(String s) {
            try {
                return StringEncrypter.getInstance().decrypt(s);
            } catch (StringEncrypter.EncryptionException ex) {
                throw new RuntimeException(ex);
            }
        }
    }    
}