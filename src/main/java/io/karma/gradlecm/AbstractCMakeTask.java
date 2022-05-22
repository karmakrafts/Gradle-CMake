package io.karma.gradlecm;

import org.gradle.api.DefaultTask;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.MapProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputDirectory;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.Map.Entry;
import java.util.Set;

/**
 * Base class that makes sure the CMake build is always invoked
 * with the optimal number of threads. This can immensely speed
 * up build times on heavy multi-core systems.
 *
 * @author Alexander 'KitsuneAlex' Hinze
 * @since 17/05/2022
 */
public abstract class AbstractCMakeTask extends DefaultTask {
    public final Property<String> executable;
    public final DirectoryProperty sourceFolder;
    public final Property<String> generator; // for example: "Visual Studio 16 2019"
    public final DirectoryProperty workingFolder;
    public final MapProperty<String, String> env;

    protected AbstractCMakeTask() {
        final ObjectFactory factory = getProject().getObjects();

        // @formatter:off
        workingFolder   = factory.directoryProperty();
        generator       = factory.property(String.class);
        executable      = factory.property(String.class);
        sourceFolder    = factory.directoryProperty();
        env             = factory.mapProperty(String.class, String.class);
        // @formatter:on

        workingFolder.set(new File(getProject().getBuildDir(), "cmake"));
        sourceFolder.set(new File(getProject().getBuildDir(), "src" + File.separator + "main" + File.separator + "cpp"));
    }

    protected abstract void gatherParameters(final @NotNull ArrayList<String> params);

    protected abstract void gatherBuildParameters(final @NotNull ArrayList<String> params);

    protected abstract void copyConfiguration(final @NotNull CMakePluginExtension ext);

    public void configureFromProject() {
        final CMakePluginExtension ext = (CMakePluginExtension) getProject().getExtensions().getByName("cmake");
        workingFolder.set(ext.getWorkingFolder());
        generator.set(ext.getGenerator());
        executable.set(ext.getExecutable());
        sourceFolder.set(ext.getSourceFolder());
        env.set(ext.getEnv());
        copyConfiguration(ext);
    }

    private @NotNull ArrayList<String> buildCmdLine() {
        final ArrayList<String> params = new ArrayList<>();
        final String executable = this.executable.getOrElse("cmake");

        if (env.isPresent()) {
            final Set<Entry<String, String>> envEntries = env.get().entrySet();

            if (!envEntries.isEmpty()) {
                params.add(executable);
                params.add("-E");
                params.add("env");

                for (final Entry<String, String> entry : envEntries) {
                    params.add(String.format("%s=%s", entry.getKey(), entry.getValue()));
                }
            }
        }

        params.add(executable);
        gatherParameters(params);

        final ArrayList<String> buildParams = new ArrayList<>();
        gatherBuildParameters(buildParams);

        if (!buildParams.isEmpty()) {
            params.add("--");
            params.addAll(buildParams);
        }

        return params;
    }

    @Input
    @Optional
    public @NotNull Property<String> getGenerator() {
        return generator;
    }

    @Input
    @Optional
    public @NotNull Property<String> getExecutable() {
        return executable;
    }

    @InputDirectory
    public @NotNull DirectoryProperty getSourceFolder() {
        return sourceFolder;
    }

    @OutputDirectory
    public @NotNull DirectoryProperty getWorkingFolder() {
        return workingFolder;
    }

    @Input
    @Optional
    public @NotNull MapProperty<String, String> getEnv() {
        return env;
    }

    @TaskAction
    public void performAction() {
        final CMakeExecutor executor = new CMakeExecutor(getLogger(), getName());
        executor.exec(buildCmdLine(), workingFolder.getAsFile().get());
    }
}
