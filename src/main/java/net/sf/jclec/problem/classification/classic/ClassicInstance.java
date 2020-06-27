package net.sf.jclec.problem.classification.classic;

import net.sf.jclec.problem.util.dataset.Instance;

/**
 * Dataset instance for classic classification
 * 
 * @author Alberto Cano 
 * @author Amelia Zafra
 * @author Sebastian Ventura
 * @author Jose M. Luna 
 * @author Juan Luis Olmo
 */

public class ClassicInstance extends Instance
{
	/////////////////////////////////////////////////////////////
	// ----------------------------------------------- Properties
	/////////////////////////////////////////////////////////////

	private static final long serialVersionUID = 2141837612603678135L;

	/** Class value */
	
	private double classValue = -1;

	/////////////////////////////////////////////////////////////
	// --------------------------------------------- Constructors
	/////////////////////////////////////////////////////////////

	/**
	 * Default constructor
	 * 
	 * @param numberAttributes Number of attributes
	 */

	public ClassicInstance(int numberAttributes)
	{
		super(numberAttributes);
	}

	/////////////////////////////////////////////////////////////
	// ------------------------------------------- Public methods
	/////////////////////////////////////////////////////////////
	
	/**
     * Get the class of the instance
     *
     * @return the class value
     */

	public double getClassValue() 
	{
		return classValue;
	}

	/**
     * Set the class of the instance
     *
     * @param classValue the class
     */

	public void setClassValue(double classValue) {
		this.classValue = classValue;
	}
	
	/**
    * Checks if the example is equal to another
    * 
    * @return true or false
    */
    @Override
    public boolean equals(Object other)
    {
    	if (other instanceof ClassicInstance) {
    		ClassicInstance cother = (ClassicInstance) other;
			
    		if(super.equals(other) == false) return false;
			
			if(classValue != cother.getClassValue()) return false;
			else return true;
			
		} else {
			return false;
		}
    }

	/**
	 * Copy method
	 * 
	 * @return A copy of this instance
	 */

	public ClassicInstance copy() 
	{
		ClassicInstance instance = new ClassicInstance(values.length);

		for(int i=0; i<values.length; i++)
			instance.setValue(i,values[i]);
		
		instance.setClassValue(classValue);
		
		return instance;
	}
}