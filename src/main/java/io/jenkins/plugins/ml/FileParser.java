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

import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.tasks.BuildWrapper;
import hudson.tasks.BuildWrapperDescriptor;
import io.jenkins.plugins.ml.model.ParsableFile;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

public class FileParser extends BuildWrapper {
    public   List<ParsableFile> parsableFiles;

    private static final Logger LOGGER = Logger.getLogger(FileParser.class.getName());

    @DataBoundConstructor
    public FileParser(ArrayList<ParsableFile> parsableFiles){
        this.parsableFiles = parsableFiles;
    }
    @DataBoundSetter
    public void setParsableFiles(List<ParsableFile> parsableFiles){
        this.parsableFiles = parsableFiles;
    }

    @Override
    public Environment setUp(AbstractBuild build, final Launcher launcher,
                             BuildListener listener)
    {
        // Get the workspace
        LOGGER.info("Recognize project workspace and folder");
        final Optional<FilePath> projectWorkspace = Optional.ofNullable(build.getWorkspace());

        // Copy each file to the workspace
        projectWorkspace.ifPresent((workspace) -> {
            for (ParsableFile file : parsableFiles) {
                FilePath copyFrom = new FilePath(new File(file.getFileName()));
                LOGGER.info(String.format("Copying file from %s to %s", copyFrom.getName(), workspace.getName()));
                try {
                    copyFrom.copyTo(new FilePath(workspace, copyFrom.getName()));
                    LOGGER.info("Saving name");
                } catch (IOException | InterruptedException e) {
                    e.printStackTrace(listener.getLogger());
                }
            }
        });


        return new Environment() {
          @Override
          public boolean tearDown(AbstractBuild build, BuildListener listener) {

            // Delete the file after build if necessary
              projectWorkspace.ifPresent((workspace) -> parsableFiles.stream()
                      .filter(file -> file != null && file.isDeleteFilesAfterBuild())
                      .map(file -> new FilePath(new File(file.getFileName())))
                      .forEach(file -> {
                          String copyToFile = file.getName();
                          FilePath child = new FilePath(workspace, copyToFile);
                          try {
                              child.delete();
                          } catch (IOException | InterruptedException e) {
                              e.printStackTrace(listener.getLogger());
                          }
                      }));

            return true;
          }
        };
    }

    @Extension
    public static class DescriptorImpl extends BuildWrapperDescriptor {
        public DescriptorImpl() {
            super(FileParser.class);
        }

        @Override
        @Nonnull
        public String getDisplayName() {
            return "Add Files to the workspace";
        }

        @Override
        public boolean isApplicable(AbstractProject<?, ?> item) {
            return true;
        }

    }

}
