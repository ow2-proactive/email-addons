/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2016 INRIA/University of
 *                 Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.ow2.proactive.addons.email;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.ow2.proactive.addons.email.converters.BooleanConverter;
import org.ow2.proactive.addons.email.converters.IntegerConverter;
import org.ow2.proactive.addons.email.exception.EmailException;
import org.ow2.proactive.addons.email.exception.InvalidArgumentException;
import org.ow2.proactive.addons.email.exception.MissingArgumentException;

import com.google.common.collect.ImmutableList;


/**
 * Utility class for sending emails.
 *
 * @author ActiveEon Team
 */
public class EmailSender {

    private static final String REGEX_LIST_SEPARATOR = ",\\s?";

    /*
     * Define name of arguments that can be passed to the Java task
     */

    public static final String ARG_FROM = "from";

    public static final String ARG_RECIPIENTS = "to";

    public static final String ARG_CC = "cc";

    public static final String ARG_BCC = "bcc";

    public static final String ARG_SUBJECT = "subject";

    public static final String ARG_BODY = "body";

    /*
     * Define javax.mail properties that will be loaded from third-party credentials
     */

    public static final String PROPERTY_MAIL_DEBUG = "mail.debug";

    public static final String PROPERTY_MAIL_SMTP_HOST = "mail.smtp.host";

    public static final String PROPERTY_MAIL_SMTP_PORT = "mail.smtp.port";

    public static final String PROPERTY_MAIL_SMTP_USERNAME = "mail.smtp.username";

    public static final String PROPERTY_MAIL_SMTP_PASSWORD = "mail.smtp.password";

    public static final String PROPERTY_MAIL_SMTP_AUTH = "mail.smtp.auth";

    public static final String PROPERTY_MAIL_SMTP_STARTTLS_ENABLE = "mail.smtp.starttls.enable";

    public static final String PROPERTY_MAIL_SMTP_SSL_TRUST = "mail.smtp.ssl.trust";

    /*
     * Instance variables
     */

    protected boolean auth = true;
    protected boolean debug = false;
    protected boolean enableStartTls = false;

    protected int port = 587;

    protected List<String> cc;
    protected List<String> bcc;
    protected List<String> recipients;

    protected String body = "";
    protected String from;
    protected String host;
    protected String username;
    protected String password;
    protected String subject;
    protected String trustSsl = "*";

    protected EmailSender(boolean auth, boolean debug, boolean enableStartTls, int port, List<String> cc,
            List<String> bcc, List<String> recipients, String body, String from, String host, String username,
            String password, String subject, String trustSsl) {
        this.auth = auth;
        this.bcc = bcc;
        this.body = body;
        this.cc = cc;
        this.debug = debug;
        this.enableStartTls = enableStartTls;
        this.from = from;
        this.host = host;
        this.password = password;
        this.port = port;
        this.recipients = recipients;
        this.subject = subject;
        this.trustSsl = trustSsl;
        this.username = username;

        checkInstanceFieldsConsistency();
    }

    public void sendPlainTextEmail() {
        Properties props = buildSmtpConfiguration();

        Session session = Session.getInstance(props);
        MimeMessage message = new MimeMessage(session);
        Transport transport = null;

        try {
            configurePlainTextMessage(message);

            transport = session.getTransport("smtp");
            connectAndSendMessage(message, transport);
        } catch (AddressException e) {
            throw new EmailException(e);
        } catch (MessagingException e) {
            throw new EmailException(e.getMessage());
        } finally {
            if (transport != null) {
                try {
                    transport.close();
                } catch (MessagingException e) {
                    throw new EmailException(e);
                }
            }
        }
    }

    protected Properties buildSmtpConfiguration() {
        Properties props = new Properties();

        props.put(PROPERTY_MAIL_DEBUG, debug);
        props.put(PROPERTY_MAIL_SMTP_HOST, host);
        props.put(PROPERTY_MAIL_SMTP_PORT, port);
        props.put(PROPERTY_MAIL_SMTP_AUTH, auth);
        props.put(PROPERTY_MAIL_SMTP_STARTTLS_ENABLE, enableStartTls);
        props.put(PROPERTY_MAIL_SMTP_SSL_TRUST, trustSsl);

        return props;
    }

    protected void configurePlainTextMessage(MimeMessage message) throws MessagingException {
        if (bcc != null) {
            for (String email : bcc) {
                message.addRecipients(Message.RecipientType.BCC, email);
            }
        }

        if (cc != null) {
            for (String email : cc) {
                message.addRecipients(Message.RecipientType.CC, email);
            }
        }

        if (from != null) {
            message.addFrom(new Address[] { new InternetAddress(from) });
        }

        for (String recipient : recipients) {
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(recipient));
        }

