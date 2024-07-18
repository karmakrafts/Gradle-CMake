package io.karma.gradlecm;

import org.gradle.api.GradleException;
import org.gradle.api.logging.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.List;

/**
 * @author Marco 'freudi74' Freudenberger
 * @author Alexander 'KitsuneAlex' Hinze
 * @since 28/05/2019
 */
public final class CMakeExecutor {
    private final Logger logger;
    private final String taskName;

    CMakeExecutor(final @NotNull Logger logger, final @NotNull String taskName) {
        this.logger = logger;
        this.taskName = taskName;
    }

    void exec(final @NotNull List<String> cmdLine, final @NotNull File workingFolder) {
        try {
            final StringBuilder sb = new StringBuilder("  CMakePlugin.task " + taskName + " - exec: ");

            for (final String s : cmdLine) {
                sb.append(s).append(" ");
            }

            logger.info(sb.toString());

            final ProcessBuilder processBuilder = new ProcessBuilder(cmdLine);
            processBuilder.directory(workingFolder);

            if (workingFolder.mkdirs()) {
                logger.info("Working directory already exists, skipping creation");
            }

            final Process process = processBuilder.start();

            String line;

            try (final BufferedReader oReader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                try (final BufferedReader eReader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
                    while ((line = oReader.readLine()) != null) {
                        logger.info(line);
                    }
                    if ((line = eReader.readLine()) != null) {
                        logger.error("  CMakePlugin.cmakeConfigure - ERRORS: ");

                        do
                        {
                            logger.error(line);
                        }
                        while ((line = eReader.readLine()) != null);
                    }
                }
            }

            final int exitCode = process.waitFor();

            if (exitCode != 0) {
                throw new GradleException(String.format("CMake returned with abnormal exit code: %d", exitCode));
            }
        }
        catch(Throwable error) {
            logger.error("Could not execute command: {}", error.toString());
        }
    }
}

