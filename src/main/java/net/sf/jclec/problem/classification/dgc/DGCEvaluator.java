package net.sf.jclec.problem.classification.dgc;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import net.sf.jclec.IEvaluator;
import net.sf.jclec.IFitness;
import net.sf.jclec.IIndividual;
import net.sf.jclec.base.AbstractEvaluator;
import net.sf.jclec.problem.classification.classic.ClassicInstance;
import net.sf.jclec.problem.util.dataset.IExample;

import cma.CMASolution;

/**
 * Gravitation evaluator (Accuracy)
 * 
 * @author Alberto Cano 
 * @author Amelia Zafra
 * @author Sebastian Ventura
 */

public class DGCEvaluator extends AbstractEvaluator implements IEvaluator
{
	/////////////////////////////////////////////////////////////////
	// --------------------------------------------------- Properties
	/////////////////////////////////////////////////////////////////
	
	private static final long serialVersionUID = 6980709786864047100L;

	/** Gravity species */
	
	protected DGCSpecies species;
	
	/** Array of fitness values **/
	
	protected double[] fitness;
	
	/////////////////////////////////////////////////////////////////
	// ------------------------------------------------- Constructors
	/////////////////////////////////////////////////////////////////

	/**
	 * Empty constructor.
	 */
	
	public DGCEvaluator()
	{
		super();
	}
	
	public void setSpecies(DGCSpecies species)
	{
		this.species = species;
	}

	/////////////////////////////////////////////////////////////////
	// ------------------------ Overwriting AbstractEvaluator methods
	/////////////////////////////////////////////////////////////////
	
	/**
	 * Computes the fitness values of the population in parallel using as many threads as CPU cores
	 * @param pop population of real array individuals
	 * @return real array with the fitness values
	 */
	public double [] valuesOf(double[][] pop)
    {
		long time = System.currentTimeMillis();
		
        fitness = new double[pop.length];
        
        ExecutorService threadExecutor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
		
		for(int i = 0; i < pop.length; i++)
			threadExecutor.execute(new evaluationThread(i, pop[i]));
		
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
        
		evaluationTime += System.currentTimeMillis() - time;
		
        return fitness;
    }
	
	/**
	 * Computes the fitness value of an individual
	 * @param x weight values
	 * @return fitness value
	 */
	public double valueOf(double[] x)
	{
		GravitationClassifier classifier = new GravitationClassifier(species);
		
		classifier.setWeights(x);
		
		double [] predicted = classifier.classify(species.getDataset());
		
		ArrayList<IExample> instances = species.getDataset().getExamples();
		
		int fails = 0;
		
		for(int i = 0; i < instances.size(); i++)
		{
			if(((ClassicInstance) instances.get(i)).getClassValue() != predicted[i])
				fails++;				
		}
		
		// Compute the error rate
		return fails / (double) instances.size();
	}
	
	/**
	 * Returns the best solution
	 * @param pop population
	 * @param fitness fitness values
	 * @return best solution
	 */
	public CMASolution bestSolution(double[][] pop, double[] fitness)
	{
		CMASolution bestSolution = null;
		double bestFitness = 1.0;
		double bestGeomean = 0.0;
		
		double[] geomean = new double[pop.length];
		
		for(int i = 0; i < pop.length; i++)
        {
        	double max = 0;
        	double sum = 1;
        	
        	for(int j = 0; j < pop[i].length; j++)
        		if(pop[i][j] > max)
        			max = pop[i][j];
        	
        	for(int j = 0; j < pop[i].length; j++)
        		sum *= pop[i][j] / max;
        	
        	geomean[i] = sum;
        }
		
		for(int i = 0; i < pop.length; i++)
        {
			if(fitness[i] < bestFitness || fitness[i] == bestFitness && geomean[i] > bestGeomean)
	    	{
	    		bestFitness = fitness[i];
	    		bestSolution = new CMASolution(pop[i], fitness[i], 0);
	    		bestGeomean = geomean[i];
	    	}
        }
		
		return bestSolution;
	}
	
	/**
	 * Selects the best solution
	 * @param bestSolutions set of solutiones
	 * @return the best solution
	 */
	public CMASolution bestSolution(List<CMASolution> bestSolutions)
	{
		CMASolution bestSolution = null;
		double bestFitness = 1.0;
		double bestGeomean = 0.0;
		
		double[] geomean = new double[bestSolutions.size()];
		
		for(int i = 0; i < bestSolutions.size(); i++)
        {
        	double max = 0;
        	double sum = 1;
        	
        	for(int j = 0; j < bestSolutions.get(i).getX().length; j++)
        		if(bestSolutions.get(i).getX()[j] > max)
        			max = bestSolutions.get(i).getX()[j];
        	
        	for(int j = 0; j < bestSolutions.get(i).getX().length; j++)
        		sum *= bestSolutions.get(i).getX()[j] / max;
        	
        	geomean[i] = sum;
        }
		
		for(int i = 0; i < bestSolutions.size(); i++)
        {
			if(bestSolutions.get(i).getFitness() < bestFitness || bestSolutions.get(i).getFitness() == bestFitness && geomean[i] > bestGeomean)
	    	{
	    		bestFitness = bestSolutions.get(i).getFitness();
	    		bestSolution = bestSolutions.get(i);
	    		bestGeomean = geomean[i];
	    	}
        }
		
		return bestSolution;
	}
	
	private class evaluationThread extends Thread
	{
		private int index;
		private double[] ind;
		
	    public evaluationThread(int index, double[] ind)
	    {
	    	this.index = index;
	        this.ind = ind;
	    }
	    
	    public void run()
	    {
	    	fitness[index] = valueOf(ind);
	    }
    }

	@Override
	public Comparator<IFitness> getComparator() {
		return null;
	}

	@Override
	public void evaluate(IIndividual ind) {
		// GPU implementation
	}
}