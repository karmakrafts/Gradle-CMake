package io.karma.gradlecm;

import org.jetbrains.annotations.NotNull;

import java.io.File;

/**
 * Common utility functions shared accross the plugin.
 *
 * @author Alexander Hinze
 * @since 22/05/2022
 */
final class Utils {
    // @formatter:off
    private Utils() {}
    // @formatter:on

    // TODO: move into Kommons
    static boolean deleteDirectory(final @NotNull File directoryToBeDeleted) {
        final File[] allContents = directoryToBeDeleted.listFiles();
        if (allContents != null) {
            for (final File file : allContents) {
                deleteDirectory(file);
            }
        }
        return directoryToBeDeleted.delete();
    }
}
