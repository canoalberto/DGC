#ifndef _PARAM_H_
#define _PARAM_H_

// Required includes
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <jni.h>
#include <time.h>
#include <math.h>
#include <ctype.h>
#if _WIN32
#include "multithreading.cpp"
#include <windows.h>
#else
#include "multithreading.h"
#endif

// Include CUDA libs if GPU compilation
#ifdef __USE_GPU__
#include <cuda.h>
#include <cutil_inline.h>
#include <cuda_runtime_api.h>
#endif

// Include JNI interfaces

#include "jni/net_sf_jclec_problem_classification_dgc_DGCEvaluatorGPU.h"

using namespace std;

// Maximum number of CPU threads or GPU devices [0-16]
#define MAX_THREADS 4
// Number of threads per block at evaluation kernels
#define THREADS_EVAL_BLOCK 256
// Segment size alignment
#define ALIGNMENT THREADS_EVAL_BLOCK

// Maximum number of individuals evaluated concurrently [0-256] Recommended values 32*i
#define BLOCK_SIZE_GRAVITY 128

// Plan structure to let tasks know its thread number and population size
typedef struct {
    int thread;
    int size;
} Plan;

#endif
