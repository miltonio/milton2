package io.milton.mail.pop;

import io.milton.mail.Message;
import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UpdateState implements PopState {

    private final static Logger log = LoggerFactory.getLogger(UpdateState.class);
    
    PopSession popSession;

    public UpdateState(PopSession popSession) {
        super();
        this.popSession = popSession;
        log.info("CREATED UpdateState");
    }

    public void enter(IoSession session, PopSession popSession) {
        if( popSession.messages != null ) {
            for (Message m : popSession.messages) {
                if (m.isMarkedForDeletion()) {
                    log.debug("deleting: " + m.getId() );
                    m.getResource().deleteMessage();
                }
            }
        }
        popSession.reply(session, "+OK");
        session.close();
    }

    public void exit(IoSession session, PopSession popSession) {
    }
}
