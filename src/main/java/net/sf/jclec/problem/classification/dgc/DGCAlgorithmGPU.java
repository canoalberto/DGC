package net.sf.jclec.problem.classification.dgc;

import org.apache.commons.configuration.Configuration;

public class DGCAlgorithmGPU extends DGCAlgorithm
{
	private static final long serialVersionUID = 6785848826151686232L;

	@Override
	public void configure(Configuration configuration)
	{
		super.configure(configuration);
		
		if(evaluator instanceof DGCEvaluatorGPU)
			((DGCEvaluatorGPU) evaluator).prepare(this);
	}
}