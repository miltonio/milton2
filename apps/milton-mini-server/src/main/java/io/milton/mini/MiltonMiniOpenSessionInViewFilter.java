package io.milton.mini;

import io.milton.vfs.db.utils.SessionManager;
import io.milton.http.Filter;
import io.milton.http.FilterChain;
import io.milton.http.Request;
import io.milton.http.Response;
import io.milton.vfs.data.DataSession;
import io.milton.vfs.db.Branch;
import io.milton.vfs.db.Repository;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author brad
 */
public class MiltonMiniOpenSessionInViewFilter implements Filter {

    private static final Logger log = LoggerFactory.getLogger(MiltonMiniOpenSessionInViewFilter.class);

    public static void setRollbackOnly(Request request) {
        request.getAttributes().put("rollback", Boolean.TRUE);
    }
     
    
    private final SessionManager sessionManager;

    public MiltonMiniOpenSessionInViewFilter(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    @Override
    public void process(final FilterChain chain, final Request request, final Response response) {
        long tm = System.currentTimeMillis();

        Transaction tx = null;
        try {
            Session s = sessionManager.open();
            tx = s.beginTransaction();
            chain.process(request, response);
            Boolean b = (Boolean) request.getAttributes().get("rollback");
            if (b != null && b) {
                log.warn("Rolling back");
                tx.rollback();
            } else {
                //Only commit on mutating methods
                if (request.getMethod().isWrite) {
                    tx.commit();
                } else {
                    tx.rollback();
                }
            }
        } catch(Throwable e) {
            log.error("Exception caught in filter", e);
            if( tx != null ) {
                log.warn("Rollback..");
                tx.rollback();
            }
            throw new RuntimeException(e);
        } finally {
            sessionManager.close();
        }

        tm = System.currentTimeMillis() - tm;
        log.info("Finished request: " + tm + "ms  for " + request.getAbsolutePath() + " method=" + request.getMethod());
    }
}
