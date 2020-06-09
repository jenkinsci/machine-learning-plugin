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
import hudson.Extension;
import hudson.Util;
import hudson.init.InitMilestone;
import hudson.init.Initializer;
import hudson.model.Job;
import hudson.model.JobProperty;
import hudson.model.JobPropertyDescriptor;
import hudson.util.ListBoxModel;
import jenkins.model.Jenkins;
import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.DataBoundConstructor;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

public class ServerJobProperty extends JobProperty<Job<?,?>> {

    private final String serverName;

    @DataBoundConstructor
    public ServerJobProperty(String serverName) {
        serverName = Util.fixEmptyAndTrim(serverName);
        if (serverName == null) {
            // defaults to the first one
            List<Server> sites = IPythonGlobalConfiguration.get().getServers();
            if (!sites.isEmpty()) {
                serverName = sites.get(0).getServerName();
            }
        }
        this.serverName = serverName;
    }
    @Nullable
    public Server getServer() {
        List<Server> sites = IPythonGlobalConfiguration.get().getServers();

        if (serverName == null && sites.size() > 0) {
            // default
            return sites.get(0);
        }

        Stream<Server> streams = sites.stream();
        if (owner != null) {
            Stream<Server> stream2 = ServerFolderProperty.getServersFromFolders(owner.getParent())
                    .stream();
            streams = Stream.concat(streams, stream2).parallel();
        }

        return streams.filter(Server -> Server.getServerName().equals(serverName))
                .findFirst().orElse(null);
    }

    @Extension
    public static final class DescriptorImpl extends JobPropertyDescriptor {

        @Override
        public boolean isApplicable(Class<? extends Job> jobType) {
            return Job.class.isAssignableFrom(jobType);
        }

        @Override
        public String getDisplayName() {
            return "Servers";
        }
        public void setServers(Server Server) {
            IPythonGlobalConfiguration.get().getServers().add(Server);
        }

        public ListBoxModel doFillServerNameItems(@AncestorInPath AbstractFolder<?> folder) {
            ListBoxModel items = new ListBoxModel();
            for (Server site : IPythonGlobalConfiguration.get().getServers()) {
                items.add(site.getServerName());
            }
            if (folder != null) {
                List<Server> serversFromFolder = ServerFolderProperty.getServersFromFolders(folder);
                serversFromFolder.stream().map(Server::getServerName).forEach(items::add);
            }
            return items;
        }

        @Initializer(after= InitMilestone.EXTENSIONS_AUGMENTED)
        public void migrate() {
            //noinspection deprecation
            DescriptorImpl descriptor = (DescriptorImpl) Objects.requireNonNull(Jenkins.getInstance())
                    .getDescriptor(ServerJobProperty.class);
            if (descriptor != null) {
                descriptor.load(); // force readResolve without registering descriptor as configurable
            }
        }

    }
}
