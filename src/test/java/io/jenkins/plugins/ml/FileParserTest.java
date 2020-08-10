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

import hudson.FilePath;
import hudson.model.FreeStyleProject;
import io.jenkins.plugins.ml.model.ParsableFile;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;


public class FileParserTest {

  @Rule
  public JenkinsRule jenkins = new JenkinsRule();

  private ParsableFile mockFile;
  private ArrayList<ParsableFile> array;
  private FileParser fParser;
  private FreeStyleProject project;

  @Before
  public void createCommonMocks() throws IOException{
    project = jenkins.createFreeStyleProject("p");
  }

  @Test
  public void copyJupyterNoteBookTest() throws InterruptedException, ExecutionException, IOException {
    Path resourceDirectory = Paths.get("src", "test", "resources", "demo.ipynb");
    String absolutePath = resourceDirectory.toFile().getAbsolutePath();
    FilePath filePath = new FilePath(new File(absolutePath));
    assertNotNull(filePath);

    mockFile = new ParsableFile(absolutePath,false,"NONE",null);
    assertNotNull(mockFile);
    array = new ArrayList<>();
    array.add(mockFile);
    fParser = new FileParser(array);
    project.getBuildWrappersList().add(fParser);
    project.scheduleBuild2(0).get();
    assertTrue(project.getWorkspace().child("demo.ipynb").exists());
  }

}
