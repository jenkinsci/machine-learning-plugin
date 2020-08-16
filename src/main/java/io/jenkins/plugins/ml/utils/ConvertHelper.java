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

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import hudson.AbortException;
import hudson.FilePath;
import org.apache.zeppelin.jupyter.JupyterUtil;
import org.apache.zeppelin.jupyter.zformat.Note;
import org.apache.zeppelin.jupyter.zformat.Paragraph;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;


public class ConvertHelper {

    /**
     * Note is a Zeppelin formatted object which has paragraphs instead of cell when converting Jupyter notebooks. Paragraphs can be identified using config attribute as either contains
     * markdown --> ace/mode/markdown or
     * code  --> ace/mode/python
     */
    public static final String MARKDOWN_ANNOTATION = "ace/mode/markdown";

    /**
     * @param jupyterFile Path for the jupyter notebook
     * @return Plain text which contains python code only
     * @throws IOException          when file path does not exist
     * @throws InterruptedException exception on input stream reading
     */
    public static String jupyterToText(FilePath jupyterFile) throws IOException, InterruptedException {
        if (!jupyterFile.exists()) throw new AbortException("Invalid file");
        // Convert Jupyter Notebook to JSON
        try (final InputStreamReader inputStreamReader = new InputStreamReader(jupyterFile.read(), Charset.forName("UTF-8"))) {
            Note n = new JupyterUtil().getNote(inputStreamReader, "python", "\n", "#");
            StringBuilder outText = new StringBuilder();
            for (Paragraph para : n.getParagraphs()) {
                // skipping markdowns
                if (para.getConfig().get("editorMode").equals(MARKDOWN_ANNOTATION)) continue;
                outText.append(para.getText());
            }
            return outText.toString();
        }

    }

    /**
     * @param jupyterFile Path for the jupyter notebook
     * @return JSON object
     * @throws IOException when file path does not exist
     * @throws InterruptedException exception on input stream reading
     */
    public static JsonObject jupyterToJSON(FilePath jupyterFile) throws IOException, InterruptedException {
        if (!jupyterFile.exists()) throw new AbortException("Invalid file");
        try (final InputStreamReader inputStreamReader = new InputStreamReader(jupyterFile.read(), Charset.forName("UTF-8"))) {
            Note n = new JupyterUtil().getNote(inputStreamReader, "python", "\n", "#");
            Gson gson = new Gson();
            JsonElement tree = gson.toJsonTree(n);
            return tree.getAsJsonObject();
        }

    }

    /**
     * @param jupyterFile Path for the jupyter notebook
     * @return Array of text which contains python code only
     * @throws IOException          when file path does not exist
     * @throws InterruptedException exception on input stream reading
     */
    public static ArrayList<String> jupyterToTextArray(FilePath jupyterFile) throws IOException, InterruptedException {
        if (!jupyterFile.exists()) throw new AbortException("Invalid file");
        // Convert Jupyter Notebook to JSON
        try (final InputStreamReader inputStreamReader = new InputStreamReader(jupyterFile.read(), Charset.forName("UTF-8"))) {
            Note n = new JupyterUtil().getNote(inputStreamReader, "python", "\n", "#");
            ArrayList outTextArray = new ArrayList();
            for (Paragraph para : n.getParagraphs()) {
                // skipping markdowns
                if (para.getConfig().get("editorMode").equals(MARKDOWN_ANNOTATION)) continue;
                outTextArray.add(para.getText());
            }
            return outTextArray;
        }

    }
}
