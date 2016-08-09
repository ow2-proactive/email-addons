This project provide an utility class but also a Java task that allow to send
an email from a ProActive Workflow.

# Installation

Run the following Gradle command to generate JAR file:

```
gradle clean jar
```

It will generate a JAR file in `build/libs/emailnotification-addons-X.Y.Z-SNAPSHOT.jar`

Copy the JAR file in the `addons` folder of your ProActive installation.

# Usage

Two workflow examples that can be imported in the Studio Web portal are
available in the examples folder.

# Third party credentials

Both tasks assume that configuration values for connecting to an SMTP server
are entered as key/value pairs in the third-party credentials associated to
the user that runs the task.
[See the documentation for more information](http://doc.activeeon.com/latest/user/ProActiveUserGuide.html#_third_party_credentials).

This configuration can be achieved from the Scheduler Web portal, the 
ProActive client or even through the REST API.

## Examples

## Free

| Key  | Value |
| ------------------- | ------------- |
| mail.smtp.host      | smtp.free.fr  |
| mail.smtp.username  | user@free.fr  |
| mail.smtp.password  | user_password |

## Gmail

Password authentication to Google servers requires extra configuration. 
See https://www.google.com/settings/security/lesssecureapps.

| Key  | Value |
| ------------------------- | --------------- |
| mail.smtp.host            | smtp.gmail.com  |
| mail.smtp.starttls.enable | true            |
| mail.smtp.ssl.trust       | smtp.gmail.com  |
| mail.smtp.username        | user@gmail.com  |
| mail.smtp.password        | user_password   |

## Outlook

Password authentication to Microsoft servers requires extra configuration. 
See http://pchelp.ricmedia.com/how-to-fix-550-5-3-4-requested-action-not-taken-error/

| Key  | Value |
| ------------------------- | --------------- |
| mail.smtp.host            | smtp.gmail.com  |
| mail.smtp.starttls.enable | true            |
| mail.smtp.ssl.trust       | smtp.gmail.com  |
| mail.smtp.username        | user@gmail.com  |
| mail.smtp.password        | user_password   |

