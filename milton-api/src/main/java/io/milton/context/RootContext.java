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

import java.io.Closeable;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RootContext extends Context implements Closeable {

    private static final Logger log = LoggerFactory.getLogger(RootContext.class);
    private final FactoryCatalog factoryCatalog;

    public RootContext() {
        factoryCatalog = new FactoryCatalog();
    }

    public RootContext(FactoryCatalog factoryCatalog) {
        this(factoryCatalog, null);
    }

    public RootContext(FactoryCatalog factoryCatalog, List<Object> initialContents) {
        if (factoryCatalog == null) {
            throw new IllegalArgumentException("factoryCatalog cannot be null");
        }
        this.factoryCatalog = factoryCatalog;
        put("configFile", factoryCatalog.getConfigFile());
        try {
            if (initialContents != null) {
                for (Object o : initialContents) {
                    this.put(o);
                }
            }
            for (Map.Entry<String, String> entry : factoryCatalog.keys.entrySet()) {
                this.put(entry.getKey(), entry.getValue());
            }
            for (Factory f : factoryCatalog.factories) {
                f.init(this);
            }
        } catch (Throwable e) {
            log.error("Exception initializing the rootcontext. Forcing shutdown", e);
            try {
                this.shutdown();
            } catch (Throwable e2) {
                log.error("Failed to shutdown properly", e2);
            }
            throw new RuntimeException(e);
        }
    }

    public void put(Object... beans) {
        for (Object o : beans) {
            put(o);
        }
    }

    /**
     * Execute without any return value
     */
    public void execute(Executable2 exec) {
        RequestContext.setCurrent(null);
        RequestContext context = RequestContext.getInstance(this);
        Registration reg = null;
        try {
            reg = context.put(exec, context); // the context is its own onRemove handler
            exec.execute(context);
        } catch (Throwable e) {
            log.error("error executing: " + exec, e);
            throw new RuntimeException(e);
        } finally {
            reg.remove();
            context.tearDown();
            RequestContext.setCurrent(null);
        }
    }

    public <T> T execute(Executable<T> exec) {
        RequestContext context = RequestContext.getInstance(this);
        Registration reg = null;
        try {
            reg = context.put(exec, context); // the context is its own onRemove handler
            T t = exec.execute(context);
            return t;
        } catch (Throwable e) {
            throw new RuntimeException(e);
        } finally {
            reg.remove();
            if (RequestContext.peekInstance() != null) {
                throw new RuntimeException("Did not close down properly");
            }
            context.tearDown();
        }
    }

    /**
     * Throws an exception if returning null
     */
    @Override
    protected Registration getRegistration(Class c) throws IllegalArgumentException {
        return getOrCreateRegistration(c, this);
    }

    @Override
    protected Registration getRegistration(String id) {
        return getOrCreateRegistration(id, this);
    }

    @Override
    Registration getOrCreateRegistration(Class c, Context context) {
        Registration reg = super.getRegistration(c);
        if (reg != null) {
            return reg;
        }
        Factory f = factoryCatalog.get(c);
        if (f == null) {
            return null;
        }
        reg = f.insert(this, context);
        if (reg == null) {
            throw new NullPointerException("factory " + f.getClass().getName() + " returned null reg");
        }
        return reg;
    }

    @Override
    Registration getOrCreateRegistration(String id, Context context) {
        Registration reg = super.getRegistration(id);
        if (reg != null) {
            return reg;
        }
        Factory f = factoryCatalog.get(id);
        if (f == null) {
            return null;
        }
        //if( f == null ) throw new IllegalArgumentException("No item of ID: " + id );
        return f.insert(this, context);
    }

    public void shutdown() {
        log.warn("shutdown");
        Object[] items = itemByClass.values().toArray();
        for (Object o : items) {
            Registration r = (Registration) o;
            try {
                log.debug("remove: " + r.item.getClass());
                r.remove();
            } catch (Throwable e) {
                log.error("Failed to do remove on registration: " + r);
            }
        }
        for (Factory f : factoryCatalog.factories) {
            log.warn("destroy: " + f.getClass());
            try {
                f.destroy();
            } catch (Throwable e) {
                log.error("Failed to do destroy on factory: " + f);
            }
        }
    }

    /**
     * This weird little guy is to allow us to set this root context into a
     * locator which was defined in spring config before this one.
     *
     * @param loc
     */
    public void setRootContextLocator(RootContextLocator loc) {
        loc.setRootContext(this);
    }

    /**
     * Same as shutdown
     */
    public void close() {
        shutdown();
    }
}
