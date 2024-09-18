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
package io.milton.mini.controllers;

import io.milton.annotations.AccessControlList;
import io.milton.annotations.Authenticate;
import io.milton.annotations.ChildOf;
import io.milton.annotations.ChildrenOf;
import io.milton.annotations.Email;
import io.milton.annotations.Get;
import io.milton.annotations.MakeCollection;
import io.milton.annotations.ModifiedDate;
import io.milton.annotations.Name;
import io.milton.annotations.Post;
import io.milton.annotations.Principal;
import io.milton.annotations.ResourceController;
import io.milton.annotations.Root;
import io.milton.annotations.UniqueId;
import io.milton.annotations.Users;
import io.milton.cloud.common.CurrentDateService;
import io.milton.common.JsonResult;
import io.milton.common.ModelAndView;
import io.milton.config.HttpManagerBuilder;
import io.milton.config.InitListener;
import io.milton.http.HttpManager;
import io.milton.http.http11.auth.DigestResponse;
import io.milton.mini.PasswordManager;
import io.milton.resource.AccessControlledResource;
import io.milton.vfs.data.DataSession;
import io.milton.vfs.db.CalEvent;
import io.milton.vfs.db.Group;
import io.milton.vfs.db.GroupMembership;
import io.milton.vfs.db.GroupRole;
import io.milton.vfs.db.Organisation;
import io.milton.vfs.db.Profile;
import io.milton.vfs.db.Repository;
import io.milton.vfs.db.utils.SessionManager;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import jakarta.inject.Inject;
import org.hashsplit4j.api.BlobStore;
import org.hashsplit4j.api.HashStore;
import org.hibernate.Session;
import org.hibernate.Transaction;

@ResourceController
public class MiltonMiniController implements InitListener {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(MiltonMiniController.class);

    public enum Role {

        Admin(AccessControlledResource.Priviledge.ALL),
        Author(AccessControlledResource.Priviledge.READ, AccessControlledResource.Priviledge.WRITE, AccessControlledResource.Priviledge.UNLOCK),
        Viewer(AccessControlledResource.Priviledge.READ),
        UserAdmin(AccessControlledResource.Priviledge.READ_ACL, AccessControlledResource.Priviledge.WRITE_ACL);

        AccessControlledResource.Priviledge[] privs;

        Role(AccessControlledResource.Priviledge... privs) {
            this.privs = privs;
        }
    }

    @Inject
    private BlobStore blobStore;

    @Inject
    private HashStore hashStore;

    @Inject
    private CurrentDateService currentDateService;

    @Inject
    private PasswordManager passwordManager;

    @Inject
    private SessionManager sessionManager;

    @Override
    public void beforeProtocolBuild(HttpManagerBuilder b) {

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
        if (name.equals("login.html")) {
            return new LoginPage();
        }
        return null;
    }

    @Get
    public String showLoginPage(LoginPage p) {
        return "login";
    }

    @ChildOf
    public ScratchPage getScratchPage(MiltonMiniController root, String name) {
        if (name.equals("scratch.html")) {
            return new ScratchPage(name);
        }
        return null;
    }

    @Get
    public String showScratchPage(ScratchPage p) {
        return "scratch";
    }

    @ChildrenOf
    public UsersHome getUsersHome(MiltonMiniController root) {
        return new UsersHome();
    }

    @ChildrenOf
    public RepoHome findRepoHome(MiltonMiniController root) {
        Organisation org = Organisation.getRootOrg(SessionManager.session());
        return new RepoHome("files", org);
    }

    @ChildrenOf
    public List<Repository> findRepositories(RepoHome repoHome) {
        return repoHome.org.getRepositories();
    }

    @ChildOf(pathSuffix = "new")
    public Profile createNewProfile(UsersHome usersHome) {
        return createNewProfile();
    }

    public Profile createNewProfile() {
        Profile m = new Profile();
        m.setCreatedDate(new Date());
        m.setModifiedDate(new Date());
        return m;
    }

    @Get(params = {"editMode"})
    public ModelAndView showUserEditPage(Profile profile) throws UnsupportedEncodingException {
        return new ModelAndView("profile", profile, "profileEditPage");
    }

    @Get
    public ModelAndView showUserPage(Profile profile) throws UnsupportedEncodingException {
        return new ModelAndView("profile", profile, "profilePage");
    }

    @AccessControlList
    public Collection<AccessControlledResource.Priviledge> getUserPriviledges(MiltonMiniController target, Profile currentUser) {
        return getPriviledges(currentUser);
    }

    @AccessControlList
    public Collection<AccessControlledResource.Priviledge> getUserPriviledges(Profile target, Profile currentUser) {
        if (currentUser == null) {
            return AccessControlledResource.NONE;
        } else {
            if (currentUser.getId() == target.getId()) {
                return AccessControlledResource.READ_WRITE;
            } else {
                return getPriviledges(currentUser);
            }
        }
    }

