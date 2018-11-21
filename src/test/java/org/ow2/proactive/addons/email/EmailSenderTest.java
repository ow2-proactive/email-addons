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

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Properties;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;
import org.ow2.proactive.addons.email.exception.InvalidArgumentException;
import org.ow2.proactive.addons.email.exception.MissingArgumentException;

import com.google.common.collect.ImmutableList;


/**
 * Unit tests related to {@link EmailSender}.
 *
 * @author ActiveEon Team
 */
public class EmailSenderTest {

    /*
     * Check the mapping between fields passed as parameters and the instance fields.
     */
    @Test
    public void testConstructor() {
        EmailSender emailSender = new EmailSender(false,
                                                  true,
                                                  true,
                                                  25,
                                                  ImmutableList.of("cc@company.com"),
                                                  ImmutableList.of("bcc@company.com"),
                                                  ImmutableList.of("recipients@company.com"),
                                                  "body",
                                                  "from@company.com",
                                                  "host",
                                                  "username",
                                                  "password",
                                                  "subject",
                                                  "trust",
                                                  "file_path",
                                                  "file_name");

        assertThat(emailSender.auth).isFalse();
        assertThat(emailSender.debug).isTrue();
        assertThat(emailSender.enableStartTls).isTrue();

        assertThat(emailSender.port).isEqualTo(25);

        assertThat(emailSender.cc).contains("cc@company.com");
        assertThat(emailSender.bcc).contains("bcc@company.com");
        assertThat(emailSender.recipients).contains("recipients@company.com");

        assertThat(emailSender.body).isEqualTo("body");
        assertThat(emailSender.from).isEqualTo("from@company.com");
        assertThat(emailSender.host).isEqualTo("host");
        assertThat(emailSender.username).isEqualTo("username");
        assertThat(emailSender.password).isEqualTo("password");
        assertThat(emailSender.subject).isEqualTo("subject");
        assertThat(emailSender.trustSsl).isEqualTo("trust");
    }

    @Test(expected = InvalidArgumentException.class)
    public void testTooLongSubject() {
        new EmailSender(false,
                        true,
                        true,
                        25,
                        ImmutableList.of("cc@company.com"),
                        ImmutableList.of("bcc@company.com"),
                        ImmutableList.of("recipients@company.com"),
                        "body",
                        "from@company.com",
                        "host",
                        "username",
                        "password",
                        "This is a really really really really long subject that will exceed the authorized length",
                        "trust",
                        "file_path",
                        "file_name");
    }

    @Test(expected = MissingArgumentException.class)
    public void testNullFrom() {
        new EmailSender(false,
                        true,
                        true,
                        25,
                        ImmutableList.of("cc@company.com"),
                        ImmutableList.of("bcc@company.com"),
                        ImmutableList.of("recipients@company.com"),
                        "body",
                        null,
                        "host",
                        "username",
                        "password",
                        "This is a really really really really long subject that will exceed the authorized length",
                        "trust",
                        "file_path",
                        "file_name");
    }

    @Test(expected = MissingArgumentException.class)
    public void testNullSubject() {
        new EmailSender(false,
                        true,
                        true,
                        25,
                        ImmutableList.of("cc@company.com"),
                        ImmutableList.of("bcc@company.com"),
                        ImmutableList.of("recipients@company.com"),
                        "body",
                        "from@company.com",
                        "host",
                        "username",
                        "password",
                        null,
                        "trust",
                        "file_path",
                        "file_name");
    }

    @Test(expected = MissingArgumentException.class)
    public void testNullBody() {
        new EmailSender(false,
                        true,
                        true,
                        25,
                        ImmutableList.of("cc@company.com"),
                        ImmutableList.of("bcc@company.com"),
                        ImmutableList.of("recipients@company.com"),
                        null,
                        "from@company.com",
                        "host",
                        "username",
                        "password",
                        "subject",
                        "trust",
                        "file_path",
                        "file_name");
    }

