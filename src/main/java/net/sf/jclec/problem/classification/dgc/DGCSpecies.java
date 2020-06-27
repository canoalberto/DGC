package net.sf.jclec.problem.classification.dgc;

import java.util.ArrayList;

import net.sf.jclec.problem.classification.classic.ClassicInstance;
import net.sf.jclec.problem.util.dataset.IDataset;
import net.sf.jclec.problem.util.dataset.IMetadata;
import net.sf.jclec.problem.util.dataset.IExample;
import net.sf.jclec.problem.util.dataset.attribute.AttributeType;
import net.sf.jclec.problem.util.dataset.attribute.CategoricalAttribute;
import net.sf.jclec.problem.util.dataset.attribute.IAttribute;
import net.sf.jclec.problem.util.dataset.attribute.IntegerAttribute;
import net.sf.jclec.problem.util.dataset.attribute.NumericalAttribute;
import net.sf.jclec.realarray.RealArrayIndividualSpecies;
import net.sf.jclec.util.range.Closure;
import net.sf.jclec.util.range.IRange;
import net.sf.jclec.util.range.Interval;

/**
 * Gravitation species
 * 
 * @author Alberto Cano 
 * @author Amelia Zafra
 * @author Sebastian Ventura
 */

public class DGCSpecies extends RealArrayIndividualSpecies
{
	/////////////////////////////////////////////////////////////////
	// ----------------------------------------------------Properties
	/////////////////////////////////////////////////////////////////
	
	private static final long serialVersionUID = 6146640813026259238L;
	
	// Species properties
	
	private IDataset trainSet;
	
	private int[] numberInstances;
	
	private boolean[] numericalAttribute;
	
	public double[] fmin, fmax;
	
	public double[][][] nominalLabels;
	
	public double[][][] nominalLabelsProb;

	public int classesNumber;

	public int attributesNumber;
	
	/////////////////////////////////////////////////////////////////
	// ------------------------------------------------- Constructors
	/////////////////////////////////////////////////////////////////
	
	/**
	 * Empty constructor
	 */
	
	public DGCSpecies() 
	{
		super();
	}
	
	/////////////////////////////////////////////////////////////////
	// ----------------------------------------------- Public methods
	/////////////////////////////////////////////////////////////////
	
	/**
	 * Prepares the datasets metadata information
	 * 
	 * @param trainSet train dataset
	 * @param testSet test dataset
	 */
	public void prepare(IDataset trainSet, IDataset testSet)
	{
		this.trainSet = trainSet;
		
		IMetadata metadata = trainSet.getMetadata();
		ArrayList<IExample> instances = trainSet.getExamples();
		
		classesNumber = metadata.numberOfClasses();
		attributesNumber = metadata.numberOfAttributes();
		
		// Allocate memory fot genotype Schema
		genotypeSchema = new IRange[attributesNumber * classesNumber];
		
		numericalAttribute = new boolean[attributesNumber];
		
		// Obtains the input attributes
		for(int j = 0; j < classesNumber; j++)
		for(int i = 0; i < attributesNumber; i++)
		{
			genotypeSchema[j*attributesNumber + i] = new Interval(0, 1, Closure.ClosedClosed);
		}
		
		setGenotypeSchema(genotypeSchema);
		
		numberInstances = new int[trainSet.getMetadata().numberOfClasses()];
		
		for(IExample inst : instances)
			numberInstances[(int) ((ClassicInstance) inst).getClassValue()]++;
		
		fmin = new double[attributesNumber];
		fmax = new double[attributesNumber];
		
		for(int i = 0; i < attributesNumber; i++)
		{
			IAttribute attribute = trainSet.getMetadata().getAttribute(i);
			
			//Checks the attribute type and assign the value type
			switch(attribute.getType())
			{
				case Numerical:
				{
					fmin[i] = ((NumericalAttribute) attribute).intervalValues().getLeft();
					fmax[i] = ((NumericalAttribute) attribute).intervalValues().getRight();
					numericalAttribute[i] = true;
					break;
				}
				case Integer:
				{
					fmin[i] = ((IntegerAttribute) attribute).intervalValues().getLeft();
					fmax[i] = ((IntegerAttribute) attribute).intervalValues().getRight();
					numericalAttribute[i] = true;
					break;
				}
				case Categorical:
				{
					fmin[i] = ((CategoricalAttribute) attribute).intervalValues().getLeft();
					fmax[i] = ((CategoricalAttribute) attribute).intervalValues().getRight();
					numericalAttribute[i] = false;
					break;
				}
				default:
					System.out.println("Type is not supported");
			}
		}
		
		// Normalize train data values
		
		for(IExample instance : trainSet.getExamples())
		{
			double[] values = ((ClassicInstance) instance).getValues();
			
			for(int i = 0; i < values.length; i++)
			{
				IAttribute attribute = trainSet.getMetadata().getAttribute(i);
				
				//Checks the attribute type and assign the value type
				if(attribute.getType() != AttributeType.Categorical)
					values[i] = (values[i] - fmin[i]) / (float) (fmax[i] - fmin[i]);
			}
			
			((ClassicInstance) instance).setValues(values);
		}
		
		// Normalize test data values
		
		if(testSet != null)
		for(IExample instance : testSet.getExamples())
		{
			double[] values = ((ClassicInstance) instance).getValues();
			
			for(int i = 0; i < values.length; i++)
			{
				IAttribute attribute = trainSet.getMetadata().getAttribute(i);
				
				//Checks the attribute type and assign the value type
				if(attribute.getType() != AttributeType.Categorical)
					values[i] = (values[i] - fmin[i]) / (float) (fmax[i] - fmin[i]);
			}
			
			((ClassicInstance) instance).setValues(values);
		}
	}
	
	/**
	 * @return the numberInstances
	 */
	public int[] getNumberInstances() {
		return numberInstances;
	}
	
	/**
	 * @return is an attribute numerical
	 */
	public boolean isNumerical(int attIndex)
	{
		return numericalAttribute[attIndex];
	}
	
	/**
	 * @return the dataset
	 */
	public IDataset getDataset() {
		return trainSet;
	}
	
	public void normalizeInstance(IExample instance)
	{
		double[] values = ((ClassicInstance) instance).getValues();
		
		for(int i = 0; i < values.length; i++)
		{
			IAttribute attribute = trainSet.getMetadata().getAttribute(i);
			
			//Checks the attribute type and assign the value type
			if(attribute.getType() != AttributeType.Categorical)
				values[i] = (values[i] - fmin[i]) / (float) (fmax[i] - fmin[i]);
		}
		
		((ClassicInstance) instance).setValues(values);
	}

	public double getRange(int attribute) {
		return fmax[attribute] - fmin[attribute];
	}
}