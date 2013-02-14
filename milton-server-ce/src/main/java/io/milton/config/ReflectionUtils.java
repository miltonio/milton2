/*
 * Copyright 2013 McEvoy Software Ltd.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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

/**
 *
 * @author brad
 */
public class ReflectionUtils {
    public static List<Class> getClassNamesFromPackage(String packageName) throws IOException, ClassNotFoundException {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        URL packageURL;
        ArrayList<Class> classes = new ArrayList<Class>();

        String packagePath = packageName.replace(".", "/");
        packageURL = classLoader.getResource(packagePath);

        ClassLoader cld = Thread.currentThread().getContextClassLoader();

        if (packageURL.getProtocol().equals("jar")) {
            String jarFileName;
            JarFile jf;
            Enumeration<JarEntry> jarEntries;
            String entryName;

            // build jar file name, then loop through zipped entries
            jarFileName = URLDecoder.decode(packageURL.getFile(), "UTF-8");
            jarFileName = jarFileName.substring(5, jarFileName.indexOf("!"));
            System.out.println(">" + jarFileName);
            jf = new JarFile(jarFileName);
            jarEntries = jf.entries();
            while (jarEntries.hasMoreElements()) {
                entryName = jarEntries.nextElement().getName();
                if (entryName.startsWith(packagePath) && entryName.length() > packagePath.length() + 5) {
                    if (entryName.endsWith(".class")) {
                        System.out.println("entryName: " + entryName);
                        //entryName = entryName.substring(packageName.length()+1, entryName.lastIndexOf('.'));
                        String className = entryName.replace("/", ".");
                        className = className.substring(0, className.length() - 6);
                        System.out.println("classname: " + className);
                        Class c = cld.loadClass(className);
                        classes.add(c);
                    }
                }
            }

            // loop through files in classpath
        } else {
            File directory = new File(packageURL.getFile());
            String[] files = directory.list();
            for (int i = 0; i < files.length; i++) {
                if (files[i].endsWith(".class")) {
                    classes.add(Class.forName(packageName + '.' + files[i].substring(0, files[i].length() - 6)));
                }
            }            
        }
        return classes;
    }	
}
