/*
 * Copyright 2013 McEvoy Software Ltd.
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
package com.ettrema.http.caldav;

import junit.framework.TestCase;

/**
 *
 * @author brad
 */
public class TestScratch extends TestCase{
    public void test() {
        System.out.println("ismilton: " + isMilton(false, true, true, true));
    }
    public static void main(String[] args) throws Exception {
//        String fuseHome = "/home/brad/proj/fuse-admin";
//
//        URL[] urls = {new URL("file://" + fuseHome + "/fuse-app/target/classes/"), new URL("file://" + fuseHome + "/fuse-war/target/classes/")};
//        URLClassLoader c = new URLClassLoader(urls);
//
//        Object t = c.loadClass("com.fuselms.apps.autoload.Autoloader");
//        System.out.println("t: " + t);
//
//        Object runner = c.loadClass("com.fuselms.scratch.ScratchRunner").newInstance();
//        Method m = runner.getClass().getMethod("start", String.class);
//        m.invoke(runner, fuseHome);
        System.out.println("ismilton: " + isMilton(false, true, true, true));
    }

    private static boolean isMilton(boolean A, boolean B, boolean C, boolean D) {


        if (A && (B || C) || D) {
            return true;
        }
        return false;

    }    
}
