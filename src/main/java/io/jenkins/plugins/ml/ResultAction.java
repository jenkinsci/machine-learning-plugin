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
import hudson.model.Action;
import hudson.model.Run;
import io.jenkins.plugins.ml.model.Summary;
import jenkins.model.RunAction2;

import javax.annotation.CheckForNull;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.stream.Collectors;

/**
 * The type Result action.
 */
public class ResultAction implements RunAction2 {

    private transient Run<?, ?> run;
    private List<Summary> imageFiles;
    private List<Summary> htmlFiles;
    private transient FilePath path;

    /**
     * Instantiates a new Result action.
     *
     * @param run the run
     * @param ws  the ws
     */
    public ResultAction(final Run<?, ?> run, final FilePath ws) {
        this.run = run;
        this.path = new FilePath(ws, run.getId());

    }

    @CheckForNull
    @Override
    public String getIconFileName() {
        return "document.png";
    }

    @CheckForNull
    @Override
    public String getDisplayName() {
        return "Results";
    }

    @CheckForNull
    @Override
    public String getUrlName() {
        return "results";
    }

    /**
     * Gets run.
     *
     * @return the run
     */
    public Run getRun() {
        return run;
    }

    /**
     * Gets image files.
     *
     * @return the image files
     */
    public List<Summary> getImageFiles() {
        return imageFiles;
    }

    /**
     * Gets html files.
     *
     * @return the html files
     */
    public List<Summary> getHtmlFiles() {
        return htmlFiles;
    }

    /**
     * Get build id string.
     *
     * @return the string
     */
    public String getBuildId() {
        return run.getId();
    }

    /**
     * Get href string fo files in the workspace.
     *
     * @return the string
     */
    public String getHref() {
        return "../../ws/" +
                getBuildId() +
                "/";
    }

    /**
     * Update image and html file lists to show on action.
     */
    private void updateFiles() {
        try {
            if (path.exists()) {
                imageFiles = getVisualsByPath(path, "png");
                htmlFiles = getVisualsByPath(path, "html");
//                Collections.sort(imageFiles, new Comparator<Summary>() {
//                    public int compare(Summary s1, Summary s2) {
//                        return s1.getTask().compareTo(s2.getTask());
//                    }
//                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Gets List of summary objects by path and file format.
     *
     * @param path    the path
     * @param fFormat the f format
     * @return the visuals by path
     * @throws Exception the exception
     */
    public List<Summary> getVisualsByPath(FilePath path, String fFormat) throws Exception {
        ListIterator<FilePath> list = path.list().listIterator();
        List<FilePath> paths = new ArrayList<>();
        for (; list.hasNext(); ) {
            FilePath fp = list.next();
            paths.addAll(fp.list());
        }
        return paths.stream()
                .filter(p -> p.getName().toLowerCase().endsWith(fFormat))
                .map(p -> new Summary(p.getName(), p.getParent().getName().toString()))
                .collect(Collectors.toList());

    }

    /**
     * Update summary action.
     *
     * @return the action
     */
    public Action updateSummary() {
        updateFiles();
        return this;
    }

    @Override
    public void onAttached(Run<?, ?> run) {
        this.run = run;
        updateFiles();

    }

    @Override
    public void onLoad(Run<?, ?> run) {
        this.run = run;

    }

    /**
     * Gets path.
     *
     * @return the path
     */
    public FilePath getPath() {
        return path;
    }
}
