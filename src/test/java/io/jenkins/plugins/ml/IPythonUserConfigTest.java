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

import org.apache.zeppelin.interpreter.InterpreterException;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class IPythonUserConfigTest {

    private IPythonUserConfig userConfig;

    @Test
    public void testDefaultIPythonConfig() throws InterpreterException, IOException {
        userConfig = new IPythonUserConfig("python", 1000, 3);
        assertEquals("Kernel is not matching", "python", userConfig.getkernel());
        assertEquals("Timeout not matched",1000,userConfig.getIPythonLaunchTimeout());
        assertEquals("Max results not matched",3,userConfig.getMaxResult());

        /*
        In JENKINS-62556 the jenkins build checks fails due to the IPython env
        // setup Interpreter
        InterpreterManager interpreterManager = new IPythonInterpreterManager(userConfig);
        interpreterManager.initiateInterpreter();

        //Check whether it runs or not
        String result = interpreterManager.invokeInterpreter("print('default')");
        assertNotNull("Interpreter manager not initialized",interpreterManager);
        assertTrue("Bad interpretation",result.contains("default"));
        */
    }

}
