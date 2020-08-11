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

package io.jenkins.plugins.ml.utils;

import hudson.FilePath;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class ConvertHelperTest {

    @Test
    public void testJupyterToText() throws IOException, InterruptedException {
        Path resourceDirectory = Paths.get("src", "test", "resources", "demo.ipynb");
        String absolutePath = resourceDirectory.toFile().getAbsolutePath();
        FilePath file = new FilePath(new File(absolutePath));
        assertNotNull(file);
        String text = ConvertHelper.jupyterToText(file);
        assertTrue(text.contains("pickle.load(file)"));
        assertTrue(text.contains("Test score"));
        assertTrue(text.contains("LogisticRegression"));

    }

    @Test
    public void testJupyterWithBashToText() throws IOException, InterruptedException {
        Path resourceDirectory = Paths.get("src", "test", "resources", "demo_bash.ipynb");
        String absolutePath = resourceDirectory.toFile().getAbsolutePath();
        FilePath file = new FilePath(new File(absolutePath));
        assertNotNull(file);
        String text = ConvertHelper.jupyterToText(file);
        assertTrue(text.contains("! git --version"));
    }

    @Test
    public void testRemoveMarkDown() throws IOException, InterruptedException {
        Path resourceDirectory = Paths.get("src", "test", "resources", "JS.ipynb");
        String absolutePath = resourceDirectory.toFile().getAbsolutePath();
        FilePath file = new FilePath(new File(absolutePath));
        assertNotNull(file);
        String text = ConvertHelper.jupyterToText(file);
        assertTrue(!text.startsWith("#"));
    }

}
