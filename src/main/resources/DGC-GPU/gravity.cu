int* h_numberInstancesClass;
__constant__ char ISNUMERICAL[256];

__device__ float distance(int instanciaA, int instanciaB, float* d_weights, int numAttributes, float* instancesData, int numberInstances_A, int base)
{
	float distance = 0.0f;

	for(int i = 0; i < numAttributes; i++)
		if(ISNUMERICAL[i])
		{
			float diff = instancesData[instanciaB + i*numberInstances_A] - instancesData[instanciaA + i*numberInstances_A];
			distance += d_weights[base + i] * diff * diff;
		}
		else
			distance += d_weights[base + i] * (instancesData[instanciaB + i*numberInstances_A] == instancesData[instanciaA + i*numberInstances_A] ? 0 : 1);

	return distance;
}

__global__ void kernelCalculateGravity(unsigned char* result, int Class, float* gravityValues, float* instancesData, int* instancesClass, int numberAttributes, int numberInstances, int numberInstances_A, int numberClasses, int* numberInstancesClass, float* d_weights) 
{
	int instance = blockDim.y * blockIdx.y + threadIdx.y;

	if(instance < numberInstances)
	{
		float gravityValue = 0.0f;

		for(int i = 0; i < numberInstances; i++)
			if(instancesClass[i] == Class && i != instance)
				gravityValue += 1.0f / distance(instance, i, d_weights, numberAttributes, instancesData, numberInstances_A, blockIdx.x*numberAttributes*numberClasses + instancesClass[i]*numberAttributes);

		gravityValue *= 1.0f - ((numberInstancesClass[Class] - 1) / (float) numberInstances);

		int memPosition = blockIdx.x*numberInstances_A + instance;

		if(gravityValue > gravityValues[memPosition])
		{
			gravityValues[memPosition] = gravityValue;

			if(instancesClass[instance] == Class)
				result[memPosition] = 0; // HIT
			else
				result[memPosition] = 1; // FAIL
		}
	}
}

/**
 * Reduction GPU Confusion Matrix kernel
 */

__global__ void MC_kernelGravity(unsigned char* result, jfloat* fitness, int numberInstances, int numberInstances_A) 
{
	__shared__ int MC[THREADS_EVAL_BLOCK];

	MC[threadIdx.y] = 0;

	int base = blockIdx.x*numberInstances_A + threadIdx.y;
	int top =  numberInstances - threadIdx.y;

	// Performs the reduction of the thread corresponding values
	for(int i = 0; i < top; i+=THREADS_EVAL_BLOCK)
	{
		MC[threadIdx.y] += result[base + i];
	}

	__syncthreads();

	// Calculates the final amount
	if(threadIdx.y == 0)
	{
		int fails = 0;
		
		for(int i = 0; i < THREADS_EVAL_BLOCK; i++)
			fails += MC[i];
		
		// Set the fitness to the individual
		fitness[blockIdx.x] = fails / (float) numberInstances;
	}
}

/**
 * GPU device thread that performs the evaluation of a portion of the population
 *
 * @param The job plan for the thread
 */
