package io.milton.mini;

import io.milton.cloud.common.CurrentDateService;
import io.milton.http.Request;
import io.milton.vfs.data.DataSession;
import io.milton.vfs.db.Branch;
import io.milton.vfs.db.Profile;
import io.milton.vfs.db.Repository;
import io.milton.vfs.db.utils.SessionManager;
import javax.inject.Inject;
import org.hashsplit4j.api.BlobStore;
import org.hashsplit4j.api.HashStore;
import org.hibernate.Session;

/**
 *
 * @author brad
 */
public class DataSessionManager {

    @Inject
    private HashStore hashStore;
    @Inject
    private BlobStore blobStore;
    @Inject
    private CurrentDateService currentDateService;    
    
    public DataSession get(Request request, Repository repo) {
        return get(request, repo, false, null);
    }
    
    public DataSession get(Request request, Repository repo, boolean autoCreateBranch, Profile currentUser) {
        String sessKey = "dataSession-" + repo.getId();
        DataSession dataSession = (DataSession) request.getAttributes().get(sessKey);
        if (dataSession == null) {
            Session session = SessionManager.session();
            Branch trunk = repo.liveBranch();
            if( trunk == null ) {
                if( autoCreateBranch ) {
                    trunk = repo.createBranch(Branch.TRUNK, currentUser, session);
                }                
            }
            if (trunk != null) {
                dataSession = new DataSession(trunk, session, hashStore, blobStore, currentDateService);
                request.getAttributes().put(sessKey, dataSession);
            }
        }
        return dataSession;
    }
    
}
