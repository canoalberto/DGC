#include "parameters.h"

/**
 * Global vars required by algorithms
 */

SEMAPHORE wait_sem[MAX_THREADS],post_sem[MAX_THREADS];
CUTThread threadID[MAX_THREADS];
Plan plan[MAX_THREADS];

int numClasses, numberAttributes, numberInstances, numberInstances_A, numThreads, populationSize;
bool evaluate = true;
float *h_instancesData;
int *h_instancesClass;
jobject algorithm;

/**
 * Free the dynamic memory space
 */
void nativeFree(JNIEnv *env, jobject obj)
{
	evaluate = false;

    // Wake up threads to finish them
	for(int i = 0; i < numThreads; i++)
	SEM_POST (&wait_sem[i]);
	
	free(h_instancesData);
	free(h_instancesClass);
	
	cutWaitForThreads(threadID, numThreads);

	#if _WIN32
	for(int i = 0; i < numThreads; i++)
	{
		CloseHandle(wait_sem[i]);
		CloseHandle(post_sem[i]);
	}
	#endif
}

/**
 * Gets the VM running at the host
 */
static void Get_VM(JavaVM** jvm_p, JNIEnv** env_p) {

	JavaVM jvmBuffer;
	JavaVM* vmBuf = &jvmBuffer;
	jsize jvmTotalNumberFound = 0;  
	jint resCheckVM = JNI_GetCreatedJavaVMs(&vmBuf, 1, &jvmTotalNumberFound);
	
	if (jvmTotalNumberFound < 1)
	{
		fprintf(stderr, "No JVM found\n");
		exit(0);
	}
	*jvm_p = vmBuf;

	(*jvm_p)->AttachCurrentThread((void**)env_p, NULL);
}

#include "gravity.cu"
