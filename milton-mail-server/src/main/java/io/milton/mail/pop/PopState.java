package io.milton.mail.pop;

import org.apache.mina.core.session.IoSession;


/**
 *
 * @author brad
 */
public interface PopState {
        void enter(IoSession session, PopSession popSession);

        void exit(IoSession session, PopSession popSession);
}
