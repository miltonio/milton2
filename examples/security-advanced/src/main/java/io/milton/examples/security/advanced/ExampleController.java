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
package io.milton.examples.security.advanced;

import io.milton.annotations.AccessControlList;
import io.milton.annotations.ChildrenOf;
import io.milton.annotations.PutChild;
import io.milton.annotations.ResourceController;
import io.milton.annotations.Root;
import io.milton.resource.AccessControlledResource;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

@ResourceController
public class ExampleController  {

    private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(ExampleController.class);

    private final List<Day> days = new ArrayList<Day>();

    public ExampleController() {
        Calendar cal = Calendar.getInstance();
        for( int i=0; i<7; i++ ){ 
            cal.set(Calendar.DAY_OF_MONTH, i);
            String dayName = cal.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.US);
            days.add(new Day(dayName));
        }
    }
            
    @Root
    public ExampleController getRoot() {
        return this;
    }    
    
    @ChildrenOf
    public List<Day> getProducts(ExampleController root) {
        return days;
    }
    
    @ChildrenOf
    public List<DayFile> getProductFiles(Day product) {
        return product.getFiles();
    }

    @AccessControlList
    public List<AccessControlledResource.Priviledge> getPriviledges(Day target, String currentUser ) {
        log.info("Current user: " + currentUser + " - set by DayOfWeekAuthenticationHandler");
        log.info("Resource being accessed: " + target.getName());
        if( target.getName().equals(currentUser)) {
            log.info("Current user is the same as the resource beign accessed, so give full rights");
            return AccessControlledResource.READ_WRITE;
        } else {
            log.info("Current user is different to resource beign accessed, so give read-only rights");
            return AccessControlledResource.READ_CONTENT;
        }
    }
    
    @PutChild
    public DayFile upload(Day product, String newName, byte[] bytes) {
        DayFile pf = new DayFile(newName, bytes);
        product.getFiles().add(pf);
        return pf;
    }
    
    public class Day {
        private String name;
        private List<DayFile> files = new ArrayList<DayFile>();

        public Day(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }             

        public List<DayFile> getFiles() {
            return files;
        }                
    }
    
    public class DayFile {
        private String name;
        private byte[] bytes;

        public DayFile(String name, byte[] bytes) {
            this.name = name;
            this.bytes = bytes;
        }

        public String getName() {
            return name;
        }                
    }
}
