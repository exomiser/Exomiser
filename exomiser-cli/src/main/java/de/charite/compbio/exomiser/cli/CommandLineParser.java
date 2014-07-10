/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.cli;

import static de.charite.compbio.exomiser.core.ExomiserSettings.*;
import de.charite.compbio.exomiser.core.ExomiserSettings;
import de.charite.compbio.exomiser.core.ExomiserSettings.SettingsBuilder;
import de.charite.compbio.exomiser.priority.PriorityType;
import de.charite.compbio.exomiser.util.OutputFormat;
import jannovar.common.ModeOfInheritance;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.Parser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Handles parsing of the commandline input to provide properly typed data to
 * the rest of the application in the form of an ExomiserSettings object.
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
@Component
public class CommandLineParser {

    private static final Logger logger = LoggerFactory.getLogger(CommandLineParser.class);

    @Autowired
    private final Options options;

    public CommandLineParser(Options options) {
        this.options = options;
    }

    /**
     * Parse the command line.
     *
     * @param args the list of arguments from the command line
     * @return an ExomiserSettings object built from the command line.
     */
    public SettingsBuilder parseCommandLineArguments(String[] args) {

        Parser parser = new GnuParser();
        CommandLine cmd;
        try {
            cmd = parser.parse(options, args);
            return parseCommandLine(cmd);
        } catch (ParseException ex) {
            logger.error("Unable to parse command line argumnets. Please check you have typed the parameters correctly.", ex);
        }

        return null;
    }

    private SettingsBuilder parseCommandLine(CommandLine commandLine) {

        logger.info("Parsing {} command line options:", commandLine.getOptions().length);

        SettingsBuilder settingsBuilder = new ExomiserSettings.SettingsBuilder();

        if (commandLine.hasOption(SETTINGS_FILE_OPTION)) {
            settingsBuilder = parseSettingsFile(Paths.get(commandLine.getOptionValue(SETTINGS_FILE_OPTION)));
            logger.warn("Settings file parameters will be overridden by command-line parameters!");
        }
        for (Option option : commandLine.getOptions()) {
            logger.info("--{} : {}", option.getLongOpt(), option.getValues());
            setBuilderValue(option.getLongOpt(), option.getValue(), settingsBuilder);
        }

        return settingsBuilder;

    }

    /**
     * Parses the settings file and sets the values from this into a settings
     * object.
     *
     * @param value
     * @param settingsBuilder
     */
    private SettingsBuilder parseSettingsFile(Path settingsFile) {

        SettingsBuilder settingsBuilder = new ExomiserSettings.SettingsBuilder();
        
        try (Reader reader = Files.newBufferedReader(settingsFile, Charset.defaultCharset())) {
            Properties settingsProperties = new Properties();

            settingsProperties.load(reader);
            logger.info("Loaded settings from properties file: {}", settingsProperties);
            for (String key : settingsProperties.stringPropertyNames()) {
                setBuilderValue(key, settingsProperties.getProperty(key), settingsBuilder);
            }

        } catch (IOException ex) {
            logger.error("Unable to parse settings from file {}", settingsFile, ex);
        }
        return settingsBuilder;
    }
    
    private void setBuilderValue(String key, String value, SettingsBuilder settingsBuilder) throws NumberFormatException {
        switch (key) {
            //REQUIRED
            case VCF_OPTION:
                settingsBuilder.vcfFilePath(Paths.get(value));
                break;
            case PED_OPTION:
                settingsBuilder.pedFilePath(Paths.get(value));
                break;
            case PRIORITISER_OPTION:
                settingsBuilder.usePrioritiser(PriorityType.valueOfCommandLine(value));
                break;
                
                //FILTER OPTIONS
            case MAX_FREQ_OPTION:
                settingsBuilder.maximumFrequency(Float.parseFloat(value));
                break;
            case GENETIC_INTERVAL_OPTION:
                settingsBuilder.geneticInterval(value);
                break;
            case MIN_QUAL_OPTION:
                settingsBuilder.minimumQuality(Float.parseFloat(value));
                break;
            case INCLUDE_PATHOGENIC_OPTION:
                //default is false
                settingsBuilder.includePathogenic(true);
                break;
            case REMOVE_DBSNP_OPTION:
                //default is false
                if (value == null || value.isEmpty()) {
                    //the command line is just a switch
                    settingsBuilder.removeDbSnp(true);                
                } else {
                    //but the json/properties file specify true or false
                    settingsBuilder.removeDbSnp(Boolean.parseBoolean(value));
                }
                break;
            case REMOVE_OFF_TARGET_OPTION:
                //default is true
                if (value == null || value.isEmpty()) {
                    settingsBuilder.removeOffTargetVariants(false);
                } else {
                    settingsBuilder.removeOffTargetVariants(Boolean.parseBoolean(value));
                }
                break;
                
                //PRIORITISER OPTIONS
            case CANDIDATE_GENE_OPTION:
                settingsBuilder.candidateGene(value);
                break;
            case HPO_IDS_OPTION:
                settingsBuilder.hpoIdList(parseHpoStringList(value));
                break;
            case SEED_GENES_OPTION:
                settingsBuilder.seedGeneList(makeIntegerList(value));
                break;
            case DISEASE_ID_OPTION:
                settingsBuilder.diseaseId(value);
                break;
            case MODE_OF_INHERITANCE_OPTION:
                settingsBuilder.modeOfInheritance(parseInheritanceMode(value));
                break;
                
                //OUTPUT OPTIONS
            case NUM_GENES_OPTION:
                settingsBuilder.numberOfGenesToShow(Integer.parseInt(value));
                break;
            case OUT_FILE_OPTION:
                //TODO: out-file and out-format are now somewhat inter-dependent
                settingsBuilder.outFileName(value);
                break;
            case OUT_FORMAT_OPTION:
                settingsBuilder.outputFormat(parseOutputFormat(value));
                break;
        }
    }

