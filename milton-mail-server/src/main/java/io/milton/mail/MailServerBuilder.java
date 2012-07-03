/*
 * Copyright 2012 McEvoy Software Ltd.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package io.milton.mail;

import io.milton.mail.pop.MinaPopServer;
import io.milton.mail.pop.PopServer;
import io.milton.mail.receive.AuthenticatedSmtpServer;
import io.milton.mail.receive.SmtpServer;
import io.milton.mail.receive.SubethaSmtpServer;
import io.milton.mail.send.AspirinMailSender;
import io.milton.mail.send.MailSender;
import java.util.List;
import org.masukomi.aspirin.core.config.Configuration;
import org.masukomi.aspirin.core.listener.ListenerManager;
import org.masukomi.aspirin.core.store.mail.MailStore;
import org.masukomi.aspirin.core.store.mail.FileMailStore;
import org.masukomi.aspirin.core.store.queue.QueueStore;
import org.masukomi.aspirin.core.store.queue.SimpleQueueStore;

/**
 *
 * @author brad
 */
public class MailServerBuilder {

    private MailResourceFactory mailResourceFactory;
    private MailSender mailSender;
    private SmtpServer smtpServer;
    private SmtpServer msaSmtpServer;
    private PopServer popServer;
    private Configuration aspirinConfiguration;
    private QueueStore queueStore;
    private MailStore mailStore;
    private boolean enableSender = true;
    private boolean enableSmtp = true;
    private boolean enableMsa = true;
    private boolean enablePop = false;
    private List<Filter> filters;
    private int smtpPort = 25;
    private boolean enableSmtpTls = false;

    public MailServer build() {
        if (mailSender == null) {
            if (enableSender) {
                ListenerManager listenerManager = new ListenerManager();
                if (aspirinConfiguration == null) {
                    aspirinConfiguration = new Configuration();
                }
                if (queueStore == null) {
                    queueStore = new SimpleQueueStore(aspirinConfiguration, listenerManager);
                }
                if (mailStore == null) {
                    mailStore = new FileMailStore(aspirinConfiguration);
                }
                mailSender = new AspirinMailSender(queueStore, mailStore);
            }
        }
        if (smtpServer == null) {
            if (enableSmtp) {
                smtpServer = new SubethaSmtpServer(smtpPort, enableSmtpTls, mailResourceFactory, filters);
            }
        }
        if (msaSmtpServer == null) {
            if (enableMsa) {
                msaSmtpServer = new AuthenticatedSmtpServer(mailResourceFactory, mailSender, filters);
            }
        }
        if (popServer == null) {
            if (enablePop) {
                popServer = new MinaPopServer(mailResourceFactory, filters);
            }
        }
        return new MailServer(mailSender, smtpServer, popServer, msaSmtpServer);
    }

    public MailSender getMailSender() {
        return mailSender;
    }

    public void setMailSender(MailSender mailSender) {
        this.mailSender = mailSender;
    }

    public SmtpServer getMsaSmtpServer() {
        return msaSmtpServer;
    }

    public void setMsaSmtpServer(SmtpServer msaSmtpServer) {
        this.msaSmtpServer = msaSmtpServer;
    }

    public PopServer getPopServer() {
        return popServer;
    }

    public void setPopServer(PopServer popServer) {
        this.popServer = popServer;
    }

    public SmtpServer getSmtpServer() {
        return smtpServer;
    }

    public void setSmtpServer(SmtpServer smtpServer) {
        this.smtpServer = smtpServer;
    }

    public List<Filter> getFilters() {
        return filters;
    }

    public void setFilters(List<Filter> filters) {
        this.filters = filters;
    }

    public MailResourceFactory getMailResourceFactory() {
        return mailResourceFactory;
    }

    public void setMailResourceFactory(MailResourceFactory mailResourceFactory) {
        this.mailResourceFactory = mailResourceFactory;
    }

    public boolean isEnableMsa() {
        return enableMsa;
    }

    public void setEnableMsa(boolean enableMsa) {
        this.enableMsa = enableMsa;
    }

    public boolean isEnablePop() {
        return enablePop;
    }

    public void setEnablePop(boolean enablePop) {
        this.enablePop = enablePop;
    }

    public boolean isEnableSender() {
        return enableSender;
    }

    public void setEnableSender(boolean enableSender) {
        this.enableSender = enableSender;
    }

    public boolean isEnableSmtp() {
        return enableSmtp;
    }

    public void setEnableSmtp(boolean enableSmtp) {
        this.enableSmtp = enableSmtp;
    }

    public Configuration getAspirinConfiguration() {
        return aspirinConfiguration;
    }

    public void setAspirinConfiguration(Configuration aspirinConfiguration) {
        this.aspirinConfiguration = aspirinConfiguration;
    }

    public int getSmtpPort() {
        return smtpPort;
    }

    public void setSmtpPort(int smtpPort) {
        this.smtpPort = smtpPort;
    }

    public boolean isEnableSmtpTls() {
        return enableSmtpTls;
    }

    public void setEnableSmtpTls(boolean enableSmtpTls) {
        this.enableSmtpTls = enableSmtpTls;
    }
    
    
}
