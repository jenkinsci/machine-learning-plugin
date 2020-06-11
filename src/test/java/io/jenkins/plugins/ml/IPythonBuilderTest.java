/*
 * The MIT License
 *
 * Copyright 2020 Loghi Perinpanayagam.
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


package io.jenkins.plugins.ml;

import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlInput;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import hudson.model.FreeStyleProject;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

import java.util.List;

import static org.junit.Assert.assertNotNull;

public class IPythonBuilderTest {

    private HtmlForm form;
    private HtmlPage configPage;
    private FreeStyleProject project;

    @Rule
    public JenkinsRule jenkins = new JenkinsRule();

    @Before
    public void createMocks() throws Exception {
        configPage = jenkins.createWebClient().goTo("configure");
        project = jenkins.createFreeStyleProject();
        form = configPage.getFormByName("config");
        IPythonServerGlobalConfigurationTest.getButton(form, "Add new Server")
                .click();
        List<HtmlInput> serverName = form.getInputsByName("_.serverName");
        serverName.get(0).setValueAttribute("localHost");
        List<HtmlInput> serverAddress = form.getInputsByName("_.serverAddress");
        serverAddress.get(0).setValueAttribute("127.0.0.1");
        List<HtmlInput> launchTimeout = form.getInputsByName("_.launchTimeout");
        launchTimeout.get(0).setValueAttribute("5");
        List<HtmlInput> maxResults = form.getInputsByName("_.maxResults");
        maxResults.get(0).setValueAttribute("3");
        // submit the global configurations
        jenkins.submit(form);

    }
    @Test
    public void testAdditionBuild() throws Exception {

        ServerJobProperty ijj = new ServerJobProperty("localHost");
        assertNotNull("Job property is null",ijj);
        project.addProperty(ijj);
        ServerJobProperty jobProp = project.getProperty(ServerJobProperty.class);
        assertNotNull(jobProp);

        /* Builder test
        String name = "%text 37";
        IPythonBuilder builder = new IPythonBuilder("32+5",null,"text");
        project.getBuildersList().add(builder);
        FreeStyleBuild build = jenkins.buildAndAssertSuccess(project);
        jenkins.assertLogContains( name, build);
        */
    }
}
