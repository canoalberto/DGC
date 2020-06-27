package net.sf.jclec.problem.classification;

import java.util.ArrayList;
import java.util.HashMap;

import net.sf.jclec.problem.util.dataset.IDataset;
import net.sf.jclec.problem.util.dataset.IMetadata;
import net.sf.jclec.problem.util.dataset.attribute.IAttribute;

/**
 * Implementation of IMetadata interface for classification algorithms
 * 
 * @author Alberto Cano 
 * @author Amelia Zafra
 * @author Sebastian Ventura
 * @author Jose M. Luna 
 * @author Juan Luis Olmo
 */

public abstract class ClassificationMetadata implements IMetadata 
{
	/////////////////////////////////////////////////////////////////
	// ------------------------------------------- Internal variables
	/////////////////////////////////////////////////////////////////
	
	private static final long serialVersionUID = 7370514914850394015L;

	/** Array list containing all attributes of this meta data */
	
    protected ArrayList<IAttribute> attributesList = new ArrayList<IAttribute>();

    /** Mapping of attribute names to attributes */
    
    protected HashMap<String, IAttribute> attributesMap = new HashMap<String, IAttribute>();
    
	/////////////////////////////////////////////////////////////////
	// -------------------------------------------------- Constructor
	/////////////////////////////////////////////////////////////////

    /**
     * Empty constructor
     */
    
	public ClassificationMetadata() 
	{
		super();
	}

	/////////////////////////////////////////////////////////////////
	// ----------------------------------------------- Public methods
	/////////////////////////////////////////////////////////////////
	
	/**
	 * {@inheritDoc}
	 */
	
	public IAttribute getAttribute(String attributeName) 
	{		
		return attributesMap.get(attributeName);
	}

	/**
	 * {@inheritDoc}
	 */
	
	public IAttribute getAttribute(int attributeIndex) 
	{
		return attributesList.get(attributeIndex);
	}

	/**
	 * {@inheritDoc}
	 */
	
	public int getIndex(IAttribute attribute) 
	{
		return attributesList.indexOf(attribute);
	}

	/**
	 * {@inheritDoc}
	 */
	
	public int getAttributeIndex(String attributeName) 
	{
		IAttribute attribute = attributesMap.get(attributeName);
		if (attribute == null) {
			return -1;
		}
		else {
			return attributesList.indexOf(attribute);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	
    public boolean addAttribute( IAttribute attribute )
    {
   		String attributeName = attribute.getName();
   		if ( attributeName == null || attributesMap.get(attributeName) != null ) {
   			return false;    			
   		}
   		else {
       		attributesList.add( attribute );
       		attributesMap.put(attributeName, attribute);
       		return true;
    	}
    }
    
	/**
	 * Returns the number of examples of the different classes
	 * 
	 * @param dataset the dataset
	 * @return array of number of examples per class
	 */
	public abstract int[] numberOfExamples(IDataset dataset);
    
	/**
	 * {@inheritDoc}
	 */
	
	public abstract int numberOfAttributes(); 
	
	/**
	 * {@inheritDoc}
	 */
	
	public abstract int numberOfClasses(); 
	
	/**
	 * Copy method
	 * 
	 * @return A copy of this metadata
	 */
	
	public abstract ClassificationMetadata copy();
}