package io.milton.mini;

import io.milton.cloud.common.CurrentDateService;
import io.milton.http.Request;
import io.milton.vfs.data.DataSession;
import io.milton.vfs.db.Branch;
import io.milton.vfs.db.Profile;
import io.milton.vfs.db.Repository;
import io.milton.vfs.db.utils.SessionManager;
import jakarta.inject.Inject;
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

    /**
     * Get an existing data session in this request, or start a new one, for
     * the live branch of the given repository
     *
     * @param request
     * @param repo
     * @param autoCreateBranch
     * @param currentUser
     * @return
     */
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
                dataSession = new DataSession(trunk, session, hashStore, blobStore, currentDateService, null);
                request.getAttributes().put(sessKey, dataSession);
            }
        }
        return dataSession;
    }

}
