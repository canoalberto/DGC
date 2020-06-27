package net.sf.jclec.problem.classification.dgc;

import java.util.ArrayList;
import java.util.List;

import net.sf.jclec.IAlgorithmListener;
import net.sf.jclec.IConfigure;
import net.sf.jclec.problem.classification.classic.ClassicClassificationAlgorithm;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationRuntimeException;

import cma.CMAEvolutionStrategy;
import cma.CMASolution;

/**
 * Gravitation algorithm using CMA-ES optimizer
 * 
 * @author Alberto Cano 
 * @author Amelia Zafra
 * @author Sebastian Ventura
 */

public class DGCAlgorithm extends ClassicClassificationAlgorithm
{
	// ///////////////////////////////////////////////////////////////
	// --------------------------------------- Serialization constant
	// ///////////////////////////////////////////////////////////////

	/** Generated by Eclipse */

	private static final long serialVersionUID = -8711970425735016406L;
	
	// Algorithm properties
	
	private List<CMASolution> bestSolutions;
	
	private int iteration;
	
	private int maxIterations;
	
	private long counteval;
	
	private long seed;

	private CMAEvolutionStrategy cma;

	private CMASolution bestSolution;

	/////////////////////////////////////////////////////////////////
	// ------------------------------------------------- Constructors
	/////////////////////////////////////////////////////////////////

	/**
	 * Empty (default) constructor
	 */
	
	public DGCAlgorithm() 
	{
		super();
	}

	/////////////////////////////////////////////////////////////////
	// ----------------------------------------------- Public methods
	/////////////////////////////////////////////////////////////////
	
	public void prepareWeka(long seed)
	{
		this.seed = seed;
		
		// Number of CMA-ES generations
		setMaxOfEvaluations(Integer.MAX_VALUE);
		
		// Number of CMA-ES iterations
		maxIterations = 3;
		
		bestSolutions = new ArrayList<CMASolution>();
	}

	// IConfigure interface
	
	public void configure(Configuration configuration)
	{
		seed = configuration.getLong("seed");
		
		setListenerSettings(configuration);
		setEvaluatorSettings(configuration);
		setDatasetSettings(configuration);
		
		species = new DGCSpecies();
		
		((DGCSpecies) species).prepare(getTrainSet(), getTestSet());
		
		// Maximum of generations
		setMaxOfGenerations(configuration.getInt("max-of-generations", Integer.MAX_VALUE));
		
		// Maximum of evaluations
		setMaxOfEvaluations(configuration.getInt("max-of-evaluations", Integer.MAX_VALUE));
		
		// Maximum of iterations
		maxIterations = configuration.getInt("max-of-iterations", 3);
		
		// Population size
		setPopulationSize(configuration.getInt("population-size"));
		
		if(populationSize == 0)
			populationSize = (int) (4 * Math.log(Math.pow((getTrainSet().getMetadata().numberOfAttributes()) * getTrainSet().getMetadata().numberOfClasses(), 2)));
		
		((DGCEvaluator) evaluator).setSpecies((DGCSpecies) species);
		
		bestSolutions = new ArrayList<CMASolution>();
	}
	
	/////////////////////////////////////////////////////////////////
	// -------------------------------------------- Protected methods
	/////////////////////////////////////////////////////////////////
	
	@Override
	protected void doInit() 
	{
		// Nothing to do here
	}
	
	@Override
	protected void doSelection() 
	{
		// Nothing to do here
	}

