package io.milton.mail.pop;

import org.apache.mina.core.session.IoSession;

public class GreetingState implements PopState {

    PopSession popSession;

    public GreetingState(PopSession popSession) {
        super();
        this.popSession = popSession;
    }

    public void enter(IoSession session, PopSession popSession) {
        popSession.reply(session, "+OK POP3 ready"); //todo
        popSession.transitionTo(session, new AuthState(popSession));
    }

    public void exit(IoSession session, PopSession popSession) {
    }

    public void messageReceived(IoSession session, Object msg, PopSession popSession) {
    }
}
