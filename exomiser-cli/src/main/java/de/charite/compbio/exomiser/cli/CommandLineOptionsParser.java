/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.cli;

import de.charite.compbio.exomiser.cli.options.OptionMarshaller;
import static de.charite.compbio.exomiser.cli.options.SettingsFileOptionMarshaller.SETTINGS_FILE_OPTION;
import de.charite.compbio.exomiser.core.analysis.Settings.SettingsBuilder;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Properties;
import javax.annotation.Resource;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Handles parsing of the commandline input to provide properly typed data to
 * the rest of the application in the form of an ExomiserSettings object.
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
@Component
public class CommandLineOptionsParser {

    private static final Logger logger = LoggerFactory.getLogger(CommandLineOptionsParser.class);

    @Resource
    private Map<String, OptionMarshaller> optionMarshallers;

    public SettingsBuilder parseCommandLine(CommandLine commandLine) {

        logger.info("Parsing {} command line options:", commandLine.getOptions().length);

        SettingsBuilder settingsBuilder = new SettingsBuilder();

        if (commandLine.hasOption(SETTINGS_FILE_OPTION)) {
            Path settingsFile = Paths.get(commandLine.getOptionValue(SETTINGS_FILE_OPTION));
            settingsBuilder = parseSettingsFile(settingsFile);
            logger.warn("Settings file parameters will be overridden by command-line parameters!");
        }
        for (Option option : commandLine.getOptions()) {
            logger.info("--{} : {}", option.getLongOpt(), option.getValues());
            String key = option.getLongOpt();
            String[] values = option.getValues();
            setBuilderValue(key, values, settingsBuilder);
        }

        //return a Map<Analysis, OutputSettings>
        return settingsBuilder;

    }

    /**
     * Parses the settings file and sets the values from this into a settings
     * object.
     *
     * @param settingsFile
     * @return
     */
    public SettingsBuilder parseSettingsFile(Path settingsFile) {

        SettingsBuilder settingsBuilder = new SettingsBuilder();

        try (Reader reader = Files.newBufferedReader(settingsFile, Charset.defaultCharset())) {
            Properties settingsProperties = new Properties();

            settingsProperties.load(reader);
            logger.info("Loaded settings from properties file: {}", settingsProperties);
            for (String key : settingsProperties.stringPropertyNames()) {
                String[] values = settingsProperties.getProperty(key).split(",");
                setBuilderValue(key, values, settingsBuilder);
            }

        } catch (IOException ex) {
            logger.error("Unable to parse settings from file {}", settingsFile, ex);
        }
        return settingsBuilder;
    }
    
    private void setBuilderValue(String key, String[] values, SettingsBuilder settingsBuilder) {

        if (optionMarshallers.containsKey(key)) {
            OptionMarshaller optionMarshaller = optionMarshallers.get(key);
            optionMarshaller.applyValuesToSettingsBuilder(values, settingsBuilder);
        }
    }
}
