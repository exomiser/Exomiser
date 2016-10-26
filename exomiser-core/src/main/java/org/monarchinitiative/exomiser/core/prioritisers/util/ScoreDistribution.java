package org.monarchinitiative.exomiser.core.prioritisers.util;

import java.util.List;

public class ScoreDistribution {

	private double[] scores;
	private double[] pvalues;
	private double numberRandomizations;
	private int numberQueryTerms;
	private int mimNumber;
	
	public int getNumberQueryTerms() {
		return numberQueryTerms;
	}

	public int getMimNumber() {
		return mimNumber;
	}

	
	public void setDistribution(List<Double> scoresAL, List<Double> pvaluesAL, double numRandomizations){
		
		this.scores = new double[scoresAL.size()];
		this.pvalues = new double[pvaluesAL.size()];
		
		for (int i = 0 ; i < scoresAL.size(); i++){
			this.scores[i] 	= scoresAL.get(i);
			this.pvalues[i] = pvaluesAL.get(i);
		}
		
		this.numberRandomizations 	= numRandomizations;
		
	}

	public double getPvalue(double score, double roundFactor) {
		
		score 	= round(score, roundFactor);
		
		/* maybe this value is too high */
		if ( scores[scores.length-1] < score ) 
			return (double)1/numberRandomizations;
		
		/* init with 1 */
		double pvalue = 1;
		
		for (int i = 0; i < scores.length ; i++){
			
			double scoreCand = scores[i];
			if ( score <= scoreCand ){
				pvalue 	= pvalues[i];
				break;
			}
			
		}
		return pvalue;
	}

	public static double round( double d , double fact) { 
	    return Math.rint( d * fact ) / fact; 
	  } 

	@Override
	public String toString() {
		StringBuffer buff = new StringBuffer("score dist for: "+mimNumber+":\n");
		for (int i = 0 ; i< scores.length ; i++){
			buff.append(scores[i]+"-"+pvalues[i]+"\n");
		}
		return buff.toString();
	}
	
}



