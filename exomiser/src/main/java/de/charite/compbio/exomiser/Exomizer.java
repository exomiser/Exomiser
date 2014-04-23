package de.charite.compbio.exomiser;

import de.charite.compbio.exomiser.common.FilterType;
import de.charite.compbio.exomiser.exception.ExomizerException;
import de.charite.compbio.exomiser.exception.ExomizerInitializationException;
import de.charite.compbio.exomiser.exome.Gene;
import de.charite.compbio.exomiser.exome.VariantEvaluation;
import de.charite.compbio.exomiser.filter.IFilter;
import de.charite.compbio.exomiser.filter.TargetFilter;
import de.charite.compbio.exomiser.io.ExomiserDatabase;
import de.charite.compbio.exomiser.io.PublishedMutationSearcher;
import de.charite.compbio.exomiser.io.html.HTMLWriter;
import de.charite.compbio.exomiser.io.html.HTMLWriterBOQA;
import de.charite.compbio.exomiser.io.html.HTMLWriterCRE;
import de.charite.compbio.exomiser.io.html.HTMLWriterWalker;
import de.charite.compbio.exomiser.priority.IPriority;
import de.charite.compbio.exomiser.priority.Prioritiser;
import de.charite.compbio.exomiser.priority.util.DataMatrix;
import de.charite.compbio.exomiser.reference.Network;
import de.charite.compbio.exomiser.reference.STRINGNetwork;
import jannovar.annotation.AnnotationList;
import jannovar.common.ModeOfInheritance;
import jannovar.exception.AnnotationException;
import jannovar.exception.JannovarException;
import jannovar.exception.PedParseException;
import jannovar.exception.VCFParseException;
import jannovar.exome.Variant;
import jannovar.exome.VariantTypeCounter;
import jannovar.io.PedFileParser;
import jannovar.io.SerializationManager;
import jannovar.io.VCFReader;
import jannovar.pedigree.Pedigree;
import jannovar.reference.Chromosome;
import jannovar.reference.TranscriptModel;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
 * database created by the {@link exomizer.PopulateExomiserDatabase PopulateExomiserDatabase},
 * program, as well as data about gene models from the UCSC database (See {@link jannovar.reference.TranscriptModel TranscriptModel}).
 * The candidate genes are then filtered and prioritized according to flexible
 * criteria. files with the results of filtering. <P> The Exomizer can be
 * started via the command line, in which case the main function will use the {@link #Exomizer(String[])}
 * constructor. Alternatively, the Exomizer can be started from the Apacha
 * tomcat framework (ExomeWalker code). In this case, the {@link #Exomizer()}
 * constructor should be used, and the various setter functions should be used
 * to set the parameters. <P> When started from the command line, the Exomizer
 * will output a single self-contained HTML file that also includes CSS style
 * information.
 *
 * @author Peter N Robinson
 * @version 0.65 (9 February, 2014)
 */
public class Exomizer {

    private static final Logger logger = LoggerFactory.getLogger(Exomizer.class);
    /**
     * Name of the VCF file used for input.
     */
    private String vcf_file = null;
    /**
     * Base name of the VCF file used for input (without path)
     */
    private String vcf_file_basename = null;
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
     * Store lines of header of VCF file, in case we want to print them out
     * again.
     */
    private List<String> header = null;
    /**
     * File to which we will write results, name can be changed via command
     * line.
     */
    private String outfile = "exomizer.html";
    /**
     * List of Variants parsed from VCF file that have passed user-indicated
     * filter
     */
    private List<VariantEvaluation> variantList = null;
    /**
     * List of all Genes that are to be prioritized. Note that the Genes will
     * contain the variants that have passed the filtering step.
     */
    private List<Gene> geneList = null;
    /**
     * List of all transcripts used for annotation the variants (For instance,
     * from the knownGene.txt file from UCSC). See the Jannovar package for
     * details.
     */
    private List<TranscriptModel> knownGenesList = null;
    /**
     * List of all sample names of VCF file
     */
    private ArrayList<String> sampleNames = null;
    /**
     * Map of Chromosomes
     */
    private HashMap<Byte, Chromosome> chromosomeMap = null;
    /**
     * Name of file with serialized UCSC data. This needs to be created from
     * several UCSC KnownGene files by the Annotator code in this package (see
     * the {@code Jannovar} program).
     */
    private String UCSCserializedFile = null;
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
    // The following are to be used for initializing the IFilter classes
    /**
     * Frequency threshold for variants. Only variants that are either not
     * recorded in the thousand genomes/ESP data or that are rarer than this
     * threshold will be retained. Note that the threshold is expected to be a
     * value such as 0.01, but it is stored here as a String because the
     * IFilter-derived classes have a standard function for initialization that
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
     * Total number of variants in original VCF file.
     */
    private int before_NS_SS_I;
    /**
     * Total number of NS/SS/I variants in original VCF file (nonsynonymous,
     * splicing, indel).
     */
    private int after_NS_SS_I;
    /**
     * Total number of samples (sequenced persons) in the input VCF file.
     */
    private int n_samples = 0;
    /**
     * Pedigree of the persons whose samples were sequenced. Created on the
     * basis of a PED file for multisample VCF files, or as a default
     * single-sample Pedigree for single-sample VCF files.
     */
    private Pedigree pedigree = null;
    /**
     * Path to the pedigree (ped) file for multisample VCF files.
     */
    private String pedFilePath = null;
    /**
     * BufferedReader to a StringReader that contains the contents of an
     * uploaded PED file. This variable is provided so that tomcat servers can
     * pass in a handle to a file without needing to first write this file to
     * disk.
     */
    private BufferedReader pedBufferedReader = null;
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

    public static void main(String argv[]) {
        /**
         * ***********************************************************
         */
        /*
         * 1) Open the connection to database.
         */
        /**
         * ***********************************************************
         */
        Exomizer exomizer = new Exomizer(argv);
        try {
            exomizer.openNewDatabaseConnection();
        } catch (ExomizerInitializationException ex) {
            logger.error("Could not open SQL connection. Terminating program...", ex);
            System.exit(1);
        }
        /**
         * ***********************************************************
         */
        /*
         * 2) Input the transcript definition file from Jannovar that
         */
        /*
         * is used to annotate the variants.
         */
        /**
         * ***********************************************************
         */
        try {
            exomizer.deserializeUCSCdata();
        } catch (ExomizerException e) {
            logger.error("Error with deserialization:", e);
            System.exit(1);
        }
        /**
         * ***********************************************************
         */
        /*
         * 3) Read a VCF file (this creates an ArrayList of Variants)
         */
        /**
         * ***********************************************************
         */
        try {
            exomizer.parseVCFFile();
        } catch (ExomizerException e) {
            logger.error("Error with VCF input:", e);
            System.exit(1);
        }
        /**
         * ***********************************************************
         */
        /*
         * 4) Read a PED file if the VCF file has multiple samples
         */
        /**
         * ***********************************************************
         */
        try {
            exomizer.processPedigreeData();
            /*
             * Note: for single sample VCF files, this method will construct a
             * "dummy" pedigree object.
             */
        } catch (ExomizerException e) {
            logger.error("Error with pedigree data input:", e);
            System.exit(1);
        }
        /**
         * ***********************************************************
         */
        /*
         * 5) This function takes care of most of the analysis.
         */
        /**
         * ***********************************************************
         */
        try {
            logger.info("INIT FILTERS AND PRIORITISERS");
            exomizer.initializeFiltersAndPrioritizers();
            logger.info("ANNOTATING VARIANTS");
            exomizer.annotateVariants();
            logger.info("FILTERING AND PRIORITISING");
            exomizer.executePrioritization();
        } catch (ExomizerException e) {
            logger.error("Error while prioritizing VCF data: ", e);
            System.exit(1);
        }
        logger.info("OUTPUTTING RESULTS");
        /**
         * ***********************************************************
         */
        /*
         * 6) Output to HTML (default) or TSV (needs to be set via
         */
        /*
         * the --tsv flag on the command line)
         */
        /**
         * ***********************************************************
         */
        if (exomizer.useTSVFile()) {
            exomizer.outputTSV();
        } else if (exomizer.useVCFFile()) {
            exomizer.outputVCF();
        } else {
            /*
             * The following function decides based on the flag useCRE, useBOQA,
             * or useRandomWalk what kind of HTML to produce.
             */
            try {
                exomizer.outputHTML();
            } catch (ExomizerException e) {
                logger.error("Error writing output: ", e);
                System.exit(1);
            }
        }
        logger.info("FINISHED EXOMISER");

    }

    /**
     * This function is intended to let webservers know that we still need to
     * initialize the Pedigree object for VCF files with only one sample. There
     * are probably more elegant ways of doing this, consider refactoring.
     */
    public boolean singleSamplePedigreeNeedsInitialization() {
        if (this.pedigree == null && this.getNumberOfSamples() == 1) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * The constructor sets a few class variables based on command line
     * arguments.
     *
     * @param argv an array with the command line arguments
     */
    public Exomizer(String argv[]) {
        logger.info("STARTING EXOMISER");
        parseCommandLineArguments(argv);
        this.status_message = new ArrayList<String>();
        if (outfile == null) {
            logger.error("Outfile not indicated, terminating program execution...");
            System.exit(1);
        }
    }

    /**
     * This constructor, without arguments, is intended to be used by the Tomcat
     * framework. Client code will need to set parameters using setters rather
     * than via the command line.
     */
    public Exomizer() {
        this.status_message = new ArrayList<String>();
    }

    /**
     * Constructor for web app
     *
     * @param ucsc_file A serialized file with the UCSC knownGene objects use
     * for annotating variants.
     */
    public Exomizer(String ucsc_file) {
        this.UCSCserializedFile = ucsc_file;
        try {
            deserializeUCSCdata();
        } catch (ExomizerException e) {
            logger.error("Error trying to de-serialise UCSC HG data: ", e);
            System.exit(1);
        }
    }

    /**
     * Constructor for Sanger web app
     */
    public Exomizer(HashMap<Byte, Chromosome> cMap) {
        this.chromosomeMap = cMap;
    }

    /**
     * @return the number of samples represented in the VCF file.
     */
    public int getNumberOfSamples() {
        return this.n_samples;
    }

    /**
     * @return List of Strings representing the sample names in the VCF file.
     */
    public List<String> getVCFSampleNames() {
        return this.sampleNames;
    }

    /**
     * Use the logic in the Prioritiser class to perform filtering and
     * prioritizing genes, and ranking the candidate genes. (see {@link exomizer.priority.Prioritiser Prioritiser}).
     * <p> Note that we assume that the transcript definition data has been
     * deserialized before this method is called. Also, the annotation of the
     * variants must have been performed. <P> Note that we now are downweighting
     * genes that have a lot of variants This will give genes such as the HLA
     * genes lower scores (they tend to have some many rare variants, that at
     * least one has a good pathogenicity score).
     *
     * @throws Exception if something goes wrong with processing the VCF file.
     */
    public void executePrioritization() throws ExomizerException {
        this.geneList = this.prioritiser.executePrioritization(this.variantList, this.useRankBasedScoring());
    }

    /**
     * This function decides whether to do scoring of genes according to the raw
     * score (default) or by rank (used for Phenomizer and for GeneWanderer
     * applications).
     */
    private boolean useRankBasedScoring() {
        if (this.useCRE) {
            return false;
        } else if (this.useRandomWalk) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * This function intends to check that PED file data is compatible to VCF
     * sample names. That is, are the names in the PED file identical with the
     * names in the VCF file? If there is a discrepancy, this function will
     * throw an exception. If everything is OK there, this function will
     * additional rearrange the order of the persons represented in the PED file
     * so that it is identical to the order in the VCF file. This will make
     * Pedigree analysis more efficient and the code more straightforward.
     */
    public void consolidateVCFandPedFileSamples() throws ExomizerException {
        if (this.pedigree == null) {
            String e = "[consolidateVCFandPedFileSamples] Error: PED file null";
            throw new ExomizerException(e);
        }
        try {
            /*
             * this is the function that does all of the work.
             */
            this.pedigree.adjustSampleOrderInPedFile(this.sampleNames);
        } catch (PedParseException ppe) {
            String e = String.format("[consolidateVCFandPedFileSamples]"
                    + " Error incurred while reordering ped file: %s", ppe.toString());
            throw new ExomizerException(e);
        }
    }

    /**
     * The Exomiser can perform filtering of VCF data according to pedigree
     * data. If the VCF file contains multiple samples, then the Exomiser
     * requires a corresponding PED file to be able to analyze the inheritance
     * pattern of the variants. The PED file is parsed by the PEDFileParser
     * class in jannovar. This results in a Pedigree object that is used to do
     * the pedigree analysis. The VCF Parser from Jannovar creates Genotype
     * objects for each variant in the VCF file, and these can be compared to
     * the pedigree information. The {@link exomizer.exome.Gene Gene} class
     * coordinates this analysis. <P> Note that for single-sample VCF files, a
     * Pedigree object is still constructed, and we assume that the sample is
     * from an affected person.
     */
    public void processPedigreeData() throws ExomizerException {
        logger.info("PROCESSING PEDIGREE DATA");
        if (this.n_samples == 1) {
            String sample = "single sample";
            if (this.sampleNames.size() > 0) {
                sample = this.sampleNames.get(0);
            }
            this.pedigree = Pedigree.constructSingleSamplePedigree(sample);
        } else {
            PedFileParser parser = new PedFileParser();
            if (this.pedFilePath == null && this.pedBufferedReader == null) {
                String s1 = String.format("[Exomiser] VCF file has %d samples but no PED file available.", this.n_samples);
                String s2 = String.format("%s\n[Exomiser] PED file must be be provided for multi-sample VCF files, terminating program.", s1);
                throw new ExomizerException(s2);
            }
            try {
                if (this.pedFilePath != null) {
                    this.pedigree = parser.parseFile(this.pedFilePath);
                    consolidateVCFandPedFileSamples();
                } else { /*
                     * If we get here, then pedBufferedReader is initialized
                     */
                    this.pedigree = parser.parseStream(this.pedBufferedReader);
                }
            } catch (PedParseException e) {
                String s1 = null;
                if (this.pedFilePath != null) {
                    s1 = this.pedFilePath;
                } else {
                    s1 = "PED file from input stream";
                }
                String s2 = String.format("Error while parsing PED file: \"%s\": %s", this.pedFilePath, e.getMessage());
                throw new ExomizerException(s2);
            }
        }
        /**
         * **********************************************************
         */
        /*
         * The Gene class uses the pedigree for segregation
         */
        /*
         * analysis, and thus gets a static reference to the
         */
        /*
         * Pedigree object.
         */
        /**
         * **********************************************************
         */
        Gene.setPedigree(this.pedigree);
    }

    /**
     * <P> Jannovar makes a serialized file that represents a
     * HashMap<String,TranscriptModel> containing each and every
     * {@link jannovar.reference.TranscriptModel TranscriptModel} object. This
     * method both deserializes this file and also adds each TranscriptModel to
     * the corresponding IntervalTree of the
     * {@link jannovar.reference.Chromosome Chromosome} object. When we are
     * done, the {@link exomizer.Exomizer#chromosomeMap} contains Chromosome
     * objects for chromosomes 1-22,X,Y, and M, each of which contains the
     * TranscriptModel objects for each of the genes located on those
     * chromosomes.
     */
    public void deserializeUCSCdata() throws ExomizerException {
        logger.info("DESERIALISING UCSC...");
        ArrayList<TranscriptModel> kgList = null;
        SerializationManager manager = new SerializationManager();
        try {
            kgList = manager.deserializeKnownGeneList(this.UCSCserializedFile);
        } catch (JannovarException je) {
            String s = String.format("Unable to deserialize the UCSC gene definition file: %s", je.toString());
            throw new ExomizerException(s);
        }
        this.chromosomeMap = Chromosome.constructChromosomeMapWithIntervalTree(kgList);
        logger.info("DONE DESERIALISING UCSC");
    }

    /**
     * Iterates over all the variants parsed from the VCF file and provides each
     * one with an annovar-style annotation.
     */
    public void annotateVariants() {
        Chromosome c = null;
        for (VariantEvaluation ve : this.variantList) {
            Variant v = ve.getVariant();
            // System.out.println(v);
            byte chr = v.getChromosomeAsByte();
            int pos = v.get_position();
            String ref = v.get_ref();
            String alt = v.get_alt();
            c = chromosomeMap.get(chr);
            if (c == null) {
                logger.error("Could not identify chromosome {}", chr);
            } else {
                try {
                    AnnotationList anno = c.getAnnotationList(pos, ref, alt);
                    if (anno == null) {
                        logger.info("No annotations found for variant {}", v);
                        continue;
                    }
                    v.setAnnotation(anno);
                } catch (AnnotationException ae) {
                    String s = String.format("Annotation exception for variant %s (%s)", v.getChromosomalVariant(), ae.toString());
                    this.status_message.add(s);
                } catch (Exception e) {
                    String s = String.format("Format/parse exception for variant %s (%s)", v.getChromosomalVariant(), e.toString());
                    this.status_message.add(s);
                }
            }
        }
    }

    /**
     * This method initializes the variant-level and gene-level filtering and
     * prioritization. Most of the action takes place in the class {@link exomizer.priority.Prioritiser Prioritiser}.
     */
    public void initializeFiltersAndPrioritizers() throws ExomizerInitializationException {
        this.prioritiser = new Prioritiser(this.connection);
        if (this.use_target_filter){
            this.prioritiser.addTargetFilter();
        }
        this.prioritiser.addFrequencyFilter(this.frequency_threshold, this.filterOutAlldbSNP);
        if (this.quality_threshold != null) {
            this.prioritiser.addQualityFilter(this.quality_threshold);
        }
        /*
         * the following shows P for everything and filters out if
         * use_pathogenicity_filter==true.
         */
        this.prioritiser.addPathogenicityFilter(this.use_pathogenicity_filter, this.use_target_filter);
        if (this.interval != null) {
            this.prioritiser.addLinkageFilter(this.interval);
        }
        this.prioritiser.addOMIMPrioritizer();
        if (this.inheritance_filter_type != null) {
            this.prioritiser.addInheritancePrioritiser(this.inheritance_filter_type);
        }
        if (doPhenomizerPrioritization()) {
            this.prioritiser.addPhenomizerPrioritiser(this.phenomizerDataDirectory, this.hpo_ids);
        } else if (doMGIPhenodigmPrioritization()) {
            this.prioritiser.addMGIPhenodigmPrioritiser(this.disease);
        } else if (hpo_ids != null) {
            if (doBOQAPrioritization()) {
                this.prioritiser.addBOQAPrioritiser(this.hpoOntologyFile, this.hpoAnnotationFile, this.hpo_ids);
            } else if (this.randomWalkFilePath != null && this.randomWalkIndexPath != null) {
                this.prioritiser.addDynamicPhenoWandererPrioritiser(this.randomWalkFilePath, this.randomWalkIndexPath, this.hpo_ids, this.candidateGene, this.disease, this.randomWalkMatrix);
            } else {
                this.prioritiser.addDynamicPhenodigmPrioritiser(this.hpo_ids);
            }
        } else if (doZFINPhenodigm()) {
            this.prioritiser.addZFINPrioritiser(this.disease);
        } else if (this.randomWalkFilePath != null && this.randomWalkIndexPath != null && this.disease != null) {
            this.prioritiser.addDynamicPhenoWandererPrioritiser(this.randomWalkFilePath, this.randomWalkIndexPath, this.disease, this.candidateGene, this.disease, this.randomWalkMatrix);
        } else if (this.randomWalkFilePath != null && this.randomWalkIndexPath != null && this.entrezSeedGenes != null) {
            this.prioritiser.addExomeWalkerPrioritiser(this.randomWalkFilePath, this.randomWalkIndexPath, this.entrezSeedGenes);
        }
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
     * This method can be used to add a {@link exomizer.priority.IPriority
     * IPriority} object that has been constructed elsewhere. This is
     * particularly useful for the ExomeWalker code base if it is started from
     * an apache tomcat Server, because we can construct the GeneWanderer object
     * once (it has ca. 1.5 Gb data) and keep it in memory as long as the
     * ExomeWalker servlet is in memory
     *
     * @param ip the {@link exomizer.priority.IPriority IPriority} that will be
     * added to the list of prioriitizers.
     */
    public void setPrioritizer(IPriority ip) throws ExomizerInitializationException {
        if (ip == null) {
            String s = "Attempt to initialize Exomiser with NULL IPriority object";
            throw new ExomizerInitializationException(s);
        }
        this.prioritiser.setPrioritizer(ip);
    }

    /**
     * Input the VCF file using the VCFReader class. The method will initialize
     * the snv_list, which contains one item for each variant in the VCF file,
     * as well as the header, which contains a list of the header lines of the
     * VCF file that will be used for printing the output filtered VCF. Note
     * that the {@code VCFReader} class is from the jannovar library. <P> The {@code Variant}
     * class is also from the Jannovar library, and it contains all of the
     * relevant information about variants that can be obtained from the VCF
     * file. The exomizer package has a class called
     * {@link exomizer.exome.VariantEvaluation VariantEvaluation}, which is used
     * to capture all of the evaluations (pathogenicity, frequency) etc., that
     * are not represented in the VCF file itself.
     *
     * @throws ExomizerException
     */
    public void parseVCFFile() throws ExomizerException {
        logger.info("READING VCF");
        /**
         * ***********************************************************
         */
        /*
         * 1) Input the VCF file from filepath or stream
         */
        /**
         * ***********************************************************
         */
        VCFReader parser = null;
        /*
         * Now decide whether the user has passed a file path or a
         * BufferedReader handle (the latter is likely to happen if the Exomizer
         * is being used by a tomcat server).
         */
        try {
            if (this.vcf_file != null) {
                parser = new VCFReader(this.vcf_file);
            } else if (this.vcfBufferedReader != null) {
                parser = new VCFReader(this.vcfBufferedReader);
            } else {
                String s = "Error, no VCF file found. Need to pass the path to a VCF file or an opened file handle";
                throw new ExomizerException(s);
            }
            parser.parseFile();
        } catch (VCFParseException ve) {
            String message = "Could not parse the VCF file: " + ve.toString();
            logger.error(message);
            throw new ExomizerException(message);
        }
        ArrayList<Variant> vlist = parser.getVariantList();
        this.variantList = new ArrayList<VariantEvaluation>();
        for (Variant v : vlist) {
            this.variantList.add(new VariantEvaluation(v));
        }
        this.header = parser.get_vcf_header();
        this.status_message = parser.get_html_message();
        this.before_NS_SS_I = parser.get_total_number_of_variants();
        this.sampleNames = parser.getSampleNames();
        this.n_samples = parser.getNumberOfSamples();
    }

    /**
     * @return A list of all genes.
     */
    public List<Gene> getGeneList() {
        return geneList;
    }

    public int getNumberOfGenes() {
        if (geneList == null) {
            return 0;
        }
        return geneList.size();
    }

    /**
     * @return A list of all filters.
     */
    public List<IFilter> getFilterList() {
        return this.prioritiser.getFilterList();
    }

    /**
     * @return A list of all prioritisers.
     */
    public List<IPriority> getPrioritisationList() {
        return this.prioritiser.getPriorityList();
    }

    /**
     * Connect to database and store connection in handle this.connect.
     */
    public void openNewDatabaseConnection() throws ExomizerInitializationException {
        logger.info("GETTING DATABASE CONNECTION");
        this.connection = ExomiserDatabase.openNewDatabaseConnection();
    }

    /**
     * Close database connection - required to stop Sanger server building up
     * stale connections
     */
    public void closeDatabaseConnection() throws ExomizerInitializationException {
        logger.info("CLOSING DATABASE CONNECTION");
        ExomiserDatabase.closeDatabaseConnection(this.connection);
    }

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
     */
    public void outputTSV() {
        String fname = vcf_file + ".results.tsv";
        logger.info("Writing TSV file to: {}", fname);
        try {
            FileWriter fstream = new FileWriter(fname);
            BufferedWriter out = new BufferedWriter(fstream);
            if (this.variantList == null) {
                out.write("<P>Error: Variant list not initialized correctly!</P>");
                out.close();
                return;
            }
            for (Gene g : geneList) {
                out.write(g.getTSVRow());
                if (candidateGene == null) {
                    out.write("\n");
                } else {
                    if (candidateGene.equals(g.getGeneSymbol())) {
                        out.write("\t1\n");
                    } else if (g.getGeneSymbol().startsWith(candidateGene + ",")) {
                        // bug fix for new Jannovar labelling where can have
                        // multiple genes per var
                        // but first one is most pathogenic
                        out.write("\t1\n");
                    } else {
                        out.write("\t0\n");
                    }
                }
            }
            out.close();
        } catch (IOException e) {
            logger.error("Error writing TSV file:", e);
        }
    }

    /**
     * This function, which will be called if the --vcf flag is used on the
     * command line, can be used to output VCF data which is useful for
     * downstream analysis using other software such as VarSIFTER
     */
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
        try {
            htmlWriter.writeVariantDistributionTable(vtc, this.sampleNames);
        } catch (ExomizerException e) {
            logger.error("Exception while output variant distribution: {}", e);
        }
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
     */
    public VariantTypeCounter getVariantTypeCounter() {
        VariantTypeCounter vtc = null;
        for (IFilter f : this.prioritiser.getFilterList()) {
            if (f.getFilterTypeConstant() == FilterType.EXOME_TARGET_FILTER) {
                TargetFilter tf = (TargetFilter) f;
                vtc = tf.getVariantTypeCounter();
                break;
            }
        }
        if (vtc == null) {
            TargetFilter tf = new TargetFilter();
            tf.filter_list_of_variants(this.variantList);
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
     * ClinVar to the top piroritized genes.
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
            List<VariantEvaluation> varList = g.get_variant_list();
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
     */
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
            } else if (this.useBOQA) {
                this.htmlWriter = new HTMLWriterBOQA(fname);
            } else if (this.useCRE) {
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
     * Parse the command line.
     *
     * @param args Copy of the command line parameters.
     */
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
                setBOQA_TermList(cmd.getOptionValue("B"));
                setUseBoqa();
            }
            if (cmd.hasOption("D")) {
                setUCSCserializedFile(cmd.getOptionValue("D"));
            }
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
    public void setDoClinicallyRelevantExomeServer() {
        this.useCRE = true;
        this.useRandomWalk = false;
        this.useBOQA = false;
    }

    public void setUCSCserializedFile(String ucscFile) {
        this.UCSCserializedFile = ucscFile;
    }

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

    /**
     * Set the VCF path ({@link #vcf_file}) as well as the base name of the VCF
     * file ({@link #vcf_file_basename}).
     */
    public void setVCFfile(String fname) {
        this.vcf_file = fname;
        int sep = fname.lastIndexOf("/");
        int wsep = fname.lastIndexOf("\\");
        sep = Math.max(sep, wsep);
        this.vcf_file_basename = fname.substring(sep + 1);
    }

    /**
     * This method is intended to be used by a tomcat server (e.g., ExomeWalker)
     * to pass in a file handle to a VCF file that has been uploaded and read
     * into a StringReader that in turn has been used to construct a
     * BufferedReader.
     *
     * @param br A file handle to a VCF file.
     */
    public void setVCFbufferedReader(BufferedReader br) {
        this.vcfBufferedReader = br;
    }

    public String getVCFfile() {
        return this.vcf_file;
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
     */
    public void setBOQA_TermList(String list) {
        this.hpo_ids = list;
    }

    public void setPedFile(String path) {
        this.pedFilePath = path;
    }

    /**
     * This method is intended to be used by a tomcat server (e.g., ExomeWalker)
     * to pass in a file handle to a PED file that has been uploaded and read
     * into a StringReader that in turn has been used to construct a
     * BufferedReader.
     *
     * @param br A file handle to a PED file.
     */
    public void setPedBufferedReader(BufferedReader br) {
        this.pedBufferedReader = br;
    }

    public String getPedFile() {
        return this.pedFilePath;
    }

    public Pedigree getPedigree() {
        return this.pedigree;
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
}