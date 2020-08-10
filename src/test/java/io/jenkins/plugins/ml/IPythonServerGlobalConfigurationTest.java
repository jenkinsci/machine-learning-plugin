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

import com.gargoylesoftware.htmlunit.html.*;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.List;

import static junit.framework.TestCase.assertEquals;

public class IPythonServerGlobalConfigurationTest {

    private HtmlForm form;
    private HtmlPage configPage;

    @Rule
    public final JenkinsRule j = new JenkinsRule();

    @Before
    public void setup() throws IOException, SAXException {
        configPage = j.createWebClient().goTo("configure");
        form = configPage.getFormByName("config");
        getButton(form, "Add new Kernel")
                .click();
        getButton(form, "Add new Kernel")
                .click();
    }

    @Test
    public void reloadConfigPageTest() throws Exception {

        List<HtmlInput> serverName = form.getInputsByName("_.serverName");
        serverName.get(0).setValueAttribute("localHost");
        serverName.get(1).setValueAttribute("IPython_Server");
        List<HtmlInput> kernel = form.getInputsByName("_.kernel");
        kernel.get(0).setValueAttribute("python");
        kernel.get(1).setValueAttribute("ir");
        List<HtmlInput> launchTimeout = form.getInputsByName("_.launchTimeout");
        launchTimeout.get(0).setValueAttribute("5");
        launchTimeout.get(1).setValueAttribute("1");
        List<HtmlInput> maxResults = form.getInputsByName("_.maxResults");
        maxResults.get(0).setValueAttribute("3");
        maxResults.get(1).setValueAttribute("1");

        j.submit(form);

        form = configPage.getFormByName("config");
        serverName = form.getInputsByName("_.serverName");
        kernel = form.getInputsByName("_.kernel");
        launchTimeout = form.getInputsByName("_.launchTimeout");
        maxResults= form.getInputsByName("_.maxResults");

        assertEquals("localHost", serverName.get(0).getValueAttribute());
        assertEquals("python", kernel.get(0).getValueAttribute());
        assertEquals("5", launchTimeout.get(0).getValueAttribute());
        assertEquals("3", maxResults.get(0).getValueAttribute());

        assertEquals("IPython_Server", serverName.get(1).getValueAttribute());
        assertEquals("ir", kernel.get(1).getValueAttribute());
        assertEquals("1", launchTimeout.get(1).getValueAttribute());
        assertEquals("1", maxResults.get(1).getValueAttribute());

        configPage.refresh();

        assertEquals("localHost", serverName.get(0).getValueAttribute());
        assertEquals("python", kernel.get(0).getValueAttribute());
        assertEquals("5", launchTimeout.get(0).getValueAttribute());
        assertEquals("3", maxResults.get(0).getValueAttribute());

        assertEquals("IPython_Server", serverName.get(1).getValueAttribute());
        assertEquals("ir", kernel.get(1).getValueAttribute());
        assertEquals("1", launchTimeout.get(1).getValueAttribute());
        assertEquals("1", maxResults.get(1).getValueAttribute());
    }

    /*
     * Helper function to get button with captions
     */
    public static HtmlButton getButton(HtmlForm form, String caption) {
        List<HtmlButton> buttons = form.getElementsByAttribute("button",
                "type", "button");
        HtmlButton rtn = null;
        for (HtmlButton button : buttons) {
            DomNode child = button.getFirstChild();
            if (caption.equals(child.getNodeValue())) {
                rtn = button;
                break;
            }
        }
        return rtn;
    }
}
