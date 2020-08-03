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

import org.apache.zeppelin.interpreter.*;
import org.apache.zeppelin.jupyter.JupyterInterpreter;
import org.apache.zeppelin.resource.LocalResourcePool;
import org.apache.zeppelin.resource.ResourcePool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class IPythonKernelInterpreter implements KernelInterpreter  {

    /**
     * Our logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(IPythonKernelInterpreter.class);
    private final String serverGatewayAddress;
    private final long iPythonLaunchTimeout;
    private final long maxResult;

    private LazyOpenInterpreter interpreter;
    private ResourcePool resourcePool;

    /**
     * @param userConfig - user's configuration including server address etc.
     */
    public IPythonKernelInterpreter(IPythonUserConfig userConfig) {
        this.serverGatewayAddress = userConfig.getServerGatewayAddress();
        this.iPythonLaunchTimeout = userConfig.getIPythonLaunchTimeout();
        this.maxResult = userConfig.getMaxResult();

        // properties for the interpreter
        Properties properties = new Properties();
        properties.setProperty("zeppelin.python.maxResult", String.valueOf(maxResult));
        properties.setProperty("zeppelin.python.gatewayserver_address", serverGatewayAddress);
        properties.setProperty("zeppelin.ipython.launch.timeout", String.valueOf(iPythonLaunchTimeout));
        properties.setProperty("zeppelin.py4j.useAuth","false");

        // Initiate a Lazy interpreter
        interpreter = new LazyOpenInterpreter(new JupyterInterpreter(properties));
        resourcePool = new LocalResourcePool("local");

    }

    /**
     * IPython will be connected and interpreted through the object of this class.
     * @param code - python code to be executed
     * @return the list of result of the interpreted code
     */
    @Override
    public List<InterpreterResultMessage> interpretCode(String code, String kernel) throws IOException, InterpreterException {
        InterpreterResult result;
        InterpreterContext context = getInterpreterContext(kernel);
        result = interpreter.interpret(code, context);
        LOGGER.info(result.code().toString());
        List<InterpreterResultMessage> rst = context.out.toInterpreterResultMessage();

        if (rst.size() > 0) {
            return rst;
        }
        return null;
    }

    public void start() throws InterpreterException {
        interpreter.open();
    }

    @Override
    public void shutdown() throws InterpreterException {
        interpreter.close();
    }

    @Override
    public String toString() {
        return "IPython Interpreter";
    }

    private InterpreterContext getInterpreterContext(String kernel) {
        Map<String, String> localProperties = new HashMap<>();
        localProperties.put("kernel", kernel);
        return InterpreterContext.builder()
                .setNoteId("noteId")
                .setParagraphId("paragraphId")
                .setInterpreterOut(new InterpreterOutput(null))
                .setLocalProperties(localProperties)
                .setResourcePool(resourcePool)
                .build();
    }

    public void setInterpreter(LazyOpenInterpreter interpreter) {
        this.interpreter = interpreter;
    }

    LazyOpenInterpreter getInterpreter() {
        return interpreter;
    }
}
