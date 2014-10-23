/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.cli;

import de.charite.compbio.exomiser.cli.options.OptionMarshaller;
import de.charite.compbio.exomiser.core.model.ExomiserSettings;
import static de.charite.compbio.exomiser.core.model.ExomiserSettings.*;
import de.charite.compbio.exomiser.core.model.ExomiserSettings.SettingsBuilder;
import de.charite.compbio.exomiser.core.model.GeneticInterval;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
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

        SettingsBuilder settingsBuilder = new ExomiserSettings.SettingsBuilder();

        if (commandLine.hasOption(SETTINGS_FILE_OPTION)) {
            settingsBuilder = parseSettingsFile(Paths.get(commandLine.getOptionValue(SETTINGS_FILE_OPTION)));
            logger.warn("Settings file parameters will be overridden by command-line parameters!");
        }
        for (Option option : commandLine.getOptions()) {
            logger.info("--{} : {}", option.getLongOpt(), option.getValues());
            String key = option.getLongOpt();
            String[] values = option.getValues();
            setBuilderValue(key, values, settingsBuilder);
        }

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

        SettingsBuilder settingsBuilder = new ExomiserSettings.SettingsBuilder();

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

    public Collection<SettingsBuilder> parseBatchFile(Path batchFilePath) {

        List<SettingsBuilder> settingsBuilders = new ArrayList<>();

        logger.info("Parsing settings from batch file {}", batchFilePath);
        try (BufferedReader reader = Files.newBufferedReader(batchFilePath, Charset.defaultCharset())) {
            String line;
            while ((line = reader.readLine()) != null) {
                Path settingsFile = Paths.get(line);
                SettingsBuilder settingsBuilder = parseSettingsFile(settingsFile);
                settingsBuilders.add(settingsBuilder);
            }

        } catch (IOException ex) {
            logger.error("Unable to parse batch file {}", batchFilePath, ex);
        }
        return settingsBuilders;
    }

    private void setBuilderValue(String key, String[] values, SettingsBuilder settingsBuilder) {
              
        if (optionMarshallers.containsKey(key)) {
            OptionMarshaller optionMarshaller = optionMarshallers.get(key);
            optionMarshaller.applyValuesToSettingsBuilder(values, settingsBuilder);
        }
    }
}
