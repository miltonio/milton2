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
package io.milton.mini;

import io.milton.annotations.Authenticate;
import io.milton.annotations.Calendars;
import io.milton.annotations.ChildOf;
import io.milton.annotations.ChildrenOf;
import io.milton.annotations.Get;
import io.milton.annotations.ModifiedDate;
import io.milton.annotations.Name;
import io.milton.annotations.Post;
import io.milton.annotations.ResourceController;
import io.milton.annotations.Root;
import io.milton.annotations.UniqueId;
import io.milton.annotations.Users;
import io.milton.cloud.common.CurrentDateService;
import io.milton.cloud.common.DefaultCurrentDateService;
import io.milton.cloud.common.store.FileSystemBlobStore;
import io.milton.common.JsonResult;
import io.milton.common.ModelAndView;
import io.milton.vfs.content.DbHashStore;
import io.milton.vfs.data.DataSession;
import io.milton.vfs.db.Branch;
import io.milton.vfs.db.CalEvent;
import io.milton.vfs.db.Calendar;
import io.milton.vfs.db.Organisation;
import io.milton.vfs.db.Profile;
import io.milton.vfs.db.Repository;
import io.milton.vfs.db.utils.SessionManager;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.hashsplit4j.api.BlobStore;
import org.hashsplit4j.api.HashStore;
import org.hibernate.Session;
import org.hibernate.Transaction;

@ResourceController
public class MiltonMiniController {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(MiltonMiniController.class);

	private File blobsRoot;
	private BlobStore blobStore;
	private HashStore hashStore;
	private CurrentDateService currentDateService = new DefaultCurrentDateService();
	private PasswordManager passwordManager = new PasswordManager();
	
    public MiltonMiniController() {
		blobsRoot = new File("target" + File.pathSeparator + "blobs");
		blobStore = new FileSystemBlobStore(blobsRoot);
		hashStore = new DbHashStore();
    }

    @Root
    public MiltonMiniController getRoot() {
        return this;
    }
    
    @Post
    public JsonResult doHomePagePost(MiltonMiniController root) {
        return new JsonResult(true);
    }
        
    
    @Name
    public String getRootName(MiltonMiniController root) {
        return "";
    }
    
    @Get
    public String showHomePage(MiltonMiniController root) {
        return "homePage";
    }
    
    @ChildOf
    public LoginPage getLoginPage(MiltonMiniController root, String name) {
        if( name.equals("login.html")) {
            return  new LoginPage();
        }
        return null;
    }
    
    @Get
    public String showLoginPage(LoginPage p) {
        System.out.println("show login page");
        return "login";
    }
    

    @ChildrenOf
    public UsersHome getUsersHome(MiltonMiniController root) {
        return new UsersHome();
    }
    
    
    @ChildOf(pathSuffix="new")
    public Profile createNewProfile(UsersHome usersHome) {
        Profile m = new Profile();
        m.setCreatedDate(new Date());
        m.setModifiedDate(new Date());
        return m;
    }       
    
    @Get(params={"editMode"})
    public ModelAndView showUserEditPage(Profile profile) throws UnsupportedEncodingException {
        return new ModelAndView("profile", profile, "profileEditPage"); 
    }       
    
    @Get
    public ModelAndView showUserPage(Profile profile) throws UnsupportedEncodingException {
        return new ModelAndView("profile", profile, "profilePage"); 
    }         
    
    @Post(bindData=true)
    public Profile saveProfile(Profile profile) {
        log.info("saveProfile: " + profile.getName());
        Transaction tx = SessionManager.session().beginTransaction();
        profile.setModifiedDate(new Date());
        SessionManager.session().save(profile);
        SessionManager.session().flush();
        tx.commit();
        log.info("saved musician");
        return profile;
    }    
    
    @Post(params={"password"})
    public Profile changePassword(Profile profile, Map<String,String> params) {
        log.info("changePassword: " + profile.getName());
        Transaction tx = SessionManager.session().beginTransaction();
        profile.setModifiedDate(new Date());
        String pwd = params.get("password");
        passwordManager.setPassword(profile, pwd);
        SessionManager.session().save(profile);
        SessionManager.session().flush();
        tx.commit();
        log.info("changed Password");
        return profile;
    }      
    
    @ChildrenOf
    public SharedHome getSharedFoldersHome(MiltonMiniController root) {
		Organisation org = Organisation.getRootOrg(SessionManager.session());
        return new SharedHome(org);
    }

    @Get
    public String showUsersHome(UsersHome usersHome) {
        return "usersHome";
    }
    
    @ChildrenOf
    public List<Repository> getSharedFolders(SharedHome sharedHome) {
        return sharedHome.org.getRepositories();
    }
	
	
    @ChildrenOf
    @Users
    public List<Profile> getUsers(UsersHome usersHome) {
        return Profile.findAll(SessionManager.session());
    }


    @ChildrenOf
    public List getRepositoryRootItems(Repository r) {
        Branch b = r.liveBranch();
		Session session = SessionManager.session();
		DataSession dataSession = new DataSession(b, session, hashStore, blobStore, currentDateService);
		return dataSession.getRootDataNode().getChildren();
    }

    @Authenticate
    public Boolean checkPassword(Profile user, String password) {
        return passwordManager.verifyPassword(user, password);
    }

    @UniqueId
    public String getUniqueId(DataSession.DataNode m) {
        // We'll just lock on the path
		String id = buildUniqueId(m);
		return id;
    }

    @ModifiedDate
    public Date getModifiedDate(CalEvent m) {
        return m.getModifiedDate();
    }

	private String buildUniqueId(DataSession.DataNode m) {
		if( m.getParent() != null ) {
			return buildUniqueId(m.getParent()) + "/" + m.getName();
		} else {
			return m.getBranch().getId() + "";
		}
	}

    public class UsersHome {
        public String getName() {
            return "users";
        }
    }

	
	public class SharedHome {
		private final Organisation org;

		public SharedHome(Organisation org) {
			this.org = org;
		}
		
		
		
		public String getName() {
			return "shared";
		}

		public Organisation getOrg() {
			return org;
		}				
	}
    
    public class LoginPage {
        public String getName() {
            return "login.html";
        }
    }
}
