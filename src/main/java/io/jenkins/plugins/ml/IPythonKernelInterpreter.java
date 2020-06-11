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
import org.apache.zeppelin.python.IPythonInterpreter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
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
    private InterpreterResultMessage interpreterResultMessage ;

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
        interpreter = new LazyOpenInterpreter(new IPythonInterpreter(properties));

    }

    /**
     * IPython will be connected and interpreted through the object of this class.
     * @param code - python code to be executed
     * @return the result of the interpreted code
     */
    @Override
    public InterpreterResultMessage interpretCode(String code) throws IOException, InterpreterException {
        InterpreterResult result;
        InterpreterContext context = getInterpreterContext();
        result = interpreter.interpret(code, context);
        LOGGER.info(result.code().toString());
        int sizeOfResult = context.out.toInterpreterResultMessage().size();

        // Context output will depend on the code to be interpreted
        if( sizeOfResult != 0){
            interpreterResultMessage = context.out.toInterpreterResultMessage().get(0);
        }else{
            // Contains non-output lines
            interpreterResultMessage = new InterpreterResultMessage(InterpreterResult.Type.TEXT,"");
        }
        return interpreterResultMessage;
    }

    public void start(){
        try {
            interpreter.open();
        } catch (InterpreterException e) {
            LOGGER.error("Unsupported operation");
        }
    }

    @Override
    public void shutdown() {
        try {
            interpreter.close();
        } catch (InterpreterException e) {
            LOGGER.error("Unsupported operation for shutting down");
        }
    }

    @Override
    public String toString() {
        return "IPython Interpreter";
    }

    private static InterpreterContext getInterpreterContext() {
        return new InterpreterContext.Builder().setNoteId("noteID").setParagraphId("paragraphId").setReplName("replName").setInterpreterOut(new InterpreterOutput(null)).build();

    }

    public void setInterpreter(LazyOpenInterpreter interpreter) {
        this.interpreter = interpreter;
    }

    LazyOpenInterpreter getInterpreter() {
        return interpreter;
    }
}
