/*
 * ProActive Parallel Suite(TM):
 * The Open Source library for parallel and distributed
 * Workflows & Scheduling, Orchestration, Cloud Automation
 * and Big Data Analysis on Enterprise Grids & Clouds.
 *
 * Copyright (c) 2007 - 2017 ActiveEon
 * Contact: contact@activeeon.com
 *
 * This library is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation: version 3 of
 * the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 */
package org.ow2.proactive.addons.email;

import java.util.*;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.ow2.proactive.addons.email.converters.IntegerConverter;
import org.ow2.proactive.addons.email.exception.ConversionException;
import org.ow2.proactive.addons.email.exception.EmailException;
import org.ow2.proactive.addons.email.exception.InvalidArgumentException;
import org.ow2.proactive.addons.email.exception.MissingArgumentException;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;


/**
 * Utility class for sending emails.
 *
 * @author ActiveEon Team
 */
public class EmailSender {

    private static final String REGEX_LIST_SEPARATOR = ",\\s?";

    private static final String SMTP_PROPERTY_PREFIX = "mail.";

    /*
     * Define name of arguments that can be passed to the Java task
     */

    public static final String ARG_FROM = "from";

    public static final String ARG_RECIPIENTS = "to";

    public static final String ARG_CC = "cc";

    public static final String ARG_BCC = "bcc";

    public static final String ARG_SUBJECT = "subject";

    public static final String ARG_BODY = "body";

    public static final String ARG_FILETOATTACH = "file_path";

    public static final String ARG_FILENAME = "file_name";

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

    protected List<String> cc;

    protected List<String> bcc;

    protected List<String> recipients;

    protected String body = "";

    protected String from;

    protected String subject;

    protected String fileToAttach;

    protected String fileName = "attachment.txt";

    protected Properties properties;

    protected EmailSender(Properties properties, List<String> cc, List<String> bcc, List<String> recipients,
            String body, String from, String subject, String fileToAttach, String fileName) {
        this.properties = properties;
        this.bcc = bcc;
        this.body = body;
        this.cc = cc;
        this.from = from;
        this.recipients = recipients;
        this.subject = subject;
        this.fileToAttach = fileToAttach;
        this.fileName = fileName;

        checkInstanceFieldsConsistency();
    }

    private void setProperties(Properties properties) {
        this.properties = properties;
    }

