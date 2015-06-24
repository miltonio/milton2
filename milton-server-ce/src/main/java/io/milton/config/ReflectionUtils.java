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
package io.milton.config;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author brad
 */
public class ReflectionUtils {

	private static final Logger log = LoggerFactory.getLogger(ReflectionUtils.class);

	public static List<Class> getClassNamesFromPackage(String packageName) throws IOException, ClassNotFoundException {
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		URL packageURL;
		ArrayList<Class> classes = new ArrayList<Class>();

		String packagePath = packageName.replace(".", "/");
		packageURL = classLoader.getResource(packagePath);
		if (packageURL == null) {
			log.warn("getClassNamesFromPackage: No package could be found: " + packagePath + " from classloader: " + classLoader);
			return classes;
		}

		ClassLoader cld = Thread.currentThread().getContextClassLoader();

		if (packageURL.getProtocol().equals("jar")) {
			String jarFileName;
			JarFile jf;
			Enumeration<JarEntry> jarEntries;
			String entryName;

			// build jar file name, then loop through zipped entries
			jarFileName = URLDecoder.decode(packageURL.getFile(), "UTF-8");
			jarFileName = jarFileName.substring(5, jarFileName.indexOf("!"));
			jf = new JarFile(jarFileName);
			jarEntries = jf.entries();
			while (jarEntries.hasMoreElements()) {
				entryName = jarEntries.nextElement().getName();
				if (entryName.startsWith(packagePath) && entryName.length() > packagePath.length() + 5) {
					if (entryName.endsWith(".class")) {
						//entryName = entryName.substring(packageName.length()+1, entryName.lastIndexOf('.'));
						String className = entryName.replace("/", ".");
						className = className.substring(0, className.length() - 6);
						Class c = cld.loadClass(className);
						classes.add(c);
					}
				}
			}
			jf.close();

			// loop through files in classpath
		} else {
			String f = URLDecoder.decode(packageURL.getFile(), "UTF-8");
			File directory = new File(f);
			String[] files = directory.list();
			if (files != null && files.length > 0) {
				if (log.isTraceEnabled()) {
					log.trace("Loading classes from directory: " + directory.getAbsolutePath() + " files=" + files.length);
				}

				for (int i = 0; i < files.length; i++) {
					if (files[i].endsWith(".class")) {
						String className = packageName + '.' + files[i].substring(0, files[i].length() - 6);
						Class c = cld.loadClass(className);
						classes.add(c);
					}
				}
			} else {
				log.info("No files found in package: " + packageName + " with physical path=" + directory.getAbsolutePath());
			}
		}
		return classes;
	}

	public static Class loadClass(String className) throws ClassNotFoundException {
		ClassLoader cld = Thread.currentThread().getContextClassLoader();
		return cld.loadClass(className);
	}
}
