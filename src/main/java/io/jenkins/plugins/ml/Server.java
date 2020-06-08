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
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;
import hudson.model.Job;
import org.kohsuke.stapler.DataBoundConstructor;

import javax.annotation.Nonnull;

public class Server extends AbstractDescribableImpl<Server> {
    private String serverName;
    private String serverAddress;
    private long launchTimeout;
    private long maxResults;

    @DataBoundConstructor
    public Server(String serverName, String serverAddress, long launchTimeout,long maxResults) {
        this.serverName = serverName;
        this.serverAddress = serverAddress;
        this.launchTimeout = launchTimeout;
        this.maxResults = maxResults;
    }

    String getServerName() {
        return serverName;
    }

    public String getServerAddress() {
        return serverAddress;
    }

    public long getLaunchTimeout() {
        return launchTimeout;
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
    }

    /**
     * Gets the {@link Server} associated with the given job.
     *
     * @return local host
     *         if no such was found.
     */
    public static Server get(Job<?, ?> p) {
        if(p != null) {
            ServerJobProperty ijp = p.getProperty(ServerJobProperty.class);
            if (ijp != null) {
                // Looks in global configuration for the server configured
                Server site = ijp.getSite();
                if (site != null) {
                    return site;
                }
            }
        }
        return new Server("localhost","127.0.0.1",1000,2);
        }

}
