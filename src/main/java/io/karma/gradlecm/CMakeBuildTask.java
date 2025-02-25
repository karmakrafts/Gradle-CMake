package io.karma.gradlecm;

import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Optional;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

/**
 * @author Marco 'freudi74' Freudenberger
 * @author Alexander 'KitsuneAlex' Hinze
 * @since 28/05/2019
 */
public class CMakeBuildTask extends AbstractCMakeTask {
    private static final int numHostThreads = Runtime.getRuntime().availableProcessors();
    /**
     * Find the optimal number of threads to use for the build;
     * We usually aim for n-2 threads, where n is the number of logical host threads,
     * but since some systems (especially VMs for CI/CDs etc.) might have 2 or less threads,
     * we define the optimal number of threads as max(min(n, 2), n - 2), which gives us
     * the results we are looking for.
     * TODO: maybe make this configurable at some point..
     */
    private static final int numUsableThreads = Math.max(Math.min(numHostThreads, 2), numHostThreads - 2);

    private final Property<String> executable;
    private final Property<String> buildConfig;
    private final Property<String> buildTarget;
    private final Property<Boolean> buildClean;

    public CMakeBuildTask() {
        super(); // Make sure our shared properties are initialized

        final ObjectFactory factory = getProject().getObjects();

        setGroup("cmake");
        setDescription("Build a configured Build with CMake");

        // @formatter:off
        executable  = factory.property(String.class);
        buildConfig = factory.property(String.class);
        buildTarget = factory.property(String.class);
        buildClean  = factory.property(Boolean.class);
        // @formatter:on
    }

    @Override
    protected void copyConfiguration(final @NotNull CMakePluginExtension ext) {
        executable.set(ext.getExecutable());
        buildConfig.set(ext.getBuildConfig());
        buildTarget.set(ext.getBuildTarget());
        buildClean.set(ext.getBuildClean());
    }

    @Override
    protected void gatherParameters(final @NotNull ArrayList<String> params) {
        params.add("--build");
        params.add("."); // working folder will be executable working dir --- workingFolder.getAsFile().get().getAbsolutePath()

        if (buildConfig.isPresent()) {
            params.add("--config");
            params.add(buildConfig.get());
        }

        if (buildTarget.isPresent()) {
            params.add("--target");
            params.add(buildTarget.get());
        }

        if (buildClean.getOrElse(false)) {
            params.add("--clean-first");
        }
    }

    @Override
    protected void gatherBuildParameters(final @NotNull ArrayList<String> params) {
        final String gen = generator.getOrNull();

        if (gen != null) {
            if (gen.equals("Unix Makefiles") || gen.equals("MinGW Makefiles")) {
                params.add("-j");
                params.add(Integer.toString(numUsableThreads));
            }
        }
    }

    @Input
    @Optional
    public @NotNull Property<String> getExecutable() {
        return executable;
    }

    @Input
    @Optional
    public @NotNull Property<String> getBuildConfig() {
        return buildConfig;
    }

    @Input
    @Optional
    public @NotNull Property<String> getBuildTarget() {
        return buildTarget;
    }

    @Input
    @Optional
    public @NotNull Property<Boolean> getBuildClean() {
        return buildClean;
    }
}
