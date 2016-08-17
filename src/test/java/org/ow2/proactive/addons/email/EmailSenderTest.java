package org.ow2.proactive.addons.email;

import java.io.Serializable;

import org.ow2.proactive.addons.email.exception.InvalidArgumentException;
import org.ow2.proactive.addons.email.exception.MissingArgumentException;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.junit.Test;

import static com.google.common.truth.Truth.assertThat;


/**
 * @author ActiveEon Team
 */
public class EmailSenderTest {

    /*
     * Check the mapping between fields passed as parameters and the instance fields.
     */
    @Test
    public void testConstructor() {
        EmailSender emailSender = new EmailSender(false, true, true, 25, ImmutableList.of("cc@company.com"),
            ImmutableList.of("bcc@company.com"), ImmutableList.of("recipients@company.com"), "body",
            "from@company.com", "host", "username", "password", "subject", "trust");

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
        new EmailSender(false, true, true, 25, ImmutableList.of("cc@company.com"),
            ImmutableList.of("bcc@company.com"), ImmutableList.of("recipients@company.com"), "body",
            "from@company.com", "host", "username", "password",
            "This is a really really really really long subject that will exceed the authorized length",
            "trust");
    }

    @Test(expected = MissingArgumentException.class)
    public void testNullFrom() {
        new EmailSender(false, true, true, 25, ImmutableList.of("cc@company.com"),
            ImmutableList.of("bcc@company.com"), ImmutableList.of("recipients@company.com"), "body", null,
            "host", "username", "password",
            "This is a really really really really long subject that will exceed the authorized length",
            "trust");
    }

    @Test(expected = MissingArgumentException.class)
    public void testNullSubject() {
        new EmailSender(false, true, true, 25, ImmutableList.of("cc@company.com"),
            ImmutableList.of("bcc@company.com"), ImmutableList.of("recipients@company.com"), "body",
            "from@company.com", "host", "username", "password", null, "trust");
    }

    @Test(expected = MissingArgumentException.class)
    public void testNullBody() {
        new EmailSender(false, true, true, 25, ImmutableList.of("cc@company.com"),
            ImmutableList.of("bcc@company.com"), ImmutableList.of("recipients@company.com"), null,
            "from@company.com", "host", "username", "password", null, "trust");
    }

    // TODO could test sendPlainTextEmail by mocking some internal objects

}
