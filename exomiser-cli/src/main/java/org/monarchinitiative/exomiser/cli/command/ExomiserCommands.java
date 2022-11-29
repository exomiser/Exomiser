package org.monarchinitiative.exomiser.cli.command;

import picocli.CommandLine;
import picocli.CommandLine.Command;

import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

@Command(name = "exomiser",
        mixinStandardHelpOptions = true,
        versionProvider = ExomiserCommands.PropertiesVersionProvider.class,
        subcommands = {
                AnalysisCommand.class,
                BatchCommand.class,
                DiffReanalysisCommand.class
        }
)
public non-sealed class ExomiserCommands implements ExomiserCommand {

    /**
     * {@link CommandLine.IVersionProvider} implementation that returns version information from a {@code /exomiser.version} file in the classpath.
     */
    static class PropertiesVersionProvider implements CommandLine.IVersionProvider {
        public String[] getVersion() throws Exception {
            URL url = getClass().getResource("/exomiser.version");
            if (url == null) {
                return new String[]{"No exomiser.version file found in the classpath. Is exomiser-core.jar in the classpath?"};
            }
            try (InputStream inputStream = url.openStream()) {
                Properties properties = new Properties();
                properties.load(inputStream);
                return new String[]{
                        properties.getProperty("buildVersion")
                };
            }
        }
    }
}
