package de.charite.compbio.exomiser.priority.util;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

public class ScoreDistributionContainer {

	private boolean verboseParsing;

	private int maxNumberQueryTerms = 20;

	public ScoreDistributionContainer(boolean verboseInParsing) {

		this.verboseParsing = verboseInParsing;
		this.key2scoreDistribution = new HashMap<String, Map<String, ScoreDistribution>>();
	}

	private Map<String, Map<String, ScoreDistribution>> key2scoreDistribution = null;

	public ScoreDistributionContainer() {
		this.key2scoreDistribution = new HashMap<String, Map<String, ScoreDistribution>>();
	}

	public synchronized void addDistribution(String diseaseId, int numberQueryTerms, boolean symmetric, ScoreDistribution actualDistribution) {

		String key = getKey(symmetric, numberQueryTerms);

		Map<String, ScoreDistribution> mim2scoredist;
		if (key2scoreDistribution.containsKey(key))
			mim2scoredist = key2scoreDistribution.get(key);
		else
			mim2scoredist = new HashMap<String, ScoreDistribution>();

		mim2scoredist.put(diseaseId, actualDistribution);
		key2scoreDistribution.put(key, mim2scoredist);

	}

	public static String getKey(boolean symmetric, int numberQueryTerms) {

		if (symmetric)
			return numberQueryTerms + "_symmetric";
		else
			return numberQueryTerms + "";
	}

	public ScoreDistribution getDistribution(String entrezGeneId, int numQueryTerms, boolean symmetric, String scoreDistributionFolder) {

		if (numQueryTerms > maxNumberQueryTerms)
			numQueryTerms = maxNumberQueryTerms;

		while (true) {

			String key = getKey(symmetric, numQueryTerms);
			Map<String, ScoreDistribution> mim2scoredist = key2scoreDistribution.get(key);
			ScoreDistribution scoreDist = null;
			if (mim2scoredist != null)
				scoreDist = mim2scoredist.get(entrezGeneId);

			if (mim2scoredist == null || scoreDist == null) {
				if (verboseParsing) {
					System.err.println("Could not find scoreDistribution for entrezid " + entrezGeneId + " and " + numQueryTerms + " and "
							+ symmetric);
					System.err.println("Used key: " + key);
				}
				if (numQueryTerms > 1) {
					numQueryTerms = numQueryTerms - 1;
					if (verboseParsing)
						System.err.println("setting numQueryTerms to: " + numQueryTerms);

					if (!didParseDistributions(symmetric, numQueryTerms)) {
						if (verboseParsing)
							System.err.println("try parsing file for new numQueryTerms");
						parseDistributions(symmetric, numQueryTerms, scoreDistributionFolder);
					}

				}
				else {
					System.err.println("NO WAY! Could not even find scoreDistribution for entrezgene " + entrezGeneId + " and " + numQueryTerms
							+ " and " + symmetric);
					System.err.println("returning null now");
					return null;
				}
			}
			else {
				return scoreDist;
			}
		}
	}

	public boolean didParseDistributions(boolean symmetric, int numQueryTerms) {

		if (numQueryTerms > maxNumberQueryTerms)
			numQueryTerms = maxNumberQueryTerms;
		return key2scoreDistribution.containsKey(getKey(symmetric, numQueryTerms));
	}

	/**
	 * 
	 * 
	 * @param symmetric
	 * @param numQueryTerms
	 * @param distributionsFolder
	 */
	public synchronized void parseDistributions(boolean symmetric, int numQueryTerms, String distributionsFolder) {

		if (numQueryTerms > maxNumberQueryTerms)
			numQueryTerms = maxNumberQueryTerms;

		String key = getKey(symmetric, numQueryTerms);
		String line = null;
		String file = distributionsFolder + key + ".out";

		// if (name.startsWith("TermOv") || name.startsWith("SimpleFeature"))
		// file = folder+key;
		// else
		// file = folder+key+".out";

		if (verboseParsing)
			System.out.println("try parsing file: " + file);
		try {
			final BufferedReader in = new BufferedReader(new FileReader(file));
			ScoreDistribution actualDistribution = null;
			String actualDiseaseId = null;
			double numberRandomizations = -1;
			List<Double> scores = new ArrayList<Double>();
			List<Double> pvalues = new ArrayList<Double>();
			while ((line = in.readLine()) != null) {

				if (line.startsWith(">")) {

					line = line.replaceAll(">", "");

					if (actualDistribution != null) {
						actualDistribution.setDistribution(scores, pvalues, numberRandomizations);
						addDistribution(actualDiseaseId, numQueryTerms, symmetric, actualDistribution);
						scores = new ArrayList<Double>();
						pvalues = new ArrayList<Double>();
					}
					actualDistribution = new ScoreDistribution();

					String[] split = line.split("_");
					actualDiseaseId = split[0];
					numberRandomizations = Double.parseDouble(split[1]);
				}
				else {
					int indexFirstMinus = line.indexOf('-');
					Double score;
					try {
						score = Double.parseDouble(line.substring(0, indexFirstMinus));
					} catch (NumberFormatException e) {
						indexFirstMinus = line.indexOf('-', indexFirstMinus + 1);
						score = Double.parseDouble(line.substring(0, indexFirstMinus));
					}
					Double pValue = Double.parseDouble(line.substring(indexFirstMinus + 1));
					scores.add(score);
					pvalues.add(pValue);
				}
			}// end while
			if (verboseParsing)
				System.out.println("done while loop.... add last");

			actualDistribution.setDistribution(scores, pvalues, numberRandomizations);
			addDistribution(actualDiseaseId, numQueryTerms, symmetric, actualDistribution);
		} catch (IOException e) {
			throw new RuntimeException(line + "\n" + e);

		}
		if (verboseParsing)
			System.out.println("done parsing");

	}

	public void setMaxNumberQueryTerms(int maxNumberQueryTerms) {
		this.maxNumberQueryTerms = maxNumberQueryTerms;
	}

}
