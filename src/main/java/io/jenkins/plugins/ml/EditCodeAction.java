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

import com.google.common.collect.ImmutableList;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import hudson.FilePath;
import hudson.model.*;
import jenkins.model.ParameterizedJobMixIn;
import jenkins.model.RunAction2;
import jenkins.scm.api.SCMRevisionAction;
import net.sf.json.JSONObject;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.DoNotUse;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.kohsuke.stapler.verb.POST;

import javax.annotation.CheckForNull;
import javax.servlet.ServletException;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class EditCodeAction implements RunAction2 {

    private static final Iterable<Class<? extends Action>> COPIED_ACTIONS = ImmutableList.of(
            ParametersAction.class,
            SCMRevisionAction.class
    );
    private transient Run run;
    private transient FilePath filePath;
    private final static transient String CODE_MIRROR_MODE = "javascript";

    public EditCodeAction(Run run, FilePath filePath) {
        this.run = run;
        this.filePath = filePath;
    }

    @CheckForNull
    @Override
    public String getIconFileName() {
        return "document.png";
    }

    @CheckForNull
    @Override
    public String getDisplayName() {
        return "Edit " + filePath.getName();
    }

    @CheckForNull
    @Override
    public String getUrlName() {
        return "edit_" + filePath.getName();
    }

    /* accessible to Jelly */
    public boolean isRebuildEnabled() {
        return run.hasPermission(Item.BUILD);
    }

    @Restricted(DoNotUse.class)
    @POST
    public void doRun(StaplerRequest req, StaplerResponse rsp) throws ServletException, IOException, InterruptedException {
        JSONObject form = req.getSubmittedForm();
        filePath.write(form.getString("code"), "UTF-8");
        if (isRebuildEnabled()) {
            run2();
        }
        rsp.sendRedirect("../.."); // back to Job
    }

    public @CheckForNull
    Queue.Item run2() {
        List<Action> actions = new ArrayList<>();

        for (Class<? extends Action> c : COPIED_ACTIONS) {
            actions.addAll(run.getActions(c));
        }
        return ParameterizedJobMixIn.scheduleBuild2(run.getParent(), 0, actions.toArray(new Action[0]));
    }

    @Override
    public void onAttached(Run<?, ?> run) {
        this.run = run;
    }

    @Override
    public void onLoad(Run<?, ?> run) {
        this.run = run;
    }

    /* accessible by jelly */
    @SuppressFBWarnings("RCN_REDUNDANT_NULLCHECK_WOULD_HAVE_BEEN_A_NPE")
    public String getCode() throws IOException {
        StringBuilder code = new StringBuilder();
        try (final BufferedReader br = Files.newBufferedReader(Paths.get(filePath.getRemote()))) {
            String line;
            while ((line = br.readLine()) != null) {
                code.append(line).append("\n");
            }
        }
        return code.toString();

    }

    public String getCodeMirrorMode() {
        return CODE_MIRROR_MODE;
    }
}
