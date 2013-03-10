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
package com.helloworld;

import io.milton.annotations.ChildrenOf;
import io.milton.annotations.PutChild;
import io.milton.annotations.ResourceController;
import io.milton.annotations.Root;
import java.util.ArrayList;
import java.util.List;

@ResourceController
public class HelloWorldController  {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(HelloWorldController.class);

    private List<Product> products = new ArrayList<Product>();

    public HelloWorldController() {
        products.add(new Product("hello"));
        products.add(new Product("world"));
    }
            
    @Root
    public HelloWorldController getRoot() {
        return this;
    }    
    
    @ChildrenOf
    public List<Product> getProducts(HelloWorldController root) {
        return products;
    }
    
    @ChildrenOf
    public List<ProductFile> getProductFiles(Product product) {
        return product.getProductFiles();
    }
    
    @PutChild
    public ProductFile upload(Product product, String newName, byte[] bytes) {
        ProductFile pf = new ProductFile(newName, bytes);
        product.getProductFiles().add(pf);
        return pf;
    }
    
    public class Product {
        private String name;
        private List<ProductFile> productFiles = new ArrayList<ProductFile>();

        public Product(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }             

        public List<ProductFile> getProductFiles() {
            return productFiles;
        }                
    }
    
    public class ProductFile {
        private String name;
        private byte[] bytes;

        public ProductFile(String name, byte[] bytes) {
            this.name = name;
            this.bytes = bytes;
        }

        public String getName() {
            return name;
        }                
    }
}
