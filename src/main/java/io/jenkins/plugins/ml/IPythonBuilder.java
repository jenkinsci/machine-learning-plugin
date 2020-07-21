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

import com.google.gson.*;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import hudson.*;
import hudson.model.AbstractProject;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import hudson.util.FormValidation;
import io.jenkins.plugins.ml.utils.ConvertHelper;
import jenkins.security.MasterToSlaveCallable;
import jenkins.tasks.SimpleBuildStep;
import org.apache.commons.lang3.StringUtils;
import org.apache.zeppelin.interpreter.InterpreterException;
import org.apache.zeppelin.jupyter.zformat.Note;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.nio.charset.Charset;

public class IPythonBuilder extends Builder implements SimpleBuildStep, Serializable {

    private static final long serialVersionUID = 1L;

    private static final Logger LOGGER = LoggerFactory.getLogger(IPythonBuilder.class);

    private final String code;
    private final String filePath;
    private final String parserType;

    @DataBoundConstructor
    public IPythonBuilder(String code,String filePath, String parserType) {
        this.code = code;
        this.filePath = Util.fixEmptyAndTrim(filePath);
        this.parserType = parserType;
    }

    @Override
    @SuppressFBWarnings("NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE")
    public void perform(@Nonnull Run<?, ?> run, @Nonnull FilePath ws, @Nonnull Launcher launcher, @Nonnull TaskListener listener) throws AbortException {
        try {
            // get the properties of the job
            ServerJobProperty ipythonServerJobProperty = run.getParent().getProperty(ServerJobProperty.class);
            String serverName = ipythonServerJobProperty.getServer().getServerName();
            String serverAddress = ipythonServerJobProperty.getServer().getServerAddress();
            long launchTimeout = ipythonServerJobProperty.getServer().getLaunchTimeoutInMilliSeconds();
            long maxResults = ipythonServerJobProperty.getServer().getMaxResults();
            listener.getLogger().println("Executed server : " + serverName);
            // Get the right channel to execute the code
            launcher.getChannel().call(new MasterToSlaveCallable<Void, Exception>() {

                private static final long serialVersionUID = 1791985298575049757L;
                @Override
                public Void call() {
                    // create configuration
                    IPythonUserConfig jobUserConfig = new IPythonUserConfig(serverAddress,launchTimeout,maxResults);
                    try ( IPythonInterpreterManager interpreterManager = new IPythonInterpreterManager(jobUserConfig)){
                        interpreterManager.initiateInterpreter();
                        LOGGER.info("Connection initiated successfully");
                        listener.getLogger().println("Code Parser type : " + parserType);
                        listener.getLogger().println("Executed code output : ");
                        if(parserType.equals("text")){
                            listener.getLogger().println(interpreterManager.invokeInterpreter(code));
                        } else {

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
                            run.addAction(new EditCodeAction(run, tempFilePath));
                            switch (ext) {
                                case ipynb:
                                    listener.getLogger().println(StringUtils.stripStart(interpreterManager.invokeInterpreter(ConvertHelper.jupyterToText(tempFilePath)), "%text"));
                                    break;
                                case json:
                                    // Zeppelin note book or JSON file will be interpreted line by line
                                    try (final InputStreamReader inputStreamReader = new InputStreamReader(tempFilePath.read(), Charset.forName("UTF-8"))) {
                                        Gson gson = new GsonBuilder().create();
                                        Note n = gson.fromJson(inputStreamReader, Note.class);
                                        JsonObject obj = gson.toJsonTree(n).getAsJsonObject();
                                        JsonArray array = obj.get("paragraphs").getAsJsonArray();
                                        for (JsonElement element : array)
                                            if (element.isJsonObject()) {
                                                // get each cell form the JSON element
                                                JsonObject cell = element.getAsJsonObject();
                                                String code = cell.get("text").getAsString();
                                                listener.getLogger().println(code);
                                                listener.getLogger().println(StringUtils.stripStart(interpreterManager.invokeInterpreter(code), "%text"));
                                            }
                                    }
                                    break;
                                case py:
                                    listener.getLogger().println(StringUtils.stripStart(interpreterManager.invokeInterpreter(tempFilePath.readToString()), "%text"));
                                    break;
                                default:
                                    listener.getLogger().println("File is not supported by the machine learning plugin");
                            }
                        }

                    } catch (InterruptedException | InterpreterException | IOException e) {
                        e.printStackTrace(listener.getLogger());
                    }

                    return null;
                }
            });

        } catch (Throwable e) {
            e.printStackTrace(listener.getLogger());
            throw  new AbortException(e.getMessage());

        }
    }

    public String getCode() {
        return code;
    }

    public String getFilePath() {
        return filePath;
    }

    enum FileExtension {
        ipynb,
        json,
        py,
        txt
    }

    @Symbol("ipythonBuilder")
    @Extension
    public static final class DescriptorImpl extends BuildStepDescriptor<Builder> {

        public FormValidation doCheckCode(@QueryParameter String value) {
            if (Util.fixEmptyAndTrim(value) == null)
                return FormValidation.error("Code is empty");
            return FormValidation.ok();
        }

        public FormValidation doCheckFilePath(@QueryParameter String filePath) {
            if (Util.fixEmptyAndTrim(filePath) == null)
                return FormValidation.warning("The file path is required to execute");
            return FormValidation.ok();
        }

        @Override
        public boolean isApplicable(Class<? extends AbstractProject> aClass) {
            return true;
        }

        @Override
        public String getDisplayName() {
            return "IPython Builder";
        }

    }
}
