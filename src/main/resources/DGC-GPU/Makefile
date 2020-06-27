ARCH     = -m64
GPU_ARCH = 
JAVA_DIR = /usr/lib/jvm/default-java
CUDA_INC = /usr/local/cuda/include
CUDA_LIB = /usr/local/cuda/lib64

#############################################################################################

SRC_FILE = jclec-gpu.cu
COMPILER = nvcc --ptxas-options=-v $(GPU_ARCH) -Xcompiler -fPIC -I$(CUDA_INC) -L$(CUDA_LIB)

todo: javah multithreading gpu lib
javah: 
	$(JAVA_DIR)/bin/javac -classpath ../../../../target/DGC-1.0-jar-with-dependencies.jar:../../../../lib/JCLEC4-base-1.0-jar-with-dependencies.jar -h jni ../../java/net/sf/jclec/problem/classification/dgc/DGCEvaluatorGPU.java
multithreading: multithreading.cpp
	g++ $(ARCH) -O3 -fPIC -lpthread -c -o multithreading.o multithreading.cpp -I$(CUDA_INC)
gpu:
	$(COMPILER) -O3 $(ARCH) -shared -I. -I$(JAVA_DIR)/include/linux -I$(JAVA_DIR)/include -c $(SRC_FILE) -o jclec-gpu.o $(FLAGS) -lpthread -lm 
ptx:
	nvcc $(GPU_ARCH) -ptx jclec-gpu.cu -I$(CUDA_INC) -L$(CUDA_LIB) -O3  $(ARCH) -I$(JAVA_DIR)/include/linux -I$(JAVA_DIR)/include $(FLAGS)
lib: jclec-gpu.o
	g++ $(ARCH) -O3 -fPIC -shared -I$(CUDA_INC) -L$(CUDA_LIB) -Wl,-soname,jclec_gpu multithreading.o jclec-gpu.o -o libjclec_gpu.so -lc -lpthread $(FLAGS) -lcuda -lcudart
clean:
	rm multithreading.o jclec-gpu.o libjclec_gpu.so