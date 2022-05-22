package io.karma.gradlecm;

import org.gradle.api.DefaultTask;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.TaskAction;

/**
 * Provides automatic set-up and configuration of
 * a Clang-based cross-compilation toolchain on the fly.
 * This may require root/administrative privileges.
 *
 * @author Alexander Hinze
 * @since 22/05/2022
 */
public class CMakeSetupTask extends DefaultTask {
    CMakeSetupTask() {}

    @TaskAction
    public void performAction() {
        // TODO: ...
    }
}
