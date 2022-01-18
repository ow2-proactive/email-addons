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

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.common.collect.ImmutableMap;


/**
 * @author ActiveEon Team
 */
public class EmailSenderBuilderTest {

    private static URI EMAIL_CONFIGURATION;

    private static final String EXTRA_PROPERTY_MAIL_SMTP_SSL_ENABLE = "mail.smtp.ssl.enable";

    private static final String EXTRA_PROPERTY_MAIL_SMTP_CONNECTIONTIMEOUT = "mail.smtp.connectiontimeout";

    @BeforeClass
    public static void setJobDescriptorcUri() throws Exception {
        EMAIL_CONFIGURATION = EmailSenderBuilderTest.class.getResource("/org/ow2/proactive/addons/email/email.properties")
                                                          .toURI();
    }

    @Test
    public void testDefaultValues() {
        EmailSender.Builder builder = new EmailSender.Builder();

        assertThat(builder.isAuthEnabled()).isTrue();
        assertThat(builder.isDebugEnabled()).isFalse();
        assertThat(builder.isStartTlsEnabled()).isFalse();

        assertThat(builder.getPort()).isEqualTo(587);

        assertThat(builder.getCc()).hasSize(0);
        assertThat(builder.getBcc()).hasSize(0);
        assertThat(builder.getRecipients()).hasSize(0);

        assertThat(builder.getBody()).isEmpty();
        assertThat(builder.getFrom()).isNull();
        assertThat(builder.getHost()).isNull();
        assertThat(builder.getUsername()).isNull();
        assertThat(builder.getPassword()).isNull();
        assertThat(builder.getSubject()).isNull();
        assertThat(builder.getTrustSsl()).isEqualTo("*");
        assertThat(builder.getFileToAttach()).isNull();
        assertThat(builder.getFileName()).isEqualTo("attachment.txt");
    }

    @Test
    public void testConstructor() {

        ImmutableMap.Builder<String, Serializable> config = ImmutableMap.builder();

        config.put(EmailSender.ARG_FROM, "from");
        config.put(EmailSender.ARG_RECIPIENTS, "a,b,c");
        config.put(EmailSender.ARG_CC, "d, e");
        config.put(EmailSender.ARG_BCC, "f, g,h, i");
        config.put(EmailSender.ARG_SUBJECT, "subject");
        config.put(EmailSender.ARG_BODY, "body");
        config.put(EmailSender.ARG_FILETOATTACH, "file_path");
        config.put(EmailSender.ARG_FILENAME, "file_name");

        config.put(EmailSender.PROPERTY_MAIL_DEBUG, "true");
        config.put(EmailSender.PROPERTY_MAIL_SMTP_HOST, "smtp.host.com");
        config.put(EmailSender.PROPERTY_MAIL_SMTP_PORT, "25");
        config.put(EmailSender.PROPERTY_MAIL_SMTP_USERNAME, "username");
        config.put(EmailSender.PROPERTY_MAIL_SMTP_PASSWORD, "password");
        config.put(EmailSender.PROPERTY_MAIL_SMTP_AUTH, "true");
        config.put(EmailSender.PROPERTY_MAIL_SMTP_STARTTLS_ENABLE, "true");
        config.put(EmailSender.PROPERTY_MAIL_SMTP_SSL_TRUST, "all");

        EmailSender.Builder builder = new EmailSender.Builder(config.build());

        assertThat(builder.getFrom()).isEqualTo("from");
        assertThat(builder.getRecipients()).containsExactly("a", "b", "c");
        assertThat(builder.getCc()).containsExactly("d", "e");
        assertThat(builder.getBcc()).containsExactly("f", "g", "h", "i");
        assertThat(builder.getSubject()).isEqualTo("subject");
        assertThat(builder.getBody()).isEqualTo("body");

        assertThat(builder.isDebugEnabled()).isTrue();
        assertThat(builder.getHost()).isEqualTo("smtp.host.com");
        assertThat(builder.getPort()).isEqualTo(25);
        assertThat(builder.getUsername()).isEqualTo("username");
        assertThat(builder.getPassword()).isEqualTo("password");
        assertThat(builder.isAuthEnabled()).isTrue();
        assertThat(builder.isStartTlsEnabled()).isTrue();
        assertThat(builder.getTrustSsl()).isEqualTo("all");
        assertThat(builder.getFileToAttach()).isEqualTo("file_path");
        assertThat(builder.getFileName()).isEqualTo("file_name");
    }

    @Test
    public void testConstructorProperties() {

        final Properties properties = new Properties();
        try {
            InputStream fis = new FileInputStream(EMAIL_CONFIGURATION.getPath());
            properties.load(fis);
        } catch (IOException e) {
            Assert.fail(e.getMessage());
        }

        EmailSender.Builder builder = new EmailSender.Builder(properties);

        assertThat(builder.getHost()).isEqualTo("smtp.gmail.com");
        assertThat(builder.getPort()).isEqualTo(587);
        assertThat(builder.getUsername()).isEqualTo("example@username.com");
        assertThat(builder.getPassword()).isEqualTo("password");
        assertThat(builder.isAuthEnabled()).isTrue();
        assertThat(builder.isStartTlsEnabled()).isTrue();
    }

