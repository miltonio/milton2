package io.milton.mail.receive;

import java.util.ArrayList;
import org.subethamail.smtp.MessageHandlerFactory;
import org.subethamail.smtp.command.StartTLSCommand;
import org.subethamail.smtp.server.Command;
import org.subethamail.smtp.server.CommandHandler;
import org.subethamail.smtp.server.CommandRegistry;
import org.subethamail.smtp.server.SMTPServer;

class TlsDisabledSmtpServer extends SMTPServer {

    public TlsDisabledSmtpServer(MessageHandlerFactory handlerFactory) {
        super(handlerFactory);
    }


    @Override
    public CommandHandler getCommandHandler() {
        // maybe?
        return createCommandHandler();
    }


    protected CommandHandler createCommandHandler() {
        ArrayList<Command> availableCommands = new ArrayList<>();
        for (CommandRegistry registry : CommandRegistry.values()) {
            if (!(registry.getCommand() instanceof StartTLSCommand)) {
                availableCommands.add(registry.getCommand());
            }
        }
        return new CommandHandler(availableCommands);
    }
}
