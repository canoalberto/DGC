package net.sf.jclec.problem.classification;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import net.sf.jclec.AlgorithmEvent;
import net.sf.jclec.IAlgorithmListener;
import net.sf.jclec.IConfigure;
import net.sf.jclec.IIndividual;
import net.sf.jclec.fitness.IValueFitness;
import net.sf.jclec.problem.util.dataset.IDataset;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang.builder.EqualsBuilder;

/**
 * Abstract listener for classification algorithms
 * 
 * @author Alberto Cano 
 * @author Amelia Zafra
 * @author Sebastian Ventura
 * @author Jose M. Luna 
 * @author Juan Luis Olmo
 */

public abstract class ClassificationReporter implements IAlgorithmListener, IConfigure 
{
	/////////////////////////////////////////////////////////////////
	// --------------------------------------- Serialization constant
	/////////////////////////////////////////////////////////////////

	/** Generated by Eclipse */
	
	private static final long serialVersionUID = -8548482239030974796L;

	/////////////////////////////////////////////////////////////////
	// --------------------------------------------------- Properties
	/////////////////////////////////////////////////////////////////

	/** Report directory name */
	
	protected String reportDirName;
	
	/** Global report name */
	
	protected String globalReportName;
		
	/** Report frequency */
		
	protected  int reportFrequency;
	
	/** Init and end time */
	
	protected long initTime, endTime;

	/** Report directory */
	
	protected File reportDirectory;
	
	/////////////////////////////////////////////////////////////////
	// ------------------------------------------------- Constructors
	/////////////////////////////////////////////////////////////////
	
	/**
	 * Default (empty) constructor
	 */
	
	public ClassificationReporter() 
	{
		super();
	}

	/////////////////////////////////////////////////////////////////
	// ------------------------------- Setting and getting properties
	/////////////////////////////////////////////////////////////////

	/**
	 * Get the report directory name
	 * 
	 * @return report directory name
	 */
	
	public final String getReportDirName() 
	{
		return reportDirName;
	}
	
	/**
	 * Set the report directory name
	 * 
	 * @param reportDirName directory name
	 */

	public final void setReportDirName(String reportDirName) 
	{
		this.reportDirName = reportDirName;
	}
	
	/**
	 * Get the global report name
	 * 
	 * @return global report name
	 */
	
	public final String getGlobalReportName() 
	{
		return globalReportName;
	}
	
	/**
	 * Set the global report name
	 * 
	 * @param globalReportName report name
	 */

	public final void setGlobalReportName(String globalReportName) 
	{
		this.globalReportName = globalReportName;
	}

	/**
	 * Get the report frequency
	 * 
	 * @return report frequency
	 */
	
	public final int getReportFrequency() 
	{
		return reportFrequency;
	}

	/**
	 * Set the report frequency
	 * 
	 * @param reportFrequency frequency
	 */
	
	public final void setReportFrequency(int reportFrequency) 
	{
		this.reportFrequency = reportFrequency;
	}
	
	/////////////////////////////////////////////////////////////////
	// -------------------- Implementing IAlgorithmListener interface
	/////////////////////////////////////////////////////////////////
	
	/**
	 * {@inheritDoc} 
	 */

	public void algorithmStarted(AlgorithmEvent event) 
	{
		initTime = System.currentTimeMillis();
		
		Date now = new Date();
		String date = new SimpleDateFormat("yyyy.MM.dd'_'HH.mm.ss.SS").format(now);
		
		// Init report directory
		reportDirectory = new File(reportDirName + "_" + date);
		if (! reportDirectory.mkdir())
			throw new RuntimeException("Error creating report directory");
		
		// Do report
		doIterationReport((ClassificationAlgorithm) event.getAlgorithm());
	}

	/**
	 * {@inheritDoc} 
	 */
	public void algorithmFinished(AlgorithmEvent event) 
	{
		endTime = System.currentTimeMillis();
		doDataReport((ClassificationAlgorithm) event.getAlgorithm());
		doClassificationReport((ClassificationAlgorithm) event.getAlgorithm());
	}

	/**
	 * {@inheritDoc} 
	 */
	public void iterationCompleted(AlgorithmEvent event) 
	{
		doIterationReport((ClassificationAlgorithm) event.getAlgorithm());
	}
	
	/////////////////////////////////////////////////////////////////
	// ---------------------------- Implementing IConfigure interface
	/////////////////////////////////////////////////////////////////
	
	/**
	 * {@inheritDoc} 
	 */
	
	public void configure(Configuration settings) 
	{
		// Get report-dir-name
		String reportDirName = settings.getString("report-dir-name", "report");
		// Set reportDirName 
		setReportDirName(reportDirName);
		// Get global-report-name
		String globalReportName = settings.getString("global-report-name", "global-report");
		// Set globalReportName 
		setGlobalReportName(globalReportName);
		// Get report-frequency
		int reportFrequency = settings.getInt("report-frequency", 1);
		// Set reportFrequency
		setReportFrequency(reportFrequency);
	}

