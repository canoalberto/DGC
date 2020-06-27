package net.sf.jclec.problem.util.dataset;

import java.util.ArrayList;

import org.apache.commons.configuration.Configuration;

/**
 * IDataset abstract implementation
 * 
 * @author Alberto Cano 
 * @author Amelia Zafra
 * @author Sebastian Ventura
 * @author Jose M. Luna 
 * @author Juan Luis Olmo
 */

public abstract class AbstractDataset implements IDataset
{
	/////////////////////////////////////////////////////////////////
	// --------------------------------------- Serialization constant
	/////////////////////////////////////////////////////////////////

	private static final long serialVersionUID = -5863981824188521799L;
	
	/////////////////////////////////////////////////////////////////
	// --------------------------------------------------- Properties
	/////////////////////////////////////////////////////////////////

	/** Dataset name */
	
	protected String datasetName;
	
	/** Data file name */
	
	protected String fileName;
	
	/** Dataset specification */
	
	protected IMetadata metadata;
	
	/** Dataset examples */
	
	protected ArrayList<IExample> examples;
	
	/////////////////////////////////////////////////////////////////
	// ------------------------------------------------- Constructors
	/////////////////////////////////////////////////////////////////

	/**
	 * Empty constructor
	 */
	
	public AbstractDataset() 
	{
		super();
	}

	/////////////////////////////////////////////////////////////////
	// ----------------------------------------------- Public methods
	/////////////////////////////////////////////////////////////////

	/**
	 * Sets the name of this dataset.
	 * 
	 * @param name the name of the dataset 
	 */
	
	public final void setName(String name)
	{
		this.datasetName = name;
	}
	
	/**
	 * {@inheritDoc}
	 */
	
	public String getName() 
	{
		return datasetName;
	}
	
	/**
	 * Access to current name
	 * 
	 * @return Current filename
	 */
	
	public String getFileName() 
	{
		return fileName;
	}

	/**
	 * Set filename
	 * 
	 * @param fileName New filename
	 */
	
	public void setFileName(String fileName) 
	{
		this.fileName = fileName;
	}
	
	/**
	 * {@inheritDoc}
	 */
	
	public IMetadata getMetadata() 
	{
		return metadata;
	}
	
	/**
	 * Set the metadata
	 * 
	 * @param metadata the metadata
	 */
	
	public void setMetadata(IMetadata metadata)
	{
		this.metadata = metadata;
	}

	/**
	 * Get the number of examples
	 * 
	 * @return the number of examples
	 */
		
	public int numberOfExamples()
	{
		return examples.size();
	}
	
	/**
	 * {@inheritDoc}
	 */
	
	public void configure(Configuration settings)
	{
		// Set file name
		setFileName(settings.getString(""));
	}
}