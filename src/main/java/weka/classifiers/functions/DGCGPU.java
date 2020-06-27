package weka.classifiers.functions;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Vector;

import net.sf.jclec.problem.classification.classic.ClassicClassificationMetadata;
import net.sf.jclec.problem.classification.classic.ClassicInstance;
import net.sf.jclec.problem.classification.dgc.DGCAlgorithmGPU;
import net.sf.jclec.problem.classification.dgc.DGCEvaluatorGPU;
import net.sf.jclec.problem.classification.dgc.DGCSpecies;
import net.sf.jclec.problem.classification.dgc.GravitationClassifier;
import net.sf.jclec.problem.util.dataset.ArffDataSet;
import net.sf.jclec.problem.util.dataset.IExample;
import net.sf.jclec.problem.util.dataset.attribute.CategoricalAttribute;
import net.sf.jclec.problem.util.dataset.attribute.NumericalAttribute;
import net.sf.jclec.util.range.Closure;
import net.sf.jclec.util.range.Interval;
import weka.classifiers.AbstractClassifier;
import weka.core.Attribute;
import weka.core.Capabilities;
import weka.core.Capabilities.Capability;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Option;
import weka.core.OptionHandler;
import weka.core.SelectedTag;
import weka.core.Tag;
import weka.core.TechnicalInformation;
import weka.core.TechnicalInformation.Field;
import weka.core.TechnicalInformation.Type;
import weka.core.TechnicalInformationHandler;
import weka.core.Utils;
import weka.core.WeightedInstancesHandler;

/**
 * DGC: Data Gravitation Classification Algorithm
 * 
 * A. Cano, A. Zafra, and S. Ventura. Weighted Data Gravitation Classification for Standard and Imbalanced Data.
 * IEEE Transactions on Cybernetics, 43 (6) pages 1672-1687, 2013.
 * 
 * @author Alberto Cano 
 */

public class DGCGPU extends AbstractClassifier implements OptionHandler, WeightedInstancesHandler, TechnicalInformationHandler
{
	private static final long serialVersionUID = 6639194007580838497L;

	protected DGCAlgorithmGPU algorithm;

	protected int populationSize = 0;
	
	protected int generations = 500;

	protected int seed = 123456789;
	
	protected ArffDataSet dataset;

	protected ClassicClassificationMetadata metadata;

	protected ArrayList<String> classNames;
	
	protected String classAttName;
	
	private int problemType = 1;

	private static final int STANDARD = 1;
	private static final int IMBALANCED = 2;
	
	protected static final Tag[] dataTags = {
			new Tag(STANDARD, "Standard data sets"),
			new Tag(IMBALANCED, "Imbalanced data sets"),
	};

	/**
	 * @return the populationSize
	 */
	public int getPopulationSize() {
		return populationSize;
	}

	/**
	 * @param populationSize the populationSize to set
	 */
	public void setPopulationSize(int populationSize) {
		this.populationSize = populationSize;
	}

	/**
	 * @return the generations
	 */
	public int getGenerations() {
		return generations;
	}

	/**
	 * @param generations the generations to set
	 */
	public void setGenerations(int generations) {
		this.generations = generations;
	}

	/**
	 * @return the seed
	 */
	public int getSeed() {
		return seed;
	}

	/**
	 * @param seed the seed to set
	 */
	public void setSeed(int seed) {
		this.seed = seed;
	}
	
	/**
	 * @return the problem selector tag
	 */
	public SelectedTag getProblemType() {
		return new SelectedTag(problemType, dataTags);
	}

	/**
	 * @param selectorTag the selectorTag to set
	 */
	public void setProblemType(SelectedTag tag) {
		if(tag.getTags() == dataTags)
			problemType = tag.getSelectedTag().getID();
	}

	/**
	 * Returns an enumeration describing the available options
	 * 
	 * @return an enumeration of all the available options
	 */
	public Vector<Option> listCommonOptions()
	{
		Vector<Option> newVector = new Vector<Option>();

		newVector.addElement(new Option("\tSet the population size " + "\n\t(default automatically calculated)", "P",1,"-P <population size>"));
		newVector.addElement(new Option("\tSet the number of generations " +"\n\t(default 500)", "G",1,"-G <number of generations>"));
		newVector.addElement(new Option("\tSet seed" +"\n\t(default 123456789)", "D",1,"-D <seed>"));
		newVector.addElement(new Option("\tSet data type" +"\n", "S",1,"-S <data type>"));

		return newVector;
	}