        message.setSubject(subject);
        message.setText(body);
    }

    protected void connectAndSendMessage(MimeMessage message, Transport transport) throws MessagingException {
        transport.connect(username, password);
        transport.sendMessage(message, message.getAllRecipients());
    }

    private void checkInstanceFieldsConsistency() {
        if (from == null) {
            throw new MissingArgumentException("from");
        }

        if (recipients == null || recipients.size() == 0) {
            throw new MissingArgumentException("recipient");
        }

        if (subject == null) {
            throw new MissingArgumentException("subject");
        }

        if (body == null) {
            throw new MissingArgumentException("body");
        }

        // cf. RFC 2228: http://www.faqs.org/rfcs/rfc2822.html
        if (subject.length() > 78) {
            throw new InvalidArgumentException("The specified subject is too long: " + subject.length() +
                " characters specified but 78 allowed");
        }
    }

    /**
     * Utility class for creating an instance of {@link EmailSender}.
     */
    public static class Builder {

        private boolean auth = true;
        private boolean debug = false;
        private boolean enableStartTls = false;

        private int port = 587;

        private List<String> bcc;
        private List<String> cc;
        private List<String> recipients;

        private String body = "";
        private String from;
        private String host;
        private String username;
        private String password;
        private String subject;
        private String trustSsl = "*";

        public Builder() {
            bcc = new ArrayList<>();
            cc = new ArrayList<>();
            recipients = new ArrayList<>();
        }

        public Builder(Map<String, Serializable> options) {
            this();
            loadJavaMailConfiguration(options);
            loadArguments(options);
        }

        private void loadJavaMailConfiguration(Map<String, Serializable> options) {
            String value = getAsString(options, PROPERTY_MAIL_DEBUG);
            if (value != null) {
                debug = BooleanConverter.getInstance().convert(PROPERTY_MAIL_DEBUG, value);
            }

            value = getAsString(options, PROPERTY_MAIL_SMTP_HOST);
            if (value != null) {
                host = value;
            } else {
                throw new MissingArgumentException(PROPERTY_MAIL_SMTP_HOST);
            }

            value = getAsString(options, PROPERTY_MAIL_SMTP_PORT);
            if (value != null) {
                port = IntegerConverter.getInstance().convert(PROPERTY_MAIL_SMTP_PORT, value);
            }

            value = getAsString(options, PROPERTY_MAIL_SMTP_USERNAME);
            if (value != null) {
                username = value;
            }

            value = getAsString(options, PROPERTY_MAIL_SMTP_PASSWORD);
            if (value != null) {
                password = value;
            }

            value = getAsString(options, PROPERTY_MAIL_SMTP_AUTH);
            if (value != null) {
                auth = BooleanConverter.getInstance().convert(PROPERTY_MAIL_SMTP_AUTH, value);
            }

            value = getAsString(options, PROPERTY_MAIL_SMTP_STARTTLS_ENABLE);
            if (value != null) {
                enableStartTls = BooleanConverter.getInstance().convert(PROPERTY_MAIL_SMTP_STARTTLS_ENABLE,
                        value);
            }

            value = getAsString(options, PROPERTY_MAIL_SMTP_SSL_TRUST);
            if (value != null) {
                trustSsl = value;
            }
        }

        private void loadArguments(Map<String, Serializable> args) {

            if (args.containsKey(ARG_FROM)) {
                from = getAsString(args, ARG_FROM);
            }

            if (args.containsKey(ARG_RECIPIENTS)) {
                recipients.addAll(emailAddressesAsList(args, ARG_RECIPIENTS));
            }

            if (args.containsKey(ARG_SUBJECT)) {
                subject = getAsString(args, ARG_SUBJECT);
            }

            if (args.containsKey(ARG_BODY)) {
                body = getAsString(args, ARG_BODY);
            }

            if (args.containsKey(ARG_CC)) {
                cc.addAll(emailAddressesAsList(args, ARG_CC));
            }

            if (args.containsKey(ARG_BCC)) {
                bcc.addAll(emailAddressesAsList(args, ARG_BCC));
            }
        }

        private ImmutableList<String> emailAddressesAsList(Map<String, Serializable> args,
                String argRecipients) {
            return ImmutableList.copyOf((getAsString(args, argRecipients)).split(REGEX_LIST_SEPARATOR));
        }

        private String getAsString(Map<String, Serializable> map, String argFrom) {
            return (String) map.get(argFrom);
        }

        public Builder setAuth(boolean auth) {
            this.auth = auth;
            return this;
        }

        /**
         * Set the author of the message.
         *
         * @param from the author of the message.
         * @return the builder instance.
         */
        public Builder setFrom(String from) {
            this.from = from;
            return this;
        }

        /**
         * Add an address for the primary recipient(s) of the message.
         *
         * @param recipient the email to add.
         * @return the builder instance.
         */
        public Builder addRecipient(String recipient) {
            if (this.recipients == null) {
                this.recipients = new ArrayList<>();
            }

            this.recipients.add(recipient);
            return this;
        }

        public Builder setRecipients(List<String> recipients) {
            this.recipients = recipients;
            return this;
        }

        /**
         * The addresse of another who is to receive the message,
         * though the content of the message may not be directed at him/her.
         *
         * @param cc the email to add as carbon copy.
         * @return the builder instance.
         */
        public Builder addCc(String cc) {
            if (this.cc == null) {
                this.cc = new ArrayList<>();
            }

            this.cc.add(cc);
            return this;
        }

        public Builder setCc(List<String> cc) {
            this.cc = cc;
            return this;
        }

        /**
         * The address of the recipient of the message whose addresse is not to be revealed
         * to other recipients of the message.
         *
         * @param bcc the email to add as blind carbon copy.
         * @return the builder instance.
         */
        public Builder addBcc(String bcc) {
            if (this.bcc == null) {
                this.bcc = new ArrayList<>();
            }

            this.bcc.add(bcc);
            return this;
        }

        public Builder setBcc(List<String> bcc) {
            this.bcc = bcc;
            return this;
        }

        /**
         * Specify if debugging mode of the underlying library that is
         * used to send emails should be enabled or not.
         *
         * @param value
         * @return the builder instance.
         */
        public Builder setDebug(boolean value) {
            this.debug = value;
            return this;
        }

        /**
         * The subject of the message that is sent.
         *
         * @param subject the subject value. Must not exceed 78 characters.
         * @return the builder instance.
         */
        public Builder setSubject(String subject) {
            this.subject = subject;
            return this;
        }

        /**
         * The body (content) of the message that is sent.
         * @param body the content of the message.
         * @return the builder instance.
         */
        public Builder setBody(String body) {
            this.body = body;
            return this;
        }

        /**
         * The username to use for connecting to the specified host.
         *
         * @param username the user name
         * @return the builder instance.
         */
        public Builder setUsername(String username) {
            this.username = username;
            return this;
        }

        /**
         * The password to use for connecting to the specified host.
         *
         * @param password the password.
         * @return the builder instance.
         */
        public Builder setPassword(String password) {
            this.password = password;
            return this;
        }

        /**
         * The SMTP server to connect to.
         *
         * @param host the SMTP server to connect to.
         * @return the builder instance.
         */
        public Builder setHost(String host) {
            this.host = host;
            return this;
        }

        /**
         * The SMTP server port to connect to.
         *
         * @param port the SMTP server port to connect to. Defaults to 587.
         * @return the builder instance.
         */
        public Builder setPort(int port) {
            this.port = port;
            return this;
        }

        /**
         * Define whether STARTTLS command is used or not.
         *
         * @param enableStartTls if true, enables the use of the STARTTLS command (if supported by the server)
         * to switch the connection to a TLS-protected connection before issuing any login commands.
         * Defaults to false.
         * @return the builder instance.
         */
        public Builder setEnableStartTls(boolean enableStartTls) {
            this.enableStartTls = enableStartTls;
            return this;
        }

        /**
         * Define SSL hosts to trust.
         *
         * @param trustSsl if set to "*", all hosts are trusted. If set to a whitespace separated list of
         *                 hosts, those hosts are trusted. Otherwise, trust depends on the certificate the
         *                 server presents.
         * @return the builder instance.
         */
        public Builder setTrustSsl(String trustSsl) {
            this.trustSsl = trustSsl;
            return this;
        }

        public boolean isDebugEnabled() {
            return debug;
        }

        public boolean isAuthEnabled() {
            return auth;
        }

        public String getFrom() {
            return from;
        }

        public List<String> getRecipients() {
            return recipients;
        }

        public List<String> getCc() {
            return cc;
        }

        public List<String> getBcc() {
            return bcc;
        }

        public String getSubject() {
            return subject;
        }

        public String getBody() {
            return body;
        }

        public String getUsername() {
            return username;
        }

        public String getPassword() {
            return password;
        }

        public String getHost() {
            return host;
        }

        public int getPort() {
            return port;
        }

        public boolean isStartTlsEnabled() {
            return enableStartTls;
        }

        public String getTrustSsl() {
            return trustSsl;
        }

        public EmailSender build() {
            EmailSender emailNotifier = new EmailSender(auth, debug, enableStartTls, port, cc, bcc,
                recipients, body, from, host, username, password, subject, trustSsl);
            return emailNotifier;
        }

        @Override
        public String toString() {
            return "Builder{" + "auth=" + auth + ", debug=" + debug + ", enableStartTls=" + enableStartTls +
                ", port=" + port + ", bcc=" + bcc + ", cc=" + cc + ", recipients=" + recipients + ", body='" +
                body + '\'' + ", from='" + from + '\'' + ", host='" + host + '\'' + ", username='" +
                username + '\'' + ", password='" + password + '\'' + ", subject='" + subject + '\'' +
                ", trustSsl='" + trustSsl + '\'' + '}';
        }

    }

}
