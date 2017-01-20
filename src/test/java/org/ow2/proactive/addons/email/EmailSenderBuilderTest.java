package org.ow2.proactive.addons.email;

import static com.google.common.truth.Truth.assertThat;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.URI;
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

    @BeforeClass
    public static void setJobDescriptorcUri() throws Exception {
        EMAIL_CONFIGURATION = EmailSenderBuilderTest.class.getResource(
                "/org/ow2/proactive/addons/email/email.properties").toURI();
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
    }

    @Test
    public void testConstructorProperties() {

        final Properties properties = new Properties();
        try{
            InputStream fis = new FileInputStream(EMAIL_CONFIGURATION.getPath());
            properties.load(fis);
        }catch(IOException e){
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

}