	/**
	 * Parses a given list of options. <p/>
	 *
	<!-- options-start -->
	 * Valid options are: <p/>
	 * 
	 * <pre> -P &lt;population size&gt;
	 *  The population size (default: 20).</pre>
	 * 
	 * <pre> -G &lt;number of generations&gt;
	 *  The number of generations (default: 10).</pre>
	 *  
	 * <pre> -S &lt;seed&gt;
	 *  The seed for random values (default: 111111111).</pre>
	 *  
	 * <pre> -C &lt;crossover probability&gt;
	 *  The crossover probability (default: 0.5).</pre>
	 *  
	 * <pre> -M &lt;mutation probability&gt;
	 *  The mutation probability (default: 0.1).</pre>
	 *  
	<!-- options-end -->
	 *
	 * @param options the list of options as an array of strings
	 * @throws Exception if an option is not supported
	 */
	public void setOptions(String[] options) throws Exception
	{
		// Other options
		String optionString = Utils.getOption('P', options);
		if (optionString.length() != 0)
			populationSize = (new Integer(optionString)).intValue();

		optionString = Utils.getOption('G', options);
		if (optionString.length() != 0)
			generations = (new Integer(optionString)).intValue();

		optionString = Utils.getOption('D', options);
		if (optionString.length() != 0)
			seed = (new Integer(optionString)).intValue();
		
		if(Utils.getFlag('S', options))
			setProblemType(new SelectedTag(STANDARD, dataTags));

		Utils.checkForRemainingOptions(options);
	} 	

	/**
	 * Gets the current settings of the Classifier.
	 *
	 * @return an array of strings suitable for passing to setOptions
	 */
	public String [] getOptions()
	{
		String [] options = new String [30];
		int current = 0;

		options[current++] = "-P"; options[current++] = "" + populationSize;
		options[current++] = "-G"; options[current++] = "" + generations;
		options[current++] = "-D"; options[current++] = "" + seed;
		options[current++] = "-S"; options[current++] = "" + problemType;

		while (current < options.length) {
			options[current++] = "";
		}
		
		return options;
	}

	/**
	 * Returns a string describing classifier
	 * @return a description suitable for
	 * displaying in the explorer/experimenter gui
	 */
	public String globalInfo()
	{
		return  "Classifier algorithm. For more information, see\n\n" + getTechnicalInformation().toString();
	}

	public void configureMetadata(Instances instances) throws Exception
	{
		// can classifier handle the data?
		getCapabilities().testWithFail(instances);

		// remove instances with missing class
		instances.deleteWithMissingClass();

		// Set population size
		if(populationSize == 0)
			populationSize = (int) (4 * Math.log(Math.pow((instances.numAttributes()) * instances.numClasses(), 2)));
		
		algorithm.setPopulationSize(populationSize);

		// Set maximum of generations
		algorithm.setMaxOfGenerations(generations);
		
		algorithm.prepareWeka(seed);

		// Configure Datasets
		dataset = new ArffDataSet();
		
		metadata = new ClassicClassificationMetadata();

		ArrayList<IExample> trainInstances = new ArrayList<IExample>();
		
		// Get Metadata
		Enumeration enumeration = instances.enumerateAttributes();
		
		int attindex = 0;
		
		while(enumeration.hasMoreElements())
		{
			Attribute att =  (Attribute) enumeration.nextElement();
			
			switch(att.type())
			{
				case Attribute.NUMERIC:
				{
					NumericalAttribute attribute = new NumericalAttribute();
					attribute.setName(att.name());

					Interval intervals = new Interval();
					intervals.setClosure(Closure.ClosedClosed);
					intervals.setLeft(instances.attributeStats(attindex).numericStats.min);
					intervals.setRight(instances.attributeStats(attindex).numericStats.max);
					attribute.setInterval(intervals);

					metadata.addAttribute(attribute);
					break;
				}
				case Attribute.NOMINAL:
				{
					CategoricalAttribute attribute = new CategoricalAttribute();
					attribute.setName(att.name());
					
					List<String> categoriesList = new ArrayList<String>();
					
					Enumeration<Object> categories = att.enumerateValues();
					
					while(categories.hasMoreElements())
					{
						categoriesList.add((String) categories.nextElement());
					}

					attribute.setCategories(categoriesList);

					metadata.addAttribute(attribute);
					break;
				}
			}

			attindex++;
		}

		metadata.setClassIndex(instances.classIndex());

		classNames = new ArrayList<String>();
		
		enumeration = instances.classAttribute().enumerateValues();
		
		while(enumeration.hasMoreElements())
		{
			classNames.add((String) enumeration.nextElement());
		}
		
		classAttName = instances.classAttribute().name();
		
		CategoricalAttribute attributeClass = new CategoricalAttribute();
		attributeClass.setName(classAttName);
		attributeClass.setCategories(classNames);
		metadata.addAttribute(attributeClass);
		
		dataset.setMetadata(metadata);

		// Get dataset instances
		enumeration = instances.enumerateInstances();
		
		while(enumeration.hasMoreElements())
		{
			Instance inst =  (Instance) enumeration.nextElement();
			ClassicInstance instance = new ClassicInstance(metadata.numberOfAttributes());
			
			for(int i = 0; i < metadata.numberOfAttributes(); i++)
				instance.setValue(i, inst.value(i));
			
			instance.setClassValue(inst.classValue());
			trainInstances.add(instance);
		}

		dataset.setExamples(trainInstances);
		
		algorithm.setTrainSet(dataset);
		
		algorithm.setSpecies(new DGCSpecies());
		
		((DGCSpecies) algorithm.getSpecies()).prepare(algorithm.getTrainSet(), algorithm.getTestSet());
		
		//TODO
//		if(problemType == 1)
//			algorithm.setEvaluator(new DGCEvaluator());
//		else
//			algorithm.setEvaluator(new DGCEvaluatorAUC());
		
		algorithm.setEvaluator(new DGCEvaluatorGPU());
		
		((DGCEvaluatorGPU) algorithm.getEvaluator()).setSpecies((DGCSpecies) algorithm.getSpecies());
		
		((DGCEvaluatorGPU) algorithm.getEvaluator()).prepare(algorithm);
	}
	
