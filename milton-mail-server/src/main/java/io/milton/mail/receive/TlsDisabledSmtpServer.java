package io.milton.mail.receive;

import java.util.ArrayList;
import java.util.Collection;
import org.subethamail.smtp.MessageHandlerFactory;
import org.subethamail.smtp.MessageListener;
import org.subethamail.smtp.command.StartTLSCommand;
import org.subethamail.smtp.server.Command;
import org.subethamail.smtp.server.CommandHandler;
import org.subethamail.smtp.server.CommandRegistry;
import org.subethamail.smtp.server.SMTPServer;

class TlsDisabledSmtpServer extends SMTPServer {

    public TlsDisabledSmtpServer(MessageHandlerFactory handlerFactory) {
        super(handlerFactory);
    }

    public TlsDisabledSmtpServer(Collection<MessageListener> listeners) {
        super(listeners);
    }

    @Override
    public CommandHandler getCommandHandler() {
        // maybe?
        return createCommandHandler();
    }

    
    protected CommandHandler createCommandHandler() {
        ArrayList<Command> availableCommands = new ArrayList<Command>();
        for (CommandRegistry registry : CommandRegistry.values()) {
            if (!(registry.getCommand() instanceof StartTLSCommand)) {
                availableCommands.add(registry.getCommand());
            }
        }
        CommandHandler cmd = new CommandHandler(availableCommands);
        return cmd;
    }
}
