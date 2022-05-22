/**
 * Copyright 2019 Marco Freudenberger
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.karma.gradlecm;

import org.gradle.api.Project;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.MapProperty;
import org.gradle.api.provider.Property;
import org.jetbrains.annotations.NotNull;

import java.io.File;

/**
 * @author Marco 'freudi74' Freudenberger
 * @author Alexander 'KitsuneAlex' Hinze
 * @since 28/05/2019
 */
public final class CMakePluginExtension {
    // parameters used by config and build step
    private final Property<String> executable;
    private final DirectoryProperty workingFolder;
    private final Property<String> generator; // for example: "Visual Studio 16 2019"
    private final MapProperty<String, String> env;
    private final MapProperty<String, String> shellEnv;

    // parameters used by config step
    private final DirectoryProperty sourceFolder;
    private final Property<String> configurationTypes;
    private final Property<String> installPrefix;
    private final Property<String> platform; // for example "x64" or "Win32" or "ARM" or "ARM64", supported on vs > 8.0
    private final Property<String> toolset; // for example "v142", supported on vs > 10.0
    private final Property<Boolean> buildSharedLibs;
    private final Property<Boolean> buildStaticLibs;
    private final MapProperty<String, String> defs;

    // parameters used on build step
    private final Property<String> buildConfig;
    private final Property<String> buildTarget;
    private final Property<Boolean> buildClean;


    public CMakePluginExtension(final @NotNull Project project) {
        final ObjectFactory factory = project.getObjects();

        // @formatter:off
        executable          = factory.property(String.class);
        workingFolder       = factory.directoryProperty();
        generator           = factory.property(String.class);
        env                 = factory.mapProperty(String.class, String.class);
        shellEnv            = factory.mapProperty(String.class, String.class);

        sourceFolder        = factory.directoryProperty();
        configurationTypes  = factory.property(String.class);
        installPrefix       = factory.property(String.class);
        platform            = factory.property(String.class);
        toolset             = factory.property(String.class);
        buildSharedLibs     = factory.property(Boolean.class);
        buildStaticLibs     = factory.property(Boolean.class);
        defs                = factory.mapProperty(String.class, String.class);

        buildConfig         = factory.property(String.class);
        buildTarget         = factory.property(String.class);
        buildClean          = factory.property(Boolean.class);
        // @formatter:on

        // default values
        workingFolder.set(new File(project.getBuildDir(), "cmake"));
        sourceFolder.set(new File(project.getBuildDir(), "src" + File.separator + "main" + File.separator + "cpp"));
    }

    public @NotNull Property<String> getExecutable() {
        return executable;
    }

    public @NotNull DirectoryProperty getWorkingFolder() {
        return workingFolder;
    }

    public @NotNull DirectoryProperty getSourceFolder() {
        return sourceFolder;
    }

    public @NotNull Property<String> getConfigurationTypes() {
        return configurationTypes;
    }

    public @NotNull Property<String> getInstallPrefix() {
        return installPrefix;
    }

    public @NotNull Property<String> getGenerator() {
        return generator;
    }

    public @NotNull Property<String> getPlatform() {
        return platform;
    }

    public @NotNull Property<String> getToolset() {
        return toolset;
    }

    public @NotNull Property<Boolean> getBuildSharedLibs() {
        return buildSharedLibs;
    }

    public @NotNull Property<Boolean> getBuildStaticLibs() {
        return buildStaticLibs;
    }

    public @NotNull MapProperty<String, String> getDefs() {
        return defs;
    }

    public @NotNull Property<String> getBuildConfig() {
        return buildConfig;
    }

    public @NotNull Property<String> getBuildTarget() {
        return buildTarget;
    }

    public @NotNull Property<Boolean> getBuildClean() {
        return buildClean;
    }

    public @NotNull MapProperty<String, String> getEnv() {
        return env;
    }

    public @NotNull MapProperty<String, String> getShellEnv() {
        return shellEnv;
    }
}