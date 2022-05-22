package io.karma.gradlecm;

import io.karma.kommons.function.Functions;
import io.karma.kommons.util.ExceptionUtils;
import io.karma.kommons.util.SystemInfo;
import org.gradle.api.GradleException;
import org.gradle.api.logging.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * @author Marco 'freudi74' Freudenberger
 * @author Alexander 'KitsuneAlex' Hinze
 * @since 28/05/2019
 */
public final class CMakeExecutor {
    // @formatter:off
    private static final String[] envCmd = SystemInfo.isWindows()
        ? new String[]{"cmd.exe", "/c", "set"}
        : new String[]{"sh", "-c", "export"};
    // @formatter:on

    private final Logger logger;
    private final String taskName;
    private final HashMap<String, String> env = new HashMap<>();

    CMakeExecutor(final @NotNull Logger logger, final @NotNull String taskName) {
        this.logger = logger;
        this.taskName = taskName;
    }

    void setEnv(final @NotNull Map<String, String> env) {
        this.env.putAll(env);
    }

    void exec(final @NotNull List<String> cmdLine, final @NotNull File workingFolder) {
        Functions.tryDo(() -> {
            final StringBuilder sb = new StringBuilder("  CMakePlugin.task " + taskName + " - exec: ");

            for (final String s : cmdLine) {
                sb.append(s).append(" ");
            }

            logger.info(sb.toString());

            if (!env.isEmpty()) {
                logger.info("Setting up shell environment variables");

                final ArrayList<String> auxCommands = new ArrayList<>();
                final Set<Entry<String, String>> entries = env.entrySet();

                for(final Entry<String, String> entry : entries) {
                    auxCommands.addAll(Arrays.asList(envCmd));
                    auxCommands.add(String.format("%s=%s", entry.getKey(), entry.getValue()));
                    auxCommands.add("&"); // Joining commands un-conditionally works the same on all OSs, luckily
                }

                cmdLine.addAll(0, auxCommands); // Insert before the given CLI commands
            }

            final ProcessBuilder processBuilder = new ProcessBuilder(cmdLine);
            processBuilder.directory(workingFolder);

            if (workingFolder.mkdirs()) {
                logger.info("Working directory already exists, skipping creation");
            }

            final Process process = processBuilder.start();

            String line;

            try(final BufferedReader oReader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                try(final BufferedReader eReader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
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
        }, e -> ExceptionUtils.handleError(e, logger::error));
    }
}

