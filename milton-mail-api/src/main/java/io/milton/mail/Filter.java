package io.milton.mail;


public interface Filter {

    public void doEvent(FilterChain chain, Event event);

}
