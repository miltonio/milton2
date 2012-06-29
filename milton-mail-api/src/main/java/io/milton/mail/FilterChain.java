package io.milton.mail;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Passes the request and response along a series of filters
 *
 *  By default the MailServer loads a single filter which executes the appropriate
 *  handler for the http method
 *
 *  Additional filters can be added using HttpManager.addFilter
 */
public class FilterChain {

    private final static Logger log = LoggerFactory.getLogger(FilterChain.class);

    final List<Filter> filters;
    final Filter terminal;
    int pos = 0;

    public FilterChain(List<Filter> filters, Filter terminal) {
        this.filters =  filters;
        this.terminal = terminal;

    }

    public void doEvent(Event event) {
        if( pos < filters.size() ) {
            Filter filter = filters.get(pos++);
            if( filter != null ) {
                filter.doEvent(this,event);
                return ;
            }
            log.warn( "Configuration problem. null filter at position: " + pos);
        }
        if( terminal != null ) {
            terminal.doEvent(this, event);
        } else {
            log.warn("there appears to be no filters to process the request! Should be at least a terminal filter");
        }
    }
}
