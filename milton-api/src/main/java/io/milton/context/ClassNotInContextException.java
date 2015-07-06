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

/**
 * Represents a missing class
 *
 * @author brad
 */
public class ClassNotInContextException extends RuntimeException{
    private static final long serialVersionUID = 1L;
    private final Class missing;

    public ClassNotInContextException( Class missing ) {
        super("The requested class is not in context: " + missing.getCanonicalName());
        this.missing = missing;
    }

    public Class getMissing() {
        return missing;
    }

}
