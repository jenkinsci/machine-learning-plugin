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


import java.io.Serializable;
import java.util.Objects;

public class IPythonUserConfig implements Serializable {
    private final String kernel;
    private final long iPythonLaunchTimeout;
    private final long maxResult;

    /**
     * Constructor for configuration
     */

    public IPythonUserConfig(String kernel, long iPythonLaunchTimeout, long maxResult) {
        this.kernel = kernel;
        this.iPythonLaunchTimeout = iPythonLaunchTimeout;
        this.maxResult = maxResult;
    }

    public String getkernel() {
        return kernel;
    }

    public long getIPythonLaunchTimeout() {
        return iPythonLaunchTimeout;
    }

    public long getMaxResult() {
        return maxResult;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        IPythonUserConfig userConfig = (IPythonUserConfig) o;
        return kernel.equals(userConfig.getkernel()) &&
                iPythonLaunchTimeout == userConfig.getIPythonLaunchTimeout() &&
                maxResult == userConfig.getMaxResult();
    }

    @Override
    public int hashCode() {
        return Objects.hash(kernel, iPythonLaunchTimeout, maxResult);
    }

}
