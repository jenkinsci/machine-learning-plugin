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

import com.cloudbees.hudson.plugins.folder.AbstractFolder;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import hudson.*;
import hudson.model.AbstractProject;
import hudson.model.Result;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import io.jenkins.plugins.ml.utils.ConvertHelper;
import jenkins.security.MasterToSlaveCallable;
import jenkins.tasks.SimpleBuildStep;
import org.apache.zeppelin.interpreter.InterpreterException;
import org.apache.zeppelin.jupyter.zformat.Note;
import org.apache.zeppelin.jupyter.zformat.Paragraph;
import org.jenkinsci.Symbol;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.NoExternalUse;
import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.util.List;
import java.util.stream.Stream;

public class IPythonBuilder extends Builder implements SimpleBuildStep, Serializable {

    private static final long serialVersionUID = 1L;

    private static final Logger LOGGER = LoggerFactory.getLogger(IPythonBuilder.class);

    private final String code;
    private final String filePath;
    private final String parserType;
    private final String task;
    private final String kernelName;

    @DataBoundConstructor
    public IPythonBuilder(String code, String filePath, String parserType, String task, String kernelName) {
        this.code = code;
        this.filePath = Util.fixEmptyAndTrim(filePath);
        this.parserType = parserType;
        this.task = task;
        kernelName = Util.fixEmptyAndTrim(kernelName);
        if (kernelName == null) {
            // defaults to the first one
            List<Server> sites = IPythonGlobalConfiguration.get().getServers();
            if (!sites.isEmpty()) kernelName = sites.get(0).getKernel();
        }
        this.kernelName = kernelName;
    }

    @Override
    @SuppressFBWarnings("NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE")
    public void perform(@Nonnull Run<?, ?> run, @Nonnull FilePath ws, @Nonnull Launcher launcher, @Nonnull TaskListener listener) throws AbortException {
        try {

            // get the properties of the job
            String serverName = getServer().getServerName();
            String kernel = getServer().getKernel();
            long launchTimeout = getServer().getLaunchTimeoutInMilliSeconds();
            long maxResults = getServer().getMaxResults();
            listener.getLogger().println("Executed kernel : " + kernel.toUpperCase());
            listener.getLogger().println("Language : " + serverName.toUpperCase());
            // create configuration
            IPythonUserConfig jobUserConfig = new IPythonUserConfig(kernel, launchTimeout, maxResults);
            // Get the right channel to execute the code
            run.setResult(launcher.getChannel().call(new ExecutorImpl(ws, listener, jobUserConfig)));

        } catch (Throwable e) {
            e.printStackTrace(listener.getLogger());
            throw  new AbortException(e.getMessage());
        }
    }

    @Nullable
    private Server getServer() {
        List<Server> sites = IPythonGlobalConfiguration.get().getServers();

        if (kernelName == null && sites.size() > 0) {
            // default
            return sites.get(0);
        }
        Stream<Server> streams = sites.stream();
        return streams.filter(Server -> Server.getKernel().equals(kernelName))
                .findFirst().orElse(null);
    }

    public String getCode() {
        return code;
    }
    public String getFilePath() {
        return filePath;
    }

    @CheckForNull
    public String getParserType() {
        return parserType;
    }

    @CheckForNull
    public String getTask() {
        return task;
    }

    public String getKernelName() {
        return kernelName;
    }

    public boolean isText() {
        return parserType.equals("text");
    }
    enum FileExtension {
        ipynb,
        json,
        txt
    }

    @Symbol("ipythonBuilder")
    @Extension
    public static final class DescriptorImpl extends BuildStepDescriptor<Builder> {

        public FormValidation doCheckCode(@QueryParameter String code) {
            if (Util.fixEmptyAndTrim(code) == null)
                return FormValidation.error("Code is empty");
            return FormValidation.ok();
        }

        public FormValidation doCheckFilePath(@QueryParameter String filePath) {
            if (Util.fixEmptyAndTrim(filePath) == null)
                return FormValidation.warning("The file path is required to execute");
            return FormValidation.ok();
        }

