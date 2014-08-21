/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.charite.compbio.exomiser.cli;

import de.charite.compbio.exomiser.core.model.ExomiserSettings;
import static de.charite.compbio.exomiser.core.model.ExomiserSettings.*;
import de.charite.compbio.exomiser.core.model.ExomiserSettings.SettingsBuilder;
import de.charite.compbio.exomiser.core.model.GeneticInterval;
import de.charite.compbio.exomiser.priority.PriorityType;
import de.charite.compbio.exomiser.core.writer.OutputFormat;
import jannovar.common.ModeOfInheritance;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
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

    public SettingsBuilder parseCommandLine(CommandLine commandLine) {

        logger.info("Parsing {} command line options:", commandLine.getOptions().length);

        SettingsBuilder settingsBuilder = new ExomiserSettings.SettingsBuilder();

        if (commandLine.hasOption(SETTINGS_FILE_OPTION)) {
            settingsBuilder = parseSettingsFile(Paths.get(commandLine.getOptionValue(SETTINGS_FILE_OPTION)));
            logger.warn("Settings file parameters will be overridden by command-line parameters!");
        }
        for (Option option : commandLine.getOptions()) {
            logger.info("--{} : {}", option.getLongOpt(), option.getValues());
            setBuilderValue(option.getLongOpt(), option.getValues(), settingsBuilder);
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
                setBuilderValue(key, settingsProperties.getProperty(key).split(","), settingsBuilder);
            }

        } catch (IOException ex) {
            logger.error("Unable to parse settings from file {}", settingsFile, ex);
        }
        return settingsBuilder;
    }

    private void setBuilderValue(String key, String[] values, SettingsBuilder settingsBuilder) throws NumberFormatException {
        String value = "";
        if (values != null) {
            value = values[0];
        }

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
                if (value == null || value.isEmpty()) {
                    //use the default null value
                    break;
                }
                settingsBuilder.geneticInterval(GeneticInterval.parseString(value));
                break;
            case MIN_QUAL_OPTION:
                settingsBuilder.minimumQuality(Float.parseFloat(value));
                break;
            case KEEP_NON_PATHOGENIC_MISSENSE_OPTION:
                //default is true
                settingsBuilder.keepNonPathogenicMissense(Boolean.parseBoolean(value));
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
                settingsBuilder.hpoIdList(parseHpoStringList(values));
                break;
            case SEED_GENES_OPTION:
                settingsBuilder.seedGeneList(parseEntrezSeedGeneList(values));
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
                settingsBuilder.outputFormats(parseOutputFormat(values));
                break;
        }
    }

    @Override
    public String toString() {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("java -jar exomizer-cli-n.n.n.jar", options);
        return "ExomiserCommandLineOptionsParser{" + options + '}';
    }

    private List<String> parseHpoStringList(String[] values) {
        logger.debug("Parsing HPO values from: {}", values);

        List<String> hpoList = new ArrayList<>();

        if (values.length == 0) {
            return hpoList;
        }

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
                logger.error("Malformed HPO input string \"{}\". Term \"{}\" does not match the HPO identifier pattern: {}", values, token, hpoPattern);
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

    private Set<OutputFormat> parseOutputFormat(String[] values) {
        List<OutputFormat> outputFormats = new ArrayList<>();
        logger.debug("Parsing output options: {}", values);

        for (String outputFormatString : values) {
            switch (outputFormatString.trim()) {
                case "HTML":
                    outputFormats.add(OutputFormat.HTML);
                    break;
                case "TAB":
                    outputFormats.add(OutputFormat.TSV);
                    break;
                case "TSV":
                    outputFormats.add(OutputFormat.TSV);
                    break;
                case "VCF":
                    outputFormats.add(OutputFormat.VCF);
                    break;
                default:
                    logger.info("{} is not a recognised output format. Please choose one or more of HTML, TAB, VCF - defaulting to HTML", outputFormatString);
                    outputFormats.add(OutputFormat.HTML);
                    break;
            }
        }
        logger.debug("Setting output formats: {}", outputFormats);
        return EnumSet.copyOf(outputFormats);
    }

    private List<Integer> parseEntrezSeedGeneList(String[] values) {

        List<Integer> returnList = new ArrayList<>();

        if (values.length == 0) {
            return returnList;
        }

        Pattern entrezGeneIdPattern = Pattern.compile("[0-9]+");

        for (String string : values) {
            Matcher entrezGeneIdPatternMatcher = entrezGeneIdPattern.matcher(string);
            if (entrezGeneIdPatternMatcher.matches()) {
                Integer integer = Integer.parseInt(string.trim());
                returnList.add(integer);
            } else {
                logger.error("Malformed Entrez gene ID input string \"{}\". Term \"{}\" does not match the Entrez gene ID identifier pattern: {}", values, string, entrezGeneIdPattern);
            }
        }

        return returnList;
    }

}
