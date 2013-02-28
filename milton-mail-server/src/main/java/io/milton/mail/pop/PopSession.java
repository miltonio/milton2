package io.milton.mail.pop;

import io.milton.mail.Message;
import io.milton.mail.MailResourceFactory;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;
import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PopSession {

    private final static Logger log = LoggerFactory.getLogger(PopSession.class);

    UUID sessionId;
    PopState state;
    AuthState auth;
    final MailResourceFactory resourceFactory;
    Collection<Message> messageResources;
    ArrayList<Message> messages;

    PopSession(IoSession session, final MailResourceFactory resourceFactory) {
        super();
        sessionId = UUID.randomUUID();
        this.resourceFactory = resourceFactory;
        state = new GreetingState(this);
        state.enter(session, this);
        
    }

    void messageReceived(IoSession session, Object msg) {
        try {
            log.info("messageReceived: state: " + state.getClass() + " msg:" + msg);
            String sMsg = (String) msg;
            String[] arr = sMsg.split(" ");
            String sCmd = arr[0];
            sCmd = sCmd.toLowerCase();
            Method m = state.getClass().getMethod(sCmd, IoSession.class, PopSession.class, arr.getClass());
            if (m == null) {
                throw new RuntimeException("un-handled command: " + sCmd);
            } else {
                m.invoke(state, session, this, arr);
            }
        } catch (IllegalAccessException ex) {
            throw new RuntimeException(ex);
        } catch (IllegalArgumentException ex) {
            throw new RuntimeException(ex);
        } catch (InvocationTargetException ex) {
            throw new RuntimeException(ex);
        } catch (NoSuchMethodException ex) {
            throw new RuntimeException(ex);
        } catch (SecurityException ex) {
            throw new RuntimeException(ex);
        }
    }

    void transitionTo(IoSession session, PopState newState) {
        state.exit(session, this);
        state = newState;
        state.enter(session, this);
        log.info("new state: " + state.getClass());
    }

    void reply(IoSession session, String msg) {
        log.info("reply: " + msg);
        log.info("..length: " + msg.length());
//        session.write(msg + (char)13 + (char)10);
                session.write(msg + (char)13);
//        session.write(msg + "\n");
    }

    void replyMultiline(IoSession session, String content) {
        // todo: handle special case of single line consisting of a .
        session.write(content + "\n");
    }
}
