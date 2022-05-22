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

import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.GradleScriptException;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.logging.LogLevel;
import org.gradle.api.tasks.TaskContainer;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * @author Marco 'freudi74' Freudenberger
 * @since 28/05/2019
 */
public final class CMakePlugin implements Plugin<Project> {
    @Override
    public void apply(final @NotNull Project project) {
        final CMakePluginExtension ext = project.getExtensions().create("cmake", CMakePluginExtension.class, project);
        final TaskContainer tasks = project.getTasks();

        //tasks.register("cmakeSetup", CMakeSetupTask.class, task -> {
        //    /* .. */
        //});

        tasks.register("cmakeConfigure", CMakeConfigureTask.class, task -> {
            task.getExecutable().set(ext.getExecutable());
            task.getWorkingFolder().set(ext.getWorkingFolder());
            task.getSourceFolder().set(ext.getSourceFolder());
            task.getConfigurationTypes().set(ext.getConfigurationTypes());
            task.getInstallPrefix().set(ext.getInstallPrefix());
            task.getGenerator().set(ext.getGenerator());
            task.getPlatform().set(ext.getPlatform());
            task.getToolset().set(ext.getToolset());
            task.getBuildSharedLibs().set(ext.getBuildSharedLibs());
            task.getBuildStaticLibs().set(ext.getBuildStaticLibs());
            task.getDefs().set(ext.getDefs());
        });

        tasks.register("cmakeBuild", CMakeBuildTask.class, task -> {
            task.getExecutable().set(ext.getExecutable());
            task.getWorkingFolder().set(ext.getWorkingFolder());
            task.getBuildConfig().set(ext.getBuildConfig());
            task.getBuildTarget().set(ext.getBuildTarget());
            task.getBuildClean().set(ext.getBuildClean());
        });

        tasks.register("cmakeClean", DefaultTask.class, task -> {
            task.setGroup("cmake");
            task.setDescription("Clean CMake configuration");

            task.doFirst(t -> {
                final File workingFolder = ext.getWorkingFolder().getAsFile().get().getAbsoluteFile();
                if (workingFolder.exists()) {
                    project.getLogger().info("Deleting folder " + workingFolder);
                    if (!Utils.deleteDirectory(workingFolder)) {
                        throw new GradleException("Could not delete working folder " + workingFolder);
                    }
                }
            });
        });

        tasks.register("cmakeGenerators", DefaultTask.class, task -> {
            task.setGroup("cmake");
            task.setDescription("List available CMake generators");

            task.doFirst(t -> {
                // should go to clean...
                final ProcessBuilder pb = new ProcessBuilder(ext.getExecutable().getOrElse("cmake"), "--help");

                try {
                    // start
                    final Process process = pb.start();
                    final BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

                    String line;
                    boolean foundGenerators = false;

                    while ((line = reader.readLine()) != null) {
                        if (line.equals("Generators")) {
                            foundGenerators = true;
                        }
                        if (foundGenerators) {
                            project.getLogger().log(LogLevel.QUIET, line);
                        }
                    }
                    process.waitFor();
                }
                catch (IOException | InterruptedException e) {
                    throw new GradleScriptException("cmake --help failed.", e);
                }
            });
        });

        tasks.getByName("cmakeBuild").dependsOn(tasks.getByName("cmakeConfigure"));
    }
}