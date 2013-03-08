package io.milton.mail.pop;

import io.milton.common.ChunkWriter;
import io.milton.common.ChunkingOutputStream;
import io.milton.mail.Message;
import io.milton.mail.MessageFolder;
import io.milton.mail.MessageResource;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import javax.mail.Session;
import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TransactionState extends BaseState {

    private final static Logger log = LoggerFactory.getLogger(TransactionState.class);
    MessageFolder inbox;

    TransactionState(PopSession popSession) {
        super(popSession);
        this.popSession = popSession;
        popSession.messages = new ArrayList<Message>();
        inbox = popSession.auth.mbox.getInbox();
        if( inbox != null ) {
            int num = 1;
            Collection<MessageResource> messageResources = inbox.getMessages();
            if( messageResources != null ) {                
                for (MessageResource mr : messageResources) {
                    Message m = new Message(mr, num++);
                    popSession.messages.add(m);
                }
            }
            log.debug("session messages: " + popSession.messages.size());
        } else {
            log.warn("user has no inbox: " + popSession.auth.user + " on resource of type: " + popSession.auth.mbox.getClass());
        }
    }

    private Message get(PopSession popSession, int num) {
        return popSession.messages.get(num - 1);
    }

    public void enter(IoSession session, PopSession popSession) {
        // don't know what this is doing here..
//        popSession.reply(session, "+OK " + popSession.auth.user + " has " + inbox.numMessages() + " messages (" + inbox.totalSize() + " octets)");
        log.info("entering transaction state");
    }

    public void exit(IoSession session, PopSession popSession) {
    }

    public void uidl(IoSession session, PopSession popSession, String[] args) {
        if (args.length <= 1) {
            popSession.reply(session, "+OK");
            for (Message m : popSession.messages) {
                popSession.reply(session, "" + m.getId() + " " + m.hashCode());
            }
            popSession.reply(session, ".");
        } else {
            String sNum = args[1];
            int num = Integer.parseInt(sNum);
            Message m = get(popSession, num);
            if (m == null) {
                popSession.reply(session, "-ERR no such message");
            } else {
                popSession.reply(session, "+OK " + m.hashCode());
            }
        }
    }

    public void list(IoSession session, PopSession popSession, String[] args) {
        log.debug("list: " + args.length);
        if (args.length <= 1) {
            popSession.reply(session, "+OK");
            for (Message m : popSession.messages) {
                popSession.reply(session, "" + m.getId() + " " + m.size());
            }
            popSession.reply(session, ".");
        } else {
            String sNum = args[1];
            int num = Integer.parseInt(sNum);
            Message m = get(popSession, num);
            if (m == null) {
                popSession.reply(session, "-ERR no such message");
            } else {
                popSession.reply(session, "+OK " + m.size());
            }
        }
    }

    public void capa(IoSession session, PopSession popSession, String[] args) {
        popSession.reply(session, "+OK Capability list follows");
        popSession.reply(session, ".");
    }

    public void stat(IoSession session, PopSession popSession, String[] args) {
        int size = 0;
        if( inbox != null ) {
            size = inbox.totalSize();
        } else {
            log.warn("No inbox for user: " + popSession.auth.user);
        }
        int messages = 0;
        if( popSession.messages != null ) {
            messages = popSession.messages.size();
        }
        popSession.reply(session, "+OK " + messages + " " + size);
    }

    public void retr(final IoSession session, PopSession popSession, String[] args) {
        String sNum = args[1];
        int num = Integer.parseInt(sNum);
        Message m = get(popSession, num);

        if (m == null) {
            popSession.reply(session, "-ERR no such message");
        } else {
            popSession.reply(session, "+OK " + m.size() + " octets");
            Session mailSess = null;
            ChunkWriter store = new ChunkWriter() {

                @Override
                public void newChunk(int i, byte[] data) {
                    IoBuffer bb = IoBuffer.wrap(data);
                    session.write(bb);
                }
            };
            ChunkingOutputStream out = new ChunkingOutputStream(store, 1024);
            try {
                m.getResource().writeTo(out);
                out.flush();
            } catch (Exception e) {
                log.error("exception sending message", e);
            } finally {
                close(out);
            }
            popSession.reply(session, ".");

        }
    }

    private void close(OutputStream out) {
        if( out == null ) return ;
        try {
            out.close();
        } catch (IOException ex) {
            log.warn("",ex);
        }
    }

    public void dele(IoSession session, PopSession popSession, String[] args) {
        String sNum = args[1];
        int num = Integer.parseInt(sNum);
        Message mid = get(popSession, num);
        if (mid != null) {
            mid.markForDeletion();
            popSession.reply(session, "+OK");
            return;
        } else {
            popSession.reply(session, "-ERR no such message");
        }
    }
}
