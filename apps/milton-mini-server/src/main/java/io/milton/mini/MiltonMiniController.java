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
import io.milton.annotations.ChildrenOf;
import io.milton.annotations.ModifiedDate;
import io.milton.annotations.ResourceController;
import io.milton.annotations.Root;
import io.milton.annotations.UniqueId;
import io.milton.annotations.Users;
import io.milton.cloud.common.CurrentDateService;
import io.milton.cloud.common.DefaultCurrentDateService;
import io.milton.cloud.common.store.FileSystemBlobStore;
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
import java.util.Date;
import java.util.List;
import org.hashsplit4j.api.BlobStore;
import org.hashsplit4j.api.HashStore;
import org.hibernate.Session;

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

    @ChildrenOf
    public UsersHome getUsersHome(MiltonMiniController root) {
        return new UsersHome();
    }
    @ChildrenOf
    public SharedHome getSharedFoldersHome(MiltonMiniController root) {
		Organisation org = Organisation.getRootOrg(SessionManager.session());
        return new SharedHome(org);
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
    public CalendarsHome getCalendarsHome(Profile user) {
        return new CalendarsHome(user);
    }

    @ChildrenOf
    @Calendars
    public List<Calendar> getCalendars(CalendarsHome cals) {
        return cals.user.getCalendars();
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

    public class CalendarsHome {

        private final Profile user;

        public CalendarsHome(Profile user) {
            this.user = user;
        }

        public String getName() {
            return "cals";
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
}