    @Test
    public void testBuildSmtpConfiguration() {
        EmailSender emailSender = new EmailSender(false,
                                                  true,
                                                  true,
                                                  25,
                                                  ImmutableList.of("cc@company.com"),
                                                  ImmutableList.of("bcc@company.com"),
                                                  ImmutableList.of("recipients@company.com"),
                                                  "body",
                                                  "from@company.com",
                                                  "host",
                                                  "username",
                                                  "password",
                                                  "subject",
                                                  "trust",
                                                  "file_path",
                                                  "file_name");

        Properties properties = emailSender.buildSmtpConfiguration();

        assertThat(properties).hasSize(6);

        assertThat(properties).containsEntry(EmailSender.PROPERTY_MAIL_DEBUG, true);
        assertThat(properties).containsEntry(EmailSender.PROPERTY_MAIL_SMTP_HOST, "host");
        assertThat(properties).containsEntry(EmailSender.PROPERTY_MAIL_SMTP_PORT, 25);
        assertThat(properties).containsEntry(EmailSender.PROPERTY_MAIL_SMTP_AUTH, false);
        assertThat(properties).containsEntry(EmailSender.PROPERTY_MAIL_SMTP_STARTTLS_ENABLE, true);
        assertThat(properties).containsEntry(EmailSender.PROPERTY_MAIL_SMTP_SSL_TRUST, "trust");
    }

    @Test
    public void testConfigurePlainTextMessage() throws MessagingException {
        EmailSender emailSender = new EmailSender(false,
                                                  true,
                                                  true,
                                                  25,
                                                  ImmutableList.of("cc1@company.com",
                                                                   "cc2@company.com",
                                                                   "cc3@company.com"),
                                                  ImmutableList.of("bcc1@company.com", "bcc2@company.com"),
                                                  ImmutableList.of("recipient@company.com"),
                                                  "body",
                                                  "from@company.com",
                                                  "host",
                                                  "username",
                                                  "password",
                                                  "subject",
                                                  "trust",
                                                  "file_path",
                                                  "file_name");

        MimeMessage mimeMessageMock = Mockito.mock(MimeMessage.class);

        emailSender.configurePlainTextMessage(mimeMessageMock);

        InOrder inOrder = Mockito.inOrder(mimeMessageMock);
        inOrder.verify(mimeMessageMock).addRecipients(eq(Message.RecipientType.CC), eq("cc1@company.com"));
        inOrder.verify(mimeMessageMock).addRecipients(eq(Message.RecipientType.CC), eq("cc2@company.com"));
        inOrder.verify(mimeMessageMock).addRecipients(eq(Message.RecipientType.CC), eq("cc3@company.com"));

        inOrder = Mockito.inOrder(mimeMessageMock);
        inOrder.verify(mimeMessageMock).addRecipients(eq(Message.RecipientType.BCC), eq("bcc1@company.com"));
        inOrder.verify(mimeMessageMock).addRecipients(eq(Message.RecipientType.BCC), eq("bcc2@company.com"));

        verify(mimeMessageMock).addRecipient(eq(Message.RecipientType.TO),
                                             eq(new InternetAddress("recipient@company.com")));

        verify(mimeMessageMock).addFrom(eq(new Address[] { new InternetAddress("from@company.com") }));

        verify(mimeMessageMock).setSubject(eq("subject"));
        verify(mimeMessageMock).setText(eq("body"));
    }

    @Test
    public void testConnectAndSendMessage() throws Exception {
        EmailSender emailSender = new EmailSender(false,
                                                  true,
                                                  true,
                                                  25,
                                                  ImmutableList.of("cc1@company.com",
                                                                   "cc2@company.com",
                                                                   "cc3@company.com"),
                                                  ImmutableList.of("bcc1@company.com", "bcc2@company.com"),
                                                  ImmutableList.of("recipient@company.com"),
                                                  "body",
                                                  "from@company.com",
                                                  "host",
                                                  "username",
                                                  "password",
                                                  "subject",
                                                  "trust",
                                                  "file_path",
                                                  "file_name");

        MimeMessage mimeMessageMock = Mockito.mock(MimeMessage.class);
        Transport transportMock = Mockito.mock(Transport.class);

        when(mimeMessageMock.getAllRecipients()).thenReturn(new Address[] { new InternetAddress("recipient@company.com") });

        emailSender.connectAndSendMessage(mimeMessageMock, transportMock);

        InOrder inOrder = Mockito.inOrder(transportMock);
        inOrder.verify(transportMock).connect(eq("username"), eq("password"));
        inOrder.verify(transportMock).sendMessage(mimeMessageMock, mimeMessageMock.getAllRecipients());
    }

}
