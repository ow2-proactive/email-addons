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
import org.ow2.proactive.addons.email.exception.MissingPropertyException;

import com.google.common.collect.ImmutableList;


/**
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

    public static final List<String> REQUIRED_ARGUMENTS = ImmutableList.of(ARG_FROM, ARG_RECIPIENTS,
            ARG_SUBJECT, ARG_BODY);

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
     * Instance variables containing values
     */

    // Specify author of the message
    private String from;

    // Address(es) of the primary recipient(s) of the message.
    private List<String> recipients;

    // The addresses of others who are to receive the message, though the content of the message may not be directed at them.
    private List<String> cc;

    // Addresses of recipients of the message whose addresses are not to be revealed to other recipients of the message.
    private List<String> bcc;

    // Short string identifying the topic of the message
    private String subject;

    // The content of the message.
    private String body = "";

    private boolean debug = false;

    private String host;

    private int port = 587;

    private String username;

    private String password;

    private boolean auth = true;

    private boolean enableStartTls = false;

    private String trustSsl = "*";

    private EmailSender(boolean auth, List<String> bcc, String body, List<String> cc, boolean debug,
            boolean enableStartTls, String from, String host, int port, List<String> recipients,
            String subject, String trustSsl, String username, String password) {
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
    }

    public void sendPlainTextEmail() {
        Properties props = new Properties();

        props.put("mail.debug", debug);
        props.put("mail.smtp.host", host);
        props.put("mail.smtp.port", port);
        props.put("mail.smtp.auth", auth);
        props.put("mail.smtp.starttls.enable", Boolean.toString(enableStartTls));
        props.put("mail.smtp.ssl.trust", trustSsl);

        Session session = Session.getInstance(props);
        MimeMessage message = new MimeMessage(session);
        Transport transport = null;

        try {
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

            transport = session.getTransport("smtp");
            transport.connect(username, password);
            transport.sendMessage(message, message.getAllRecipients());
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

    public static class Builder {

        private boolean debug = false;
        private boolean auth = true;
        private String from;
        private List<String> recipients;
        private List<String> cc;
        private List<String> bcc;
        private String subject;
        private String body = "";
        private String username;
        private String password;
        private String host;
        private int port = 587;
        private boolean enableStartTls = false;
        private String trustSsl = "*";

        public Builder() {

        }

        public Builder(Map<String, Serializable> options) {
            loadEmailApiConfiguration(options);
            loadArguments(options);
        }

        private void loadEmailApiConfiguration(Map<String, Serializable> options) {
            String value = getAsString(options, PROPERTY_MAIL_DEBUG);
            if (value != null) {
                debug = BooleanConverter.getInstance().convert(PROPERTY_MAIL_DEBUG, value);
            }

            value = getAsString(options, PROPERTY_MAIL_SMTP_HOST);
            if (value != null) {
                host = value;
            } else {
                throw new MissingPropertyException(PROPERTY_MAIL_SMTP_HOST);
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
                recipients = emailAddressesAsList(args, ARG_RECIPIENTS);
            }

            if (args.containsKey(ARG_SUBJECT)) {
                setSubject(getAsString(args, ARG_SUBJECT));
            }

            if (args.containsKey(ARG_BODY)) {
                body = getAsString(args, ARG_BODY);
            }

            if (args.containsKey(ARG_CC)) {
                cc = emailAddressesAsList(args, ARG_CC);
            }

            if (args.containsKey(ARG_BCC)) {
                bcc = emailAddressesAsList(args, ARG_BCC);
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

        public Builder setFrom(String from) {
            this.from = from;
            return this;
        }

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

        public Builder setDebug(boolean value) {
            this.debug = value;
            return this;
        }

        public Builder setSubject(String subject) {
            this.subject = subject;

            if (subject.length() > 78) {
                throw new InvalidArgumentException("The specified subject is too long: " +
                        subject.length() + " characters specified but 78 allowed");
            }

            return this;
        }

        public Builder setBody(String body) {
            this.body = body;
            return this;
        }

        public Builder setUsername(String username) {
            this.username = username;
            return this;
        }

        public Builder setPassword(String password) {
            this.password = password;
            return this;
        }

        public Builder setHost(String host) {
            this.host = host;
            return this;
        }

        public Builder setPort(int port) {
            this.port = port;
            return this;
        }

        public Builder setEnableStartTls(boolean enableStartTls) {
            this.enableStartTls = enableStartTls;
            return this;
        }

        public Builder setTrustSsl(String trustSsl) {
            this.trustSsl = trustSsl;
            return this;
        }

        public EmailSender build() {
            EmailSender emailNotifier = new EmailSender(auth, bcc, body, cc, debug, enableStartTls, from,
                host, port, recipients, subject, trustSsl, username, password);
            return emailNotifier;
        }

    }

}
