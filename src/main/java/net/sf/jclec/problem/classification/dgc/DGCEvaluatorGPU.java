package net.sf.jclec.problem.classification.dgc;

import java.util.ArrayList;

import net.sf.jclec.problem.classification.classic.ClassicClassificationMetadata;
import net.sf.jclec.problem.classification.classic.ClassicInstance;
import net.sf.jclec.problem.util.dataset.IExample;

public class DGCEvaluatorGPU extends DGCEvaluator
{
	/** Native functions */
	public native void nativeMalloc(int popSize, int numThreads, int numberAttributes, int numberInstances, int numberClasses, DGCEvaluatorGPU object);
	public native void nativeEvaluate(int popSize, DGCEvaluatorGPU object);
	public native void nativeFree();
	
	protected DGCAlgorithmGPU algorithm;
	
	/** Individuals list to evaluate */
	
	protected double[][] population;
	
	/** Instances */
	
	protected ArrayList<IExample> instances;
	
	/** Number of CPU threads or GPU devices */

	private int numberThreads;

	private int numberAttributes;
	
	/////////////////////////////////////////////////////////////////
	// --------------------------------------- Serialization constant
	/////////////////////////////////////////////////////////////////

	/** Generated by Eclipse */
	
	private static final long serialVersionUID = 3613350191235561000L;

	/////////////////////////////////////////////////////////////////
	// -------------------------------------------------- Constructor
	/////////////////////////////////////////////////////////////////

	/**
	 * Empty constructor.
	 */
	public DGCEvaluatorGPU() 
	{
		super();
	}
	
	/**
	 * Empty constructor.
	 */
	public DGCEvaluatorGPU(DGCSpecies species) 
	{
		super();
		setSpecies(species);
	}

	/////////////////////////////////////////////////////////////////
	// ------------------------ Overwriting FreitasEvaluator methods
	/////////////////////////////////////////////////////////////////
	
	/**
	 * Prepares the GPU or the CPU threads to perform evaluations
	 * 
	 * @param the algorithm
	 */
	
	public void prepare(DGCAlgorithmGPU algorithm)
	{
		this.algorithm = algorithm;
		instances = algorithm.getTrainSet().getExamples();
		
		numberThreads = 1;
		
		try {
			System.loadLibrary("jclec_gpu");
		} catch (Exception e) {
			System.out.println("Can't load jclec_gpu library. Please make sure to include gpu library path");
			System.exit(0);
		}
		
		int numberInstances = instances.size();
		int numberClasses = ((ClassicClassificationMetadata)algorithm.getTrainSet().getMetadata()).numberOfClasses();
		numberAttributes = ((ClassicClassificationMetadata)algorithm.getTrainSet().getMetadata()).numberOfAttributes();
		int popSize = algorithm.getPopulationSize();
		
		nativeMalloc(popSize, numberThreads, numberAttributes, numberInstances, numberClasses, this);
	}
	
	public float getWeight(int individual, int attribute, int Class)
	{
		return (float) population[individual][Class*numberAttributes + attribute]; 
	}
	
	public int getNumberInstances(int Class)
	{
		return ((DGCSpecies) algorithm.getSpecies()).getNumberInstances()[Class];
	}
	
	public boolean isNumerical(int att)
	{
		return ((DGCSpecies) algorithm.getSpecies()).isNumerical(att);
	}
	
	/**
	 * Gets the attribute value for a certain instance
	 * 
	 * @param the instance index and the attribute index
	 * @return the value
	 */
	
	public float getValue(int instance, int attribute)
	{
		return (float) ((ClassicInstance) instances.get(instance)).getValue(attribute);
	}
	
	public float getClassValue(int instance)
	{
		return (float) ((ClassicInstance) instances.get(instance)).getClassValue();
	}
	
	/**
	 * Set the fitness to index individual
	 * 
	 * @param the index from the individual at indsToEvaluate and its fitness
	 */	
	
	public void setFitness(int index, float fit)
	{
		fitness[index] = fit;
	}
	
	public double [] valuesOf(double[][] pop)
    {
		population = pop;
		
		fitness = new double[pop.length];
		
        nativeEvaluate(pop.length, this);
        
        return fitness;
    }
}