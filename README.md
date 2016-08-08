This project provide an utility class but also a Java task that allow to send
an email from a ProActive Workflow.

# Installation

Run the following Gradle command to generate JAR file:

```
gradle clean jar
```

It will generate a JAR file in `build/libs/email-addons-X.Y.Z-SNAPSHOT.jar`

Copy the JAR file in the `addons` folder of your ProActive installation.

# Usage

Two workflow examples that can be imported in the Studio Web portal are
available in the examples folder.

Both tasks assume that configuration values for connecting to an SMTP server
are entered as key/value pairs in the third-party credentials associated to
the user that runs the task.
[See the documentation for more information](http://doc.activeeon.com/latest/user/ProActiveUserGuide.html#_third_party_credentials).

Please note that sending emails from Google or Microsoft servers may require extra
configurations for authenticating with success:

  - https://www.google.com/settings/security/lesssecureapps
  - http://pchelp.ricmedia.com/how-to-fix-550-5-3-4-requested-action-not-taken-error/
