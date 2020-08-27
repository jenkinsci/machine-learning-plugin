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

import hudson.Extension;
import hudson.Util;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;
import hudson.util.FormValidation;
import org.apache.zeppelin.interpreter.InterpreterException;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.verb.POST;

import javax.annotation.Nonnull;
import java.util.logging.Level;
import java.util.regex.Pattern;

/**
 * The type Server.
 */
public class Server extends AbstractDescribableImpl<Server> {
    private final String serverName;
    private final String kernel;
    private final long launchTimeout;
    private final long maxResults;

    private static final Pattern pattern = Pattern.compile("^[a-zA-Z0-9_]+$");
    private static final java.util.logging.Logger GRPC_IO_LOGGER = java.util.logging.Logger.getLogger("io.grpc.internal");
    private static Level GRPC_IO_LOGGER_ORIGINAL_LEVEL = GRPC_IO_LOGGER.getLevel();
    /**
     * Instantiates a new Server.
     *
     * @param serverName    the server name
     * @param kernel        the kernel
     * @param launchTimeout the launch timeout
     * @param maxResults    the max results
     */
    @DataBoundConstructor
    public Server(String serverName, String kernel, long launchTimeout, long maxResults) {
        this.serverName = serverName;
        this.kernel = kernel;
        this.launchTimeout = launchTimeout;
        this.maxResults = maxResults;
    }

    /**
     * Gets server name.
     *
     * @return the server name
     */
    public String getServerName() {
        return serverName;
    }

    /**
     * Gets launch timeout.
     *
     * @return the launch timeout
     */
    public long getLaunchTimeout() {
        return launchTimeout;
    }

    /**
     * Gets launch timeout in milli seconds.
     *
     * @return the launch timeout in milli seconds
     */
    public long getLaunchTimeoutInMilliSeconds() {
        return launchTimeout * 1000;
    }

    /**
     * Gets max results.
     *
     * @return the max results
     */
    public long getMaxResults() {
        return maxResults;
    }

    /**
     * Gets kernel.
     *
     * @return the kernel
     */
    public String getKernel() {
        return kernel;
    }

    @Override
    public Descriptor<Server> getDescriptor() {
        return null;
    }

    /**
     * The type Descriptor.
     */
    @Extension
    public static class DescriptorImpl extends Descriptor<Server> {
        @Nonnull
        @Override
        public String getDisplayName() {
            return "Server";
        }

        /**
         * Do check server name form validation.
         *
         * @param serverName the server name
         * @return the form validation
         */
        public FormValidation doCheckServerName(@QueryParameter String serverName) {
            if( Util.fixEmptyAndTrim(serverName) == null){
                return FormValidation.warning("* Optional ");
            }

            if( pattern.matcher(serverName).matches() ){
                return FormValidation.ok();
            }else{
                return FormValidation.warning(" Try any valid name", serverName);
            }
        }

        /**
         * Do check launch timeout form validation.
         *
         * @param launchTimeout the launch timeout
         * @return the form validation
         */
        public FormValidation doCheckLaunchTimeout(@QueryParameter String launchTimeout) {
            try {
                Integer num = Integer.valueOf(launchTimeout);
                if (num >= 0) {
                    return FormValidation.ok();
                }
            } catch (Exception e) {
                return FormValidation.error("Timeout should be a valid number ");
            }
            return FormValidation.ok();
        }

        /**
         * Do check max results form validation.
         *
         * @param maxResults the max results
         * @return the form validation
         */
        public FormValidation doCheckMaxResults(@QueryParameter String maxResults) {
            try{
                Integer num = Integer.valueOf(maxResults);
                if(num >= 1){
                    return FormValidation.ok();
                }
            }catch (Exception e){
                return FormValidation.error("Max results should be a valid number ");
            }
            return FormValidation.ok();
        }

        /**
         * Validate the server by testing it
         *
         * @param kernel        the kernel
         * @param launchTimeout the launch timeout
         * @param maxResults    the max results
         * @return the form validation
         * @throws Exception throws NumberFormatException when passing invalid maxResults
         */
        @POST
        public FormValidation doValidate(
                @QueryParameter String kernel,
                @QueryParameter String launchTimeout,
                @QueryParameter String maxResults)
                throws Exception {

            if (Util.fixEmptyAndTrim(kernel) != null) {
                try{
                    // Disable GRPC internal exceptions due to SEVERE log message when closing kernel interpreter
                    GRPC_IO_LOGGER.setLevel(Level.OFF);
                    IPythonUserConfig userConfig = new IPythonUserConfig(kernel, Integer.parseInt(launchTimeout)*1000, Integer.parseInt(maxResults), ".");
                    try (InterpreterManager interpreterManager = new IPythonInterpreterManager(userConfig)) {
                        interpreterManager.initiateInterpreter();
                        if (interpreterManager.testConnection()) {
                            return FormValidation.ok("Connection Successful");
                        } else {
                            return FormValidation.error("Connection failed");
                        }
                    } catch (InterpreterException exception) {
                        return FormValidation.error("No " + kernel + " kernel available");
                    }
                } catch (NumberFormatException ex){
                    return FormValidation.error("Number/s is/are not valid ");
                }finally {
                    GRPC_IO_LOGGER.setLevel(GRPC_IO_LOGGER_ORIGINAL_LEVEL);
                }
            }
            return FormValidation.warning("kernel name is required. Click on help for more info");
        }
    }


}
