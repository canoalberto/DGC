package net.sf.jclec.problem.util.dataset.attribute;

import net.sf.jclec.JCLEC;

/**
 * Interface for dataset attributes
 * 
 * @author Alberto Cano 
 * @author Amelia Zafra
 * @author Sebastian Ventura
 * @author Jose M. Luna 
 * @author Juan Luis Olmo
 */

public interface IAttribute extends JCLEC
{
	/**
	 * Access to the attribute name.
	 * 
	 * @return attribute name
	 */
	
	public String getName();
	
	/**
	 * Access to the attribute type.
	 * 
	 * @return attribute name
	 */
	
	public AttributeType getType();
	
	/**
	 * Show an String which represents a given real value.
	 * 
	 * @param ivalue Internal value to show
	 * 
	 * @return The real value of the attribute
	 */
	
	public String show(double ivalue);
	
	/**
	 * Parse an string to obtain  the internal value of this attribute.
	 * 
	 * @param string String to parse
	 * 
	 * @return The external value of the attribute
	 */
	
	public double parse(String string);
}