    public void sendPlainTextEmailWithAttachment() {
        Properties props = buildSmtpConfiguration();
        Session session = Session.getDefaultInstance(props, buildAuthenticator());
        MimeMessage message = new MimeMessage(session);
        Transport transport = null;

        try {
            configurePlainTextMessageWithAttachment(message);
            transport = session.getTransport("smtp");
            connectAndSendMessage(message, transport);
        } catch (MessagingException e) {
            throw new EmailException(e);
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

    public void sendPlainTextEmail() {
        Properties props = buildSmtpConfiguration();

        Session session = Session.getInstance(props, buildAuthenticator());
        MimeMessage message = new MimeMessage(session);
        Transport transport = null;

        try {
            configurePlainTextMessage(message);
            transport = session.getTransport("smtp");
            connectAndSendMessage(message, transport);
        } catch (MessagingException e) {
            throw new EmailException(e);
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

    protected void configurePlainTextMessageWithAttachment(MimeMessage message) throws MessagingException {
        configurePlainTextMessage(message);

        Multipart multipart = new MimeMultipart();

        String file = fileToAttach;
        if (file == null) {
            throw new MissingArgumentException("attached_file_path");
        }

        String name = fileName;
        if (name == null) {
            throw new MissingArgumentException("attached_file_name");
        }

        MimeBodyPart messageBodyPart = new MimeBodyPart();
        messageBodyPart.setText(body);
        multipart.addBodyPart(messageBodyPart);

        MimeBodyPart messageAttachmentPart = new MimeBodyPart();

        DataSource source = new FileDataSource(file);
        messageAttachmentPart.setDataHandler(new DataHandler(source));
        messageAttachmentPart.setFileName(name);
        multipart.addBodyPart(messageAttachmentPart);

        message.setContent(multipart);
    }

    protected void connectAndSendMessage(MimeMessage message, Transport transport) throws MessagingException {
        transport.connect();
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

    @VisibleForTesting
    protected Properties buildSmtpConfiguration() {
        Properties props = new Properties();
        for (Map.Entry prop : properties.entrySet()) {
            if (prop.getKey().toString().startsWith(SMTP_PROPERTY_PREFIX)) {
                String strValue = String.valueOf(prop.getValue());
                String strKey = String.valueOf(prop.getKey());

                if (strValue.equals(Boolean.TRUE.toString()) || strValue.equals(Boolean.FALSE.toString())) {
                    props.put(prop.getKey(), Boolean.valueOf(strValue));
                    continue;
                }

                try {
                    int intValue = IntegerConverter.getInstance().convert(strKey, strValue);
                    props.put(strKey, intValue);
                    continue;
                } catch (ConversionException e) {
                    // It is not an Integer, continue loop
                }

                // If it's neither a boolean nor an int, then it's a String
                props.put(strKey, strValue);
            }
        }
        return props;
    }

    private Authenticator buildAuthenticator() {
        return new javax.mail.Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(properties.getProperty(PROPERTY_MAIL_SMTP_USERNAME),
                                                  properties.getProperty(PROPERTY_MAIL_SMTP_PASSWORD));
            }
        };
    }

    /**
     * Utility class for creating an instance of {@link EmailSender}.
     */
    public static class Builder {

        private List<String> bcc;

        private List<String> cc;

        private List<String> recipients;

        private String body = "";

        private String from;

        private String subject;

        private String fileToAttach;

        private String fileName = "attachment.txt";

        private Properties properties;

        public Builder() {
            bcc = new ArrayList<>();
            cc = new ArrayList<>();
            recipients = new ArrayList<>();
            properties = new Properties();
            loadJavaMailPropertiesOrSetDefault(Collections.emptyMap());
        }

        public Builder(Map<?, ?> options) {
            this();
            Map<?, ?> opts = new HashMap<>(options);
            checkIfPropertyIsProvidedElseThrow(PROPERTY_MAIL_SMTP_HOST, opts);
            loadArguments(opts);
            loadJavaMailPropertiesOrSetDefault(opts);
            withProperties(opts);
        }

        private void loadJavaMailPropertiesOrSetDefault(Map<?, ?> options) {

            String debug = Optional.ofNullable(getAsString(options, PROPERTY_MAIL_DEBUG)).orElse("false");
            properties.put(PROPERTY_MAIL_DEBUG, debug);
            options.remove(PROPERTY_MAIL_DEBUG);

            String port = Optional.ofNullable(getAsString(options, PROPERTY_MAIL_SMTP_PORT)).orElse("587");
            properties.put(PROPERTY_MAIL_SMTP_PORT, port);
            options.remove(PROPERTY_MAIL_SMTP_PORT);

            String auth = Optional.ofNullable(getAsString(options, PROPERTY_MAIL_SMTP_AUTH)).orElse("true");
            properties.put(PROPERTY_MAIL_SMTP_AUTH, auth);
            options.remove(PROPERTY_MAIL_SMTP_AUTH);

            String enableStartTls = Optional.ofNullable(getAsString(options, PROPERTY_MAIL_SMTP_STARTTLS_ENABLE))
                                            .orElse("false");
            properties.put(PROPERTY_MAIL_SMTP_STARTTLS_ENABLE, enableStartTls);
            options.remove(PROPERTY_MAIL_SMTP_STARTTLS_ENABLE);

            String trustSsl = Optional.ofNullable(getAsString(options, PROPERTY_MAIL_SMTP_SSL_TRUST)).orElse("*");
            properties.put(PROPERTY_MAIL_SMTP_SSL_TRUST, trustSsl);
            options.remove(PROPERTY_MAIL_SMTP_SSL_TRUST);
        }

        private void loadArguments(Map<?, ?> args) {

            if (args.containsKey(ARG_FROM)) {
                from = getAsString(args, ARG_FROM);
                args.remove(ARG_FROM);
            }

            if (args.containsKey(ARG_RECIPIENTS)) {
                recipients.addAll(emailAddressesAsList(args, ARG_RECIPIENTS));
                args.remove(ARG_RECIPIENTS);
            }

            if (args.containsKey(ARG_SUBJECT)) {
                subject = getAsString(args, ARG_SUBJECT);
                args.remove(ARG_SUBJECT);
            }

            if (args.containsKey(ARG_BODY)) {
                body = getAsString(args, ARG_BODY);
                args.remove(ARG_BODY);
            }

            if (args.containsKey(ARG_FILETOATTACH)) {
                fileToAttach = getAsString(args, ARG_FILETOATTACH);
                args.remove(ARG_FILETOATTACH);
            }

            if (args.containsKey(ARG_FILENAME)) {
                fileName = getAsString(args, ARG_FILENAME);
                args.remove(ARG_FILENAME);
            }

            if (args.containsKey(ARG_CC)) {
                cc.addAll(emailAddressesAsList(args, ARG_CC));
                args.remove(ARG_CC);
            }

            if (args.containsKey(ARG_BCC)) {
                bcc.addAll(emailAddressesAsList(args, ARG_BCC));
                args.remove(ARG_BCC);
            }
        }

        private void checkIfPropertyIsProvidedElseThrow(String property, Map<?, ?> options) {
            if (options.containsKey(property)) {
                properties.put(property, getAsString(options, property));
                options.remove(property);
            } else {
                throw new MissingArgumentException(property);
            }
        }

        private ImmutableList<String> emailAddressesAsList(Map<?, ?> args, String argRecipients) {
            return ImmutableList.copyOf((getAsString(args, argRecipients)).split(REGEX_LIST_SEPARATOR));
        }

        private String getAsString(Map<?, ?> map, String argFrom) {
            return (String) map.get(argFrom);
        }

        public Builder setAuth(boolean auth) {
            this.properties.put(PROPERTY_MAIL_SMTP_AUTH, auth);
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
            properties.put(PROPERTY_MAIL_DEBUG, value);
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
            properties.put(PROPERTY_MAIL_SMTP_USERNAME, username);
            return this;
        }

        /**
         * The password to use for connecting to the specified host.
         *
         * @param password the password.
         * @return the builder instance.
         */
        public Builder setPassword(String password) {
            properties.put(PROPERTY_MAIL_SMTP_PASSWORD, password);
            return this;
        }

        /**
         * The SMTP server to connect to.
         *
         * @param host the SMTP server to connect to.
         * @return the builder instance.
         */
        public Builder setHost(String host) {
            properties.put(PROPERTY_MAIL_SMTP_HOST, host);
            return this;
        }

        /**
         * The SMTP server port to connect to.
         *
         * @param port the SMTP server port to connect to. Defaults to 587.
         * @return the builder instance.
         */
        public Builder setPort(int port) {
            properties.put(PROPERTY_MAIL_SMTP_PORT, port);
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
            properties.put(PROPERTY_MAIL_SMTP_STARTTLS_ENABLE, enableStartTls);
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
            properties.put(PROPERTY_MAIL_SMTP_SSL_TRUST, trustSsl);
            return this;
        }

        public Builder setAttachmentPath(String filToAttach) {
            this.fileToAttach = filToAttach;
            return this;
        }

        public Builder setAttachmentName(String fileName) {
            this.fileName = fileName;
            return this;
        }

        public void withProperties(Map<?, ?> properties) {
            this.properties.putAll(properties);
        }

        public boolean isDebugEnabled() {
            return Boolean.parseBoolean(properties.getProperty(PROPERTY_MAIL_DEBUG));
        }

        public boolean isAuthEnabled() {
            return Boolean.parseBoolean(properties.getProperty(PROPERTY_MAIL_SMTP_AUTH));
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
            return properties.getProperty(PROPERTY_MAIL_SMTP_USERNAME);
        }

        public String getPassword() {
            return properties.getProperty(PROPERTY_MAIL_SMTP_PASSWORD);
        }

        public String getHost() {
            return properties.getProperty(PROPERTY_MAIL_SMTP_HOST);
        }

        public int getPort() {
            return Integer.parseInt(properties.getProperty(PROPERTY_MAIL_SMTP_PORT));
        }

        public boolean isStartTlsEnabled() {
            return Boolean.parseBoolean(properties.getProperty(PROPERTY_MAIL_SMTP_STARTTLS_ENABLE));
        }

        public String getTrustSsl() {
            return properties.getProperty(PROPERTY_MAIL_SMTP_SSL_TRUST);
        }

        public String getFileToAttach() {
            return fileToAttach;
        }

        public String getFileName() {
            return fileName;
        }

        public Properties getProperties() {
            return properties;
        }

        public EmailSender build() {
            return new EmailSender(properties, cc, bcc, recipients, body, from, subject, fileToAttach, fileName);
        }

        @Override
        public String toString() {
            return "Builder{" + "properties= " + properties + " bcc=" + bcc + ", cc=" + cc + ", recipients=" +
                   recipients + ", body='" + body + '\'' + ", from='" + from + '\'' + ", username='" + '\'' +
                   ", subject='" + subject + '\'' + '}';
        }

    }

}
