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

package io.jenkins.plugins.ml.model;

import hudson.Extension;
import hudson.Util;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

import javax.annotation.Nonnull;

public class ParsableFile extends AbstractDescribableImpl<ParsableFile> {

    enum SourceCodeConvertType{
        // Type of source code to be handled by IPython builder
        JSON,
        PY,
        NONE
    }

    private final String fileName;
    private final boolean deleteFilesAfterBuild;
    private final String convertType;
    private final String saveConverted;


    @DataBoundConstructor
    public ParsableFile(String fileName, boolean deleteFilesAfterBuild, String convertType, String saveConverted) {
        this.fileName = fileName;
        this.deleteFilesAfterBuild = deleteFilesAfterBuild;
        this.convertType = convertType;
        this.saveConverted = saveConverted;
    }

    public String getFileName() {
        return fileName;
    }

    public boolean isDeleteFilesAfterBuild() {
        return deleteFilesAfterBuild;
    }

    public boolean isChecked() {
        return !convertType.equals(SourceCodeConvertType.NONE.toString());
    }


    public static ListBoxModel.Option createOption(String jobName, Enum<?> enumOption, String convertType) {
        return new ListBoxModel.Option(jobName, enumOption.name(), enumOption.name().equals(convertType));
    }

    public String getConvertType() {
        return convertType;
    }

    public String getSaveConverted() {
        return saveConverted;
    }

    @Extension
    public static class DescriptorImpl extends Descriptor<ParsableFile> {
        @Nonnull
        @Override
        public String getDisplayName() {
            return "File";
        }

        // Fill the list box from the enum
        public ListBoxModel doFillConvertTypeItems(@QueryParameter String convertType) {
            ListBoxModel model = new ListBoxModel();

            model.add(createOption("None", SourceCodeConvertType.NONE, convertType));
            model.add(createOption("JSON", SourceCodeConvertType.JSON, convertType));
            model.add(createOption("Python", SourceCodeConvertType.PY, convertType));

            return model;
        }

        public FormValidation doCheckFileName(@QueryParameter String fileName) {
            if (Util.fixEmptyAndTrim(fileName) != null) {
                return FormValidation.ok();
            }else{
                return FormValidation.warning("File path is required to copy file");
            }
        }
    }
}
