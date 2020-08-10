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
import java.util.regex.Pattern;

public class Server extends AbstractDescribableImpl<Server> {
    private final String serverName;
    private final String kernel;
    private final long launchTimeout;
    private final long maxResults;

    private static final Pattern pattern = Pattern.compile("^[a-zA-Z0-9_]+$");

    @DataBoundConstructor
    public Server(String serverName, String kernel, long launchTimeout, long maxResults) {
        this.serverName = serverName;
        this.kernel = kernel;
        this.launchTimeout = launchTimeout;
        this.maxResults = maxResults;
    }

    public String getServerName() {
        return serverName;
    }

    public long getLaunchTimeout() {
        return launchTimeout;
    }

    public long getLaunchTimeoutInMilliSeconds() {
        return launchTimeout*1000;
    }

    public long getMaxResults() {
        return maxResults;
    }

    public String getKernel() {
        return kernel;
    }

    @Override
    public Descriptor<Server> getDescriptor() {
        return null;
    }

    @Extension
    public static class DescriptorImpl extends Descriptor<Server> {
        @Nonnull
        @Override
        public String getDisplayName() {
            return "Server";
        }

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

        public FormValidation doCheckLaunchTimeout(@QueryParameter String launchTimeout) {
           try{
               Integer num = Integer.valueOf(launchTimeout);
               if(num >= 0){
                   return FormValidation.ok();
               }
           }catch (Exception e){
               return FormValidation.error("Timeout should be a valid number ");
           }
           return FormValidation.ok();
        }

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
         */
        @POST
        public FormValidation doValidate(
                @QueryParameter String kernel,
                @QueryParameter String launchTimeout,
                @QueryParameter String maxResults
                                         ) throws Exception {

            if (Util.fixEmptyAndTrim(kernel) != null) {
                try{
                    IPythonUserConfig userConfig = new IPythonUserConfig(kernel, Integer.parseInt(launchTimeout), Integer.parseInt(maxResults));
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
                }
            }
            return FormValidation.warning("Server address is required. Click on help for more info");
        }
    }


}