	/////////////////////////////////////////////////////////////////
	// ------------------------- Overwriting java.lang.Object methods
	/////////////////////////////////////////////////////////////////
	
	
	public boolean equals(Object other)
	{
		if (other instanceof ClassificationReporter) {
			ClassificationReporter cother = (ClassificationReporter) other;
			EqualsBuilder eb = new EqualsBuilder();
			eb.append(reportDirName, cother.reportDirName);
			eb.append(reportFrequency, cother.reportFrequency);
			return eb.isEquals();
		}
		else 
			return false;
	}

	/////////////////////////////////////////////////////////////////
	// -------------------------------------------- Protected methods
	/////////////////////////////////////////////////////////////////
	
	/**
	 * Make a report with individuals and their fitness
	 * 
	 * @param algorithm Algorithm
	 */
	protected void doIterationReport(ClassificationAlgorithm algorithm)
	{
		// Population individuals
		List<IIndividual> inds = algorithm.getInhabitants();
		// Actual generation
		int generation = algorithm.getGeneration();
		
		if (generation%reportFrequency == 0) 
		{		
			String reportFilename = String.format("Iteration_%d_%d.rep", algorithm.getExecution(), generation);
			
			try {
				// Report file
				File reportFile = new File(reportDirectory, reportFilename);
				// Report writer
				FileWriter reportWriter = null;
				
				try {
					reportFile.createNewFile();
					reportWriter = new FileWriter (reportFile);
				}
				catch(IOException e3){
					e3.printStackTrace();
				}
				
				StringBuffer buffer = new StringBuffer();
				
				//Prints individuals
				for(int i=0; i<inds.size(); i++)
				{
					IClassifier ind = ((IClassifierIndividual) inds.get(i)).getPhenotype();
					buffer.append(ind.toString(algorithm.getTrainSet().getMetadata()) + "; Fitness: " + ((IValueFitness) inds.get(i).getFitness()).getValue() + System.getProperty("line.separator")); 
				}
				
				reportWriter.append(buffer.toString());
				reportWriter.close();
			} 
			catch (IOException e) {
				throw new RuntimeException("Error writing report file");
			}
		}
	}
    
	/**
	 * Make the data report over the train and test datasets
	 * 
	 * @param algorithm Algorithm
	 */
    protected void doDataReport(ClassificationAlgorithm algorithm)
	{
    	// Test report name
		String testReportFilename = "TestDataReport.txt";
		// Train report name
		String trainReportFilename = "TrainDataReport.txt";
		// Test file writer
		FileWriter testFile = null;
		// Train file writer
		FileWriter trainFile = null;
		// Test Report file
		File testReportFile = new File(reportDirectory, testReportFilename);
		// Train Report file
		File trainReportFile = new File(reportDirectory, trainReportFilename);
		
		try {
			testReportFile.createNewFile();
			testFile = new FileWriter (testReportFile);
			trainReportFile.createNewFile();
			trainFile = new FileWriter (trainReportFile);

			classify(algorithm.getTrainSet(), algorithm.getClassifier(), trainFile);
			classify(algorithm.getTestSet(), algorithm.getClassifier(), testFile);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
    
    /**
  	 * This method prints the confusion matrix with the correct indentation
  	 * 
  	 * @param confusionMatrix the confusion matrix
  	 * @return the confusion matrix
  	 */
    protected String printConfusionMatrix(String[][] confusionMatrix)
    {
    	String s = "";
    	
    	// Find out what the maximum number of columns is in any row
    	int maxColumns = 0;
    	for (int i = 0; i < confusionMatrix.length; i++) {
    		maxColumns = Math.max(confusionMatrix[i].length, maxColumns);
    	}

    	// Find the maximum length of a string in each column
    	int[] lengths = new int[maxColumns];
    	for (int i = 0; i < confusionMatrix.length; i++) {
    		for (int j = 0; j < confusionMatrix[i].length; j++) {
    			lengths[j] = Math.max(confusionMatrix[i][j].length(), lengths[j]);
    		}
    	}

    	// Generate a format string for each column
    	String[] formats = new String[lengths.length];
    	for (int i = 0; i < lengths.length; i++) {
    		formats[i] = "%1$" + lengths[i] + "s" 
    				+ (i + 1 == lengths.length ? System.getProperty("line.separator") : " ");
    	}

    	// Print 'em out
    	for (int i = 0; i < confusionMatrix.length; i++) {
    		for (int j = 0; j < confusionMatrix[i].length; j++) {
    			s += String.format(formats[j], confusionMatrix[i][j]);
    		}
    	}
    	
    	return s;
    }
    
	/**
	 * This method classifies a dataset and write the results in the FileWriter
	 * 
	 * @param dataset The dataset
	 * @param classifier The classifier
	 * @param file The file to write
	 */
    protected abstract void classify(IDataset dataset, IClassifier classifier, FileWriter file);
    
	/**
	 * Make the classifier report over the train and test datasets
	 * 
	 * @param algorithm Algorithm
	 */
    protected abstract void doClassificationReport(ClassificationAlgorithm algorithm);
}