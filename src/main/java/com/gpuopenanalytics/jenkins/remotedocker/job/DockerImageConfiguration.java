/*
 * Copyright (c) 2019, NVIDIA CORPORATION.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.gpuopenanalytics.jenkins.remotedocker.job;

import com.gpuopenanalytics.jenkins.remotedocker.DockerLauncher;
import com.gpuopenanalytics.jenkins.remotedocker.Utils;
import com.gpuopenanalytics.jenkins.remotedocker.config.ConfigItem;
import com.gpuopenanalytics.jenkins.remotedocker.config.VolumeConfiguration;
import hudson.Extension;
import hudson.model.AbstractBuild;
import hudson.model.Descriptor;
import hudson.util.ArgumentListBuilder;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.DataBoundConstructor;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.List;

/**
 * A {@link DockerConfiguration} created from an existing docker image.
 */
public class DockerImageConfiguration extends DockerConfiguration {

    private final String image;
    private final boolean forcePull;

    @DataBoundConstructor
    public DockerImageConfiguration(List<ConfigItem> configItemList,
                                    List<VolumeConfiguration> volumes,
                                    String image,
                                    boolean forcePull) {
        super(configItemList, volumes);
        this.image = image;
        this.forcePull = forcePull;
    }

    public String getImage() {
        return image;
    }

    public boolean isForcePull() {
        return forcePull;
    }

    @Override
    public void validate() throws Descriptor.FormException {
        if (StringUtils.isEmpty(image)) {
            throw new Descriptor.FormException("Docker image cannot be empty",
                                               "image");
        }
        for (ConfigItem item : getConfigItemList()) {
            item.validate();
        }
        for (VolumeConfiguration volume : getVolumes()) {
            volume.validate();
        }
    }

    @Override
    public void setupImage(DockerLauncher launcher,
                           String localWorkspace) {
        //No-op
    }

    @Override
    public void addCreateArgs(DockerLauncher launcher,
                              ArgumentListBuilder args,
                              AbstractBuild build) {
        if (isForcePull()) {
            args.add("--pull");
        }
        getConfigItemList().stream()
                .forEach(item -> item.addCreateArgs(launcher, args, build));
        getVolumes().stream()
                .forEach(item -> item.addArgs(args, build));

        args.add(Utils.resolveVariables(
                build.getBuildVariableResolver(), getImage()));
    }

    @Override
    public void postCreate(DockerLauncher launcher,
                           AbstractBuild build) throws IOException, InterruptedException {
        super.postCreate(launcher, build);
    }

    @Override
    public void addRunArgs(DockerLauncher launcher,
                           ArgumentListBuilder args,
                           AbstractBuild build) {
        super.addRunArgs(launcher, args, build);
    }

    @Extension
    public static class DescriptorImpl extends DockerConfigurationDescriptor {

        @Nonnull
        @Override
        public String getDisplayName() {
            return "Docker Image";
        }
    }

}