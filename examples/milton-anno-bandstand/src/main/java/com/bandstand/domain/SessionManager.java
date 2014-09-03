package com.bandstand.domain;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.orm.hibernate3.SessionFactoryUtils;
import org.springframework.orm.hibernate3.support.OpenSessionInViewFilter;

/**
 *
 * @author brad
 */
public class SessionManager {
    
    private static ThreadLocal<Session> tlSession = new ThreadLocal<Session>(); 
    
    public static Session session() {
        return SessionFactoryUtils.getSession(sessionFactory, false);
    }    
    
    private static SessionFactory sessionFactory;
    
    
    public SessionManager(SessionFactory sf) {
        sessionFactory = sf;
    }

    public Session open() {
        Session session = SessionFactoryUtils.getSession(sessionFactory, true);
        tlSession.set(session);
        return session;
    }
    
    public void close() {
        Session s = session();
        if( s != null ) {
            SessionFactoryUtils.closeSession(s);
        }
        tlSession.remove();
    }

    
        
}