        public FormValidation doCheckTask(@QueryParameter String task) {
            if (Util.fixEmptyAndTrim(task) == null)
                return FormValidation.warning("Task name is required to save the artifacts");
            return FormValidation.ok();
        }

        public ListBoxModel doFillKernelNameItems(@AncestorInPath AbstractFolder<?> folder) {
            ListBoxModel items = new ListBoxModel();
            for (Server site : IPythonGlobalConfiguration.get().getServers()) {
                items.add(site.getKernel());
            }
            if (folder != null) {
                List<Server> serversFromFolder = ServerFolderProperty.getServersFromFolders(folder);
                serversFromFolder.stream().map(Server::getKernel).forEach(items::add);
            }
            return items;
        }

        @Override
        public boolean isApplicable(Class<? extends AbstractProject> aClass) {
            return true;
        }

        @Override
        @Nonnull
        public String getDisplayName() {
            return "IPython Builder";
        }

    }

    @Restricted(NoExternalUse.class)
    private final class ExecutorImpl extends MasterToSlaveCallable<Result, Exception> {

        private FilePath ws;
        private TaskListener listener;
        private IPythonUserConfig jobUserConfig;

        private ExecutorImpl(FilePath ws, TaskListener ls, IPythonUserConfig cf) {
            this.ws = ws;
            this.listener = ls;
            this.jobUserConfig = cf;
        }

        @Override
        public Result call() {

            try (IPythonInterpreterManager interpreterManager = new IPythonInterpreterManager(jobUserConfig)) {
                interpreterManager.initiateInterpreter();
                LOGGER.info("Connection initiated successfully");
                listener.getLogger().println("Platform : " + System.getProperty("os.name").toUpperCase());
                listener.getLogger().println("Type : " + parserType.toUpperCase());
                listener.getLogger().println("Working directory : " + ws.getRemote());
                // Change working directory as workspace directory
                interpreterManager.invokeInterpreter("import os\nos.chdir('" + ws.getRemote() + "')", "test", ws);
                if (parserType.equals("text")) {
                    listener.getLogger().println(interpreterManager.invokeInterpreter(code, task, ws));
                } else {
                    if (Util.fixEmptyAndTrim(filePath) != null) {
                        // Run builder on selected notebook
                        String extension = filePath.substring(filePath.lastIndexOf(".") + 1);
                        FileExtension ext;
                        try {
                            // assign the extension from the enum
                            ext = FileExtension.valueOf(extension);
                        } catch (Exception e) {
                            ext = FileExtension.txt;
                        }
                        // create file path for the file
                        FilePath tempFilePath = ws.child(filePath);
                        listener.getLogger().println("Output : ");
                        switch (ext) {
                            case ipynb:
                                listener.getLogger().println((interpreterManager.invokeInterpreter(ConvertHelper.jupyterToText(tempFilePath), task, ws)));
                                break;
                            case json:
                                // Zeppelin note book or JSON file will be interpreted line by line
                                try (final InputStreamReader inputStreamReader = new InputStreamReader(tempFilePath.read(), Charset.forName("UTF-8"))) {
                                    Gson gson = new GsonBuilder().create();
                                    Note n = gson.fromJson(inputStreamReader, Note.class);
                                    for (Paragraph para : n.getParagraphs()) {
                                        // skipping markdowns
                                        if (para.getConfig().get("editorMode").equals("ace/mode/markdown")) {
                                            continue;
                                        }
                                        String code = para.getText();
                                        listener.getLogger().println(code);
                                        listener.getLogger().println(interpreterManager.invokeInterpreter(code, task, ws));
                                    }
                                }
                                break;
                            default:
                                listener.getLogger().println(interpreterManager.invokeInterpreter(tempFilePath.readToString(), task, ws));
                                return Result.SUCCESS;
                        }
                    } else {
                        listener.fatalError("The file path is empty");
                        return Result.FAILURE;
                    }
                }

            } catch (InterruptedException | InterpreterException | IOException e) {
                e.printStackTrace(listener.getLogger());
                return Result.FAILURE;
            }
            return Result.SUCCESS;
        }
    }
}