    @Test
    public void testExtraPropertiesAreSavedUsingWither() {
        ImmutableMap.Builder<String, Serializable> config = ImmutableMap.builder();

        config.put(EmailSender.ARG_FROM, "from");
        config.put(EmailSender.ARG_RECIPIENTS, "a,b,c");
        config.put(EmailSender.ARG_CC, "d, e");
        config.put(EmailSender.ARG_BCC, "f, g,h, i");
        config.put(EmailSender.ARG_SUBJECT, "subject");
        config.put(EmailSender.ARG_BODY, "body");
        config.put(EmailSender.ARG_FILETOATTACH, "file_path");
        config.put(EmailSender.ARG_FILENAME, "file_name");

        config.put(EmailSender.PROPERTY_MAIL_DEBUG, "true");
        config.put(EmailSender.PROPERTY_MAIL_SMTP_HOST, "smtp.host.com");
        config.put(EmailSender.PROPERTY_MAIL_SMTP_PORT, "25");
        config.put(EmailSender.PROPERTY_MAIL_SMTP_USERNAME, "username");
        config.put(EmailSender.PROPERTY_MAIL_SMTP_PASSWORD, "password");
        config.put(EmailSender.PROPERTY_MAIL_SMTP_AUTH, "true");
        config.put(EmailSender.PROPERTY_MAIL_SMTP_STARTTLS_ENABLE, "true");
        config.put(EmailSender.PROPERTY_MAIL_SMTP_SSL_TRUST, "all");

        EmailSender.Builder builder = new EmailSender.Builder(config.build());

        assertThat(builder.getFrom()).isEqualTo("from");
        assertThat(builder.getRecipients()).containsExactly("a", "b", "c");
        assertThat(builder.getCc()).containsExactly("d", "e");
        assertThat(builder.getBcc()).containsExactly("f", "g", "h", "i");
        assertThat(builder.getSubject()).isEqualTo("subject");
        assertThat(builder.getBody()).isEqualTo("body");

        assertThat(builder.isDebugEnabled()).isTrue();
        assertThat(builder.getHost()).isEqualTo("smtp.host.com");
        assertThat(builder.getPort()).isEqualTo(25);
        assertThat(builder.getUsername()).isEqualTo("username");
        assertThat(builder.getPassword()).isEqualTo("password");
        assertThat(builder.isAuthEnabled()).isTrue();
        assertThat(builder.isStartTlsEnabled()).isTrue();
        assertThat(builder.getTrustSsl()).isEqualTo("all");
        assertThat(builder.getFileToAttach()).isEqualTo("file_path");
        assertThat(builder.getFileName()).isEqualTo("file_name");
        assertThat(builder.getProperties().size()).isEqualTo(8);
    }

    @Test
    public void testExtraPropertiesWitherDoesNotReinitialize() {
        ImmutableMap.Builder<String, Serializable> config = ImmutableMap.builder();

        config.put(EmailSender.ARG_FROM, "from");
        config.put(EmailSender.ARG_RECIPIENTS, "a,b,c");
        config.put(EmailSender.ARG_CC, "d, e");
        config.put(EmailSender.ARG_BCC, "f, g,h, i");
        config.put(EmailSender.ARG_SUBJECT, "subject");
        config.put(EmailSender.ARG_BODY, "body");
        config.put(EmailSender.ARG_FILETOATTACH, "file_path");
        config.put(EmailSender.ARG_FILENAME, "file_name");

        config.put(EmailSender.PROPERTY_MAIL_DEBUG, "true");
        config.put(EmailSender.PROPERTY_MAIL_SMTP_HOST, "smtp.host.com");
        config.put(EmailSender.PROPERTY_MAIL_SMTP_PORT, "25");
        config.put(EmailSender.PROPERTY_MAIL_SMTP_USERNAME, "username");
        config.put(EmailSender.PROPERTY_MAIL_SMTP_PASSWORD, "password");
        config.put(EmailSender.PROPERTY_MAIL_SMTP_AUTH, "true");
        config.put(EmailSender.PROPERTY_MAIL_SMTP_STARTTLS_ENABLE, "true");
        config.put(EmailSender.PROPERTY_MAIL_SMTP_SSL_TRUST, "all");

        config.put(EXTRA_PROPERTY_MAIL_SMTP_CONNECTIONTIMEOUT, "120000");

        EmailSender.Builder builder = new EmailSender.Builder(config.build());
        assertThat(builder.getProperties().size()).isEqualTo(1);
        assertThat(builder.getProperties().get(EXTRA_PROPERTY_MAIL_SMTP_CONNECTIONTIMEOUT)).isNotNull();

        ImmutableMap.Builder<String, Serializable> extraProps = ImmutableMap.builder();
        extraProps.put(EXTRA_PROPERTY_MAIL_SMTP_SSL_ENABLE, "true");
        extraProps.put(EXTRA_PROPERTY_MAIL_SMTP_CONNECTIONTIMEOUT, "120000");
        builder.withProperties(extraProps.build());

        assertThat(builder.getProperties()).hasSize(10);
        assertThat(builder.getProperties()).containsEntry(EXTRA_PROPERTY_MAIL_SMTP_SSL_ENABLE, "true");
        assertThat(builder.getProperties()).containsEntry(EXTRA_PROPERTY_MAIL_SMTP_CONNECTIONTIMEOUT, "120000");
    }
}
