package io.milton.mail.pop;

import io.milton.mail.Event;
import org.apache.mina.core.session.IoSession;

/**
 *
 */
public class PopMessageEvent implements Event {
    final private IoSession session;
    private final Object msg;

    public PopMessageEvent(IoSession session, Object msg) {
        this.session = session;
        this.msg = msg;
    }

    public Object getMsg() {
        return msg;
    }

    public IoSession getSession() { 
        return session;
    }
}
