package io.karma.gradlecm;

import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.MapProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Optional;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Map.Entry;
import java.util.Set;

/**
 * This task delegates functionality to configure the current
 * build target before invoking the actual build.
 * It also handles setting up the appropriate toolchain(s) automatically.
 *
 * @author Marco 'freudi74' Freudenberger
 * @author Alexander 'KitsuneAlex' Hinze
 * @since 28/05/2019
 */
public class CMakeConfigureTask extends AbstractCMakeTask {
    private final Property<String> configurationTypes;
    private final Property<String> installPrefix;
    private final Property<String> platform; // for example "x64" or "Win32" or "ARM" or "ARM64", supported on vs > 8.0
    private final Property<String> toolset; // for example "v142", supported on vs > 10.0
    private final Property<Boolean> buildSharedLibs;
    private final Property<Boolean> buildStaticLibs;
    private final MapProperty<String, String> defs;

    public CMakeConfigureTask() {
        super(); // Make sure our shared properties are initialized

        final ObjectFactory factory = getProject().getObjects();

        setGroup("cmake");
        setDescription("Configure a Build with CMake");

        // @formatter:off
        configurationTypes  = factory.property(String.class);
        installPrefix       = factory.property(String.class);
        platform            = factory.property(String.class);
        toolset             = factory.property(String.class);
        buildSharedLibs     = factory.property(Boolean.class);
        buildStaticLibs     = factory.property(Boolean.class);
        defs                = factory.mapProperty(String.class, String.class);
        // @formatter:on
    }

    @Override
    protected void gatherParameters(final @NotNull ArrayList<String> params) {
        if (this.generator.isPresent() && !this.generator.get().isEmpty()) {
            params.add("-G");
            params.add(this.generator.get());
        }

        if (platform.isPresent() && !platform.get().isEmpty()) {
            params.add("-A");
            params.add(platform.get());
        }

        if (toolset.isPresent() && !toolset.get().isEmpty()) {
            params.add("-T");
            params.add(toolset.get());
        }

        if (configurationTypes.isPresent() && !configurationTypes.get().isEmpty()) {
            params.add("-DCMAKE_CONFIGURATION_TYPES=" + configurationTypes.get());
        }

        if (installPrefix.isPresent() && !installPrefix.get().isEmpty()) {
            params.add("-DCMAKE_INSTALL_PREFIX=" + installPrefix.get());
        }


        if (buildSharedLibs.isPresent()) {
            params.add("-DBUILD_SHARED_LIBS=" + (buildSharedLibs.get() ? "ON" : "OFF"));
        }

        if (buildStaticLibs.isPresent()) {
            params.add("-DBUILD_STATIC_LIBS=" + (buildStaticLibs.get() ? "ON" : "OFF"));
        }

        if (defs.isPresent()) {
            final Set<Entry<String, String>> defEntries = defs.get().entrySet();

            for (final Entry<String, String> entry : defEntries) {
                params.add(String.format("-D%s=%s", entry.getKey(), entry.getValue()));
            }
        }

        params.add(sourceFolder.getAsFile().get().getAbsolutePath());
    }

    @Override
    protected void gatherBuildParameters(final @NotNull ArrayList<String> params) {}

    @Override
    protected void copyConfiguration(final @NotNull CMakePluginExtension ext) {
        configurationTypes.set(ext.getConfigurationTypes());
        installPrefix.set(ext.getInstallPrefix());
        platform.set(ext.getPlatform());
        toolset.set(ext.getToolset());
        buildSharedLibs.set(ext.getBuildSharedLibs());
        buildStaticLibs.set(ext.getBuildStaticLibs());
        defs.set(ext.getDefs());
    }

    @Input
    @Optional
    public @NotNull Property<String> getConfigurationTypes() {
        return configurationTypes;
    }

    @Input
    @Optional
    public @NotNull Property<String> getInstallPrefix() {
        return installPrefix;
    }

    @Input
    @Optional
    public @NotNull Property<String> getPlatform() {
        return platform;
    }

    @Input
    @Optional
    public @NotNull Property<String> getToolset() {
        return toolset;
    }

    @Input
    @Optional
    public @NotNull Property<Boolean> getBuildSharedLibs() {
        return buildSharedLibs;
    }

    @Input
    @Optional
    public @NotNull Property<Boolean> getBuildStaticLibs() {
        return buildStaticLibs;
    }

    @Input
    @Optional
    public @NotNull MapProperty<String, String> getDefs() {
        return defs;
    }
}
