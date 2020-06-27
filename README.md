# Weighted Data Gravitation Classification for Standard and Imbalanced Data

Gravitation is a fundamental interaction whose concept and effects applied to data classification become a novel data classification technique. The simple principle of data gravitation classification (DGC) is to classify data samples by comparing the gravitation between different classes. However, the calculation of gravitation is not a trivial problem due to the different relevance of data attributes for distance computation, the presence of noisy or irrelevant attributes, and the class imbalance problem. This paper presents a gravitation-based classification algorithm which improves previous gravitation models and overcomes some of their issues. The proposed algorithm, called DGC+, employs a matrix of weights to describe the importance of each attribute in the classification of each class, which is used to weight the distance between data samples. It improves the classification performance by considering both global and local data information, especially in decision boundaries. The proposal is evaluated and compared to other well-known instance-based classification techniques, on 35 standard and 44 imbalanced data sets. The results obtained from these experiments show the great performance of the proposed gravitation model, and they are validated using several nonparametric statistical tests.

# Manuscript - IEEE Transactions on Cybernetics

https://ieeexplore.ieee.org/document/6403569

# Citing DGC

> A. Cano, A. Zafra, and S. Ventura. Weighted Data Gravitation Classification for Standard and Imbalanced Data. IEEE Transactions on Cybernetics, 43 (6) pages 1672-1687, 2013.

## Running

Edit the Weka launcher to add the GPU library to the Java library path

```
-Djava.library.path=./src/main/resources/DGC-GPU
```