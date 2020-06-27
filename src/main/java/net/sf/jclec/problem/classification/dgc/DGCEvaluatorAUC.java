package net.sf.jclec.problem.classification.dgc;

/**
 * Gravitation evaluator (AUC)
 * 
 * @author Alberto Cano 
 * @author Amelia Zafra
 * @author Sebastian Ventura
 */

public class DGCEvaluatorAUC extends DGCEvaluator
{
	/////////////////////////////////////////////////////////////////
	// ------------------------------------------------- Constructors
	/////////////////////////////////////////////////////////////////

	private static final long serialVersionUID = 2424373531601868516L;

	/**
	 * Empty constructor.
	 */
	
	public DGCEvaluatorAUC()
	{
		super();
	}

	/////////////////////////////////////////////////////////////////
	// ------------------------ Overwriting AbstractEvaluator methods
	/////////////////////////////////////////////////////////////////
	
	/**
	 * Computes the fitness of an individual
	 * @param x weight values
	 * @return 1.0 - AUC
	 */
	public double valueOf(double[] x)
	{
		GravitationClassifier classifier = new GravitationClassifier(species);
		
		classifier.setWeights(x);
		
		int[][] confusionMatrix = classifier.getConfusionMatrix(species.getDataset());

		// Compute the Area Under the Curve (AUC)
        return 1.0 - (1.0 + (confusionMatrix[0][0] / (double)  (confusionMatrix[0][0]+confusionMatrix[0][1])) - (confusionMatrix[1][0] / (double) (confusionMatrix[1][1]+confusionMatrix[1][0])))/2.0;
	}
}