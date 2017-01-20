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
package org.ow2.proactive.addons.email.task;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.ow2.proactive.addons.email.EmailSender;
import org.ow2.proactive.scheduler.common.task.TaskResult;
import org.ow2.proactive.scheduler.common.task.executable.JavaExecutable;


/**
 * Java Task that allows to send plain text emails. It assumes that some third-party credentials
 * are configured for the user that runs the task, but also that email values are given as task
 * arguments.
 * <p>
 * WARNING: this Java task is working but issues may be encountered when used from the Studio Webapp.
 * Indeed this last, does not allow some characters as argument value (e.g. <). Besides, line breaks seem
 * automatically escaped. As a consequence, no workflow template using this task is integrated in the default
 * distributed. However, this task is kept since it was developed before the {@link EmailSender} builder and
 * could maybe be integrated once the Studio issues are fixed.
 *
 * @author ActiveEon Team
 */
public class EmailTask extends JavaExecutable {

    private EmailSender emailSender;

    public EmailTask() {

    }

    @Override
    public void init(Map<String, Serializable> args) throws Exception {
        Map options = new HashMap();
        options.putAll(execInitializer.getThirdPartyCredentials());
        options.putAll(args);

        EmailSender.Builder builder = new EmailSender.Builder(options);
        emailSender = builder.build();
    }

    @Override
    public Serializable execute(TaskResult... taskResults) throws Throwable {
        try {
            emailSender.sendPlainTextEmail();
            return "true";
        } catch (RuntimeException e) {
            e.printStackTrace();
            return "false";
        }
    }

}
