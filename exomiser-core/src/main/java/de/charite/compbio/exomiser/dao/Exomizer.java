package de.charite.compbio.exomiser.dao;

import de.charite.compbio.exomiser.util.Prioritiser;
import de.charite.compbio.exomiser.core.SampleData;
import de.charite.compbio.exomiser.exception.ExomizerException;
import de.charite.compbio.exomiser.exception.ExomizerInitializationException;
import de.charite.compbio.exomiser.exome.Gene;
import de.charite.compbio.exomiser.exome.VariantEvaluation;
import de.charite.compbio.exomiser.filter.Filter;
import de.charite.compbio.exomiser.filter.FilterType;
import de.charite.compbio.exomiser.filter.TargetFilter;
import de.charite.compbio.exomiser.io.ExomiserDatabase;
import de.charite.compbio.exomiser.io.PublishedMutationSearcher;
import de.charite.compbio.exomiser.io.html.HTMLWriter;
import de.charite.compbio.exomiser.io.html.HTMLWriterCRE;
import de.charite.compbio.exomiser.io.html.HTMLWriterWalker;
import de.charite.compbio.exomiser.priority.InheritancePriority;
import de.charite.compbio.exomiser.priority.Priority;
import de.charite.compbio.exomiser.priority.PriorityFactory;
import de.charite.compbio.exomiser.priority.util.DataMatrix;
import de.charite.compbio.exomiser.reference.Network;
import de.charite.compbio.exomiser.reference.STRINGNetwork;
import de.charite.compbio.exomiser.util.OutputFormat;
import de.charite.compbio.exomiser.writer.ResultsWriter;
import de.charite.compbio.exomiser.writer.ResultsWriterFactory;
import de.charite.compbio.exomiser.writer.TsvResultsWriter;
import jannovar.common.ModeOfInheritance;
import jannovar.exome.Variant;
import jannovar.exome.VariantTypeCounter;
import jannovar.pedigree.Pedigree;
import jannovar.reference.TranscriptModel;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.sql.DataSource;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.Parser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is the main driver class for analyzing VCF files. It uses the
 * io.VCFReader and other classes to input the VCF data and to create a list of
 * Variant objects that are then filtered according to data in the postgreSQL
 * database created by the
 * {@link exomizer.PopulateExomiserDatabase PopulateExomiserDatabase}, program,
 * as well as data about gene models from the UCSC database (See
 * {@link jannovar.reference.TranscriptModel TranscriptModel}). The candidate
 * genes are then filtered and prioritized according to flexible criteria. files
 * with the results of filtering.
 * <P>
 * The Exomizer can be started via the command line, in which case the main
 * function will use the {@link #Exomizer(String[])} constructor. Alternatively,
 * the Exomizer can be started from the Apacha tomcat framework (ExomeWalker
 * code). In this case, the {@link #Exomizer()} constructor should be used, and
 * the various setter functions should be used to set the parameters.
 * <P>
 * When started from the command line, the Exomizer will output a single
 * self-contained HTML file that also includes CSS style information.
 *
 * @author Peter N Robinson
 * @version 0.65 (9 February, 2014)
 */
public class Exomizer {

    private static final Logger logger = LoggerFactory.getLogger(Exomizer.class);

    private DataSource dataSource;

    /**
     * Temporary setter for use until this can be removed.
     * @param dataSource
     * @deprecated
     */
    @Deprecated
    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * This flag indicates that we are running on our internal server, i.e.,
     * within the hospital firewall, meaning that we are allowed to show all
     * HGMD data.
     */
    private boolean withinFirewall = false;
    /**
     * A BufferedReader file handle that has been created elsewhere (e.g., by a
     * tomcat server). This replaces the vcf_file (a path to a file on disk),
     * and is intended to be used for an InputStream that has been created from
     * a String for uploading user data on a tomcat server.
     */
    private BufferedReader vcfBufferedReader = null;
    /**
     * Full path to the location of the random walk matrix file. If this
     * variable is non-null, it causes the Exomizer to prioritize on this.
     */
    private String randomWalkFilePath = null;
    /**
     * Full path to the random walk index file. This must be non-null if the
     * random-walk analysis is tobe performed.
     */
    private String randomWalkIndexPath = null;
    /**
     * Comma-separated list of entrez gene ids, as given via the command line or
     * tomcat, a user parameter for prioritizing with Random walk.
     */
    private String entrezSeedGenes = null;
    /**
     * File to which we will write results, name can be changed via command
     * line.
     */
    private String outfile = "exomizer.html";

    /**
     * List of all transcripts used for annotation the variants (For instance,
     * from the knownGene.txt file from UCSC). See the Jannovar package for
     * details.
     */
    private List<TranscriptModel> knownGenesList = null;

//    /**
//     * Map of Chromosomes
//     */
//    private HashMap<Byte, Chromosome> chromosomeMap = null;
//    /**
//     * Name of file with serialized UCSC data. This needs to be created from
//     * several UCSC KnownGene files by the Annotator code in this package (see
//     * the {@code Jannovar} program).
//     */
//    private String UCSCserializedFile = null;
    /**
     * Flag as to whether a TSV file should be output instead of HTML. Not that
     * this flag can be used in combination with the command line flag -o to set
     * the same of the outfile. The default behaviour is to output an HTML file
     * (this variable is thus initialized to {@code false}).
     */
    private boolean useTSV = false;
    /**
     * Flag that indicates whether output should be in form of a ranked VCF
     * file. (Note: untested)
     */
    private boolean useVCF = false;
    /**
     * Database handle to the postgreSQL database used by this application.
     */
    private Connection connection = null;
    /**
     * An optional interval of the form "chr2:12345-67890" that will limit the
     * search to genes within the interval (usually a linkage interval).
     */
    private String interval = null;
    /**
     * Number of prioritized genes to show in output file (if this is null, then
     * all genes are shown).
     */
    private Integer numberOfGenesToShow = null;

    /**
     * Should the exomizer output a TSV file instead of HTML?
     */
    public boolean useTSVFile() {
        return this.useTSV;
    }

    /**
     * Should the exomizer output a VCF file instead of HTML?
     */
    public boolean useVCFFile() {
        return this.useVCF;
    }
    // The following are to be used for initializing the Filter classes
    /**
     * Frequency threshold for variants. Only variants that are either not
     * recorded in the thousand genomes/ESP data or that are rarer than this
     * threshold will be retained. Note that the threshold is expected to be a
     * value such as 0.01, but it is stored here as a String because the
     * Filter-derived classes have a standard function for initialization that
     * will use this value.
     */
    private String frequency_threshold = null;
    /**
     * One of AD, AR, or XR (X chromosomal recessive). If uninitialized, this
     * prioritizer has no effect).
     */
    private ModeOfInheritance inheritanceMode = ModeOfInheritance.UNINITIALIZED;
    /**
     * Quality threshold for variants. Corresponds to QUAL column in VCF file.
     */
    private String quality_threshold = null;
    /**
     * Known or candidate gene for TSV output/ROC analysis
     */
    private String candidateGene = null;
    /**
     * This is a 6-digit OMIM code that is to be passed via the command line and
     * is currently intended to be used for MGI phenodigm prioritization: Why
     * model organism most closely resembles this target OMIM disease
     * phenotypically?
     */
    private String disease = null;
    /**
     * List of HPO Term ids (as a String) for use in phenotypic prioritization.
     */
    private String hpo_ids = null;
    /**
     * Filter out all variants with a dbSNP rs ID or entgered in the ESP data,
     * regardless of their frequency
     */
    private boolean filterOutAlldbSNP = false;
    private boolean use_mgi_phenodigm_filter = false;
    private boolean use_zfin_phenodigm_filter = false;
    private boolean use_pathogenicity_filter = false;
    private boolean use_target_filter = true;
    /**
     * Flag to indicate that we will use the BOQA prioritizer.
     */
    private boolean useBOQA = false;
    /**
     * Flag to indicate that we will use the CRE prioritizer.
     */
    private boolean useCRE = false;
    /**
     * Flag to indicate that we will use the Random Walk prioritizer.
     */
    private boolean useRandomWalk = false;
    /**
     * This String can be set to AD, AR, or X to initiate filtering according to
     * inheritance pattern.
     */
    private String inheritance_filter_type = null;
    /**
     * HTML summary of parsing the VCF file
     */
    private ArrayList<String> status_message = null;
    /**
     * Path to the human-phenotype-ontology.obo file (Needed for Phenomizer and
     * BOQA)
     */
    private String hpoOntologyFile = null;
    /**
     * Path to the phenotype_annotation.tab file (Needed for Phenomizer and
     * BOQA)
     */
    private String hpoAnnotationFile = null;
    /**
     * Path to a directory with data files needed for Phenomizer (It must
     * contain: Orphanet data (www.orphadata.org) en_product1.xml and
     * en_product6.xml. OMIM data (must register at omim.org): mim2gene.txt,
     * genemap. NCBI-data (ftp://ftp.ncbi.nlm.nih.gov/gene/DATA/): gene_info.gz
     */
    private String phenomizerDataDirectory = null;
    /**
     * An object that will be used to output the results. Note that there is a
     * small class hierarchy for different kinds of output.
     */
    private HTMLWriter htmlWriter = null;
    private Prioritiser prioritiser = null;
    /**
     * Name of the disease gene family (an OMIM phenotypic series) that is being
     * used for prioritization with ExomeWalker.
     */
    private String diseaseGeneFamilyName = null;
    /*
     * randomWalk matrix object to be held in memory for Exomiserv2 server
     */
    private DataMatrix randomWalkMatrix = null;

//    public static void main(String argv[]) {
//        /**
//         * ***********************************************************
//         */
//        /*
//         * 1) Open the connection to database.
//         */
//        /**
//         * ***********************************************************
//         */
//        Exomizer exomizer = new Exomizer();
//        logger.info("STARTING EXOMISER");
//        exomizer.parseCommandLineArguments(argv);
//        
//        if (exomizer.outfile == null) {
//            logger.error("Outfile not indicated, terminating program execution...");
//            System.exit(1);
//        }
//        try {
//            exomizer.openNewDatabaseConnection();
//        } catch (ExomizerInitializationException ex) {
//            logger.error("Could not open SQL connection. Terminating program...", ex);
//            System.exit(1);
//        }
//        /**
//         * ***********************************************************
//         */
//        /*
//         * 2) Input the transcript definition file from Jannovar that
//         */
//        /*
//         * is used to annotate the variants.
//         */
//        /**
//         * ***********************************************************
//         */
//        try {
//            exomizer.deserializeUCSCdata();
//        } catch (ExomizerException e) {
//            logger.error("Error with deserialization:", e);
//            System.exit(1);
//        }
//        /**
//         * ***********************************************************
//         */
//        /*
//         * 3) Read a VCF file (this creates an ArrayList of Variants)
//         */
//        /**
//         * ***********************************************************
//         */
//        try {
//            exomizer.parseVCFFile();
//        } catch (ExomizerException e) {
//            logger.error("Error with VCF input:", e);
//            System.exit(1);
//        }
//        /**
//         * ***********************************************************
//         */
//        /*
//         * 4) Read a PED file if the VCF file has multiple samples
//         */
//        /**
//         * ***********************************************************
//         */
//        try {
//            exomizer.processPedigreeData();
//            /*
//             * Note: for single sample VCF files, this method will construct a
//             * "dummy" pedigree object.
//             */
//        } catch (ExomizerException e) {
//            logger.error("Error with pedigree data input:", e);
//            System.exit(1);
//        }
//        /**
//         * ***********************************************************
//         */
//        /*
//         * 5) This function takes care of most of the analysis.
//         */
//        /**
//         * ***********************************************************
//         */
//        try {
//            logger.info("INIT FILTERS AND PRIORITISERS");
//            exomizer.initializeFiltersAndPrioritizers();
//            logger.info("ANNOTATING VARIANTS");
//            exomizer.annotateVariants();
//            logger.info("FILTERING AND PRIORITISING");
//            exomizer.executePrioritization();
//        } catch (ExomizerException e) {
//            logger.error("Error while prioritizing VCF data: ", e);
//            System.exit(1);
//        }
//        logger.info("OUTPUTTING RESULTS");
//        /**
//         * ***********************************************************
//         */
//        /*
//         * 6) Output to HTML (default) or TSV (needs to be set via
//         */
//        /*
//         * the --tsv flag on the command line)
//         */
//        /**
//         * ***********************************************************
//         */
//        if (exomizer.useTSVFile()) {
//            exomizer.outputTSV();
//        } else if (exomizer.useVCFFile()) {
//            exomizer.outputVCF();
//        } else {
//            /*
//             * The following function decides based on the flag useCRE, useBOQA,
//             * or useRandomWalk what kind of HTML to produce.
//             */
//            try {
//                exomizer.outputHTML();
//            } catch (ExomizerException e) {
//                logger.error("Error writing output: ", e);
//                System.exit(1);
//            }
//        }
//        logger.info("FINISHED EXOMISER");
//
//    }
    /**
     * The constructor sets a few class variables based on command line
     * arguments.
     *
     * @param argv an array with the command line arguments
     */
//    public Exomizer(String argv[]) {
//        logger.info("STARTING EXOMISER");
//        parseCommandLineArguments(argv);
//        this.status_message = new ArrayList<String>();
//        if (outfile == null) {
//            logger.error("Outfile not indicated, terminating program execution...");
//            System.exit(1);
//        }
//    }
//    public Exomizer(DataSource dataSource) {
//        this.dataSource = dataSource;
//        this.status_message = new ArrayList<String>();
//    }

    /**
     * This constructor, without arguments, is intended to be used by the Tomcat
     * framework. Client code will need to set parameters using setters rather
     * than via the command line.
     * @deprecated 
     */
    @Deprecated
    public Exomizer() {
        logger.info("STARTING EXOMISER");
        this.status_message = new ArrayList<String>();
    }

    /**
     * Constructor for web app
     *
     * @param ucsc_file A serialized file with the UCSC knownGene objects use
     * for annotating variants.
     */
//    public Exomizer(String ucsc_file) {
//        this.UCSCserializedFile = ucsc_file;
//        try {
//            deserializeUCSCdata();
//        } catch (ExomizerException e) {
//            logger.error("Error trying to de-serialise UCSC HG data: ", e);
//            System.exit(1);
//        }
//    }
//    /**
//     * Constructor for Sanger web app
//     */
//    public Exomizer(HashMap<Byte, Chromosome> cMap) {
//        this.chromosomeMap = cMap;
//    }
//    /**
//     * @return the number of samples represented in the VCF file.
//     */
//    public int getNumberOfSamples() {
//        return this.n_samples;
//    }
//    /**
//     * @return List of Strings representing the sample names in the VCF file.
//     */
//    public List<String> getVCFSampleNames() {
//        return this.sampleNames;
//    }
    /**
     * Use the logic in the Prioritiser class to perform filtering and
     * prioritizing genes, and ranking the candidate genes. (see
     * {@link exomizer.priority.Prioritiser Prioritiser}).
     * <p>
     * Note that we assume that the transcript definition data has been
     * deserialized before this method is called. Also, the annotation of the
     * variants must have been performed.
     * <P>
     * Note that we now are downweighting genes that have a lot of variants This
     * will give genes such as the HLA genes lower scores (they tend to have
     * some many rare variants, that at least one has a good pathogenicity
     * score).
     *
     * This method is deprecated - use the
     * executePrioritization(List<VariantEvaluation> variantList) method
     * instead.
     *
     * @throws Exception if something goes wrong with processing the VCF file.
     */
    @Deprecated
    public void executePrioritization() throws ExomizerException {
        this.geneList = this.prioritiser.executePrioritization(this.variantList, this.useRankBasedScoring());
    }

    /**
     * Use the logic in the Prioritiser class to perform filtering and
     * prioritizing genes, and ranking the candidate genes. (see
     * {@link exomizer.priority.Prioritiser Prioritiser}).
     * <p>
     * Note that we assume that the transcript definition data has been
     * deserialized before this method is called. Also, the annotation of the
     * variants must have been performed.
     * <P>
     * Note that we now are downweighting genes that have a lot of variants This
     * will give genes such as the HLA genes lower scores (they tend to have
     * some many rare variants, that at least one has a good pathogenicity
     * score).
     *
     * @param variantList
     * @return a list of prioritised genes
     */
    public List<Gene> executePrioritization(List<VariantEvaluation> variantList) {
        List<Gene> prioritisedGenes = this.prioritiser.executePrioritization(variantList, this.useRankBasedScoring());
        return prioritisedGenes;
    }

    /**
     * This function decides whether to do scoring of genes according to the raw
     * score (default) or by rank (used for Phenomizer and for GeneWanderer
     * applications).
     */
    private boolean useRankBasedScoring() {
        if (this.useCRE) {
            return false;
        } else {
            return this.useRandomWalk;
        }
    }

//    /**
//     * <P> Jannovar makes a serialized file that represents a
//     * HashMap<String,TranscriptModel> containing each and every
//     * {@link jannovar.reference.TranscriptModel TranscriptModel} object. This
//     * method both deserializes this file and also adds each TranscriptModel to
//     * the corresponding IntervalTree of the
//     * {@link jannovar.reference.Chromosome Chromosome} object. When we are
//     * done, the {@link exomizer.Exomizer#chromosomeMap} contains Chromosome
//     * objects for chromosomes 1-22,X,Y, and M, each of which contains the
//     * TranscriptModel objects for each of the genes located on those
//     * chromosomes.
//     */
//    public void deserializeUCSCdata() throws ExomizerException {
//        logger.info("DESERIALISING UCSC...");
//        ArrayList<TranscriptModel> kgList = null;
//        SerializationManager manager = new SerializationManager();
//        try {
//            kgList = manager.deserializeKnownGeneList(this.UCSCserializedFile);
//        } catch (JannovarException je) {
//            String s = String.format("Unable to deserialize the UCSC gene definition file: %s", je.toString());
//            throw new ExomizerException(s);
//        }
//        this.chromosomeMap = Chromosome.constructChromosomeMapWithIntervalTree(kgList);
//        logger.info("DONE DESERIALISING UCSC");
//    }
//    /**
//     * Iterates over all the variants parsed from the VCF file and provides each
//     * one with an annovar-style annotation.
//     */
//    public void annotateVariants() {
//        Chromosome c = null;
//        for (VariantEvaluation ve : this.variantList) {
//            Variant v = ve.getVariant();
//            // System.out.println(v);
//            byte chr = v.getChromosomeAsByte();
//            int pos = v.get_position();
//            String ref = v.get_ref();
//            String alt = v.get_alt();
//            c = chromosomeMap.get(chr);
//            if (c == null) {
//                logger.error("Could not identify chromosome {}", chr);
//            } else {
//                try {
//                    AnnotationList anno = c.getAnnotationList(pos, ref, alt);
//                    if (anno == null) {
//                        logger.info("No annotations found for variant {}", v);
//                        continue;
//                    }
//                    v.setAnnotation(anno);
//                } catch (AnnotationException ae) {
//                    String s = String.format("Annotation exception for variant %s (%s)", v.getChromosomalVariant(), ae.toString());
//                    this.status_message.add(s);
//                } catch (Exception e) {
//                    String s = String.format("Format/parse exception for variant %s (%s)", v.getChromosomalVariant(), e.toString());
//                    this.status_message.add(s);
//                }
//            }
//        }
//    }
    /**
     * This method initializes the variant-level and gene-level filtering and
     * prioritization. Most of the action takes place in the class
     * {@link exomizer.priority.Prioritiser Prioritiser}.
     */
    public void initializeFiltersAndPrioritizers() throws ExomizerInitializationException {
        //Make a new Prioritizer - this class is the one which co-ordinates the actual guts of the process
        //TODO: Shouldn't this actually be here in the Exomizer?
        prioritiser = new Prioritiser();
        //set the inheritance mode (required for scoring genes)
        prioritiser.setInheritanceMode(InheritancePriority.getModeOfInheritance(inheritance_filter_type));

        //add variant filters to the prioritizer 
        List<Filter> variantFilterList = makeFilters();
        prioritiser.setFilterList(variantFilterList);

        //Now add the Gene Prioritisers    
        List<Priority> genePriorityList = makePrioritizers();
        prioritiser.setPriorityList(genePriorityList);

    }

    /**
     * Utility method for wrapping-up how the
     * {@code de.charite.compbio.exomiser.filter.Filter} classes are created.
     *
     * @return
     * @throws ExomizerInitializationException
     */
    @Deprecated
    private List<Filter> makeFilters() throws ExomizerInitializationException {
        List<Filter> variantFilterList = new ArrayList<>();
        //
//        FilterFactory filterFactory = new FilterFactory(dataSource);
//
//        if (this.use_target_filter) {
//            variantFilterList.add(filterFactory.getTargetFilter());
//        }
//        variantFilterList.add(filterFactory.getFrequencyFilter(Float.valueOf(frequency_threshold), filterOutAlldbSNP));
//
//        if (this.quality_threshold != null) {
//            variantFilterList.add(filterFactory.getQualityFilter(quality_threshold));
//        }
//        /*
//         * the following shows P for everything and filters out if
//         * use_pathogenicity_filter==true.
//         */
//        variantFilterList.add(filterFactory.getPathogenicityFilter(use_pathogenicity_filter, use_target_filter));
//        
//        if (this.interval != null) {
//            variantFilterList.add(filterFactory.getIntervalFilter(interval));
//        }

        return variantFilterList;
    }

    /**
     * Utility method for wrapping-up how the
     * {@code de.charite.compbio.exomiser.priority.Priority} classes are
     * created.
     *
     * @return
     * @throws ExomizerInitializationException
     */
    @Deprecated
    private List<Priority> makePrioritizers() throws ExomizerInitializationException {
        //
        List<Priority> genePriorityList = new ArrayList<>();
        PriorityFactory priorityFactory = new PriorityFactory(dataSource);

////        this.prioritiser.addOMIMPrioritizer();
//        genePriorityList.add(priorityFactory.getOmimPrioritizer());
//        //is order *really* an issue here? If not these could be specified using inheritance? 
//        //inheritance_mode, disease, hpo_ids, candidateGene are actual input variables, the rest is configuration data
//        if (this.inheritance_filter_type != null) {
////            this.prioritiser.addInheritancePrioritiser(this.inheritance_filter_type);
//            genePriorityList.add(priorityFactory.getInheritancePrioritiser(inheritance_filter_type));
//        }
//        if (doMGIPhenodigmPrioritization()) {
////            this.prioritiser.addMGIPhenodigmPrioritiser(this.disease);
//            genePriorityList.add(priorityFactory.getMGIPhenodigmPrioritiser(disease));
//        } else if (hpo_ids != null) {
//            if (doPhenomizerPrioritization()) {
//                //this doesn't check whether hpo_ids is not null! should be part of that block? Are Phenomizer and BOQA exclusive?
////            this.prioritiser.addPhenomizerPrioritiser(this.phenomizerDataDirectory, this.hpo_ids);
//                genePriorityList.add(priorityFactory.getPhenomizerPrioritiser(phenomizerDataDirectory, hpo_ids));
//            } else if (doBOQAPrioritization()) {
////                this.prioritiser.addBOQAPrioritiser(this.hpoOntologyFile, this.hpoAnnotationFile, this.hpo_ids);
//                genePriorityList.add(priorityFactory.getBOQAPrioritiser(hpoOntologyFile, hpoAnnotationFile, hpo_ids));
//            } else if (this.randomWalkFilePath != null && this.randomWalkIndexPath != null) {
////                this.prioritiser.addDynamicPhenoWandererPrioritiser(this.randomWalkFilePath, this.randomWalkIndexPath, this.hpo_ids, this.candidateGene, this.disease, this.randomWalkMatrix);
//                genePriorityList.add(priorityFactory.getDynamicPhenoWandererPrioritiser(randomWalkFilePath, randomWalkIndexPath, hpo_ids, candidateGene, disease, randomWalkMatrix));
//            } else {
////                this.prioritiser.addDynamicPhenodigmPrioritiser(this.hpo_ids);
//                genePriorityList.add(priorityFactory.getDynamicPhenodigmPrioritiser(hpo_ids));
//            }
//        } else if (doZFINPhenodigm()) {
////            this.prioritiser.addZFINPrioritiser(this.disease);
//            genePriorityList.add(priorityFactory.getZFINPrioritiser(disease));
//        } else if (this.randomWalkFilePath != null && this.randomWalkIndexPath != null && this.disease != null) {
////            this.prioritiser.addDynamicPhenoWandererPrioritiser(this.randomWalkFilePath, this.randomWalkIndexPath, this.disease, this.candidateGene, this.disease, this.randomWalkMatrix);
//            //TODO: CHECK!! should the first disease in this constructor actually be the hpo_ids? Could do with some type-safety in this constructor.
//            genePriorityList.add(priorityFactory.getDynamicPhenoWandererPrioritiser(randomWalkFilePath, randomWalkIndexPath, disease, candidateGene, disease, randomWalkMatrix));
//        } else if (this.randomWalkFilePath != null && this.randomWalkIndexPath != null && this.entrezSeedGenes != null) {
////            this.prioritiser.addExomeWalkerPrioritiser(this.randomWalkFilePath, this.randomWalkIndexPath, this.entrezSeedGenes);
//            genePriorityList.add(priorityFactory.getExomeWalkerPrioritiser(randomWalkFilePath, randomWalkIndexPath, entrezSeedGenes));
//        }

        return genePriorityList;
    }

    /**
     * This is a utility function that returns true if values have been
     * initialized that we need to to Phenomizer prioritization.
     */
    private boolean doPhenomizerPrioritization() {
        return this.useCRE;
    }

    /**
     * This is a utility function that returns true if values have been
     * initialized that we need to to BOQA prioritization. In essence, we do
     * BOQA if the user has not chosen to do Phenomizer (no value initialized
     * for phenomizerDataDirectory), but has initialized everything else we need
     * for BOQA. We may want to make this cleaner in the future.
     */
    private boolean doBOQAPrioritization() {
        return this.useBOQA;
    }

    /**
     * This is a utility function that returns true if values have been
     * initialized that are intended for ZFIN Phenodigm analysis (see
     * {@link #parseCommandLineArguments}).
     */
    private boolean doZFINPhenodigm() {
        return this.use_zfin_phenodigm_filter;

    }

    /**
     * This is a utility function that returns true if values have been
     * initialized that we need to to MGI Phenodigm prioritization. This
     * variable is set based on the arguments that are passed to the function
     * (see {@link #parseCommandLineArguments}).
     */
    private boolean doMGIPhenodigmPrioritization() {
        return this.use_mgi_phenodigm_filter;
    }

    /**
     * This method can be used to add a
     * {@code de.charite.compbio.exomizer.priority.FilterType} object that has
     * been constructed elsewhere. This is particularly useful for the
     * ExomeWalker code base if it is started from an apache tomcat Server,
     * because we can construct the GeneWanderer object once (it has ca. 1.5 Gb
     * data) and keep it in memory as long as the ExomeWalker servlet is in
     * memory
     *
     * @param ip the {@link exomizer.priority.IPriority FilterType} that will be
     * added to the list of prioriitizers.
     */
    public void addPriority(Priority ip) throws ExomizerInitializationException {
        if (ip == null) {
            String s = "Attempt to initialize Exomiser with NULL Priority object";
            throw new ExomizerInitializationException(s);
        }
        this.prioritiser.addPriority(ip);
    }

    /**
     * @return A list of all filters.
     */
    public List<Filter> getFilterList() {
        return this.prioritiser.getFilterList();
    }

    /**
     * @return A list of all prioritisers.
     */
    public List<Priority> getPrioritisationList() {
        return this.prioritiser.getPriorityList();
    }

    /**
     * Connect to database and store connection in handle this.connect.
     */
//    public void openNewDatabaseConnection() throws ExomizerInitializationException {
//        logger.info("GETTING DATABASE CONNECTION");
//        this.connection = ExomiserDatabase.openNewDatabaseConnection();
//    }
    /**
     * Close database connection - required to stop Sanger server building up
     * stale connections
     */
//    public void closeDatabaseConnection() throws ExomizerInitializationException {
//        logger.info("CLOSING DATABASE CONNECTION");
//        ExomiserDatabase.closeDatabaseConnection(this.connection);
//    }
    /**
     * This function is intended to be used by external (webserver) code that is
     * running the exomizer and displaying the results on a website. The
     * webserver code is then responsible for managing and closing the database
     * connection.
     *
     * @param conn a postgreSQL database connection to the exomizer database.
     */
    public void setDatabaseConnection(Connection conn) {
        try {
            logger.info("Exomizer: setDatabaseConnection: closed={}", conn.isClosed());
        } catch (SQLException e) {
            logger.error("Exomizer: setDatabaseConnection - exception: connections is {}", conn);
        }
        this.connection = conn;
    }

    /**
     * Close the postgreSQL database connection. This function should be used to
     * close the database connection if the Exomizer is used from the command
     * line and if the function {@link #openNewDatabaseConnection} was used. The
     * Exomizer can also be used by the webprogram ExomeWalker; in this case,
     * that program is responsible for managing and closing the database
     * connection.
     */
    public void closeConnection() {
        ExomiserDatabase.closeDatabaseConnection(this.connection);
    }

    /**
     * This function, which will be called if the --tsv flag is used on the
     * command line, can be used to output tabular data in a form that can be
     * used to calculate ROC or precision recall. TODO. Adapt the logic of this
     * function to the evaluation pipeline.
     * @deprecated THIS IS NOW NON-FUNCTIONAL Use the ResultWriter functionality.
     */
    @Deprecated
    public void outputTSV() {
        String fname = vcf_file + ".results.tsv";
        logger.error("FILE {} NOT WRITTEN - use non-deprecated API.", fname);
        
    }
    
    /**
     * This function, which will be called if the --vcf flag is used on the
     * command line, can be used to output VCF data which is useful for
     * downstream analysis using other software such as VarSIFTER
     * @deprecated Use the {@code VcfResultsWriter} in the writer package 
     */
    @Deprecated
    public void outputVCF() {
        String fname = vcf_file + ".results.vcf";
        logger.info("Writing VCF file to: {}", fname);
        try {
            FileWriter fstream = new FileWriter(fname);
            BufferedWriter out = new BufferedWriter(fstream);
            out.write("##fileformat=VCFv4.1\n");
            out.write("#CHROM\tPOS\tID\tREF\tALT\tQUAL\tFILTER\tINFO\tFORMAT\tGENOTYPE\n");
            if (this.variantList == null) {
                out.write("<P>Error: Variant list not initialized correctly!</P>");
                out.close();
                return;
            }

            for (Gene g : geneList) {
                int N = g.getNumberOfVariants();
                for (int i = 0; i < N; ++i) {
                    VariantEvaluation ve = g.getNthVariant(i);
                    Variant v = ve.getVariant();
                    out.write(v.get_chromosome_as_string() + "\t");
                    out.write(v.get_position() + "\t");
                    out.write(".\t");
                    out.write(v.get_ref() + "\t");
                    out.write(v.get_alt() + "\t");
                    out.write(v.getVariantPhredScore() + "\t");
                    out.write("PASS\t");
                    out.write("GENE=" + g.getGeneSymbol() + ";PHENO_SCORE=" + g.getPriorityScore() + ";VARIANT_SCORE=" + g.getFilterScore()
                            + ";COMBINED_SCORE=" + g.getCombinedScore() + "\tGT\t");
                    out.write(v.getGenotypeAsString() + "\n");
                }
            }
            out.close();
        } catch (IOException e) {
            logger.error("Error writing VCF file:", e);
        }
    }

    /**
     * This method write a more or less plain HTML Footer and can be skipped if
     * the client code writes a nicer footer.
     */
    public void writeHTMLFooter(Writer out) throws IOException {
        this.htmlWriter.writeHTMLFooter();
    }

    public void writeHTMLBody(Writer out) throws IOException {

        this.htmlWriter.writeHTMLFilterSummary(this.prioritiser.getFilterList(),
                this.prioritiser.getPriorityList());
        VariantTypeCounter vtc = getVariantTypeCounter();
        htmlWriter.writeVariantDistributionTable(vtc, this.sampleNames);
        out.write("<hr/>\n");
        this.htmlWriter.writeHTMLBody(this.pedigree, this.geneList);
        if (this.status_message != null) {
            this.htmlWriter.writeStatusMessage(status_message);
        }
        this.htmlWriter.writeAbout();
    }

    /**
     * We are able to initilialize a VariantTypeCounter object either with a
     * list of Variant objects or to extract one from the TargetFilter object.
     * We use this object to print out a table of variant class distribution.
     * @deprecated Use the method with a typed parameter
     */
    @Deprecated
    public VariantTypeCounter getVariantTypeCounter() {
        VariantTypeCounter vtc = null;
        for (Filter f : this.prioritiser.getFilterList()) {
            if (f.getFilterType() == FilterType.TARGET_FILTER) {
                TargetFilter tf = (TargetFilter) f;
                vtc = tf.getVariantTypeCounter();
                break;
            }
        }
        if (vtc == null) {
            TargetFilter tf = new TargetFilter();
            tf.filterVariants(this.variantList);
            vtc = tf.getVariantTypeCounter();
        }
        return vtc;
    }

    /**
     * This function gets the HPO term names from the postgreSQL database and
     * uses a function of the class
     * {@link exomizer.io.ExomiserDatabase ExomiserDatabase} to create HTML
     * anchor elements for the terms. The term ids are taken from user input
     * from the command line.
     */
    private List<String> getHPOURLs(String idList) {
        List<String> lst = new ArrayList<String>();
        String A[] = idList.split(",");
        if (A.length == 0) {
            return null;
        }
        for (String s : A) {
            String anchor = ExomiserDatabase.getHPOTermNameAsHTMLAnchor(this.connection, s.trim());
            lst.add(anchor);
        }
        return lst;
    }

    /**
     * This method examines all variants for overlap with the HGMD Pro variants
     * that are contained in the Exomiser database. It returns an List of
     * Strings that can use to display on an HTML page.
     *
     * @see exomizer.io.PublishedMutationSearcher
     */
    private List<String> getHGMDHits() {
        PublishedMutationSearcher searcher = new PublishedMutationSearcher(this.variantList, this.connection);
        searcher.evaluateHGMDPro();
        List<String> lst = searcher.getHGMDHits();
        return lst;
    }

    /**
     * Set up an {@link exomizer.io.html.HTMLWriter HTMLWriter} object for
     * output. This function is intended to be used by an apache tomcat server.
     *
     * @param out A Writer object (e.g., StringWriter) for output
     */
    @Deprecated
    public void initializeCREWriter(Writer out) {
        this.htmlWriter = new HTMLWriterCRE(out, this.vcf_file_basename);
    }

    /**
     * Set up an {@link exomizer.io.html.HTMLWriter HTMLWriter} object for
     * output. This function is intended to be used by an apache tomcat server.
     *
     * @param out A Writer object (e.g., StringWriter) for output
     * @param VCFbase The base name of the VCF file used for the analysis.
     */
    public void initializeCREWriter(Writer out, String VCFbase) {
        this.htmlWriter = new HTMLWriterCRE(out, VCFbase);
    }

    /**
     * Set up an {@link exomizer.io.html.HTMLWriter HTMLWriter} object for
     * output. This function is intended to be used by an apache tomcat server.
     *
     * @param out A Writer object (e.g., StringWriter) for output
     */
    public void initializeWalkerWriter(Writer out) {
        this.htmlWriter = new HTMLWriterWalker(out);
    }

    /**
     * This function adds annotations about published mutations from HGMD and
     * ClinVar to the top prioritized genes.
     */
    private void addMutationHitsToTopVariants() {
        int N;
        if (this.numberOfGenesToShow == null) {
            N = this.geneList.size();
        } else {
            N = Math.min(this.numberOfGenesToShow, this.geneList.size());
        }
        PublishedMutationSearcher searcher = new PublishedMutationSearcher(this.connection);
        for (int i = 0; i < N; ++i) {
            Gene g = this.geneList.get(i);
            List<VariantEvaluation> varList = g.getVariantList();
            searcher.addPublishedMutationsToVariants(varList);
        }
    }

    /**
     * Output HTML code appropriate for the clinically relevant exome server.
     */
    public void outputCRE() throws ExomizerException {
        List<String> lst = getHPOURLs(this.hpo_ids);
        this.htmlWriter.addHPOList(lst);
        List<String> hgmd = getHGMDHits();
        this.htmlWriter.addHGMDHits(hgmd);
        addMutationHitsToTopVariants();
        try {
            if (this.numberOfGenesToShow != null) {
                this.htmlWriter.setNumberOfGenes(this.numberOfGenesToShow);
            }
            this.htmlWriter.writeHTMLHeaderAndCSS();
            this.htmlWriter.writeHTMLBody(this.pedigree, this.geneList);
            if (this.withinFirewall) {
                this.htmlWriter.writeHGMDBox();
            }
            this.htmlWriter.writeHTMLFilterSummary(this.prioritiser.getFilterList(),
                    this.prioritiser.getPriorityList());
            VariantTypeCounter vtc = getVariantTypeCounter();
            this.htmlWriter.writeVariantDistributionTable(vtc, this.sampleNames);
            Map<String, String> mp = ExomiserDatabase.getVersionInfoMap(this.connection);
            this.htmlWriter.addVersionInfo(mp);
            this.htmlWriter.writeAbout();
            this.htmlWriter.writeHTMLFooter();
            this.htmlWriter.finish();
        } catch (IOException e) {
            String s = String.format("Error : %s", e.getMessage());
            throw new ExomizerException(s);
        }
    }

    /**
     * Output an HTML page for Random Walk analysis. This function assumes that
     * the file out handle has been set elsewhere. Otherwise it writes a file
     * called "exomewalker.html"
     */
    public void outputWalker() throws ExomizerException {
        try {
            logger.info("outputWalker");
            if (this.numberOfGenesToShow == null) {
                this.numberOfGenesToShow = this.geneList.size();
            }
            this.htmlWriter.setNumberOfGenes(this.numberOfGenesToShow);
            ArrayList<String> seeds = ExomiserDatabase.getSeedGeneURLs(this.connection, this.entrezSeedGenes);
            this.htmlWriter.setSeedGeneURLs(seeds);
            logger.info("entrez genes are: {}", this.entrezSeedGenes);
            Network rwNetwork = new STRINGNetwork(this.connection, this.entrezSeedGenes);
            //this.htmlWriter.setInteractionMap(interactionMap);
            this.htmlWriter.setNetwork(rwNetwork);
            if (this.diseaseGeneFamilyName == null && seeds != null && seeds.size() > 0) {
                this.diseaseGeneFamilyName = String.format("User-defined disease gene family with %d genes", seeds.size());
            }
            this.htmlWriter.setDiseaseGeneFamilyName(this.diseaseGeneFamilyName);
            this.htmlWriter.writeHTMLHeaderAndCSS();
            this.htmlWriter.writeHTMLBody(this.pedigree, this.geneList);
            this.htmlWriter.writeHTMLFilterSummary(this.prioritiser.getFilterList(),
                    this.prioritiser.getPriorityList());
            VariantTypeCounter vtc = getVariantTypeCounter();
            this.htmlWriter.writeVariantDistributionTable(vtc, this.sampleNames);
            Map<String, String> mp = ExomiserDatabase.getVersionInfoMap(this.connection);
            this.htmlWriter.addVersionInfo(mp);
            this.htmlWriter.writeAbout();
            this.htmlWriter.writeHTMLFooter();
            this.htmlWriter.finish();
        } catch (IOException e) {
            String s = String.format("Error : %s", e.getMessage());
            throw new ExomizerException(s);
        }
    }

    /**
     * Outputs an HTML page with the results of Exomizer prioritization. This
     * function creates a file called {@code exomizer.html} (unless the name of
     * the out file has been changed via the command line).
     * @throws de.charite.compbio.exomiser.exception.ExomizerException
     * @deprecated Use the method with typed parameters. 
     */
    @Deprecated
    public void outputHTML() throws ExomizerException {
        if (this.variantList == null) {
            /*
             * This should never happen, just a sanity check.
             */
            logger.error("Attempt to write HTML File with null variant list");
            System.exit(1);
        }
        try {
            //default file name
            String fname = "exomizer.html";
            logger.info("Writing HTML file to: {}", fname);

            if (this.outfile != null) {
                fname = this.outfile;
            }
            if (this.useRandomWalk) {
                this.htmlWriter = new HTMLWriterWalker(fname);
                outputWalker();
                return;
            } 
            else if (this.useCRE) {
                this.htmlWriter = new HTMLWriterCRE(fname, this.vcf_file_basename);
                outputCRE();
                return;
            } else { /*
                 * default
                 */

                this.htmlWriter = new HTMLWriter(fname);
            }
            this.htmlWriter.writeHTMLHeaderAndCSS();
            this.htmlWriter.writeHTMLFilterSummary(this.prioritiser.getFilterList(),
                    this.prioritiser.getPriorityList());
            VariantTypeCounter vtc = getVariantTypeCounter();
            this.htmlWriter.writeVariantDistributionTable(vtc, this.sampleNames);
            logger.info("Writing HTML body with {} gene results", geneList.size());
            this.htmlWriter.writeHTMLBody(this.pedigree, this.geneList);
            this.htmlWriter.writeAbout();
            this.htmlWriter.writeHTMLFooter();
            this.htmlWriter.finish();
        } catch (IOException e) {
            String s = String.format("Error : %s", e.getMessage());
            throw new ExomizerException(s);
        }
    }
    
    /**
     * Outputs an HTML page with the results of Exomizer prioritization. This
     * function creates a file called {@code exomizer.html} (unless the name of
     * the out file has been changed via the command line).
     * @deprecated Use the {@code HtmlResultsWriter} in the writer package 
     */
    @Deprecated
    public void outputHTML(SampleData sampleData, List<Filter> filterList, List<Priority> priorityList, OutputFormat outputFormat, String outFileName) {
        if (sampleData.getVariantEvaluations().isEmpty()) {
            logger.error("Cannot wite out a results file with no variant data.");
        }
        try {
            logger.info("Writing HTML file to: {}", outFileName);

//            if (this.useRandomWalk) {
//                this.htmlWriter = new HTMLWriterWalker(outFileName);
//                outputWalker();
//                return;
//            } else if (this.useBOQA) {
//                this.htmlWriter = new HTMLWriterBOQA(outFileName);
//            } else if (this.useCRE) {
//                this.htmlWriter = new HTMLWriterCRE(outFileName, sampleData.getVcfFilePath().toString());
//                outputCRE();
//                return;
//            } else { /*
//                 * default
//                 */

                this.htmlWriter = new HTMLWriter(outFileName);
//            }
            this.htmlWriter.writeHTMLHeaderAndCSS();
            this.htmlWriter.writeHTMLFilterSummary(filterList, priorityList);
            VariantTypeCounter vtc = getVariantTypeCounter(filterList, sampleData.getVariantEvaluations());
            this.htmlWriter.writeVariantDistributionTable(vtc, sampleData.getSampleNames());
            logger.info("Writing HTML body with {} gene results", sampleData.getGeneList().size());
            this.htmlWriter.writeHTMLBody(sampleData.getPedigree(), sampleData.getGeneList());
            this.htmlWriter.writeAbout();
            this.htmlWriter.writeHTMLFooter();
            this.htmlWriter.finish();
        } catch (IOException e) {
            logger.error("Unable to write HTML file.",e);
        } catch (ExomizerException e) {
            logger.error("Unable to write HTML file.",e);
        }
    }

    /**
     * We are able to initilialize a VariantTypeCounter object either with a
     * list of Variant objects or to extract one from the TargetFilter object.
     * We use this object to print out a table of variant class distribution.
     */
    protected VariantTypeCounter getVariantTypeCounter(List<Filter> filterList, List<VariantEvaluation> variantList) {
        VariantTypeCounter vtc = null;
        for (Filter f : filterList) {
            if (f.getFilterType() == FilterType.TARGET_FILTER) {
                TargetFilter tf = (TargetFilter) f;
                vtc = tf.getVariantTypeCounter();
                break;
            }
        }
        if (vtc == null) {
            TargetFilter tf = new TargetFilter();
            tf.filterVariants(variantList);
            vtc = tf.getVariantTypeCounter();
        }
        return vtc;
    }

    /**
     * Parse the command line.
     *
     * @param args Copy of the command line parameters.
     */
    @Deprecated
    public void parseCommandLineArguments(String[] args) {
        try {
            Options options = new Options();
            options.addOption(new Option("h", "help", false, "Shows this help"));
            options.addOption(new Option("H", "help", false, "Shows this help"));
            options.addOption(new Option("v", "vcf", true, "Path to VCF file with mutations to be analyzed."));
            options.addOption(new Option("o", "outfile", true, "name of out file (default: \"exomizer.html\")"));
            options.addOption(new Option("l", "log", true, "Configuration file for logger"));

            // / Filtering options
            options.addOption(new Option("A", "omim_disease", true, "OMIM ID for disease being sequenced"));
            options.addOption(new Option("B", "boqa", true, "comma-separated list of HPO terms for BOQA"));
            options.addOption(new Option("D", "file_for_deserialising", true, "De-serialise"));
            options.addOption(new Option("F", "freq_threshold", true, "Frequency threshold for variants")); 
            options.addOption(new Option("I", "inheritance", true, "Filter variants for inheritance pattern (AR,AD,X)")); 
            options.addOption(new Option("M", "mgi_phenotypes", false, "Filter variants for MGI phenodigm score"));

            options.addOption(new Option("P", "path", false, "Filter variants for predicted pathogenicity"));
            options.addOption(new Option("Q", "qual_threshold", true, "Quality threshold for variants"));
            options.addOption(new Option("S", "SeedGenes", true, "Comma separated list of seed genes for random walk"));
            options.addOption(new Option("W", "RWmatrix", true, "Random walk matrix file"));
            options.addOption(new Option("X", "RWindex", true, "Random walk index file"));
            options.addOption(new Option("Z", "zfin_phenotypes", false, "Filter variants for ZFIN phenodigm score"));
            options.addOption(new Option("T", "keep_off_target_syn", false, "Leave in off-target, intronic and synonymous variants"));

            // Annotations that do not filter
            options.addOption(new Option(null, "interval", true, "Restrict to interval (e.g., chr2:12345-67890)"));
            options.addOption(new Option(null, "tsv", false, "Output tab-separated value (TSV) file instead of HTML"));
            options.addOption(new Option(null, "vcf_output", false, "Output VCF file instead of HTML"));
            options.addOption(new Option(null, "candidate_gene", true, "Known or suspected gene association"));
            options.addOption(new Option(null, "dbsnp", false, "Filter out all variants with an entry in dbSNP/ESP (regardless of frequency)"));
            options.addOption(new Option(null, "ped", true, "pedigree (ped) file"));
            options.addOption(new Option(null, "hpo", true, "HPO Ontology (obo) file"));
            options.addOption(new Option(null, "hpoannot", true, "HPO Annotations file"));
            options.addOption(new Option(null, "hpo_ids", true, "HPO IDs for the sample being sequenced"));
            options.addOption(new Option(null, "ngenes", true, "Number of genes to show in output"));
            options.addOption(new Option(null, "withinFirewall", false, "Set flag that we are running on private server"));
            options.addOption(new Option(null, "phenomizerData", true, "Phenomizer data directory"));

            Parser parser = new GnuParser();
            CommandLine cmd = parser.parse(options, args);
            if (cmd.hasOption("h") || cmd.hasOption("H") || args.length == 0) {
                HelpFormatter formatter = new HelpFormatter();
                formatter.printHelp("java -jar Exomizer [...]", options);
                System.exit(0);
            }

            // reset filters as webserver uses a static object and want to reset
            // each query
            this.frequency_threshold = null;
            this.quality_threshold = null;
            this.inheritance_filter_type = null;
            this.filterOutAlldbSNP = false;
            this.disease = null;
            this.hpo_ids = null;

            if (cmd.hasOption("B")) {
                setHPOids(cmd.getOptionValue("B"));
                setUseBoqa();
            }
//            if (cmd.hasOption("D")) {
//                setUCSCserializedFile(cmd.getOptionValue("D"));
//            }
            if (cmd.hasOption("F")) {
                setFrequencyThreshold(cmd.getOptionValue("F"));
            }
            if (cmd.hasOption("I")) {
                setInheritanceFilter(cmd.getOptionValue("I"));
            }
            if (cmd.hasOption("P")) {
                setUsePathogenicityFilter(true);
            } else {
                setUsePathogenicityFilter(false);
            }
            if (cmd.hasOption("T")) {
                this.use_target_filter = false;
            }
            if (cmd.hasOption("Q")) {
                setQualityThreshold(cmd.getOptionValue("Q"));
            }

            if (cmd.hasOption("o")) {
                setOutfile(cmd.getOptionValue("o"));
            }
            if (cmd.hasOption("v")) {
                setVCFfile(cmd.getOptionValue('v'));
            } else if (cmd.hasOption("V")) {
                setVCFfile(cmd.getOptionValue('V'));
            }

            if (cmd.hasOption("interval")) {
                setInterval(cmd.getOptionValue("interval"));
            }
            if (cmd.hasOption("tsv")) {
                setUseTSV(true);
            }
            if (cmd.hasOption("vcf_output")) {
                setUseVCF(true);
            }
            if (cmd.hasOption("ngenes")) {
                String n = cmd.getOptionValue("ngenes");
                setNumberOfGenesToShow(n);
            }

            if (cmd.hasOption("candidate_gene")) {
                setCandidateGene(cmd.getOptionValue("candidate_gene"));
            }
            if (cmd.hasOption("dbsnp")) {
                setFilterOutAlldbSNP(true);
            }

            if (cmd.hasOption("withinFirewall")) {
                setWithinFirewall();
            }
            if (cmd.hasOption("ped")) {
                setPedFile(cmd.getOptionValue("ped"));
            }
            if (cmd.hasOption("hpo_ids")) {
                setHPOids(cmd.getOptionValue("hpo_ids"));
            }
            /**
             * *
             * The following commands are the entry points into particular types
             * of HPO analysis: Generic, clinically relevant Exome server, Exome
             * Walker analysis. Combinations of arguments set flags that will
             * control the behaviour of the program. At least one condition must
             * be met to start the analysis. Otherwise, an error message is
             * written to STDOUT.
             */
            // / --hpo ${HPO} --hpoannot ${HPANNOT} --phenomizerData ${PHMDATA}
            // --hpo_ids ${HPTERMS}
	    /*
             * 1) Clinically relevant exome server
             */
            if (cmd.hasOption("phenomizerData") && cmd.hasOption("hpo_ids")) {
                setPhenomizerDataDirectory(cmd.getOptionValue("phenomizerData"));
                setHPOids(cmd.getOptionValue("hpo_ids"));
                setDoClinicallyRelevantExomeServer();
            } /*
             * 2) Phenotype based Random walk (PhenoWanderer) analysis
             */ else if (cmd.hasOption("W") && cmd.hasOption("X") && cmd.hasOption("A")) {
                setRandomWalkFilePath(cmd.getOptionValue("W"));
                setRandomWalkIndexPath(cmd.getOptionValue("X"));
                setTargetDisease(cmd.getOptionValue("A"));
                //setDoPhenoRandomWalk();
            } /*
             * 2) Phenotype based Random walk (PhenoWanderer) analysis using HPO
             * IDs
             */ else if (cmd.hasOption("W") && cmd.hasOption("X") && cmd.hasOption("hpo_ids")) {
                setRandomWalkFilePath(cmd.getOptionValue("W"));
                setRandomWalkIndexPath(cmd.getOptionValue("X"));
                setHPOids(cmd.getOptionValue("hpo_ids"));
                //setDoDynamicPhenoRandomWalk();
            } /*
             * 2) Random walk (GeneWanderer) analysis
             */ else if (cmd.hasOption("W") && cmd.hasOption("X") && cmd.hasOption("S")) {
                setRandomWalkFilePath(cmd.getOptionValue("W"));
                setRandomWalkIndexPath(cmd.getOptionValue("X"));
                setEntrezSeedGenes(cmd.getOptionValue("S"));
                setDoRandomWalk();
            } /*
             * 3) ZFIN Phenodigm prioritization
             */ else if (cmd.hasOption("Z") && cmd.hasOption("A")) {
                setTargetDisease(cmd.getOptionValue("A"));
                setUseZFINphenodigmFilter(true);
                setUseMGIphenodigmFilter(false);
            } /*
             * 3) MGI Phenodigm prioritization
             */ else if (cmd.hasOption("M") && cmd.hasOption("A")) {
                setTargetDisease(cmd.getOptionValue("A"));
                setUseZFINphenodigmFilter(false);
                setUseMGIphenodigmFilter(true);
            } else {
                logger.warn("Non-standard combination of arguments passed to perform analysis.");
            }
        } catch (ParseException pe) {
            logger.error("Error parsing command line options: {}", pe);
            System.exit(1);
        }
    }

    /**
     * This function is used to ensure that certain options are passed to the
     * program before we start execution.
     *
     * @param cmd An apache CommandLine object that stores the command line
     * arguments
     * @param name Name of the argument that must be present
     * @return Value of the required option as a String.
     */
    @Deprecated
    private static String getRequiredOptionValue(CommandLine cmd, char name) {
        String val = cmd.getOptionValue(name);
        if (val == null) {
            logger.error("Aborting because the required argument -{} wasn't specified! Use the -h for more help.", name);
            System.exit(-1);
        }
        return val;
    }

    /**
     * Set the flag to indicate we want to use BOQA prioritization.
     */
    public void setUseBoqa() {
        this.useBOQA = true;
        this.useCRE = false;
        this.useRandomWalk = false;
    }

    /**
     * Set the flag to perform Random Walk (GeneWanderer) analysis.
     */
    public void setDoRandomWalk() {
        this.useRandomWalk = true;
        this.useBOQA = false;
        this.useCRE = false;
    }

    public void setDoClinicallyRelevantExomeServer() {
        this.useCRE = true;
        this.useRandomWalk = false;
        this.useBOQA = false;
    }

//    /**
//     * Set the flag to perform Random Walk (GeneWanderer) analysis.
//     */
//    public void setDoPhenoRandomWalk() {
//        this.useRandomWalk = false;
//        this.useBOQA = false;
//        this.useCRE = false;
//    }
//
//    /**
//     * Set the flag to perform Random Walk (GeneWanderer) analysis.
//     */
//    public void setDoDynamicPhenoRandomWalk() {
//        this.useRandomWalk = false;
//        this.useBOQA = false;
//        this.useCRE = false;
//    }

//    public void setUCSCserializedFile(String ucscFile) {
//        this.UCSCserializedFile = ucscFile;
//    }
    public void setFrequencyThreshold(String F) {
        this.frequency_threshold = F;
    }

    public void setQualityThreshold(String Q) {
        this.quality_threshold = Q;
    }

    /**
     * See {@link #withinFirewall}.
     */
    public void setWithinFirewall() {
        this.withinFirewall = true;
    }

    /**
     * Set the number of top prioritized genes to show in the HTML output. If
     * the input string is not an integer, the function will print an error
     * message and return without setting anything.
     *
     * @param n Number of genes to show
     */
    public void setNumberOfGenesToShow(String n) {
        if (n.equalsIgnoreCase("all")) {
            this.numberOfGenesToShow = null;
            return;
        }
        try {
            this.numberOfGenesToShow = Integer.parseInt(n);
        } catch (NumberFormatException e) {
            this.numberOfGenesToShow = null;
            logger.error("Error setting number of genes to show.", e);
        }
    }

    public void setTargetDisease(String A) {
        this.disease = A;
    }

    public void setHPOids(String ids) {
        this.hpo_ids = ids;
    }

    public void setInheritanceFilter(String inh) {
        this.inheritance_filter_type = inh;
    }

    public void setUseMGIphenodigmFilter(boolean use_filter) {
        this.use_mgi_phenodigm_filter = use_filter;
    }

    public void setUseZFINphenodigmFilter(boolean use_filter) {
        this.use_zfin_phenodigm_filter = use_filter;
    }

    public void setUsePathogenicityFilter(boolean use_filter) {
        this.use_pathogenicity_filter = use_filter;
    }

    public void setOutfile(String fname) {
        this.outfile = fname;
    }

    public void setInterval(String interv) {
        this.interval = interv;
    }

    public void setUseTSV(boolean useT) {
        this.useTSV = useT;
    }

    public void setUseVCF(boolean useV) {
        this.useVCF = useV;
    }

    public void setCandidateGene(String gene) {
        this.candidateGene = gene;
    }

    public void setRandomWalkMatrix(DataMatrix rwMatrix) {
        this.randomWalkMatrix = rwMatrix;
    }

    public void setFilterOutAlldbSNP(boolean setDbsnp) {
        this.filterOutAlldbSNP = setDbsnp;
    }

    public void setRandomWalkFilePath(String path) {
        this.randomWalkFilePath = path;
    }

    public String getRandomWalkFilePath() {
        return this.randomWalkFilePath;
    }

    public void setRandomWalkIndexPath(String path) {
        this.randomWalkIndexPath = path;
    }

    public String getRandomWalkIndexPath() {
        return this.randomWalkIndexPath;
    }

    /**
     * Set a list of Entrez Gene ids, e.g., 6513,6230,450 This option is used
     * for prioritizing by protein interactions.
     */
    public void setEntrezSeedGenes(String commaSeparatedLst) {
        this.entrezSeedGenes = commaSeparatedLst;
    }

    public String getEntrezSeedGenes() {
        return this.entrezSeedGenes;
    }

    /**
     * @param list A comma-separated list of HPO terms.
     * @deprecated Duplicates {@link Exomiser#setHPOids}
     */
    @Deprecated
    public void setBOQA_TermList(String list) {
        this.hpo_ids = list;
    }

    /**
     * This method is intended to be used by a tomcat server (e.g., ExomeWalker)
     * to pass in a file handle to a PED file that has been uploaded and read
     * into a StringReader that in turn has been used to construct a
     * BufferedReader.
     *
     * @param br A file handle to a PED file.
     */
    @Deprecated
    public void setPedBufferedReader(BufferedReader br) {
        this.pedBufferedReader = br;
    }

    public void setHPOontologyFile(String file) {
        this.hpoOntologyFile = file;
    }

    public void setHPOannotationFile(String file) {
        this.hpoAnnotationFile = file;
    }

    /**
     * See the class {@link exomizer.priority.PhenomizerPriority
     * PhenomizerPriority} for details about what needs to be in this directory
     * for the phenomizer to be used for prioritization.
     */
    public void setPhenomizerDataDirectory(String path) {
        this.phenomizerDataDirectory = path;
    }

    /**
     * @param name Name of a phenotypic series (i.e., a disease-gene family).
     */
    public void setDiseaseGeneFamilyName(String name) {
        this.diseaseGeneFamilyName = name;
    }

/////////////////////////////////////////////////////////////////////////////////
     /**
     * The methods/variables in this section all deal with the parsing/storing
     * of sample data and will be moved out into the SampleData
     * SampleDataFactory classes or simply removed if no longer used. Hence they
     * are all deprecated.
     */
    
    
    /**
     * List of Variants parsed from VCF file that have passed user-indicated
     * filter
     */
    //TODO: this will be provided from outside the class
    @Deprecated
    private List<VariantEvaluation> variantList = null;
    
    @Deprecated
    public void setVariantList(List<VariantEvaluation> variantList) {
        this.variantList = variantList;
    }
    
    
    /**
     * List of all Genes that are to be prioritized. Note that the Genes will
     * contain the variants that have passed the filtering step.
     */
    @Deprecated
    private List<Gene> geneList = null;

    /**
     * @return A list of all genes.
     */
    @Deprecated
    public List<Gene> getGeneList() {
        return geneList;
    }

    @Deprecated
    public void setGeneList(List<Gene> geneList) {
        this.geneList = geneList;
    }

    @Deprecated
    public int getNumberOfGenes() {
        if (geneList == null) {
            return 0;
        }
        return geneList.size();
    }
    /**
     * List of all sample names of VCF file
     */
    @Deprecated
    private ArrayList<String> sampleNames = null;
    /**
     * Name of the VCF file used for input.
     */
    @Deprecated
    private String vcf_file = null;
    /**
     * Base name of the VCF file used for input (without path)
     */
    @Deprecated
    private String vcf_file_basename = null;
//    /**
//     * Store lines of header of VCF file, in case we want to print them out
//     * again.
//     */
//    private List<String> header = null;

    /**
     * Temporary method for setting the sampleNames needed by the HTMLWriters
     * until they are split out of this class.
     *
     * @param sampleNames
     * @deprecated
     */
    @Deprecated
    public void setSampleNames(ArrayList<String> sampleNames) {
        this.sampleNames = sampleNames;
    }
    
//    /**
//     * Total number of variants in original VCF file.
//     */
//    private int before_NS_SS_I;
//    /**
//     * Total number of NS/SS/I variants in original VCF file (nonsynonymous,
//     * splicing, indel).
//     */
//    private int after_NS_SS_I;
//    /**
//     * Total number of samples (sequenced persons) in the input VCF file.
//     */
//    private int n_samples = 0;
    /**
     * Pedigree of the persons whose samples were sequenced. Created on the
     * basis of a PED file for multisample VCF files, or as a default
     * single-sample Pedigree for single-sample VCF files.
     */
    @Deprecated
    private Pedigree pedigree = null;
    /**
     * Path to the pedigree (ped) file for multisample VCF files.
     */
    @Deprecated
    private String pedFilePath = null;
    /**
     * BufferedReader to a StringReader that contains the contents of an
     * uploaded PED file. This variable is provided so that tomcat servers can
     * pass in a handle to a file without needing to first write this file to
     * disk.
     */
    @Deprecated
    private BufferedReader pedBufferedReader = null;

    //TODO: REMOVE used by  parseCommandLineArguments
    @Deprecated
    public void setPedFile(String path) {
        this.pedFilePath = path;
    }

    //TODO: REMOVE used by  parseCommandLineArguments
    @Deprecated
    public String getPedFile() {
        return this.pedFilePath;
    }

    /**
     * Set the VCF path ({@link #vcf_file}) as well as the base name of the VCF
     * file ({@link #vcf_file_basename}).
     */
    @Deprecated
    public void setVCFfile(String fname) {
        this.vcf_file = fname;
        int sep = fname.lastIndexOf("/");
        int wsep = fname.lastIndexOf("\\");
        sep = Math.max(sep, wsep);
        this.vcf_file_basename = fname.substring(sep + 1);
    }

//    /**
//     * This method is intended to be used by a tomcat server (e.g., ExomeWalker)
//     * to pass in a file handle to a VCF file that has been uploaded and read
//     * into a StringReader that in turn has been used to construct a
//     * BufferedReader.
//     *
//     * @param br A file handle to a VCF file.
//     */
//    public void setVCFbufferedReader(BufferedReader br) {
//        this.vcfBufferedReader = br;
//    }
//
    //TODO: REMOVE used by  parseCommandLineArguments
    @Deprecated
    public String getVCFfile() {
        return this.vcf_file;
    }

    //TODO: REMOVE
    @Deprecated
    public Pedigree getPedigree() {
        return this.pedigree;
    }

    /**
     * Temporary method for providing a means of setting the pedigree
     *
     * @param pedigree
     * @deprecated
     */
    //TODO: REMOVE
    @Deprecated
    public void setPedigree(Pedigree pedigree) {
        this.pedigree = pedigree;
    }

//should be redundant - this is handled by the SampleDataFactory
//    /**
//     * This function is intended to let webservers know that we still need to
//     * initialize the Pedigree object for VCF files with only one sample. There
//     * are probably more elegant ways of doing this, consider refactoring.
//     */
//    @Deprecated
//    public boolean singleSamplePedigreeNeedsInitialization() {
//        if (this.pedigree == null && this.getNumberOfSamples() == 1) {
//            return true;
//        } else {
//            return false;
//        }
//    }
}