	/**
	 * Prints a description of the classifier.
	 *
	 * @return a description of the classifier as a string
	 */
	public String toString()
	{
		return algorithm.getClassifier().toString(metadata);
	}
	
	/**
	 * Generates the classifier.
	 *
	 * @param instances the instances to be used for building the classifier
	 * @throws Exception if the classifier can't be built successfully
	 */
	public void buildClassifier(Instances instances) throws Exception
	{
		algorithm = new DGCAlgorithmGPU();
		
		configureMetadata(instances);
		
		algorithm.execute();
	}

	/**
	 * Returns default capabilities of the classifier, i.e., of LinearRegression.
	 *
	 * @return the capabilities of this classifier
	 */
	public Capabilities getCapabilities()
	{
		Capabilities result = new Capabilities(this);

		// attributes
		result.enable(Capability.NOMINAL_ATTRIBUTES);
		result.enable(Capability.NUMERIC_ATTRIBUTES);

		// class
		result.enable(Capability.NOMINAL_CLASS);

		return result;
	}

	public double classifyInstance(Instance inst)
	{
		ClassicInstance instance = new ClassicInstance(metadata.numberOfAttributes());
		
		for(int i = 0; i < metadata.numberOfAttributes(); i++)
			instance.setValue(i, inst.value(i));
		
		instance.setClassValue(inst.classValue());
		
		((DGCSpecies) algorithm.getSpecies()).normalizeInstance(instance);

		return ((GravitationClassifier) algorithm.getClassifier()).classify(instance);
	}

	/**
	 * Returns an instance of a TechnicalInformation object, containing 
	 * detailed information about the technical background of this class,
	 * e.g., paper reference or book this class is based on.
	 * 
	 * @return the technical information about this class
	 */
	public TechnicalInformation getTechnicalInformation() {
		TechnicalInformation 	result;

		result = new TechnicalInformation(Type.ARTICLE);
		result.setValue(Field.AUTHOR, "A. Cano and A. Zafra and S. Ventura");
		result.setValue(Field.TITLE, "Weighted Data Gravitation Classification for Standard and Imbalanced Data");
		result.setValue(Field.JOURNAL, "IEEE Transactions on Cybernetics");
		result.setValue(Field.YEAR, "2013");
		result.setValue(Field.VOLUME, "43");
		result.setValue(Field.NUMBER, "6");
		result.setValue(Field.PAGES, "1672-1687");

		return result;
	}
	
	/**
	 * Main method for testing this class
	 *
	 * @param argv the commandline options
	 */
	public static void main(String [] argv){
		runClassifier(new DGCGPU(), argv);
	}
}