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

import com.google.common.net.InetAddresses;
import hudson.Extension;
import hudson.Util;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;
import hudson.model.Job;
import hudson.util.FormValidation;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

import javax.annotation.Nonnull;
import java.util.regex.Pattern;

public class Server extends AbstractDescribableImpl<Server> {
    private final String serverName;
    private final String serverAddress;
    private final long launchTimeout;
    private final long maxResults;

    private static final Pattern pattern = Pattern.compile("^[a-zA-Z0-9_]+$");

    @DataBoundConstructor
    public Server(String serverName, String serverAddress, long launchTimeout,long maxResults) {
        this.serverName = serverName;
        this.serverAddress = serverAddress;
        this.launchTimeout = launchTimeout;
        this.maxResults = maxResults;
    }

    public String getServerName() {
        return serverName;
    }

    public String getServerAddress() {
        return serverAddress;
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
                return FormValidation.warning("* Required ");
            }

            if( pattern.matcher(serverName).matches() ){
                return FormValidation.ok();
            }else{
                return FormValidation.warning(" Try another name. Not supported",serverName);
            }
        }

        public FormValidation doCheckServerAddress(@QueryParameter String serverAddress) {
            if( Util.fixEmptyAndTrim(serverAddress) == null){
                return FormValidation.warning("* Required ");
            }
            else if(InetAddresses.isInetAddress(serverAddress)){
                return FormValidation.ok();
            }else{
                return FormValidation.error("Malformed IP address ", serverAddress);
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
    }

    /**
     * Gets the {@link Server} associated with the given job.
     *
     * @param p job
     * @return the Server configured for the job
     * @throws RuntimeException throws if there is invalid job
     *
     */
    public static Server get(Job<?, ?> p) {
        if(p != null) {
            ServerJobProperty ijp = p.getProperty(ServerJobProperty.class);
            if (ijp != null) {
                // Looks in global configuration for the server configured
                Server server = ijp.getServer();
                if (server != null) {
                    return server;
                }
            }
        }
        throw new RuntimeException("Invalid job, failed to create IPython server");
    }

}
