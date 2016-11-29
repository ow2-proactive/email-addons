package org.ow2.proactive.addons.email;

import static com.google.common.truth.Truth.assertThat;

import java.io.Serializable;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;
import javax.naming.ConfigurationException;

import org.junit.Ignore;
import org.junit.Test;

import com.google.common.collect.ImmutableMap;


/**
 * System integration tests checking that {@link EmailSender} implementation is working as expected
 * when connecting to real email services such as Gmail or Outlook.
 * <p>
 * Running these integration tests requires to define some environment variables which are used to hide
 * credentials information from the source code.
 * 
 * @author ActiveEon Team
 */
public class EmailSenderIntegrationTest {

    @Test
    /*
    * If this test for gmail in emailnotification-addons will fail with error.
    * This can be due to security polices of Google.
    * You need to go to https://www.google.com/accounts/DisplayUnlockCaptcha,
    * then rerun the jenkins tasks and new application will be added as verified.
    */
    public void testEmailSenderUsingGmail() throws Exception {
        testSendEmail("smtp.gmail.com", "imap.gmail.com", System.getenv("GMAIL_EMAIL"),
                System.getenv("GMAIL_USERNAME"), System.getenv("GMAIL_PASSWORD"));
    }

    @Test
    @Ignore
    // Outlook servers are sometimes really slow to relay messages to their own infrastructure
    // For instance I noticed that it may take 14 minutes between the time where the message
    // is sent and the time at which it is accessible from the inbox. This problem seems not
    // to occurs with Gmail
    public void testEmailSenderUsingOutlook() throws Exception {
        testSendEmail("smtp-mail.outlook.com", "imap-mail.outlook.com", System.getenv("OUTLOOK_EMAIL"),
                System.getenv("OUTLOOK_USERNAME"), System.getenv("OUTLOOK_PASSWORD"));
    }

    private void testSendEmail(String smtpFqdn, String imapFqdn, String email, String username,
            String password) throws ConfigurationException, MessagingException {

        ImmutableMap.Builder<String, Serializable> configuration = new ImmutableMap.Builder<>();

        configuration.put(EmailSender.PROPERTY_MAIL_SMTP_HOST, smtpFqdn);
        configuration.put(EmailSender.PROPERTY_MAIL_SMTP_USERNAME, username);
        configuration.put(EmailSender.PROPERTY_MAIL_SMTP_PASSWORD, password);
        configuration.put(EmailSender.PROPERTY_MAIL_SMTP_AUTH, "true");
        configuration.put(EmailSender.PROPERTY_MAIL_SMTP_STARTTLS_ENABLE, "true");
        configuration.put(EmailSender.PROPERTY_MAIL_SMTP_SSL_TRUST, smtpFqdn);

        String subject = sendEmail(email, configuration.build());

        assertThat(checkEmailReception(subject, imapFqdn, username, password)).isTrue();
    }

    private String sendEmail(String recipient, Map<String, Serializable> smtpConfiguration)
            throws ConfigurationException {
        String subject = createSubject();

        EmailSender.Builder builder = new EmailSender.Builder(smtpConfiguration);
        builder.addRecipient(recipient);
        builder.setFrom("noreply@activeeon.com");
        builder.setSubject(subject);
        builder.setBody("Auto generated email for testing purposes.");

        builder.build().sendPlainTextEmail();

        return subject;
    }

    private String createSubject() {
        String uuid = UUID.randomUUID().toString();
        return "[emailnotification-addons-system-test] " + uuid;
    }

    private boolean checkEmailReception(String expectedSubject, String imapFqdn, String username,
            String password) throws MessagingException {
        Properties properties = new Properties();
        properties.put("mail.store.protocol", "imaps");
        Session session = Session.getInstance(properties);

        Store store = null;

        boolean found = false;

        try {
            store = session.getStore("imaps");
            store.connect(imapFqdn, username, password);

            Folder inbox = store.getFolder("INBOX");
            inbox.open(Folder.READ_WRITE);

            Message[] messages = inbox.getMessages();

            System.out.println("Expecting a message with expectedSubject '" + expectedSubject + "'");
            System.out.println(messages.length + " email(s) read for user " + username + " on " + imapFqdn);

            int i = 1;
            for (Message message : messages) {
                System.out.println("Message " + i + ", subject is '" + message.getSubject() + "'");

                if (message.getSubject().equals(expectedSubject)) {
                    message.setFlag(Flags.Flag.DELETED, true);
                    found = true;

                    System.out.println(
                            "Message " + i + " satisfies expected subject, message marked as deleted");
                    break;
                }

                i++;
            }

            inbox.close(true);
        } finally {
            try {
                if (store != null) {
                    store.close();
                }
            } catch (MessagingException e) {
                e.printStackTrace();
            }
        }

        return found;
    }

}
