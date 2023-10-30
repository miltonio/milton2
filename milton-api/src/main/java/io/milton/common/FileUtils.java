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
package io.milton.common;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.util.List;

/**
 * Utility class with helpful methods for file manipulations.
 */
public class FileUtils {

    /**
     * Makes file copy
     * @param source - File to copy from.
     * @param dest - File to copy to.
     */
    public void copy(File source, File dest) {
        try (FileInputStream is = new FileInputStream(source);
             FileOutputStream os = new FileOutputStream(dest)) {
            int i = is.read();
            while (i >= 0) {
                os.write(i);
                i = is.read();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Reads InputStream into ByteArrayInptuStream
     * @param is - source InputStream.
     * @return - ByteArrayOutputStream.
     * @throws IOException - when IO exception happens.
     */
    public static ByteArrayOutputStream readIn(InputStream is) throws IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        StreamUtils.readTo(is, os, true, true);
        return os;
    }

    /**
     * Reads resource relative to the given class.
     * @param cl - class relative to resource.
     * @param res - Resource name.
     * @return - resource.
     * @throws IOException in case of IO exceptions.
     */
    public static String readResource(Class cl, String res) throws IOException {
        InputStream in = cl.getResourceAsStream(res);
        if (in == null) {
            throw new IOException("Failed to read resource: " + res + " relative to class: " + cl.getCanonicalName());
        }
        ByteArrayOutputStream out = readIn(in);
        return out.toString();
    }

    /**
     * Silently closes InputStream.
     * @param in - InputStream.
     */
    public static void close(InputStream in) {
        try {
            if (in == null) {
                return;
            }
            in.close();
        } catch (IOException ex) {
        }
    }

    /**
     * Silently closes Closeable.
     * @param in - Closeable.
     */
    public static void close(Closeable in) {
        try {
            if (in == null) {
                return;
            }
            in.close();
        } catch (IOException ex) {
        }
    }

    /**
     * Silently closes Object if it has close method.
     * @param object - Object to close.
     */
    public static void close(Object object) {
        if (object == null) {
            return;
        }
//        debug("Closing: " + o);
        try {
            Method m = object.getClass().getMethod("close");
            m.invoke(object);
        } catch (IllegalArgumentException | NoSuchMethodException | InvocationTargetException | IllegalAccessException |
                 SecurityException ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Open file into InputStream
     * @param file - File to open.
     * @return - InputStream.
     * @throws FileNotFoundException when file not found.
     */
    public InputStream openFile(File file) throws FileNotFoundException {
        FileInputStream fin = null;
        BufferedInputStream br = null;
        fin = new FileInputStream(file);
        br = new BufferedInputStream(fin);
        return br;
    }

    /**
     * Open file to OutputStream.
     * @param file to write to.
     * @return OutputStream
     * @throws FileNotFoundException if file not found.
     */
    public OutputStream openFileForWrite(File file) throws FileNotFoundException {
        FileOutputStream fout = new FileOutputStream(file);
        return new BufferedOutputStream(fout);
    }

    /**
     * Read file into string.
     * @param file File to read.
     * @return String representation of the file.
     * @throws FileNotFoundException if file not found.
     */
    public String readFile(File file) throws FileNotFoundException {
        try (FileReader fr = new FileReader(file);
             BufferedReader br = new BufferedReader(fr)) {
            StringBuilder sb = new StringBuilder();
            String s;
            while ((s = br.readLine()) != null) {
                sb.append(s);
                sb.append("\n");
            }
            return sb.toString();
        } catch (FileNotFoundException e) {
            throw e;
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Read InputStream into string.
     * @param in InputStream to read.
     * @return String representation of the InputStream.
     */
    public String read(InputStream in) {
        try {
            BufferedInputStream bin = new BufferedInputStream(in);
            int s;
            byte[] buf = new byte[1024];
            StringBuilder sb = new StringBuilder();
            while ((s = bin.read(buf)) > -1) {
                sb.append(new String(buf, 0, s));
            }
            return sb.toString();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Resolves relative path.
     * @param start parent path.
     * @param path relative path to parent path.
     * @return full path.
     */
    public File resolveRelativePath(File start, String path) {
        String[] arr = path.split("/");
        File f = start;
        for (String s : arr) {
            if (s.equals("..")) {
                f = f.getParentFile();
            } else {
                f = new File(f, s);
            }
        }
        return f;
    }

    /**
     * Returns file extensions.
     * @param file to find extension.
     * @return extension.
     */
    public static String getExtension(File file) {
        return getExtension(file.getName());
    }

    /**
     * Returns file extension.
     * @param name File name.
     * @return file extension.
     */
    public static String getExtension(String name) {
        if (name == null) {
            return null;
        }
        try {
            int pos = name.lastIndexOf(".");
            if (pos > -1) {
                return name.substring(pos + 1);
            } else {
                return null;
            }
        } catch (Throwable e) {
            throw new RuntimeException(name, e);
        }
    }

    /**
     * Returns file name without extension.
     * @param name File name.
     * @return file name without extension.
     */
    public static String stripExtension(String name) {
        if (name.contains(".")) {
            String[] arr = name.split("[.]");
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < arr.length - 1; i++) {
                if (arr[i] != null) {
                    if (i != 0) {
                        sb.append(".");
                    }
                    sb.append(arr[i]);
                }
            }
            return sb.toString();
        } else {
            return name;
        }
    }

    /**
     * Change file name with new middle extension.
     * File text.txt with new extension csv will have name text.csv.txt
     * @param filename file name.
     * @param newExt new middle extension.
     * @return new file name.
     */
    public static String preprendExtension(String filename, String newExt) {
        String ext = getExtension(filename);
        filename = stripExtension(filename);
        filename = filename + "." + newExt + "." + ext;
        return filename;
    }

    /**
     * Creates new filename which has an index or new value of index.
     * @param name file name.
     * @param isFirst - indicates if it is an initial state so new index is 1.
     * @return new file name.
     */
    public static String incrementFileName(String name, boolean isFirst) {
        String mainName = stripExtension(name);
        String ext = getExtension(name);
        int count;
        if (isFirst) {
            count = 1;
        } else {
            int pos = mainName.lastIndexOf("(");
            if (pos > 0) {
                String sNum = mainName.substring(pos + 1, mainName.length() - 1);
                count = Integer.parseInt(sNum) + 1;
                mainName = mainName.substring(0, pos);
            } else {
                count = 1;
            }
        }
        String s = mainName + "(" + count + ")";
        if (ext != null) {
            s = s + "." + ext;
        }
        return s;

    }

    /**
     * replace spaces with underscores
     *
     * @param s
     * @return
     */
    public static String sanitiseName(String s) {
        s = s.replaceAll("[ ]", "_");
        return s;
    }

    /**
     * Read file in the list of lines.
     * @param file to read.
     * @param lines list where file lines will be read.
     */
    public static void readLines(File file, List<String> lines) {
        try (InputStream in = Files.newInputStream(file.toPath())) {
            for (Object oLine : IOUtils.readLines(in)) {
                lines.add(oLine.toString());
            }
        } catch (Exception e) {
            throw new RuntimeException(file.getAbsolutePath(), e);
        }
    }

    /**
     * Writes list of lines into file.
     * @param file to write to.
     * @param lines List of lines to write.
     */
    public static void writeLines(File file, List<String> lines) {
        try (FileOutputStream fout = new FileOutputStream(file)) {
            IOUtils.writeLines(lines, null, fout);
        } catch (Exception e) {
            throw new RuntimeException(file.getAbsolutePath(), e);
        }
    }
}