CUT_THREADPROC gpuThreadGravity(Plan *plan)
{
	// Set the GPU device number, each thread on a different GPU
	int device = plan->thread;
	cudaSetDeviceFlags(cudaDeviceScheduleSpin);
	int deviceCount;
	cudaGetDeviceCount(&deviceCount);
	if(deviceCount == 3 && device == 1) device = 2;
	cudaSetDevice(device);

    float* h_fitness;
	float* d_gravity;
	float* d_weights;
	float* d_instancesData;
	float* h_weights;
	int *d_instancesClass;
	int* d_numberInstancesClass;
	int threadPopulationSize;
	unsigned char* d_result;
	jfloat* d_fitness;
	JNIEnv* env;
	JavaVM* jvm;

	cudaMallocHost((void**)&h_fitness, BLOCK_SIZE_GRAVITY*sizeof(float));
	cudaMallocHost((void**)&h_weights, numberAttributes*numClasses*BLOCK_SIZE_GRAVITY*sizeof(float));
	
	// GPU dynamic memory allocation
	cudaMalloc((void**) &d_numberInstancesClass, numClasses*sizeof(int));	
	cudaMalloc((void**) &d_weights, numberAttributes*numClasses*BLOCK_SIZE_GRAVITY*sizeof(jfloat));	
	cudaMalloc((void**) &d_fitness, BLOCK_SIZE_GRAVITY*sizeof(jfloat));	
	cudaMalloc((void**) &d_instancesData, numberAttributes*numberInstances_A*sizeof(float));
	cudaMalloc((void**) &d_instancesClass, numberInstances*sizeof(int));
	cudaMalloc((void**) &d_result, BLOCK_SIZE_GRAVITY * numberInstances_A * sizeof(unsigned char));
	cudaMalloc((void**) &d_gravity, BLOCK_SIZE_GRAVITY * numberInstances_A * sizeof(float));

	// Copy instances data and classes to the GPU
	cudaMemcpy(d_instancesData, h_instancesData, numberAttributes*numberInstances_A*sizeof(float), cudaMemcpyHostToDevice );
	cudaMemcpy(d_instancesClass, h_instancesClass, numberInstances*sizeof(int), cudaMemcpyHostToDevice );
	cudaMemcpy(d_numberInstancesClass, h_numberInstancesClass, numClasses*sizeof(int), cudaMemcpyHostToDevice );

	// Signal: thread is ready to evaluate
	SEM_POST(&post_sem[plan->thread]);

	Get_VM(&jvm, &env);

	dim3 threads_evaluate(1, THREADS_EVAL_BLOCK);
	dim3 threads_mc(1,THREADS_EVAL_BLOCK);
	
	bool firstTime = true;
	
	do
	{
		// Wait until evaluation is required
		SEM_WAIT (&wait_sem[plan->thread]);

		if(evaluate)
		{
			// Get the methods from Java
			jclass cls = env->GetObjectClass(algorithm);
		
			jmethodID midR = env->GetMethodID(cls, "getWeight", "(III)F");	
			jmethodID midW = env->GetMethodID(cls, "setFitness", "(IF)V");
			
			if(firstTime)
			{
				jmethodID midN = env->GetMethodID(cls, "isNumerical", "(I)Z");	
				
				for(int i = 0; i < numberAttributes; i++)
				{
					jboolean isNumericalValue = env->CallCharMethod(algorithm, midN, i);
					cudaMemcpyToSymbol("ISNUMERICAL", &isNumericalValue, sizeof(jboolean), i*sizeof(jboolean), cudaMemcpyHostToDevice);
				}
				
				firstTime = false;
			}
	
			// Calculate the thread population size
			threadPopulationSize = (int)ceil(populationSize/(float)numThreads);

			// If population overflow, recalculate the thread actual population size
			if((plan->thread + 1) * threadPopulationSize > populationSize)
			{
				if((threadPopulationSize = populationSize - threadPopulationSize * plan->thread) < 0)
					threadPopulationSize = 0;
			}
			if(threadPopulationSize > 0)
			{
				// Calculate the base index of the individual for this thread
				int base = plan->thread * (int)ceil(populationSize/(float)numThreads);

				int blockIdxSize = BLOCK_SIZE_GRAVITY;	

				// Population is evaluated using blocks of BLOCK_SIZE_GRAVITY individuals
				for(int j = 0; j < threadPopulationSize; j += BLOCK_SIZE_GRAVITY)
				{
					// If the last block size is smaller, fix the block size to the number of the rest of individuals 
					if(j+BLOCK_SIZE_GRAVITY > threadPopulationSize)
						blockIdxSize = threadPopulationSize - j;
									
					// Copy each individual in the block from the thread population to the GPU
					for(int i = 0; i < blockIdxSize; i++)
						for(int k = 0; k < numClasses; k++)
							for(int l = 0; l < numberAttributes; l++)
								h_weights[i*numberAttributes*numClasses + k*numberAttributes + l] = env->CallFloatMethod(algorithm,midR,base+j+i,l,k);

								cudaMemcpy(d_weights, h_weights, numberAttributes*numClasses*blockIdxSize*sizeof(float), cudaMemcpyHostToDevice );

								// Setup evaluation grid size	
								dim3 grid_evaluate(blockIdxSize, (int)ceil(numberInstances/(float)THREADS_EVAL_BLOCK));

								cudaMemset(d_gravity, 0, BLOCK_SIZE_GRAVITY * numberInstances_A * sizeof(float));

								// Evaluation kernel call					
								for(int i = 0; i < numClasses; i++)
									kernelCalculateGravity <<< grid_evaluate, threads_evaluate >>> (d_result, i, d_gravity, d_instancesData, d_instancesClass, numberAttributes, numberInstances, numberInstances_A, numClasses, d_numberInstancesClass, d_weights);

								// Setup reduction grid size
								dim3 grid_mc(blockIdxSize, 1);

								// Reduction kernel call
								MC_kernelGravity <<< grid_mc, threads_mc >>> (d_result, d_fitness, numberInstances, numberInstances_A);

								// Copy the fitness values from the GPU to Host memory and set them to the individuals
								cudaMemcpy(h_fitness, d_fitness, blockIdxSize*sizeof(jfloat), cudaMemcpyDeviceToHost );	

								for(int i = 0; i < blockIdxSize; i++)
								{
									env->CallVoidMethod(algorithm, midW, base + j + i, h_fitness[i]);
								}							
				}
			}
		}
		else
		{
			// Algorithm finished, free dynamic memory
			cudaFree(d_instancesData);    
			cudaFree(d_instancesClass);
			cudaFree(d_result);
			cudaFree(d_fitness);
			cudaFree(d_weights);
			cudaFree(d_numberInstancesClass);

			cudaFreeHost(h_fitness);
			cudaFreeHost(h_weights);
		}

		// Evaluation finished
		SEM_POST(&post_sem[plan->thread]);

	}while(evaluate);

	jvm->DetachCurrentThread();
	CUT_THREADEND;
}