    @Post(bindData = true)
    public Profile saveProfile(Profile profile) {
        profile.setModifiedDate(new Date());
        // HACK, add user to admin group until we build groups page
        Organisation org = Organisation.getRootOrg(SessionManager.session());
        Session session = SessionManager.session();

        session.save(profile);
        Group g = org.group("admin", session);
        if (!g.containsUser(org, org, session)) {
            log.info("Add to admin group, hack!");
            GroupMembership gm = profile.createGroupMembership(g, org, session);
        }

        session.flush();
        return profile;
    }

    @Post(params = {"password"})
    public Profile changePassword(Profile profile, Map<String, String> params) {
        log.info("changePassword: " + profile.getName());
        profile.setModifiedDate(new Date());
        String pwd = params.get("password");
        passwordManager.setPassword(profile, pwd);
        SessionManager.session().save(profile);
        SessionManager.session().flush();
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

    @MakeCollection
    public Repository createSharedFolder(SharedHome sharedHome, String newName, @Principal Profile user) {
        Repository repo = sharedHome.org.createRepository(newName, user, SessionManager.session());
        return repo;
    }

    @ChildrenOf
    @Users
    public List<Profile> getUsers(UsersHome usersHome) {
        return Profile.findAll(SessionManager.session());
    }

    @Authenticate
    public Boolean checkPasswordBasic(Profile user, String password) {
        return passwordManager.verifyPassword(user, password);
    }

    @Authenticate
    public Boolean checkPasswordDigest(Profile user, DigestResponse digest) {
        return passwordManager.verifyDigest(digest, user);
    }

    @Email
    public String getUserEmail(Profile profile) {
        return profile.getEmail();
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
        if (m.getParent() != null) {
            return buildUniqueId(m.getParent()) + "/" + m.getName();
        } else {
            return m.getBranch().getId() + "";
        }
    }

    @Override
    public void beforeInit(HttpManagerBuilder b) {

    }

    @Override
    public void afterInit(HttpManagerBuilder b) {

    }

    /**
     * Check the root organisation exists
     *
     * @param b
     * @param m
     */
    @Override
    public void afterBuild(HttpManagerBuilder b, HttpManager m) {
        Session session = sessionManager.open();
        Transaction tx = session.beginTransaction();
        try {
            Organisation rootOrg = Organisation.getRootOrg(session);
            if (rootOrg == null) {
                log.info("Creating root organisation");
                rootOrg = new Organisation();
                Date now = currentDateService.getNow();
                rootOrg.setCreatedDate(now);
                rootOrg.setModifiedDate(now);
                rootOrg.setOrgId("root");
                session.save(rootOrg);
                session.flush();
            }

            Group adminGroup = rootOrg.group("admin", session);
            if (adminGroup == null) {
                adminGroup = rootOrg.createGroup("admin");
                adminGroup.setRegistrationMode(Group.REGO_MODE_CLOSED);
                session.save(adminGroup);
                adminGroup.grantRole(Role.Admin.name(), session);
                session.flush();
            }

            Profile admin = Profile.find("admin", session);
            if (admin == null) {
                admin = createNewProfile();
                admin.setName("admin");
                admin.setNickName("admin");
                session.save(admin);
                session.flush();

                admin.createGroupMembership(adminGroup, rootOrg, session);
                session.flush();

                passwordManager.setPassword(admin, "password8");
                session.flush();
            }
            Repository files = rootOrg.repository("files");
            if (files == null) {
                System.out.println("create directory");
                files = rootOrg.createRepository("files", admin, session);
                session.save(files);
                session.flush();
            }
            System.out.println("files repo: " + files);
            tx.commit();
        } catch (Exception ex) {
            log.error("EXception creating initial data", ex);
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

    public class ScratchPage {

        private String name;

        public ScratchPage(String name) {
            this.name = name;
        }

        public String getName() {
            return "name"; // DW
        }
    }

    public class RepoHome {

        private final String name;
        private final Organisation org;

        public RepoHome(String name, Organisation org) {
            this.name = name;
            this.org = org;
        }

        public String getName() {
            return name;
        }
    }

    public Set<AccessControlledResource.Priviledge> getPriviledges(Profile curUser) {
        Set<AccessControlledResource.Priviledge> privs = new HashSet<>();
        if (curUser != null) {
            if (curUser.getMemberships() != null && !curUser.getMemberships().isEmpty()) {
                for (GroupMembership m : curUser.getMemberships()) {
                    if (log.isTraceEnabled()) {
                        log.trace("getPriviledges: append privs for group membership: " + m.getGroupEntity().getName());
                    }
                    appendPriviledges(m.getGroupEntity(), privs);
                }
            } else {
                log.trace("getPriviledges: user has no group memberships");
            }
        } else {
            log.trace("anonymous request");
        }
        return privs;
    }

    private void appendPriviledges(Group g, Set<AccessControlledResource.Priviledge> privs) {
        if (g.getGroupRoles() != null) {
            for (GroupRole gr : g.getGroupRoles()) {
                String roleName = gr.getRoleName();
                Role role = Role.valueOf(roleName);
                if (role != null) {
                    privs.addAll(Arrays.asList(role.privs));
                } else {
                    log.warn("Role not found: " + roleName);
                }
            }
        }
    }
}
