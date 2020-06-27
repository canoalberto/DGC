package net.sf.jclec.problem.util.dataset;

/**
 * Dataset Instance
 * 
 * @author Alberto Cano 
 * @author Amelia Zafra
 * @author Sebastian Ventura
 * @author Jose M. Luna 
 * @author Juan Luis Olmo
 */

public class Instance implements IExample
{
	/////////////////////////////////////////////////////////////
	// ----------------------------------------------- Properties
	/////////////////////////////////////////////////////////////

	private static final long serialVersionUID = 2141837612603678135L;

	/** Attribute values */

	protected double [] values;
	
	/** Weight of this instance */

	protected double weight = 1;

	/////////////////////////////////////////////////////////////
	// --------------------------------------------- Constructors
	/////////////////////////////////////////////////////////////

	/**
	 * Default constructor
	 * 
	 * @param numberAttributes Number of attributes
	 */

	public Instance(int numberAttributes)
	{
		super();

		this.values = new double[numberAttributes];
	}

	/////////////////////////////////////////////////////////////
	// ------------------------------------------- Public methods
	/////////////////////////////////////////////////////////////
	
	/**
     * Returns the weight of this instance.
     *
     * @return instance weight
     */

	public double getWeight() 
	{
		return weight;
	}

	/**
	 * Sets the weigth for this instance
	 * 
	 * @param weight New weigth value
	 */

	public final void setWeight(double weight)
	{
		this.weight = weight;
	}

	/**
     * Returns vector's attribute value in internal format.
     *
     * @param attributeIndex attribute index for value to read.
     * 
     * @return the specified value as a double (If the corresponding
     * 		   attribute is categorical then it returns the value's 
     * 		   index as a double).
     */

	public double getValue(int attributeIndex) 
	{
		return values[attributeIndex];
	}
	
	/**
	 * Set the value of the index-th element of this instance.
	 * 
	 * @param index Index of the element to change
	 * @param value Internal value for this element
	 */

	public final void setValue(int index, double value)
	{
		this.values[index] = value;
	}

	/**
     * Get value array of the this instance.
     *
     * @return value array of the vector
     */

	public double[] getValues() 
	{
		return values;
	}

	/**
     * Set value array of the this instance.
     *
     * @param values array of the vector
     */

	public void setValues(double[] values) {
		this.values = values;
	}
	
	/**
    * Checks if the example is equal to another
    * 
    * @return true or false
    */
    @Override
    public boolean equals(Object other)
    {
    	if (other instanceof Instance) {
    		Instance cother = (Instance) other;
    		
    		if(weight != cother.getWeight()) return false;
    		if(values.length != cother.getValues().length) return false;
			
			for(int i = 0; i < values.length; i++)
				if(values[i] != cother.getValues()[i])
					return false;
			
			return true;
			
		} else {
			return false;
		}
    }
	
	/**
	 * Copy method
	 * 
	 * @return A copy of this instance
	 */

	public Instance copy() 
	{
		Instance instance = new Instance(values.length);

		for(int i=0; i<values.length; i++)
			instance.setValue(i,values[i]);
		
		return instance;
	}
}