package net.sf.jclec.problem.util.dataset.attribute;

/**
 * IAttribute abstract implementation
 * 
 * @author Alberto Cano 
 * @author Amelia Zafra
 * @author Sebastian Ventura
 * @author Jose M. Luna 
 * @author Juan Luis Olmo
 */

public abstract class AbstractAttribute implements IAttribute 
{
	/////////////////////////////////////////////////////////////////
	// --------------------------------------------------- Properties
	/////////////////////////////////////////////////////////////////

	private static final long serialVersionUID = -2103423458042526762L;
	
	/** Attribute name */
	
	protected String name;
	
	/////////////////////////////////////////////////////////////////
	// ------------------------------------------------- Constructors
	/////////////////////////////////////////////////////////////////

	/**
	 * Empty (default) constructor
	 */
	
	public AbstractAttribute() 
	{
		super();
	}

	/**
	 * Constructor that sets attribute name
	 * 
	 * @param name Attribute name
	 */
	
	public AbstractAttribute(String name) 
	{
		super();
		setName(name);
	}
	
	/////////////////////////////////////////////////////////////////
	// ----------------------------------------------- Public methods
	/////////////////////////////////////////////////////////////////

	/**
	 * Set attribute name.
	 * 
	 * @param name New attribute name
	 */
	
	public final void setName(String name) 
	{
		this.name = name;
	}

	/**
	 * {@inheritDoc}
	 */
	
	public String getName() 
	{
		return name;
	}
}
