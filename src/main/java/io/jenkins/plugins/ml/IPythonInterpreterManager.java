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

import org.apache.zeppelin.interpreter.Interpreter;
import org.apache.zeppelin.interpreter.InterpreterException;
import org.apache.zeppelin.interpreter.InterpreterGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Concrete Factory for IPython interpreter
 */

public class IPythonInterpreterManager extends InterpreterManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(IPythonInterpreterManager.class);
    private static final InterpreterGroup mockInterpreterGroup = new InterpreterGroup();
    private static int sessionId = 0;

    private KernelInterpreter kernelInterpreter;
    private IPythonUserConfig userConfig;

    IPythonInterpreterManager(IPythonUserConfig userConfig) {
        this.userConfig = userConfig;
        mockInterpreterGroup.put("session_" + sessionId, new ArrayList<Interpreter>());
    }

    /**
     * Used to create new IPythonKernelInterpreters
     * @return interpreter instance
     */
    @Override
    synchronized KernelInterpreter createInterpreter() {
        kernelInterpreter = new IPythonKernelInterpreter(this.userConfig);

        // zeppelin api for interpreter
        Interpreter interpreter = ((IPythonKernelInterpreter) kernelInterpreter).getInterpreter();
        mockInterpreterGroup.get("session_"+sessionId).add(interpreter);
        interpreter.setInterpreterGroup(mockInterpreterGroup);
        sessionId +=1 ;
        return kernelInterpreter;
    }

    @Override
    void initiateInterpreter() {
        kernelInterpreter = createInterpreter();
        kernelInterpreter.start();
    }

    @Override
    void closeInterpreter() {
        LOGGER.info(kernelInterpreter.toString());
        this.close();
    }

    @Override
    boolean testConnection() throws IOException, InterpreterException {
        String result = kernelInterpreter.interpretCode("print(test)").toString();
        return result.contains("test");
    }

    @Override
    protected String invokeInterpreter(String code) throws IOException, InterpreterException {
        return kernelInterpreter.interpretCode(code).toString();
    }

    public Integer getSessionId() {
        return sessionId;
    }

    @Override
    public void close() {
        kernelInterpreter.shutdown();
    }
}
