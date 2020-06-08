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
import hudson.util.PersistedList;
import jenkins.model.GlobalConfiguration;
import jenkins.model.Jenkins;
import org.kohsuke.stapler.DataBoundSetter;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Objects;


@Extension
public class IPythonGlobalConfiguration extends GlobalConfiguration {

    @Nonnull
    public static IPythonGlobalConfiguration get() {
        //noinspection deprecation
        return (IPythonGlobalConfiguration) Objects.requireNonNull(Jenkins.getInstance()).getDescriptorOrDie(IPythonGlobalConfiguration.class);
    }

    public List<Server> servers = new PersistedList<>(this);

    public IPythonGlobalConfiguration() {
        load();
    }

    public List<Server> getServers() {
        return servers;
    }

    @DataBoundSetter
    public void setServers(List<Server> servers) {
        this.servers = servers;
        save();
    }
}