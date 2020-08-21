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

package org.apache.zeppelin.interpreter.util;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteException;
import org.apache.commons.exec.ExecuteResultHandler;
import org.apache.commons.exec.ExecuteWatchdog;
import org.apache.commons.exec.LogOutputStream;
import org.apache.commons.exec.PumpStreamHandler;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.zeppelin.interpreter.InterpreterContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract class for launching java process.
 */
public abstract class ProcessLauncher implements ExecuteResultHandler {

  private static final Logger LOGGER = LoggerFactory.getLogger(ProcessLauncher.class);

  public enum State {
    NEW,
    LAUNCHED,
    RUNNING,
    TERMINATED,
    COMPLETED
  }

  private CommandLine commandLine;
  private Map<String, String> envs;
  private ExecuteWatchdog watchdog;
  private ProcessLogOutputStream processOutput;
  protected String errorMessage = null;
  protected State state = State.NEW;
  private boolean launchTimeout = false;
  private final String workingDirectory;

  public ProcessLauncher(CommandLine commandLine,
                         Map<String, String> envs,
                         String workingDirectory) {
    this.commandLine = commandLine;
    this.envs = envs;
    this.processOutput = new ProcessLogOutputStream();
    this.workingDirectory = workingDirectory;
  }

  public ProcessLauncher(CommandLine commandLine,
                         Map<String, String> envs,
                         ProcessLogOutputStream processLogOutput,
                         String workingDirectory) {
    this.commandLine = commandLine;
    this.envs = envs;
    this.processOutput = processLogOutput;
    this.workingDirectory = workingDirectory;
  }

  /**
   * In some cases we need to redirect process output to paragraph's InterpreterOutput.
   * e.g. In %r.shiny for shiny app
   * @param redirectedContext
   */
  public void setRedirectedContext(InterpreterContext redirectedContext) {
    if (redirectedContext != null) {
      LOGGER.info("Start to redirect process output to interpreter output");
    } else {
      LOGGER.info("Stop to redirect process output to interpreter output");
    }
    this.processOutput.redirectedContext = redirectedContext;
  }

  public void launch() {
    DefaultExecutor executor = new DefaultExecutor();
    executor.setWorkingDirectory(new File(workingDirectory));
    executor.setStreamHandler(new PumpStreamHandler(processOutput));
    this.watchdog = new ExecuteWatchdog(ExecuteWatchdog.INFINITE_TIMEOUT);
    executor.setWatchdog(watchdog);
    try {
      executor.execute(commandLine, envs, this);
      transition(State.LAUNCHED);
      LOGGER.info("Process is launched: {}", commandLine);
    } catch (IOException e) {
      this.processOutput.stopCatchLaunchOutput();
      LOGGER.error("Fail to launch process: " + commandLine, e);
      transition(State.TERMINATED);
      errorMessage = e.getMessage();
    }
  }

  public abstract void waitForReady(int timeout);

  public void transition(State state) {
    this.state = state;
    LOGGER.info("Process state is transitioned to " + state);
  }

  public void onTimeout() {
    LOGGER.warn("Process launch is time out.");
    launchTimeout = true;
    stop();
  }

  public void onProcessRunning() {
    transition(State.RUNNING);
  }

  @Override
  public void onProcessComplete(int exitValue) {
    LOGGER.warn("Process is exited with exit value " + exitValue);
    if (exitValue == 0) {
      transition(State.COMPLETED);
    } else {
      transition(State.TERMINATED);
    }
  }

  @Override
  public void onProcessFailed(ExecuteException e) {
    LOGGER.warn("Process is failed due to " + e);
    errorMessage = ExceptionUtils.getStackTrace(e);
    transition(State.TERMINATED);
  }

  public String getErrorMessage() {
    if (!StringUtils.isBlank(processOutput.getProcessExecutionOutput())) {
      return processOutput.getProcessExecutionOutput();
    } else {
      return this.errorMessage;
    }
  }

  public String getProcessLaunchOutput() {
    return this.processOutput.getProcessExecutionOutput();
  }

  public boolean isLaunchTimeout() {
    return launchTimeout;
  }

  public boolean isRunning() {
    return this.state == State.RUNNING;
  }

  public void stop() {
    if (watchdog != null && isRunning()) {
      watchdog.destroyProcess();
      watchdog = null;
    }
  }

  public void stopCatchLaunchOutput() {
    processOutput.stopCatchLaunchOutput();
  }

  public static class ProcessLogOutputStream extends LogOutputStream {

    private boolean catchLaunchOutput = true;
    private StringBuilder launchOutput = new StringBuilder();
    private InterpreterContext redirectedContext;

    public void stopCatchLaunchOutput() {
      this.catchLaunchOutput = false;
    }

    public String getProcessExecutionOutput() {
      return launchOutput.toString();
    }

    @Override
    protected void processLine(String s, int i) {
      // print Interpreter launch command for diagnose purpose
      if (s.startsWith("Interpreter launch command")) {
        LOGGER.info(s);
      } else {
        LOGGER.debug("Process Output: " + s);
      }
      if (catchLaunchOutput) {
        launchOutput.append(s + "\n");
      }
      if (redirectedContext != null) {
        try {
          redirectedContext.out.write(s + "\n");
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }
  }
}
