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
import com.cloudbees.hudson.plugins.folder.AbstractFolderProperty;
import hudson.model.ItemGroup;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Provides folder level configuration.
 */
public class ServerFolderProperty extends AbstractFolderProperty<AbstractFolder<?>> {
    /**
     * Hold the servers configuration.
     */
    private List<Server> servers = Collections.emptyList();

    /**
     * Constructor.
     */
    public ServerFolderProperty() {
    }

    /**
     * Return the IPython servers.
     *
     * @return the IPython servers
     */
    public Server[] getServer() {
        return servers.toArray(new Server[0]);
    }

    public void setServers(List<Server> Servers) {
        this.servers = Servers;
    }

    /*
    * Return list of servers from folders
     */
    static List<Server> getServersFromFolders(ItemGroup itemGroup) {
        List<Server> result = new ArrayList<>();
        while (itemGroup instanceof AbstractFolder<?>) {
            AbstractFolder<?> folder = (AbstractFolder<?>) itemGroup;
            ServerFolderProperty serverFolderProperty = folder.getProperties()
                    .get(ServerFolderProperty.class);
            if (serverFolderProperty != null && serverFolderProperty.getServer().length != 0) {
                result.addAll(Arrays.asList(serverFolderProperty.getServer()));
            }
            itemGroup = folder.getParent();
        }
        return result;
    }
}
