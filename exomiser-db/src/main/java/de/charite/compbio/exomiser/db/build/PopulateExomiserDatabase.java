//package de.charite.compbio.exomiser.db.build;
//
///** Command line functions from apache */
//import org.apache.commons.cli.CommandLine;
//import org.apache.commons.cli.GnuParser;
//import org.apache.commons.cli.HelpFormatter;
//import org.apache.commons.cli.Option;
//import org.apache.commons.cli.Options;
//import org.apache.commons.cli.ParseException;
//import org.apache.commons.cli.Parser;
//
//import java.io.File;
//import java.io.FileWriter;
//import java.io.BufferedWriter;
//import java.io.IOException;
//import java.sql.Connection;
//import java.util.ArrayList;
//import java.util.Collections;
//import java.util.Iterator;
//import java.util.Scanner;
//
//
//
////import de.charite.compbio.exomiser.exception.ExomizerException;
////import de.charite.compbio.exomiser.exception.ExomizerInitializationException;
//import de.charite.compbio.exomiser.parsers.DbSnpFrequencyParser;
//import de.charite.compbio.exomiser.parsers.EspFrequencyParser;
//import de.charite.compbio.exomiser.parsers.ExomiserDatabase;
//import de.charite.compbio.exomiser.parsers.HGMDParser;
//import de.charite.compbio.exomiser.parsers.HPOOntologyFileParser;
//import de.charite.compbio.exomiser.parsers.MIMParser;
//import de.charite.compbio.exomiser.parsers.NSFP2SQLDumpParser;
//import de.charite.compbio.exomiser.parsers.OrphaParser;
//import de.charite.compbio.exomiser.parsers.PhenoSeriesParser;
//import de.charite.compbio.exomiser.parsers.STRINGParser;
//import de.charite.compbio.exomiser.reference.Frequency;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
///**
//* This is the MAIN class for the application for the Exomiser Database application.
//* This application connects with the postgreSQL database and adds tables and data to
//* this database. All of the code that imports data into the SQL database should be controlled
//* by this class.
//* @author Peter N Robinson
//* @version 0.07 (7 December, 2013)
//*/
//public class PopulateExomiserDatabase {
//
//    private static final Logger logger = LoggerFactory.getLogger(PopulateExomiserDatabase.class);
//
//    private Connection connection = null;
//    /** Flag whether to show the statistics about the Exomiser database on the command line. */
//    private boolean showStatus = false;
//    /** Flag to show the versions of resources in Exomiser database. */
//    private boolean showVersions = false;
//
//    /** Path to directory with files from dbNFSP project,
//	which should contain one file for each chromosome */
//    private String nsfp_dir=null;
//    /**  Path to a directory that contains two files from OMIM: morbidmap and mim2gene as well as phenotype_annotation.tab. */
//    private String omim_dir=null;
//    /** Path to the dbSNP file {@code  00-All.vcf}. */
//    private String dbSNPfile=null;
//    /** Name of file with serialized UCSC data. */
//    private String ucscSerializedFile=null;
//    /** Directory with all the ESP files*/
//    private String espDirectory=null;
//    /** Path to the human-phenotype-ontology.obo file. */
//    private String hpo_path=null;
//    /** Path to the directory with file en_product1.xml (Disease data from Orphadata). */
//    private String orphanet_dir=null;
//    // Path to variant_summary.txt from NCBI ClinVar.
//    private String clinvarPath=null;
//    /**
//     * Path to file created by Perl scripts in ExomeWalker/extraStuff directory.
//     * The file is called pheno2gene.txt, see the README in that directory for information.
//     */
//    private String phenoseriesPath = null;
//    /** Flag to indicate whether we want to initialize the metadata table. */
//    private boolean initializeMetadata=false;
//
//    /** A String describing the version of the file being used for the database,
//     * e.g., version 2.04b
//     */
//    private String versionInfo=null;
//    /**
//     * A path to the datafile of the professional version of the Human Gene Mutation Database (HGMD),
//     * which is provided in the HGMD resource under {@code hgmd/pro/setup/data/allmut.txt}.
//     */
//    private String hgmdProPath=null;
//
//
//    /**
//     * Path to the STRING (http://string-db.org/) file protein.links.v9.0.txt (or 9.1).
//     */
//     private String stringPath = null;
//    /**
//     * In order to parse the STRING data (see {@link #stringPath}, we need to map between the
//     * Ensembl peptide ids (used by STRING) and EntrezGene ids. There is an R biomaRt script
//     * in the scripts directory of the Exomiser SVN repository that downloads all such mappings
//     * for homo sapiens. This creates a file with the columns ensembl_peptide_id, hgnc_gene_symbol,
//     * and entrezgene.*/
//    private String ensemblMapPath = null;
//
//
//    /**
//     * A string describing a resource. This is intended to be used together with the
//     * versionInfo option to add information about the version of the resource used to
//     * the database. For instance, to state that HPO version 2 was used, the user would
//     * pass {@code $ java -jar PopulateExomiserDatabase.jar --item HPO --version-info 2}.
//     */
//     private String item=null;
//
//    /**
//     * The main function creates a {@link PopulateExomiserDatabase} instance and
//     * calls the functions open, process, and close. The function
//     * {@link #process} calls appropriate functions to process input data depending
//     * on command line arguments.
//     */
//    public static void main(String[] args) {
//	PopulateExomiserDatabase ped = new PopulateExomiserDatabase(args);
//	try{
//	    ped.open();
//	    ped.process();
//	    ped.close();
//	} catch (Exception e) {
//	    e.printStackTrace();
//	    System.exit(1);
//	}
//    }
//
//    /**
//     * The constructor causes the command-line arguments to be
//     * parsed.
//     * @param args Command line arguments.
//     */
//    public PopulateExomiserDatabase(String[] args) {
//	parseCommandLineArguments(args);
//    }
//
//    /**
//     * Opens the handle to the postgreSQL "Exomiser" database.
//     */
//    public void open() {
//	try{
//	    ExomiserDatabase.registerPostgresqlDriver();
//	    this.connection = ExomiserDatabase.openNewDatabaseConnection();
//	} catch (Exception e) {
//	    logger.error("{}", e);
//	    System.exit(1);
//	}
//    }
//
//    /** Closes the database handle.
//     */
//    public void close() {
//	ExomiserDatabase.closeDatabaseConnection(this.connection);
//    }
//
//
//    /**
//     * Print out a list of Exomiser database tables and their row counts.
//     */
//    public void statusReport() {
//	ExomiserDatabase.printStatusReport(this.connection);
//    }
//
//    /**
//     * Print out a list of the versions of the data resources.
//     */
//    public void showVersions() {
//	ExomiserDatabase.printVersionReport(this.connection);
//    }
//
//    /**
//     * This function will decide what the user wants based on the command line arguments,
//     * and will call the corresponding function.
//     */
//    public void process() {
//	if (this.showStatus) {
//	    statusReport();
//	    System.exit(0);
//	} else if (this.initializeMetadata) {
//	    initializeMetadata();
//	} else if (this.nsfp_dir != null) {
//	    processNSFP();
//	} else if (this.omim_dir != null) {
//	    processOMIM();
//	} else if (this.dbSNPfile!=null &&
//		   this.ucscSerializedFile!=null &&
//		   this.espDirectory!=null) {
//	    processFrequencyData();
//	} else if (this.phenoseriesPath!=null) {
//	    processPhenoseries();
//	} else if (this.hpo_path != null) {
//	    processHPOFile();
//	} else if (this.item != null && this.versionInfo != null) {
//	    processVersionInfo();
//	} else if (this.hgmdProPath != null) {
//	    processHGMDProFile();
//	} else if (this.orphanet_dir != null) {
//	    processOrphanetFile();
//	} else if (this.clinvarPath!=null) {
//	    processClinVar();
//	} else if (this.stringPath != null && this.ensemblMapPath !=null) {
//	    processSTRING();
//	} else if (this.showVersions) {
//	    showVersions();
//	}else {
//	    usage();
//	}
//    }
//
//
//    /**
//     * This function will only be called if {@link #item} and
//     * {@link #versionInfo} are defined (have been passed from the
//     * command line). We use this to enter information about the
//     * versions of the data resources we have used to the
//     * postgreSQL database.
//     */
//    private void  processVersionInfo() {
//	/* check the following again to be sure, they should always
//	   be definted by the time we get here. */
//	if (this.item == null || this.versionInfo == null) {
//	    logger.error("Attempt to set version information with uninitialized variables");
//	    System.exit(1);
//	}
//
//	if (item.equals("human-phenotype-ontology.obo") || item.equals("hp.obo"))
//	    ExomiserDatabase.updateVersionInfo("human-phenotype-ontology.obo",versionInfo,connection);
//	else if (item.equalsIgnoreCase("dbNSFP"))
//	    ExomiserDatabase.updateVersionInfo("dbNSFP",versionInfo,connection);
//	else if (item.equals("ESP") || item.equals("Exome Server Project"))
//	    ExomiserDatabase.updateVersionInfo("Exome Server Project",versionInfo,connection);
//	else if (item.equals("dbSNP"))
//	    ExomiserDatabase.updateVersionInfo("dbSNP",versionInfo,connection);
//	else if (item.equalsIgnoreCase("HGMDPRO"))
//	    ExomiserDatabase.updateVersionInfo("HGMD Pro",versionInfo,connection);
//	else if (item.equalsIgnoreCase("OMIM"))
//	    ExomiserDatabase.updateVersionInfo("OMIM",versionInfo,connection);
//	else if (item.equalsIgnoreCase("OMIM_phenotypic_series"))
//	    ExomiserDatabase.updateVersionInfo("OMIM phenotypic series",versionInfo,connection);
//	else if (item.equalsIgnoreCase("STRING"))
//	    ExomiserDatabase.updateVersionInfo("STRING",versionInfo,connection);
//	else if (item.equalsIgnoreCase("ClinVar"))
//	    ExomiserDatabase.updateVersionInfo("ClinVar",versionInfo,connection);
//	else {
//	    logger.error("Did not recognize resource: " + item);
//	    logger.error("See \"processVersionInfo()\" in \"PopulateExomiserDatabase.java\"");
//
//	}
//	// TODO Ensure that the user enters correct names for items.
//	//ExomiserDatabase.updateVersionInfo(this.item, this.versionInfo,this.connection);
//    }
//
//
//    private void processSTRING() {
//	//ExomiserDatabase.createStringTable(this.connection);
//	ExomiserDatabase.createEntrezGeneTable(this.connection);
//	STRINGParser parser = new STRINGParser(this.stringPath,this.ensemblMapPath,this.connection);
//	parser.parseFiles();
//    }
//
//
//    private void  processHGMDProFile() {
//	ExomiserDatabase.createHGMDproTable(this.connection);
//	HGMDParser parser = new HGMDParser();
//	parser.parseHGMDProFile(this.hgmdProPath);
//	String propth = "/path/hgmdpro.pg";
//	try {
//	    File f = new File("hgmdpro.pg");
//	    propth = f.getAbsolutePath();
//	} catch (Exception e) {
//	    e.printStackTrace();
//	    // skip
//        }
//	String diseasePth ="/path/hgmddisease.pg";
//	try {
//	    File f = new File("hgmddisease.pg");
//	    diseasePth = f.getAbsolutePath();
//	} catch (Exception e) {
//	    e.printStackTrace();
//	    // skip
//	}
//	System.out.println("[INFO] I have just created the file \"hgmdpro.pg\" and \"hgmddisease.pg\"");
//	System.out.println("[INFO] You will need to import these files into the Exomiser database (hgmddisease first)");
//	System.out.println("\t$ psql -h localhost exomizer -U postgres -W");
//	System.out.println("\t(enter password for user \"postgres\")");
//	System.out.println(String.format("\texomizer=# COPY hgmddisease FROM \'%s' WITH DELIMITER \'|\';",diseasePth));
//	System.out.println(String.format("\texomizer=# COPY hgmdpro FROM \'%s' WITH DELIMITER \'|\';",propth));
//	System.out.println("\t(\"/path/\" refers to the full path to the \"hgmdpro.pg\" file");
//    }
//
//
//    private void  processOrphanetFile() throws ExomizerException {
//	ExomiserDatabase.createOrphanetTable(this.connection);
//	/* Path to en_product1.xml (orphanet) */
//	String orphaXMLPath=null;
//	/* Path to ORPHANET_ALL_FREQUENCIES_diseases_to_genes_to_phenotypes.txt */
//	String allFreqPath=null;
//	/** Path to  phenotype_annotation.tab */
//	String hpoAnnotPath=null;
//	String path = String.format("%s/en_product1.xml",orphanet_dir);
//	File f = new File(path);
//	if (f.exists() ) {
//	    orphaXMLPath = f.getAbsolutePath();
//	} else {
//	    System.out.println("[ERROR] Could not find \"en_product1.xml\" in directory: "+
//			       this.orphanet_dir);
//	    System.out.println("[ERROR] Please try again with the correct path");
//	    return;
//	}
//	path = String.format("%s/ORPHANET_ALL_FREQUENCIES_diseases_to_genes_to_phenotypes.txt",orphanet_dir);
//	f = new File(path);
//	if (f.exists() ) {
//	   allFreqPath = f.getAbsolutePath();
//	} else {
//	    System.out.println("[ERROR] Could not find \"ORPHANET_ALL_FREQUENCIES_diseases_to_genes_to_phenotypes.txt\" in directory: "+
//			       this.orphanet_dir);
//	    System.out.println("[ERROR] Please try again with the correct path");
//	    return;
//	}
//	path = String.format("%s/phenotype_annotation.tab",orphanet_dir);
//	f = new File(path);
//	if (f.exists() ) {
//	    hpoAnnotPath = f.getAbsolutePath();
//	} else {
//	    System.out.println("[ERROR] Could not find \"phenotype_annotation.tab\" in directory: "+
//			       this.orphanet_dir);
//	    System.out.println("[ERROR] Please try again with the correct path");
//	    return;
//	}
//
//	OrphaParser orpha = new OrphaParser(this.connection,hpoAnnotPath);
//	orpha.inputOrphadataXML(orphaXMLPath);
//	System.out.println("About to input all freq");
//	orpha.inputALL_FREQUENCIES(allFreqPath);
//
//    }
//
//    private void processHPOFile() {
//	ExomiserDatabase.createHPOTable(this.connection);
//	HPOOntologyFileParser parser = new HPOOntologyFileParser( this.connection);
//	parser.parseHPO(this.hpo_path);
//    }
//
//    /**
//     * Add the OMIM phenotypicSeries to the database.
//     */
//    private void processPhenoseries() {
//	ExomiserDatabase.createPhenoseriesTables(this.connection);
//	PhenoSeriesParser parser = new PhenoSeriesParser(this.connection,this.phenoseriesPath);
//	parser.parsePhenoSeries();
//    }
//
//
//
//
//    /**
//     * This method will create an empty table called frequency (it will delete an old copy of frequency if there
//     * is one already), and it will then parse the data from dbSNP (1000 Genomes frequency data) and from ESP to
//     * create a table called "frequency".
//     */
//    private void processFrequencyData() {
//	ExomiserDatabase.createFrequencyTable(this.connection);
//	/* First parse the dnSNP data. */
//	logger.info("parsing the sbSNP data");
//	DbSnpFrequencyParser parser = new DbSnpFrequencyParser();
//	parser.deserializeUCSCdata(this.ucscSerializedFile);
//	parser.parse_dbSNPFile(this.dbSNPfile);
//	ArrayList<Frequency> frequencyList =  parser.getFrequencyList();
//	if (frequencyList == null) {
//	    logger.error("Problem getting Frequency data");
//	    System.exit(1);
//	}
//	Collections.sort(frequencyList);
//	/* Now parse the ESP data. */
//	EspFrequencyParser espparser = new  EspFrequencyParser(frequencyList);
//	logger.info("parsing the ESP files in the directory: " + espDirectory);
//	espparser.parseESPFiles(this.espDirectory);
//	espparser.mergeAndSortFrequencyObjects();
//	frequencyList = espparser.getFrequencyList();
//	/* Remove duplicates */
//	if (frequencyList == null || frequencyList.isEmpty()) {
//	    logger.error("Attempt to remove duplicates from null or empty frequencyList");
//	    System.exit(1);
//	}
//	int i;
//	int N = frequencyList.size();
//	Frequency previous = frequencyList.get(0);
//	float previousMaxFreq = previous.getMaximumFrequency();
//	Iterator<Frequency> it = frequencyList.iterator();
//	while (it.hasNext()) {
//	    Frequency fr = it.next();
//	    float freq = fr.getMaximumFrequency();
//	    int comp = fr.compareTo(previous);
//	    if (comp == 0) {
//		/* fr is identical to previous. Store the most common values in
//		   previous and delete the current value. */
//		// TODO -- delete the current value.
//	    }
//	}
//	String fname = "frequency.pg";
//
//	try {
//	    FileWriter fwriter = new FileWriter(fname);
//	    BufferedWriter out  = new BufferedWriter(fwriter);
//	    for (Frequency f: frequencyList) {
//		out.write(f.getDumpLine());
//	    }
//	    out.close();
//	} catch (IOException e) {
//	    logger.error("Could not initialize file handles for output. {}", e);
//	    System.exit(1);
//	}
//        logUserImportInstructionsForFileName(fname);
//    }
//
//    /**
//     * This function directs process of the OMIM data that is
//     * required for the postgreSQL omim table. We have both
//     * mim2gene as well as the morbid map from the OMIM website.
//     * It also uses our phenotype_annotation.tab file. This function
//     * first checks whether these three files are there, and if so passes
//     * them on to {@link exomizer.io.MIMParser MIMParser} to do the
//     * actual work of parsing.
//     * <P>
//     * Note that there is a script in the {@code data} directory of the
//     * Exomiser repository that allows will automatically download all three
//     * files (and others). You will need to alter the script to correspond
//     * to your OMIM FTP password though.
//     */
//    private void processOMIM() {
//	ExomiserDatabase.createOMIMTable(this.connection);
//	/** Path to OMIM file mim2gene.txt */
//	String mim2genePath=null;
//	/** Path to OMIM file morbid map */
//	String morbidMapPath=null;
//	/** Path to  phenotype_annotation.tab */
//	String hpoAnnotPath=null;
//	String path = String.format("%s/mim2gene.txt", omim_dir);
//	File f = new File(path);
//	if (f.exists() ) {
//	    mim2genePath = f.getAbsolutePath();
//	} else {
//	    logger.error("Could not find 'mim2gene.txt' in directory: {}", omim_dir);
//	    logger.error("Please try again with the correct path");
//	    return;
//	}
//	path = String.format("%s/morbidmap", omim_dir);
//	f = new File(path);
//	if (f.exists()) {
//	    morbidMapPath  = f.getAbsolutePath();
//	} else {
//	    logger.error("Could not find 'morbid' in directory: ", omim_dir);
//	    logger.error("Please try again with the correct path");
//	    return;
//	}
//	path = String.format("%s/phenotype_annotation.tab",omim_dir);
//	f = new File(path);
//	if (f.exists() ) {
//	    hpoAnnotPath = f.getAbsolutePath();
//	} else {
//	    System.out.println("[ERROR] Could not find \"phenotype_annotation.tab\" in directory: "+
//			       this.omim_dir);
//	    System.out.println("[ERROR] Please try again with the correct path");
//	    return;
//	}
//
//	MIMParser parser = new MIMParser(mim2genePath, morbidMapPath, hpoAnnotPath);
//	parser.addOMIMDataToDatabase(this.connection);
//        logUserImportInstructionsForFileName(fname);
//    }
//
//
//
//
//
//    public void initializeMetadata() {
//	System.out.println("This will delete the metadata table. Are you sure you want to proceed?");
//	System.out.println("This will erase any version information currently in the database");
//	System.out.println("If you are unsure, terminate program execution (by entering 'N')");
//	System.out.println("Enter 'Yes' if you are sure you want to do this:");
//	String input = null;
//	Scanner sc = new Scanner(System.in);
//	input = sc.next();
//
//	if (input.equals("Yes")) {
//	    ExomiserDatabase.createMetaTable(this.connection);
//	} else {
//	    System.out.println(String.format("Your input was '%s'. Exiting program without doing anything!", input));
//	    System.exit(1);
//	}
//    }
//
//    /**
//     * Parse the clinvar flat file and add each variant to
//     * the exomizer database.
//     */
//    private void processClinVar() {
//	ExomiserDatabase.createClinVarTable(this.connection);
//	ClinVarParser parser = new ClinVarParser();
//	parser. parseVariantFile(this.clinvarPath);
//	parser.addClinVarDataToDatabase(this.connection);
//    }
//
//
//
//    /**
//     * This function creates the variant table, which is used to store the
//     * data from dbNSFP. It then parses the dbNSFP data and adds it to a dump
//     * file (which is much quicker than importing the data by INSERT Statements)
//     */
//    private void processNSFP() {
//	ExomiserDatabase.createNSFPTable(this.connection);
//	ArrayList<String>  chromosome_file_paths = get_list_of_NSFP_chromosome_files();
//	NSFP2SQLDumpParser parser = new NSFP2SQLDumpParser();
//	Iterator<String> it = chromosome_file_paths.iterator();
//	while (it.hasNext()) {
//	    String fname = it.next();
//	    parser.input_chromosome(fname);
//	}
//	parser.closeFilehandles();
//        logUserImportInstructionsForFileName("variant.pg");
//    }
//
//     /**
//     * Get a list of all dbNSFP Annotation files from the
//     * annotation_directory. An ArrayList is created with the
//     * paths to the 24 chromosome files from the dbNSFP project.
//     * If there is a problem with getting the paths of all 24
//     * chromosome files, this function will terminate the program.
//     * @return A list of paths to the 24 chromosome files of the dbNSFP project
//     */
//    public ArrayList<String> get_list_of_NSFP_chromosome_files()
//    {
//	if (nsfp_dir != null)
//	    logger.info("Getting list of dbNSFP files from directory: {}", this.nsfp_dir);
//	else {
//	    logger.error("dbNFSP path was not initialized");
//	    System.exit(1);
//	}
//	ArrayList<String> chromosome_file_paths = new ArrayList<String>();
//
//	File folder = new File(nsfp_dir);
//	if (folder == null || ! folder.isDirectory()) {
//	    logger.error("Could not initialize folder at path {}", nsfp_dir);
//	    System.exit(1);
//	}
//	File[] listOfFiles = folder.listFiles();
//	logger.info("There were {} files in all in the dbNSFP folder", listOfFiles.length);
//
//
//	for (int i = 0; i < listOfFiles.length; i++) {
//	    if (listOfFiles[i].isFile()) {
//		String f = listOfFiles[i].getName();
//		int ind = f.indexOf("chr"); // Just extract chromosome files
//		if (ind < 0) continue;
//		String path = nsfp_dir + "/" + f;
//		chromosome_file_paths.add(path);
//	    }
//	}
//	if (chromosome_file_paths.size() != 24) {
//	    logger.warn("Did not get all 24 chromosome files for NSFP");
//	    logger.warn("Got a total of {} chromosome files.", chromosome_file_paths.size());
//	    System.exit(1);
//	}
//	return chromosome_file_paths;
//    }
//
//
//
//
//
//      /**
//     * Parse the command line. The important options are -n: path to the directory with the NSFP files,
//     * and -C a flag indicating that we want the program to delete the current table in the postgres
//     * database and to create an empty table (using JDBC connection).
//     * @param args Copy of the command line arguments.
//     */
//    private void parseCommandLineArguments(String[] args)
//    {
//	try
//	{
//	    Options options = new Options();
//	    options.addOption(new Option("h","help",false,"Shows this help"));
//	    options.addOption(new Option(null,"status",false,"Print out Exomiser database status"));
//	    options.addOption(new Option(null,"nsfp",true,"Directory containing NSFP annotation files. Required"));
//	    options.addOption(new Option(null,"omim",true,"Path to directory containing files \"morbidmap\" and \"mim2gene\""));
//	    options.addOption(new Option(null,"dbSNP",true, "Path to dbSNP file \"00-All.vcf\""));
//	    options.addOption(new Option(null,"esp",true, "Path to directory with Exome Server Project frequency data"));
//	    options.addOption(new Option(null,"phenoseries",true,"Path to phenoseries file."));
//	    options.addOption(new Option(null,"hpo",true,"Path to human-phenotype-ontology.obo or hp.obo file"));
//	    options.addOption(new Option(null,"init-metadata",false,"Initialize metadata table (just do once in lifetime of database)"));
//	    options.addOption(new Option("D","ucsc",true,"Path to serialized ucsc knownGenes file. Required"));
//	    options.addOption(new Option(null,"version-info",true,"String describing version of resource being uploaded to database"));
//	    options.addOption(new Option(null,"item",true,"String describing a resource (to be used together with version-info)"));
//	    options.addOption(new Option(null,"hgmd-pro",true,"Path to the professional-version data file (\"allmut.txt\") of HGMD"));
//	    options.addOption(new Option(null,"show-versions",false,"Show the versions of the resources contained in database"));
//	    options.addOption(new Option(null,"orphanet",true,"Path to directory with en_product1.xml from Orphadata and " +
//					 " to ORPHANET_ALL_FREQUENCIES_diseases_to_genes_to_phenotypes.txt"));
//	    options.addOption(new Option(null,"clinvar",true,"Path to \"variant_summary.txt\" from NCBI ClinVar"));
//	    options.addOption(new Option(null,"string",true,"Path to \"protein.links.v9.1.txt\" from STRING"));
//	    options.addOption(new Option(null,"ensemblMap",true,"Path to \"ensemblProt2entrezGene.txt\" (created by our own R script)"));
//
//	    Parser parser = new GnuParser();
//	    CommandLine cmd = parser.parse(options,args);
//	    int size = cmd.getOptions().length;
//	    if (size == 0)  {
//		usage();
//		System.exit(1);
//	    }
//
//
//	    if (cmd.hasOption("h"))
//	    {
//		HelpFormatter formatter = new HelpFormatter();
//		formatter.printHelp("PopulationExomiserDatabase", options);
//		usage();
//		System.exit(0);
//	    }
//	    /* Do not put any other option command before this,
//	     * it is possible to add version info to other commands,
//	     * but the other if-expressions return once they find something.
//	     */
//	    if (cmd.hasOption("version-info")) {
//		this.versionInfo = cmd.getOptionValue("version-info");
//	    }
//
//	    if (cmd.hasOption("show-versions")) {
//		this.showVersions=true;
//		return;
//	    }
//
//	    if (cmd.hasOption("status")) {
//		this.showStatus=true;
//		return;
//	    }
//
//	     if (cmd.hasOption("init-metadata")) {
//		 this.initializeMetadata=true;
//		 return;
//	     } else {
//		 this.initializeMetadata=false;
//	     }
//
//	    if (cmd.hasOption("nsfp")) {
//		this.nsfp_dir = cmd.getOptionValue("nsfp");
//		return;
//	    }
//
//	     if (cmd.hasOption("omim")) {
//		this.omim_dir = cmd.getOptionValue("omim");
//		return;
//	     }
//
//	     if (cmd.hasOption("hpo")) {
//		this.hpo_path = cmd.getOptionValue("hpo");
//		return;
//	     }
//
//	     if (cmd.hasOption("dbSNP") && cmd.hasOption("esp") && cmd.hasOption("D")) {
//		 this.dbSNPfile=cmd.getOptionValue("dbSNP");
//		 this.ucscSerializedFile=cmd.getOptionValue("D");
//		 this.espDirectory=cmd.getOptionValue("esp");
//		 return;
//	     }
//
//	     if (cmd.hasOption("phenoseries")) {
//		 this.phenoseriesPath=cmd.getOptionValue("phenoseries");
//		 return;
//	     }
//
//	     if (cmd.hasOption("orphanet") ) {
//		 this.orphanet_dir = cmd.getOptionValue("orphanet");
//		 return;
//	     }
//
//	     if (cmd.hasOption("clinvar")) {
//		 this.clinvarPath = cmd.getOptionValue("clinvar");
//		 return;
//	     }
//
//	     if (cmd.hasOption("string") && cmd.hasOption("ensemblMap")) {
//		 this.stringPath = cmd.getOptionValue("string");
//		 this.ensemblMapPath = cmd.getOptionValue("ensemblMap");
//		 return;
//	     }
//
//
//	     /* The following are used to allow user to enter the
//		version info for resources such as dbSNP, ESP, HPO, to
//		allow the website to show how current the information used
//		for the analysis is. */
//	     if (cmd.hasOption("item")) {
//		 if (cmd.hasOption("version-info")) {
//		     this.item = cmd.getOptionValue("item");
//		     this.versionInfo = cmd.getOptionValue("version-info");
//		 } else {
//		     logger.error("If you pass an 'item' you also need to pass the version info");
//		     System.exit(1);
//		 }
//	     } else if (cmd.hasOption("version-info")) {
//		 logger.error("If you pass version info  you also need to pass an 'item'");
//		 System.exit(1);
//	     }
//
//	     else if (cmd.hasOption("hgmd-pro")) {
//		 this.hgmdProPath = cmd.getOptionValue("hgmd-pro");
//		 return;
//	     }
//
//
//
//
//	} catch (ParseException pe)
//	{
//	    logger.error("Error parsing command line options. {}", pe);
//	    System.exit(1);
//	}
//    }
//
//    /**
//     * This function is used to ensure that certain options are passed to the
//     * program before we start execution.
//     *
//     * @param cmd An apache CommandLine object that stores the command line arguments
//     * @param name Name of the argument that must be present
//     * @return Value of the required option as a String.
//     */
//    private static String getRequiredOptionValue(CommandLine cmd, char name)
//    {
//	String val = cmd.getOptionValue(name);
//	if (val == null)
//	    {
//		logger.error("Aborting because the required argument '-{}' wasn't specified! Use the -h for more help.", name);
//		System.exit(-1);
//	    }
//	return val;
//    }
//
//    /** Remind ourselves what is going on if the command line args are incorrect. */
//    private void usage() {
//
//	logger.info("PopulateExomiserDatabase");
//	logger.info("Usage: $ java -jar PopulateExomiserDatabase.jar [OPTIONS]");
//	logger.info("where options should be one of the following:");
//	logger.info("--status\tPrint out list of tables and row counts for Exomiser database");
//	logger.info("--nsfp path\tDelete current NSFP table and input NSFP data from directory at 'path'");
//	logger.info("--dbSNP path --esp path -D ucsc_hg19.ser");
//	logger.info("--phenoseries path  (initialize the OMIM data table)");
//	logger.info("\t\tDelete current frequency table and recreate from dbSNP, ESP, and Jannovar (ucsc.ser) files");
//
//    }
//
//    private void logUserImportInstructionsForFileName(String fname) {
//        logger.info("I have just created the file {}", fname);
//	logger.info("You will need to import this file into the Exomiser database");
//	logger.info("\t$ psql -h localhost exomizer -U postgres -W");
//	logger.info("\t(enter password for user \"postgres\")");
//	logger.info("\texomizer=# COPY frequency FROM \'/path/{}\' WITH DELIMITER \'|\';", fname);
//	logger.info("\t(\"/path/\" refers to the full path to the {} file", fname);
//    }
//
//
//
//}