package net.sf.jclec.problem.classification.classic;

import net.sf.jclec.problem.classification.ClassificationMetadata;
import net.sf.jclec.problem.util.dataset.IDataset;
import net.sf.jclec.problem.util.dataset.IExample;
import net.sf.jclec.problem.util.dataset.attribute.CategoricalAttribute;
import net.sf.jclec.problem.util.dataset.attribute.IAttribute;

/**
 * Implementation of IMetadata interface for classic classification algorithms
 * 
 * @author Alberto Cano 
 * @author Amelia Zafra
 * @author Sebastian Ventura
 * @author Jose M. Luna 
 * @author Juan Luis Olmo
 */

public class ClassicClassificationMetadata extends ClassificationMetadata
{
	/////////////////////////////////////////////////////////////////
	// ------------------------------------------- Internal variables
	/////////////////////////////////////////////////////////////////
	
	private static final long serialVersionUID = 7370514914850394015L;

    /** Class attribute index */
    
    private int classIndex = -1;
    
	/////////////////////////////////////////////////////////////////
	// -------------------------------------------------- Constructor
	/////////////////////////////////////////////////////////////////

    /**
     * Empty constructor
     */
    
	public ClassicClassificationMetadata() 
	{
		super();
	}

	/////////////////////////////////////////////////////////////////
	// ----------------------------------------------- Public methods
	/////////////////////////////////////////////////////////////////
	
	/**
	 * Get the class attribute index
	 * 
	 * @return the classIndex
	 */
	public int getClassIndex() {
		return classIndex;
	}

	/**
	 * Set the class attribute index
	 * 
	 * @param classIndex the classIndex
	 */
	public void setClassIndex(int classIndex) {
		this.classIndex = classIndex;
	}
	
	/**
	 * {@inheritDoc}
	 */
	
	public int numberOfAttributes() 
	{
		return attributesList.size()-1;
	}
	
	/**
	 * {@inheritDoc}
	 */
	
	public int numberOfClasses() 
	{
		CategoricalAttribute catAttr = (CategoricalAttribute) getAttribute(classIndex);
		return catAttr.getCategories().size();
	}

    /**
     * Get the class attribute
     * 
     * @return the class attribute
     */
    public IAttribute getClassAttribute()
    {
    	return attributesList.get(classIndex);
    }
    
	/**
	 * {@inheritDoc}
	 */
    public boolean isClassAttribute( int attributeIndex )
    {
    	if (attributeIndex == classIndex)
    		return true;
    	else
    		return false;
    }
    
	/**
	 * Returns the number of instances of the different classes
	 * 
	 * @param dataset the dataset
	 * @return array of number of instances per class
	 */
	public int[] numberOfExamples(IDataset dataset)
	{
		int[] numInstances = new int[numberOfClasses()];
		
		for(IExample instance : dataset.getExamples())
		{
			numInstances[(int) ((ClassicInstance) instance).getClassValue()]++;
		}
		
		return numInstances;
	}
	
	/**
	 * Copy method
	 * 
	 * @return A copy of this metadata
	 */
	
	public ClassicClassificationMetadata copy()
	{
		ClassicClassificationMetadata metadata = new ClassicClassificationMetadata();
		
		metadata.attributesList = this.attributesList;
		metadata.attributesMap = this.attributesMap;
		metadata.setClassIndex(classIndex);
		
		return metadata;
	}
}