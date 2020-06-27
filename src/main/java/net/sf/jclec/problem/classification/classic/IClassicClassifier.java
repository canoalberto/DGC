package net.sf.jclec.problem.classification.classic;

import net.sf.jclec.problem.classification.IClassifier;
import net.sf.jclec.problem.util.dataset.IDataset;
import net.sf.jclec.problem.util.dataset.IExample;

/**
 * Interface for classic classifiers
 * 
 * @author Alberto Cano 
 * @author Amelia Zafra
 * @author Sebastian Ventura
 * @author Jose M. Luna 
 * @author Juan Luis Olmo
 */

public interface IClassicClassifier extends IClassifier
{
	/**
	 * Instance classification
	 * 
	 * @param instance Instance to classify
	 * 
	 * @return A double that represents the class label for this instance
	 */
	
	public double classify(IExample instance);
	
	/**
	 * Dataset classification
	 * 
	 * Classify all the instances contained in this dataset
	 * 
	 * @param dataset Dataset which instances will be classified
	 * 
	 * @return Array of class labels
	 */
	
	public double[] classify(IDataset dataset);
	
	/**
	 * Obtains the confusion matrix for a dataset
	 * 
	 * @param dataset the dataset to classify
	 * 
	 * @return the confusion matrix
	 */
	
	public int[][] getConfusionMatrix(IDataset dataset);

	/**
	 * Copy method
	 * 
	 * @return a copy of the classifier
	 */
	
	public IClassicClassifier copy();
}