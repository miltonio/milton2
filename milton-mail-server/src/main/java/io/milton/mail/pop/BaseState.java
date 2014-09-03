package io.milton.mail.pop;

import org.apache.mina.core.session.IoSession;

public abstract class BaseState implements PopState {

    PopSession popSession;

    public BaseState(PopSession popSession) {
        super();
        this.popSession = popSession;
    }

    abstract void capa(IoSession session, PopSession popSession, String[] args);

    public void quit(IoSession session, PopSession popSession, String[] args) {
        popSession.transitionTo(session, new UpdateState(popSession));
    }
}
