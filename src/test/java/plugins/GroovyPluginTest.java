/*
 * The MIT License
 *
 * Copyright (c) 2014 Red Hat, Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package plugins;

import org.jenkinsci.test.acceptance.Matchers;
import org.jenkinsci.test.acceptance.junit.AbstractJUnitTest;
import org.jenkinsci.test.acceptance.junit.Native;
import org.jenkinsci.test.acceptance.junit.WithPlugins;
import org.jenkinsci.test.acceptance.plugins.groovy.GroovyInstallation;
import org.jenkinsci.test.acceptance.plugins.groovy.GroovyStep;
import org.jenkinsci.test.acceptance.plugins.groovy.SystemGroovyStep;
import org.jenkinsci.test.acceptance.po.FreeStyleJob;
import org.junit.Test;

@WithPlugins("groovy")
public class GroovyPluginTest extends AbstractJUnitTest {

    private FreeStyleJob job;

    @Test
    public void run_groovy() {
        configureJob();

        job.addBuildStep(GroovyStep.class).script(
                "println 'running groovy script';"
        );

        shouldReport("running groovy script");
    }

    @Test
    public void run_groovy_from_file() {
        configureJob();

        job.addShellStep("echo println \\'running groovy file\\' > script.groovy");
        job.addBuildStep(GroovyStep.class).file("script.groovy");

        shouldReport("running groovy file");
    }

    @Test
    public void run_system_groovy() {
        configureJob();

        job.addBuildStep(SystemGroovyStep.class).script(
                "job = jenkins.model.Jenkins.instance.getJob('my_job');" +
                "println \"name: ${job.displayName}. number: ${job.lastBuild.number}\""
        );

        shouldReport("name: my_job. number: 1");
    }

    @Test
    public void run_system_groovy_from_file() {
        configureJob();

        job.addShellStep("echo println \\'running groovy file\\' > script.groovy");
        job.addBuildStep(SystemGroovyStep.class).file("script.groovy");

        shouldReport("running groovy file");
    }

    @Test
    public void use_custom_groovy_version() {
        jenkins.configure();
        GroovyInstallation groovy = jenkins.getConfigPage().addTool(GroovyInstallation.class);
        groovy.name.set("groovy-2.2.1");
        groovy.installVersion("Groovy 2.2.1");
        jenkins.save();

        configureJob();

        final GroovyStep step = job.addBuildStep(GroovyStep.class);
        step.version.select("groovy-2.2.1");
        step.script(
                "println 'version: ' + groovy.lang.GroovySystem.getVersion()"
        );

        shouldReport("version: 2.2.1");
    }

    private void configureJob() {
        job = jenkins.jobs.create(FreeStyleJob.class, "my_job");
        job.configure();
    }

    private void shouldReport(String out) {
        job.save();
        job.queueBuild().shouldSucceed().shouldContainsConsoleOutput(out);
    }
}