	@Override
	protected void doGeneration() 
	{
		double[] fitness; 
		double[][] pop = null;
		
		cma = new CMAEvolutionStrategy();

		// Initialize population array values
		cma.setInitialX(0.5);
		cma.setInitialStandardDeviation(0.3);
		
		cma.options.maxTimeFractionForEigendecomposition = 1;
		cma.options.stopTolFun = 1e-12;     //function value range within iteration and of past values
		cma.options.stopTolFunHist = 1e-13; // function value range of 10+30*N/lambda past values
		cma.options.writeDisplayToFile = 0;
		
		cma.setSeed(seed + iteration);
		// Dimension of the array, (number of weights), is the product of the number of attributes and the number of classes
		cma.setDimension((getTrainSet().getMetadata().numberOfAttributes()) * getTrainSet().getMetadata().numberOfClasses());

		cma.parameters.setPopulationSize(populationSize);
		
		double[] LBound = new double[cma.getDimension()];
		double[] UBound = new double[cma.getDimension()];
		
		for(int i = 0; i < cma.getDimension(); i++)
		{
			LBound[i] = 0;
			UBound[i] = 1.0;
		}
		
		cma.LBound = LBound;
		cma.UBound = UBound;
		
		// initialize 
		if (iteration == 0)
			fitness = cma.init(); // finalize setting of population size lambda, get fitness array
		else
		{
			cma.setCountEval(counteval); // somehow a hack 
			fitness = cma.init(); // provides array to assign fitness values
		}
		
		// set additional termination criterion
		if(maxOfGenerations != 0)
			cma.options.stopMaxIter = maxOfGenerations;
		else
			cma.options.stopMaxIter = (long) (100 + 200*Math.pow(cma.getDimension(),2)*Math.sqrt(cma.parameters.getLambda()));
		
		// iteration loop
		while(cma.stopConditions.isFalse())
		{
			pop = cma.samplePopulation(); // get a new population of solutions
			
			fitness = ((DGCEvaluator) evaluator).valuesOf(pop);
			
			cma.updateDistribution(fitness);  // pass fitness array to update search distribution
		}
		
		bestSolutions.add(((DGCEvaluator) evaluator).bestSolution(pop, fitness));

		// evaluate mean value as it is the best estimator for the optimum
		cma.setFitnessOfMeanX(((DGCEvaluator) evaluator).valueOf(cma.getMeanX())); // updates the best ever solution 

		counteval = cma.getCountEval();
	}

	@Override
	protected void doReplacement() 
	{
		// Nothing to do here
	}

	@Override
	protected void doUpdate() 
	{
		// final output for the run
		cma.println("Terminated (run " + (iteration+1) + ") due to");
		for (String s : cma.stopConditions.getMessages()) 
			cma.println("      " + s);
		

		// quit restart loop if MaxFunEvals or target Fitness are reached
		for (String s : cma.stopConditions.getMessages())
			if (s.startsWith("MaxFunEvals") || s.startsWith("Fitness"))
			{
				iteration = maxIterations;
				return;
			}
	}
	
	@Override
	protected void doControl() 
	{
		iteration++;
		
		// Stop if the number of iterations is reached
		if(iteration >= maxIterations)
		{
			// Selects the best solution from the different iterations
			bestSolution = ((DGCEvaluator) evaluator).bestSolution(bestSolutions);
			
			classifier = new GravitationClassifier((DGCSpecies) species);
			
			((GravitationClassifier) classifier).setWeights(bestSolution.getX());
			
			state = FINISHED;
		}
	}
	
	@SuppressWarnings("unchecked")
	private void setListenerSettings(Configuration configuration)
	{
		// Number of defined listeners
		int numberOfListeners = configuration.getList("listener[@type]").size();
		// For each listener in list
		for (int i=0; i<numberOfListeners; i++) {
			String header = "listener("+i+")";
			try {
				// Listener classname
				String listenerClassname = 
					configuration.getString(header+"[@type]");
				// Listener class
				Class<? extends IAlgorithmListener> listenerClass =
					(Class<? extends IAlgorithmListener>) Class.forName(listenerClassname);
				// Listener instance
				IAlgorithmListener listener = listenerClass.newInstance();
				// Configure listener (if necessary)
				if (listener instanceof IConfigure) {
					((IConfigure) listener).configure(configuration.subset(header));
				}
				// Add this listener to the algorithm
				addListener(listener);
			}
			catch (ClassNotFoundException e) {
				throw new ConfigurationRuntimeException("Illegal listener classname", e);
			}
			catch (InstantiationException e) {
				throw new ConfigurationRuntimeException("Illegal listener classname", e);
			}
			catch (IllegalAccessException e) {
				throw new ConfigurationRuntimeException("Illegal listener classname", e);
			}
		}
	}
	
	public CMASolution getBestSolution() {
		return bestSolution;
	}
}