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

package com.mycompany;

import io.milton.common.Path;
import io.milton.http.ResourceFactory;
import io.milton.resource.Resource;


public class TResourceFactory implements ResourceFactory {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(TResourceFactory.class);
    
    public static final TFolderResource ROOT = new TFolderResource((TFolderResource)null,"");
    
    static {        
        String user = "Mufasa";
        //String password = "Circle Of Life";
        String password = "pwd";

//        ROOT.setSecure(user,password);

        TFolderResource folder;
        TResource file;
        file = new TTextResource(ROOT,"index.html","Hi there");
        folder = new TFolderResource(ROOT,"folder1");
        file = new TTextResource(folder,"index.html","i am a web page in folder1");
        folder = new TFolderResource(ROOT,"folder2");
        new TFolderResource(folder,"folder2a");
        folder = new TFolderResource(ROOT,"folder3");
        TFolderResource fSpecial = new TFolderResource(ROOT,"special chars");
        TFolderResource fSpecialSub = new TFolderResource(ROOT,"folder with ampersand &");
        new TFolderResource(fSpecial,"folder with percentage %");
        new TFolderResource(fSpecial,"folder with speci�l chars"); // contains ae character
        file = new TTextResource(folder,"index.html","i am a web page");
        file = new TTextResource(folder,"stuff.html","");
        folder = new TFolderResource(folder,"subfolder1");
        file = new TTextResource(folder,"index.html","");
        folder = new TFolderResource(ROOT,"secure");

        folder.setSecure(user,password);
        file = new TTextResource(folder,"index.html","");
    }
    
    
	@Override
    public Resource getResource(String host, String url) {
        log.debug("getResource: url: " + url );
        Path path = Path.path(url);
        Resource r = find(path);
		if( r == null ) {
			log.warn("getResource: not found: " + url);
		} else {
			log.debug("getResource: found: " + r.getName() + " for url: " + url);
		}
        return r;
    }

    private TResource find(Path path) {
		System.out.println("find: " + path);
        if( isRoot(path) ) {
			System.out.println("found root at: " + path);
			return ROOT;
		}
        TResource parent = find(path.getParent());
        if( parent == null ) {
			System.out.println("found null at");
			return null;
		}
        if( parent instanceof TFolderResource ) {
            TFolderResource folder = (TFolderResource)parent;
			TResource child = (TResource)folder.child(path.getName());
			if( child != null ) {
				System.out.println("found res: " + child.getName());
				return child;
			}
        }
        log.debug("not found: " + path);
        return null;
    }

    public String getSupportedLevels() {
        //return "1,2";
				return "1";
    }

    private boolean isRoot( Path path ) {
        return ( path == null || path.isRoot() );
    }

}
