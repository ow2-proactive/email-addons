# Email Notification Addons

[![Build Status](http://jenkins.activeeon.com/buildStatus/icon?job=emailnotification-addons)](http://jenkins.activeeon.com/job/emailnotification-addons/)

This project provides an utility class but also a Java task that allow 
to send an email from a ProActive Workflow.

# Installation

Run the following Gradle command to generate JAR file:

```
gradle clean jar
```

It will generate a JAR file in `build/libs/emailnotification-addons-X.Y.Z-SNAPSHOT.jar`

Copy the JAR file in the `addons` folder of your ProActive installation.

# Usage

The email notification addons is included in the standard ProActive 
Workflows and Scheduling distribution starting from version _7.15.0_.

An email notification task example is available from the Studio Web 
application.

Please look at the documentation for setting up the configuration:

http://doc.activeeon.com/latest/admin/ProActiveAdminGuide.html#_email_notification

# Tests

Running integrations tests requires to define some environment variables:

| Environment variable | Purpose |
| -------------------- | ------- |
| GMAIL_EMAIL          | The email address associated to the Gmail account used by the integration tests |
| GMAIL_USERNAME       | The username associated to the Gmail account that is used |
| GMAIL_PASSWORD       | The password associated to the Gmail account that is used |
| OUTLOOK_EMAIL        | The email address associated to the Outlook account used by the integration tests |
| OUTLOOK_USERNAME     | The username associated to the Outlook account that is used |
| OUTLOOK_PASSWORD     | The password associated to the Outlook account that is used |
