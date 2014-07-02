/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.cli;

import static de.charite.compbio.exomiser.cli.CommandLineOption.*;
import de.charite.compbio.exomiser.core.ExomiserSettings;
import de.charite.compbio.exomiser.core.ExomiserSettings.Builder;
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

    private static final String VCF = VCF_OPTION.getLongOption();

    public CommandLineParser(Options options) {
        this.options = options;
    }

    /**
     * Parse the command line.
     *
     * @param args the list of arguments from the command line
     * @return an ExomiserSettings object built from the command line.
     */
    public ExomiserSettings parseCommandLineArguments(String[] args) {

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

    private ExomiserSettings parseCommandLine(CommandLine commandLine) {

        logger.info("Parsing {} command line options:", commandLine.getOptions().length);

        Builder settingsBuilder = new ExomiserSettings.Builder();

        String settingsFile = SETTINGS_FILE_OPTION.getLongOption();
        if (commandLine.hasOption(settingsFile)) {
            settingsBuilder = parseSettingsFile(Paths.get(commandLine.getOptionValue(settingsFile)));
            logger.warn("Settings file parameters will be overridden by command-line parameters!");
        }
        for (Option option : commandLine.getOptions()) {
            logger.info("--{} : {}", option.getLongOpt(), option.getValues());
            CommandLineOption commandLineOption = CommandLineOption.valueOfLongOption(option.getLongOpt());
            switch (commandLineOption) {
                //REQUIRED
                case VCF_OPTION:
                    settingsBuilder.vcfFilePath(Paths.get(option.getValue()));
                    break;
                case PED_OPTION:
                    settingsBuilder.pedFilePath(Paths.get(option.getValue()));
                    break;
                case PRIORITISER_OPTION:
                    settingsBuilder.usePrioritiser(PriorityType.valueOfCommandLine(option.getValue()));
                    break;

                //FILTER OPTIONS
                case MAX_FREQ_OPTION:
                    settingsBuilder.maximumFrequency(Float.parseFloat(option.getValue()));
                    break;
                case INTERVAL_OPTION:
                    settingsBuilder.geneticInterval(option.getValue());
                    break;
                case MIN_QUAL_OPTION:
                    settingsBuilder.minimumQuality(Float.parseFloat(option.getValue()));
                    break;
                case INCLUDE_PATHOGENIC_OPTION:
                    //default is false
                    settingsBuilder.includePathogenic(true);
                    break;
                case REMOVE_DBSNP_OPTION:
                    //default is false
                    settingsBuilder.removeDbSnp(true);
                    break;
                case REMOVE_OFF_TARGET_OPTION:
                    //default is true
                    settingsBuilder.removeOffTargetVariants(false);
                    break;

                //PRIORITISER OPTIONS
                case CANDIDATE_GENE_OPTION:
                    settingsBuilder.candidateGene(option.getValue());
                    break;
                case HPO_IDS_OPTION:
                    settingsBuilder.hpoIdList(parseHpoStringList(option.getValue()));
                    break;
                case SEED_GENES_OPTION:
                    settingsBuilder.seedGeneList(makeIntegerList(option.getValue()));
                    break;
                case DISEASE_ID_OPTION:
                    settingsBuilder.diseaseId(option.getValue());
                    break;
                case INHERITANCE_MODE_OPTION:
                    settingsBuilder.modeOfInheritance(parseInheritanceMode(option.getValue()));
                    break;

                //OUTPUT OPTIONS
                case NUM_GENES_OPTION:
                    settingsBuilder.numberOfGenesToShow(Integer.parseInt(option.getValue()));
                    break;
                case OUT_FILE_OPTION:
                    //TODO: out-file and out-format are now somewhat inter-dependent
                    settingsBuilder.outFileName(option.getValue());
                    break;
                case OUT_FORMAT_OPTION:
                    settingsBuilder.outputFormat(parseOutputFormat(option.getValue()));
                    break;
            }
        }

        return settingsBuilder.build();

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
            case "AD":
                return ModeOfInheritance.AUTOSOMAL_DOMINANT;
            case "X":
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

    /**
     * Parses the settings file and sets the values from this into a settings
     * object.
     *
     * @param value
     * @param settingsBuilder
     */
    private Builder parseSettingsFile(Path settingsFile) {

        Builder settingsBuilder = new ExomiserSettings.Builder();
        
        try (Reader reader = Files.newBufferedReader(settingsFile, Charset.defaultCharset());) {
            Properties settingsProperties = new Properties();

            settingsProperties.load(reader);
            logger.info("Loaded settings from properties file: {}", settingsProperties);
            //REQUIRED
            settingsBuilder.vcfFilePath(Paths.get(settingsProperties.getProperty(VCF)));
            settingsBuilder.pedFilePath(Paths.get(settingsProperties.getProperty(PED)));
            settingsBuilder.usePrioritiser(PriorityType.valueOfCommandLine(settingsProperties.getProperty(PRIORITISER)));
            
            //FILTER SETTINGS
            settingsBuilder.maximumFrequency(Float.parseFloat(settingsProperties.getProperty(MAX_FREQ)));
            settingsBuilder.geneticInterval(settingsProperties.getProperty(INTERVAL));
            settingsBuilder.minimumQuality(Float.parseFloat(settingsProperties.getProperty(MIN_QUAL)));
            settingsBuilder.includePathogenic(Boolean.valueOf(settingsProperties.getProperty(INCLUDE_PATHOGENIC)));
            settingsBuilder.removeDbSnp(Boolean.valueOf(settingsProperties.getProperty(REMOVE_DBSNP)));
            settingsBuilder.removeOffTargetVariants(Boolean.valueOf(settingsProperties.getProperty(REMOVE_OFF_TARGET)));

            //PRIORITISER OPTIONS
            settingsBuilder.candidateGene(settingsProperties.getProperty(CANDIDATE_GENE));
            settingsBuilder.hpoIdList(parseHpoStringList(settingsProperties.getProperty(HPO_IDS)));
            settingsBuilder.seedGeneList(makeIntegerList(settingsProperties.getProperty(SEED_GENES)));
            settingsBuilder.diseaseId(settingsProperties.getProperty(DISEASE_ID));
            settingsBuilder.modeOfInheritance(parseInheritanceMode(settingsProperties.getProperty(INHERITANCE_MODE)));

            //OUTPUT OPTIONS
            settingsBuilder.numberOfGenesToShow(Integer.parseInt(settingsProperties.getProperty(NUM_GENES)));
            //TODO: out-file and out-format are now somewhat inter-dependent
            settingsBuilder.outFileName(settingsProperties.getProperty(OUT_FILE));
            settingsBuilder.outputFormat(parseOutputFormat(settingsProperties.getProperty(OUT_FORMAT)));
        } catch (IOException ex) {
            logger.error("Unable to parse settings from file {}", settingsFile, ex);
        }
        return settingsBuilder;
    }
    private static final String PED = PED_OPTION.getLongOption();
    private static final String PRIORITISER = PRIORITISER_OPTION.getLongOption();
    private static final String MAX_FREQ = MAX_FREQ_OPTION.getLongOption();
    private static final String INTERVAL = INTERVAL_OPTION.getLongOption();
    private static final String MIN_QUAL = MIN_QUAL_OPTION.getLongOption();
    private static final String INCLUDE_PATHOGENIC = INCLUDE_PATHOGENIC_OPTION.getLongOption();
    private static final String REMOVE_DBSNP = REMOVE_DBSNP_OPTION.getLongOption();
    private static final String REMOVE_OFF_TARGET = REMOVE_OFF_TARGET_OPTION.getLongOption();
    private static final String CANDIDATE_GENE = CANDIDATE_GENE_OPTION.getLongOption();
    private static final String HPO_IDS = HPO_IDS_OPTION.getLongOption();
    private static final String SEED_GENES = SEED_GENES_OPTION.getLongOption();
    private static final String DISEASE_ID = DISEASE_ID_OPTION.getLongOption();
    private static final String INHERITANCE_MODE = INHERITANCE_MODE_OPTION.getLongOption();
    private static final String NUM_GENES = NUM_GENES_OPTION.getLongOption();
    private static final String OUT_FILE = OUT_FILE_OPTION.getLongOption();
    private static final String OUT_FORMAT = OUT_FORMAT_OPTION.getLongOption();

}
