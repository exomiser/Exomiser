/*
 * The Exomiser - A tool to annotate and prioritize variants
 *
 * Copyright (C) 2012 - 2016  Charite Universitätsmedizin Berlin and Genome Research Ltd.
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as
 *  published by the Free Software Foundation, either version 3 of the
 *  License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.charite.compbio.exomiser.core.prioritisers.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ScoreDistributionContainer {

    private final Logger logger = LoggerFactory.getLogger(ScoreDistributionContainer.class);
    
	private boolean verboseParsing;

	private int maxNumberQueryTerms = 20;
	private Map<String, Map<String, ScoreDistribution>> key2scoreDistribution;

	public ScoreDistributionContainer(boolean verboseParsing) {
		this.verboseParsing = verboseParsing;
		this.key2scoreDistribution = new HashMap<>();
	}

	public ScoreDistributionContainer() {
		this.key2scoreDistribution = new HashMap<>();
	}

	public synchronized void addDistribution(String diseaseId, int numberQueryTerms, boolean symmetric, ScoreDistribution actualDistribution) {

		String key = getKey(symmetric, numberQueryTerms);

		Map<String, ScoreDistribution> mim2scoredist;
		if (key2scoreDistribution.containsKey(key))
			mim2scoredist = key2scoreDistribution.get(key);
		else
			mim2scoredist = new HashMap<>();

		mim2scoredist.put(diseaseId, actualDistribution);
		key2scoreDistribution.put(key, mim2scoredist);

	}

	public static String getKey(boolean symmetric, int numberQueryTerms) {
		if (symmetric)
			return Integer.toString(numberQueryTerms) + "_symmetric";
		else
			return Integer.toString(numberQueryTerms);
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
					logger.error("Could not find scoreDistribution for entrezid {} numQueryTerms: {} symmetric: {} using key: {}" , entrezGeneId, numQueryTerms, symmetric, key);
				}
				if (numQueryTerms > 1) {
					numQueryTerms = numQueryTerms - 1;
					if (verboseParsing) {
						logger.error("setting numQueryTerms to: {}", numQueryTerms);
					}
					if (!didParseDistributions(symmetric, numQueryTerms)) {
						if (verboseParsing) {
							logger.error("try parsing file for new numQueryTerms");
						}
						parseDistributions(symmetric, numQueryTerms, scoreDistributionFolder);
					}

				}
				else {
					logger.error("NO WAY! Could not even find scoreDistribution for entrezid {} numQueryTerms: {} symmetric: {} using key: {} - returning null", entrezGeneId, numQueryTerms, symmetric, key);
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
			logger.info("try parsing file: {}", file);
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
				logger.info("done while loop.... add last");

			actualDistribution.setDistribution(scores, pvalues, numberRandomizations);
			addDistribution(actualDiseaseId, numQueryTerms, symmetric, actualDistribution);
		} catch (IOException e) {
			throw new RuntimeException(line + "\n" + e);

		}
		if (verboseParsing)
			logger.info("done parsing");

	}

	public void setMaxNumberQueryTerms(int maxNumberQueryTerms) {
		this.maxNumberQueryTerms = maxNumberQueryTerms;
	}

}
