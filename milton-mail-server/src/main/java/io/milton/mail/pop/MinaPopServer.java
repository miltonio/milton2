package io.milton.mail.pop;

import io.milton.mail.Filter;
import io.milton.mail.MailResourceFactory;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;
import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.buffer.SimpleBufferAllocator;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.codec.textline.TextLineCodecFactory;
import org.apache.mina.filter.logging.LoggingFilter;
import org.apache.mina.filter.stream.StreamWriteFilter;
import org.apache.mina.transport.socket.SocketAcceptor;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MinaPopServer implements PopServer {

    private final static Logger log = LoggerFactory.getLogger(MinaPopServer.class);

    private SocketAcceptor acceptor;
    private int popPort;
    private MailResourceFactory resourceFactory;
    private final List<Filter> filters;

    public MinaPopServer(MailResourceFactory resourceFactory, List<Filter> filters) {
        this(110, resourceFactory, filters);
    }

    public MinaPopServer(int popPort, MailResourceFactory resourceFactory, List<Filter> filters) {
        this.popPort = popPort;
        this.resourceFactory = resourceFactory;
        this.filters = filters;
    }

    public MinaPopServer(int popPort, MailResourceFactory resourceFactory, Filter filter) {
        this( popPort, resourceFactory, Arrays.asList( filter));
    }

    
    
    public void start() {
        IoBuffer.setUseDirectBuffer(false);
        IoBuffer.setAllocator(new SimpleBufferAllocator());

        acceptor = new NioSocketAcceptor();

//        cfg.getFilterChain().addLast("mimemessage1", new MimeMessageIOFilter() );
        acceptor.getFilterChain().addLast("logger", new LoggingFilter());
        acceptor.getFilterChain().addLast("codec", new ProtocolCodecFilter(new TextLineCodecFactory(Charset.forName("US-ASCII"))));
        acceptor.getFilterChain().addLast("stream", new StreamWriteFilter() );
        acceptor.setHandler( new PopIOHandlerAdapter(resourceFactory, filters) );
        try {
            //cfg.getFilterChain().addLast("codec", new ProtocolCodecFilter(new TextLineCodecFactory(Charset.forName("UTF-8"))));
            acceptor.bind(new InetSocketAddress(popPort));
        } catch (IOException ex) {
            throw new RuntimeException("Couldnt bind to port: " + popPort, ex);
        }

    }

    public void stop() {
        acceptor.unbind();
        acceptor = null;
    }

    public int getPopPort() {
        return popPort;
    }

    public void setPopPort(int popPort) {
        this.popPort = popPort;
    }

    public MailResourceFactory getResourceFactory() {
        return resourceFactory;
    }

    public void setResourceFactory(MailResourceFactory resourceFactory) {
        this.resourceFactory = resourceFactory;
    }

    
    
    static PopSession sess(IoSession session) {
        return (PopSession) session.getAttribute("stateMachine");
    }

}
