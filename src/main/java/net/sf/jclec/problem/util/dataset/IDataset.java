package net.sf.jclec.problem.util.dataset;

import java.util.ArrayList;

import net.sf.jclec.IConfigure;

/**
 * Dataset Interface
 * 
 * @author Alberto Cano 
 * @author Amelia Zafra
 * @author Sebastian Ventura
 * @author Jose M. Luna 
 * @author Juan Luis Olmo
 */

public interface IDataset extends IConfigure
{
	/**
     * Get name of this dataset. 
     *
     * @return name of this dataset
     */
	
    public String getName();

    /**
     * Access to the dataset metadata specification.
     * 
     * @return Dataset specification
     */
    
    public IMetadata getMetadata();
    
    /**
     * Set the dataset metadata specification.
     * 
     * @param metadata The dataset specification
     */
    
    public void setMetadata(IMetadata metadata);
    
	/**
	 * Get the number of examples. 
	 *  
	 * @return number of examples
	 */
	
	public int numberOfExamples();
	
    /**
     * Load all datasets examples with this method.
     */
    
    public void loadExamples();
    
    /**
     * Access to all dataset examples
     * 
     * @return All the examples contained in this dataset
     */
    
    public ArrayList<IExample> getExamples();
    
    /**
     * Set the examples located in the ArrayList
     * 
     * @param examples The examples to set
     */
    
    public void setExamples(ArrayList<IExample> examples);
    
	/**
     * Add the examples located in the ArrayList
     * 
     * @param examples The examples to add
     */
    
    public void addExamples(ArrayList<IExample> examples);
    
   /**
    * Copy method
    * 
    * @return A copy of this dataset
    */
    
    public IDataset copy();	
}