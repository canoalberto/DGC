package net.sf.jclec.problem.classification.dgc;

import net.sf.jclec.problem.classification.ClassificationAlgorithm;

public class DGCPopulationReportGPU extends DGCPopulationReport
{
	private static final long serialVersionUID = 2040804942867083057L;

	@Override
	protected void doClassificationReport(ClassificationAlgorithm algorithm)
	{
    	super.doClassificationReport(algorithm);
		
		if(algorithm.getEvaluator() instanceof DGCEvaluatorGPU)
			((DGCEvaluatorGPU) algorithm.getEvaluator()).nativeFree();
	}
}
