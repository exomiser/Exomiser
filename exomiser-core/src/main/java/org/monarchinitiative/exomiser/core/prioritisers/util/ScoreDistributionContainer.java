/*
 * The Exomiser - A tool to annotate and prioritize genomic variants
 *
 * Copyright (c) 2016-2017 Queen Mary University of London.
 * Copyright (c) 2012-2016 Charité Universitätsmedizin Berlin and Genome Research Ltd.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.monarchinitiative.exomiser.core.prioritisers.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Sebastian Köhler <dr.sebastian.koehler@gmail.com>
 *
 */
public class ScoreDistributionContainer {

    private final Logger logger = LoggerFactory.getLogger(ScoreDistributionContainer.class);
    
	private boolean verboseParsing = false;
	private final String distributionsFolder;
    private final boolean symmetric;
    private final int numQueryTerms;
	private static final int MAX_NUMBER_QUERY_TERMS = 20;
	private Map<String, Map<String, ScoreDistribution>> key2scoreDistribution;

	public ScoreDistributionContainer(String distributionsFolder, boolean symmetric, int numQueryTerms) {
		this.distributionsFolder = distributionsFolder;
        this.symmetric = symmetric;
        this.numQueryTerms = limitNumQueryTerms(numQueryTerms);
		this.key2scoreDistribution = new HashMap<>();
        parseDistributions(this.numQueryTerms);
	}

	public void useVerboseParsing() {
        this.verboseParsing = true;
    }

    private int limitNumQueryTerms(int numQueryTerms) {
        //numQueryTerms is used as a look-up to a file with a filename prefixed with a number from 1-20
        //the constant MAX_NUMBER_QUERY_TERMS is used to make sure the file will be found
        return Math.min(numQueryTerms, MAX_NUMBER_QUERY_TERMS);
    }

    private static String getKey(boolean symmetric, int numberQueryTerms) {
        return symmetric ? Integer.toString(numberQueryTerms) + "_symmetric" : Integer.toString(numberQueryTerms);
	}

	public ScoreDistribution getDistribution(String entrezGeneId) {
		while (true) {
			String key = getKey(symmetric, numQueryTerms);
			Map<String, ScoreDistribution> mim2scoredist = key2scoreDistribution.get(key);
			ScoreDistribution scoreDist = null;
			if (mim2scoredist != null) {
                scoreDist = mim2scoredist.get(entrezGeneId);
            }
			if (mim2scoredist == null || scoreDist == null) {
                logger.error("Could not find scoreDistribution for entrezid {} numQueryTerms: {} symmetric: {} using key: {}" , entrezGeneId, numQueryTerms, symmetric, key);
				if (numQueryTerms > 1) {
					int oneFewer = numQueryTerms - 1;
                    logger.error("Trying to find distribution for {} terms", oneFewer);
					if (!didParseDistributions(oneFewer)) {
                        logger.error("Trying to parse {} term distribution", oneFewer);
						parseDistributions(oneFewer);
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

	private boolean didParseDistributions(int numQueryTerms) {
		return key2scoreDistribution.containsKey(getKey(symmetric, numQueryTerms));
	}

	/**
	 * 
	 * 
	 * @param numQueryTerms
	 */
	private synchronized void parseDistributions(int numQueryTerms) {

		String key = getKey(symmetric, numQueryTerms);
		String file = distributionsFolder + key + ".out";

		if (verboseParsing)
			logger.info("Reading distributions from file: {}", file);
		try (BufferedReader in = new BufferedReader(new FileReader(file))){
            ScoreDistribution actualDistribution = null;
			String actualDiseaseId = null;
			double numberRandomizations = -1;
			List<Double> scores = new ArrayList<>();
			List<Double> pvalues = new ArrayList<>();
            String line;
			while ((line = in.readLine()) != null) {

				if (line.startsWith(">")) {

					line = line.replaceAll(">", "");

					if (actualDistribution != null) {
						actualDistribution.setDistribution(scores, pvalues, numberRandomizations);
						addDistribution(actualDiseaseId, actualDistribution);
						scores = new ArrayList<>();
						pvalues = new ArrayList<>();
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
			if (verboseParsing) {
                logger.info("done while loop.... add last");
            }
            if (actualDistribution != null) {
                actualDistribution.setDistribution(scores, pvalues, numberRandomizations);
                addDistribution(actualDiseaseId, actualDistribution);
            }
		} catch (IOException e) {
			logger.error("Unable access file {} to create PhenIX score distributions", file,  e);
		}
		if (verboseParsing)
			logger.info("done parsing");
	}

    private synchronized void addDistribution(String diseaseId, ScoreDistribution actualDistribution) {
        String key = getKey(symmetric, numQueryTerms);
        key2scoreDistribution.computeIfAbsent(key, mim2scoreDist -> new HashMap<>()).put(diseaseId, actualDistribution);
    }

}
