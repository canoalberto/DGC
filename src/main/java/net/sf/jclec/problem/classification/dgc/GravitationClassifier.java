package net.sf.jclec.problem.classification.dgc;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import net.sf.jclec.problem.classification.classic.ClassicInstance;
import net.sf.jclec.problem.classification.classic.IClassicClassifier;
import net.sf.jclec.problem.util.dataset.IDataset;
import net.sf.jclec.problem.util.dataset.IExample;
import net.sf.jclec.problem.util.dataset.IMetadata;

/**
 * Gravitation classifier
 * 
 * @author Alberto Cano 
 * @author Amelia Zafra
 * @author Sebastian Ventura
 */

public class GravitationClassifier implements IClassicClassifier
{	
	/////////////////////////////////////////////////////////////////
	// --------------------------------------- Serialization constant
	/////////////////////////////////////////////////////////////////

	/** Generated by Eclipse */

	private static final long serialVersionUID = 1L;
	
	// Classifier private properties
	
	private double[] weights;
	
	private double[] predicted;

	private DGCSpecies species;

	/////////////////////////////////////////////////////////////////
	// ------------------------------------------------- Constructors
	/////////////////////////////////////////////////////////////////

	public GravitationClassifier(DGCSpecies species)
	{
		super();
		
		this.species = species;
	}
	
	/////////////////////////////////////////////////////////////////
	// ----------------------------------------------- Public methods
	/////////////////////////////////////////////////////////////////
	
	public double[] getWeights()
	{
		return weights;
	}
	
	public void setWeights(double[] weights)
	{
		this.weights = weights;
	}
	
	/**
	 * Obtains the confusion matrix for a dataset
	 * @param data the dataset
	 * @return the confusion matrix
	 */
	public int[][] getConfusionMatrix(IDataset dataset)
	{
		IMetadata metadata = dataset.getMetadata();
		int [][] confusionMatrix = new int[metadata.numberOfClasses()][metadata.numberOfClasses()];
		
		predicted = classify(dataset);
		
		for(int i = 0; i < dataset.getExamples().size(); i++) 
    	{
			confusionMatrix[(int) ((ClassicInstance) dataset.getExamples().get(i)).getClassValue()][(int) predicted[i]]++;
    	}
		
		return confusionMatrix;
	}

	/**
	 * Classifies an instance and predicts the class with the highest gravitation
	 * @param instance the instance
	 * @return predicted class
	 */
	@Override
	public double classify(IExample instance)
	{
		double maxGravity = -1;
		int maxGravityClass = 0;

		for(int i = 0; i < species.getDataset().getMetadata().numberOfClasses(); i++)
		{
			double gravity = gravity(instance, i);
			
			if(gravity > maxGravity)
			{
				maxGravity = gravity;
				maxGravityClass = i;
			}
		}
		
		return maxGravityClass;
	}
	
	/**
	 * Classifies all the instances of a dataset in parallel using as many threads as CPU cores
	 * @param dataset the dataset
	 * @return array of predicted classes
	 */
	@Override
	public double[] classify(IDataset dataset)
	{
		ArrayList<IExample> instances = dataset.getExamples();
		
		predicted = new double[instances.size()];
		
        ExecutorService threadExecutor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
		
		for(int i = 0; i < instances.size(); i++)
			threadExecutor.execute(new classificationThread(i, instances.get(i)));
		
		threadExecutor.shutdown();
		
		try
		{
			if (!threadExecutor.awaitTermination(30, TimeUnit.DAYS))
				System.out.println("Threadpool timeout occurred");
		}
		catch (InterruptedException ie)
		{
			System.out.println("Threadpool prematurely terminated due to interruption in thread that created pool");
		}
		
		return predicted;		
	}
	
	/**
	 * Computes the gravitation of an instance to a data class
	 * @param instance The instance
	 * @param Class data class
	 * @return the gravitation
	 */
	public double gravity(IExample instance, int Class)
	{
		ArrayList<IExample> instances = species.getDataset().getExamples();
		
		double gravity = 0.0;
		
		// Compute the gravitation using the distance to the other instances belonging to the data class
		for(int i = 0; i < instances.size(); i++)
			if(((ClassicInstance) instances.get(i)).getClassValue() == Class && instances.get(i) != instance)
			{
				double distance = distance(instance, instances.get(i));
				
				if(distance == 0)
				{
					gravity = Double.MAX_VALUE;
					i = instances.size();
				}
				else
					gravity += 1.0 / distance;
			}
		
		gravity *= 1.0 - ((species.getNumberInstances()[Class]-1) / (double) species.getDataset().getExamples().size());
		
		return gravity;
	}
	
	/**
	 * Computes the distance between to instances
	 * @param inst1 instance 1
	 * @param inst2 instance 2
	 * @return the distance
	 */
	private double distance(IExample inst1, IExample inst2)
	{
		int numAttributes = species.attributesNumber;
		
		double distance = 0.0;
		
		// If the classifier consider weights
		if(weights != null)
			for(int i = 0; i < numAttributes; i++)
				// If the attribute is numerical
				if(species.isNumerical(i))
					distance += weights[(int) ((ClassicInstance) inst2).getClassValue() * numAttributes + i] * Math.pow((inst2.getValue(i) - inst1.getValue(i)), 2);
				else
					distance += weights[(int) ((ClassicInstance) inst2).getClassValue() * numAttributes + i] * (inst2.getValue(i) == inst1.getValue(i) ? 0 : 1);
		else
			for(int i = 0; i < numAttributes; i++)
				// If the attribute is numerical
				if(species.isNumerical(i))
					distance += Math.pow((inst2.getValue(i) - inst1.getValue(i)), 2);
				else
					distance += inst2.getValue(i) == inst1.getValue(i) ? 0 : 1;
		
		return distance;
	}

	/**
	 * Returns a copy of the classifier
	 * @return classifier copy
	 */
	@Override
	public GravitationClassifier copy()
	{
		GravitationClassifier copy = new GravitationClassifier(species);
		copy.setWeights(weights.clone());
		return copy;
	}
	
	@Override
	public String toString(IMetadata metadata)
	{
		String classifier = new String();
		
		DecimalFormat df4 = new DecimalFormat("0.0000");
		double[] weights = this.weights.clone();
		
		// Normalize weight values
		
		double max = 0;
		
		for(int i = 0; i < weights.length; i++)
    		if(weights[i] > max)
    			max = weights[i];
		
		for(int i = 0; i < weights.length; i++)
			weights[i] /= max;
		
		// Print matrix of weights
		
		classifier = "Class \\ Attribute matrix of weights" + System.getProperty("line.separator");
		
		for(int j = 0; j < species.classesNumber; j++)
		{
			for(int i = 0; i < species.attributesNumber; i++)
			{
				classifier += df4.format(weights[j*species.attributesNumber + i]) + " ";
			}
			
			classifier += System.getProperty("line.separator");
		}
		
		return classifier;
	}
	
	private class classificationThread extends Thread
	{
		private int index;
		private IExample instance;
		
	    public classificationThread(int index, IExample instance)
	    {
	    	this.index = index;
	        this.instance = instance;
	    }
	    
	    public void run()
	    {
	    	predicted[index] = classify(instance);
	    }
    }
}