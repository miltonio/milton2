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
package io.milton.servlet;

import io.milton.http.HttpManager;
import io.milton.http.MultipleResourceFactory;
import io.milton.http.ResourceFactory;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author brad
 */
public class InitableMultipleResourceFactory extends MultipleResourceFactory {

	private Logger log = LoggerFactory.getLogger(InitableMultipleResourceFactory.class);

	public InitableMultipleResourceFactory() {
		super();
	}

	public InitableMultipleResourceFactory(List<ResourceFactory> factories) {
		super(factories);
	}

	public void init(Config config, HttpManager manager) {
		String sFactories = config.getInitParameter("resource.factory.multiple");
		init(sFactories, config, manager);
	}

	protected void init(String sFactories, Config config, HttpManager manager) {
		log.debug("init: " + sFactories);
		String[] arr = sFactories.split(",");
		for (String s : arr) {
			createFactory(s, config, manager);
		}
	}

	private void createFactory(String s, Config config, HttpManager manager) {
		log.debug("createFactory: " + s);
		Class c;
		try {
			c = Class.forName(s);
		} catch (ClassNotFoundException ex) {
			throw new RuntimeException(s, ex);
		}
		Object o;
		try {
			o = c.newInstance();
		} catch (IllegalAccessException ex) {
			throw new RuntimeException(s, ex);
		} catch (InstantiationException ex) {
			throw new RuntimeException(s, ex);
		}
		ResourceFactory rf = (ResourceFactory) o;
		if (rf instanceof Initable) {
			Initable i = (Initable) rf;
			i.init(config, manager);
		}
		factories.add(rf);
	}

	public void destroy(HttpManager manager) {
		if (factories == null) {
			log.warn("factories is null");
			return;
		}
		for (ResourceFactory f : factories) {
			if (f instanceof Initable) {
				((Initable) f).destroy(manager);
			}
		}
	}
}