    @Override
    public String toString() {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("java -jar exomizer-cli-n.n.n.jar", options);
        return "ExomiserCommandLineOptionsParser{" + options + '}';
    }

    private List<String> parseHpoStringList(String value) {
        String delimiter = ",";
        logger.info("Parsing list from: ", value);

        String values[] = value.split(delimiter);

        List<String> hpoList = new ArrayList<>();
        Pattern hpoPattern = Pattern.compile("HP:[0-9]{7}");
        //I've gone for a more verbose splitting and individual token parsing 
        //instead of doing while hpoMatcher.matches(); hpoList.add(hpoMatcher.group()) 
        //on the whole input string so tha the user has a warning about any invalid HPO ids
        for (String token : values) {
            token = token.trim();
            Matcher hpoMatcher = hpoPattern.matcher(token);
            if (hpoMatcher.matches()) { /* A well formed HPO term starts with "HP:" and has ten characters. */

                //ideally we need an HPO class as the second half of the ID is an integer.
                //TODO: add Hpo class to exomiser.core - Phenodigm.core already has one.

                hpoList.add(token);
            } else {
                logger.error("Malformed HPO input string \"{}\". Term \"{}\" does not match the HPO identifier pattern: {}", value, token, hpoPattern);
            }
        }

        return hpoList;
    }

    private ModeOfInheritance parseInheritanceMode(String value) {
        switch (value) {
            case "AR":
                return ModeOfInheritance.AUTOSOMAL_RECESSIVE;
            case "AUTOSOMAL_RECESSIVE":
                return ModeOfInheritance.AUTOSOMAL_RECESSIVE;
            case "AD":
                return ModeOfInheritance.AUTOSOMAL_DOMINANT;
            case "AUTOSOMAL_DOMINANT":
                return ModeOfInheritance.AUTOSOMAL_DOMINANT;
            case "X":
                return ModeOfInheritance.X_RECESSIVE;
            case "X_RECESSIVE":
                return ModeOfInheritance.X_RECESSIVE;
            default:
                logger.error("value {} is not one of AR, AD or X - inheritance mode has not been set", value);
                return ModeOfInheritance.UNINITIALIZED;
        }
    }

    private OutputFormat parseOutputFormat(String value) {
        switch (value) {
            case "HTML":
                return OutputFormat.HTML;
            case "TAB":
                return OutputFormat.TSV;
            case "TSV":
                return OutputFormat.TSV;
            case "VCF":
                return OutputFormat.VCF;
            default:
                return OutputFormat.HTML;
        }
    }

    private List<Integer> makeIntegerList(String value) {
        String delimiter = ",";

        List<Integer> returnList = new ArrayList<>();

        Pattern mgiGeneIdPattern = Pattern.compile("[0-9]+");

        for (String string : value.split(delimiter)) {
            Matcher mgiGeneIdPatternMatcher = mgiGeneIdPattern.matcher(string);
            if (mgiGeneIdPatternMatcher.matches()) {
                Integer integer = Integer.parseInt(string.trim());
                returnList.add(integer);
            } else {
                logger.error("Malformed MGI gene ID input string \"{}\". Term \"{}\" does not match the MGI gene ID identifier pattern: {}", value, string, mgiGeneIdPattern);
            }
        }

        return returnList;
    }
    
}