/**
 * Function executed when nativeFree() call from Java
 */
JNIEXPORT void JNICALL
Java_net_sf_jclec_problem_classification_dgc_DGCEvaluatorGPU_nativeFree(JNIEnv *env, jobject obj)
{
	nativeFree(env,obj);
}

/**
 * Function executed when nativeMalloc() call from Java
 */
JNIEXPORT void JNICALL
Java_net_sf_jclec_problem_classification_dgc_DGCEvaluatorGPU_nativeMalloc(JNIEnv *env, jobject obj, jint popSize, jint jnumThreads, jint jnumberAttributes, jint jnumberInstances, jint jnumClasses, jobject jalgorithm)
{
	algorithm = jalgorithm;
	numThreads = jnumThreads;
	numberAttributes = jnumberAttributes;
	numClasses = jnumClasses;
	numberInstances = jnumberInstances;
	numberInstances_A = ceil(numberInstances/(float)ALIGNMENT)*ALIGNMENT;

	// Set up semaphores
	for(int i = 0; i < numThreads; i++)
	{
		SEM_INIT (&wait_sem[i], 0);
		SEM_INIT (&post_sem[i], 0);
	}

	jclass cls = env->GetObjectClass(algorithm);
	jmethodID midR = env->GetMethodID(cls, "getValue", "(II)F");
	jmethodID midRR = env->GetMethodID(cls, "getNumberInstances", "(I)I");
	jmethodID midRRR = env->GetMethodID(cls, "getClassValue", "(I)F");

	h_instancesData = (float*)malloc(numberAttributes*numberInstances_A*sizeof(float));
	h_instancesClass = (int*)malloc(numberInstances*sizeof(int));	
	h_numberInstancesClass = (int*)malloc(numClasses*sizeof(int));

	for(int i = 0; i < numClasses; i++)
	{
		h_numberInstancesClass[i] = env->CallIntMethod(algorithm,midRR,i);
	}

	// Copy dataset data from Java
	for(int i = 0; i < numberInstances; i++)
	{
		for(int j = 0; j < numberAttributes; j++)
			h_instancesData[j*numberInstances_A+i] = env->CallFloatMethod(algorithm,midR,i,j);

			h_instancesClass[i] = (int) env->CallFloatMethod(algorithm,midRRR,i);
	}

	// Set up threads plans
	for(int i = 0; i < numThreads; i++)
	{
		plan[i].thread = i;
		plan[i].size = (int)ceil(popSize/(float)numThreads);
	}

	int deviceCount;
	cudaGetDeviceCount(&deviceCount);

	if(numThreads > deviceCount)
	{
		fprintf(stderr, "Can't use %d threads. CUDA devices (non-display) count is %d\n",numThreads, deviceCount);
		exit(0);
	}

	for(int i = 0; i < numThreads; i++)
		threadID[i] = cutStartThread((CUT_THREADROUTINE)gpuThreadGravity, (void *)&plan[i]);

	// SIGNAL: threads ready to evaluate
	for(int i = 0; i < numThreads; i++)
		SEM_WAIT (&post_sem[i]);
}

/**
 * Function executed when nativeEvaluate() call from Java
 * 
 * @param The number of individuals, the actual class to classify and the algorithm
 */
JNIEXPORT void JNICALL
Java_net_sf_jclec_problem_classification_dgc_DGCEvaluatorGPU_nativeEvaluate(JNIEnv *env, jobject obj, jint size, jobject jalgorithm)
{
	evaluate = true;

	algorithm = jalgorithm;
	populationSize = size;

	// SIGNAL: wake up threads to evaluate
	for(int i = 0; i < numThreads && i < size; i++)
		SEM_POST (&wait_sem[i]);

	// Wait until threads finish
	for(int i = 0; i < numThreads && i < size; i++)
		SEM_WAIT (&post_sem[i]);
}
